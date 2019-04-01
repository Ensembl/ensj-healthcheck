/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ensembl.healthcheck;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.ensembl.healthcheck.testcase.EnsTestCase;
import org.ensembl.healthcheck.autogroups.Generator;

/**
 * 
 * Class for automatically generating testgroups from testcases. It uses the 
 * the group names as which the testcases register to group the tests. 
 * Names containing underscores _ or dashes - are changed so they are valid
 * java classes. The characters are removed and the first letter of the next
 * part to be concatenated is changed to uppercase thus creating a camel
 * case name.  
 * 
 * Overview over which tests belong to which groups can be made using this:
 * 
 * <pre>
 * /bin/bash show-groups.sh
 * </pre> 
 *
 */
public class TestsInGroups {
	
	static final Logger log = Logger.getLogger(TestsInGroups.class.getCanonicalName());
	
	/**
	 * 
	 * Testcases register themselves as members of testgroups in the original
	 * healthcheck framework. This map maps the names of these old testgroups
	 * to a list of names of testcases which belong to them.
	 * 
	 */
	protected final Map<String,List<String>> testcasegroupToTheirTestcasemembers;

	/**
	 * Creates a {@link Collection} of Testcases that were found in
	 * the specified package
	 * 
	 */
	public static List<String> searchForTestcaseClasses(String packageToSearch) {
		
		TestInstantiator testInstantiator = new TestInstantiator(packageToSearch);		
		Map<String,String>            map = testInstantiator.getAliasToClassName();
		
		return new ArrayList(map.values()); 
	}
	
	/**
	 * Delivers the full name of a class. Very simple but there are so many 
	 * names a class can have. This makes sure that everywhere the same name 
	 * is used. 
	 * 
	 */
	public static String toClassName(Class c) {
		return c.getCanonicalName();
	}
	
	/**
	 * 
	 * Creates a map from a name of a testcase group to a list of names of 
	 * its testcase members. 
	 * 
	 * @param classNames An array of class names.
	 * 
	 * @return A Map<String,List<String>> as described above.
	 * 
	 */
	public static Map<String,List<String>> createTestcasegroupToMembersMapping(String[] classNames) {

		Map<String,List<String>> testcasegroupToMembers = new HashMap<String,List<String>>(); 
		
		for (String className : classNames) {

			try {
				 Class c = Class.forName(className);
				 
				 if (EnsTestCase.class.isAssignableFrom(c)) {
					 
					 EnsTestCase etc = (EnsTestCase) c.newInstance();

					 List<String> groups = etc.getGroups();

					 // By default, every testcase is a member of its own 
					 // group. This leads to a multitude of groups comprising
					 // only of a single test with the same name.
					 //
					 groups.remove(etc.getShortTestName());
					 
					 for (String groupName : groups) {

						 if (testcasegroupToMembers.containsKey(groupName)) {
							 
							 List x = testcasegroupToMembers.get(groupName);
							 x.add(toClassName(c));
							 testcasegroupToMembers.put(groupName, x);
						 } else {
							 
							 LinkedList<String> ll = new LinkedList<String>();
							 ll.add(toClassName(c));							 
							 testcasegroupToMembers.put(groupName, ll);
						 }
					 }
				 }
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return testcasegroupToMembers;
	}
	
	/**
	 * @param packageWithHealthchecks Name of packages the will be searched 
	 * for testcases.
	 * 
	 */
	TestsInGroups(String packageWithHealthchecks) {
		
		log.config(
				"Using classpath: \n\n"
				+ Debug.classpathToString()
		);

		List<String>             map                    = searchForTestcaseClasses(packageWithHealthchecks);
		Map<String,List<String>> testcasegroupToMembers = createTestcasegroupToMembersMapping(
			map.toArray(new String[]{})
		);
		
		this.testcasegroupToTheirTestcasemembers = testcasegroupToMembers;
	}
	
	/**
	 * 
	 * Creates a string representation of the mapping from testgroups to 
	 * testcases in human readable form for debugging purposes.
	 * 
	 */
	public String toString() {
		
		Map<String, List<String>> testcasegroupToMembers = this.testcasegroupToTheirTestcasemembers;
		StringBuffer result = new StringBuffer(); 

		for (String groupName : testcasegroupToMembers.keySet()) {
			
			result.append(groupName);			
			for (String memberTestcaseClassname : testcasegroupToMembers.get(groupName)) {
				
				result.append(" - " + memberTestcaseClassname + "\n");
			}
		}		
		return result.toString();
	}
	
	public static void createTestgroupSourceFiles(
		Map<String,List<String>> testcasegroupToTheirTestcasemembers,
		Generator                g,
		String                   sourceDirName,
		String                   packageName
	) {
		
		//String packageName = "testgroup.legacy";
		String dirForFiles = sourceDirName + "/" + packageName.replace(".", "/"); 
		
		File dir = new File(dirForFiles);
		
		if (dir.exists()) {
			log.info("Directory " + sourceDirName + " already exists. This is ok.");
		} else {
			boolean dirCouldBeCreated = dir.mkdirs();
	
			if (!dirCouldBeCreated) {
				throw new RuntimeException("Error creating directory "+sourceDirName+"!");
			}
		}
		
		for (String currentGroupName : testcasegroupToTheirTestcasemembers.keySet()) {
			
			String currentJavaClassName = Generator.toJavaClassName(currentGroupName);
			
			//String currentGroupClassSimpleName = "All" + currentJavaClassName;			
			String currentGroupClassSimpleName = currentJavaClassName;
			String currentOutFileName          = dirForFiles + "/" + currentGroupClassSimpleName + ".java";
			String currentClassSourceCode      = g.generate(
					currentJavaClassName,
					packageName,
					currentGroupName,
					testcasegroupToTheirTestcasemembers.get(currentGroupName)
			); 
			
			File currentOutFile = new File(currentOutFileName);
			
			if (currentOutFile.exists()) {
				log.warning(currentOutFileName + " already exists, will not overwrite!");
			} else {

				BufferedWriter out = null;			

				try {
					log.info("\nCreating testgroup file "+currentOutFileName+":\n");
					
					FileWriter fstream = new FileWriter(currentOutFileName);
			        out = new BufferedWriter(fstream);
			        out.write(currentClassSourceCode);
	
				} catch (IOException e) {
					throw new RuntimeException(
						"Error creating " + currentOutFileName,
						e
					);
				} finally {
					//
					// If the outputstream was successfully opened, then close
					// it here. If it is not closed, the files will remain empty.
					//
					if (out != null) {
						try {
							out.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	public static void main(String args[]) {
		
		String packageToSearchForTestcases = args[0];
		String dirNameForSourceClasses     = args[1];
		String packageName                 = args[2];
		
		TestsInGroups tg = new TestsInGroups(packageToSearchForTestcases);
		Generator      g = new Generator();
		
		String dirName = dirNameForSourceClasses;

		boolean allWentWell = true;
		
		try {
			createTestgroupSourceFiles(tg.testcasegroupToTheirTestcasemembers, g, dirName, packageName);
		} catch(RuntimeException e) {
			allWentWell = false;
		}
		
		if (allWentWell) {
			TestsInGroups.log.info("Tests have been generated. Now type \"ant compile\" to compile them into tests.");
		}
	}
}


