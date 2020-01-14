/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2020] EMBL-European Bioinformatics Institute
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

package org.ensembl.healthcheck.util;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.ensembl.healthcheck.GroupOfTests;

/**
 * @author michael
 * 
 * <p>
 * 	A collection of static convenience methods to deal with filtering of file 
 * 	names.
 * </p>
 *
 */
public class ClassFileFilter {
	
	private static Logger logger = Logger.getLogger("HealthCheckLogger");

	/**
	 * @param listOfClassFileNames
	 * @param classToFilterFor
	 * @return
	 * 
	 * <p>
	 * Filters the listOfClassFileNames for subclasses of classToFilterFor. 
	 * These subclasses must be true subclasses. This means that the class
	 * classToFilterFor will not be returned. 
	 * </p>
	 * 
	 * <p>
	 * If a class is abstract, it will not be returned.
	 * </p>
	 * 
	 */
	public static List<String> filterForTrueSubclassesOf(List<String> listOfClassFileNames, Class classToFilterFor) {
		
		List<String> listOfSubclasses = new ArrayList<String>(); 
		
		for (String currentClassName : listOfClassFileNames) {

			Object obj;
			
			try {
				Class newClass = Class.forName(currentClassName);

				// See if the newClass is a subclass of GroupOfTests
				//
				boolean isTestGroup = classToFilterFor.isAssignableFrom(newClass); 
				
				// We only want true subclasses
				//
				if (newClass.equals(classToFilterFor)) {
					continue;
				}
				if (!isTestGroup) {
					continue;
				}				
				if (!Modifier.isAbstract(newClass.getModifiers())) {
					obj = newClass.newInstance();
				} else {
					continue;
				}
				listOfSubclasses.add(currentClassName);

			} catch (InstantiationException ie) {
				logger.warning(currentClassName + " does not seem to be a test case class");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return listOfSubclasses;
	}
	
	/**
	 * @param listOfClassFileNames
	 * @return
	 * 
	 * <p>
	 * 	This takes a list of names of class files. For every class file it 
	 * 	builds the name of the java class that it stands for.  
	 * </p>
	 * 
	 * <p>
	 * 	This is done by stripping away the .class extension from the string 
	 * 	and replacing the file separator character which is a slash or a 
	 * 	backslash with the dots used in the package name.
	 * </p>
	 * 
	 * <p>
	 * 	The code assumes that the entire path is relevant. So if there are 
	 * 	extra parts of the path prefixed as in 
	 * 	"src/class/name/starts/here.class" this will not work. 
	 * </p>
	 * 
	 */
	public static List<String> mapClassFilesToClassName(List<String> listOfClassFileNames) {
		
		List<String> listOfClassNames = new ArrayList<String>();

		for (String currentFileName : listOfClassFileNames) {

			// Substitutes the slashes in the file names for dots. So if
			// a file is a class file, it will almost look like the 
			// package name, but suffixed with .class.
			//
			String entryName = currentFileName.replace(File.separatorChar, '.');

			String packageName = entryName.substring(0, entryName.length()-".class".length());
			
			listOfClassNames.add(packageName);
		}		
		return listOfClassNames;
	}
	
	/**
	 * @param listOfFileNames
	 * @return List&lt;String&gt;
	 * 
	 * <p>
	 * 	Searches through a list of file names and creates a list of all file
	 * 	names that end with .class.
	 * </p>
	 * 
	 */
	public static List<String> filterForClassFiles(List<String> listOfFileNames) {
		
		List<String> listOfFileRepresentingClasses = new ArrayList<String>();
		
		for (String currentFileName : listOfFileNames) {
		
			if (currentFileName.endsWith(".class")) {
				listOfFileRepresentingClasses.add(currentFileName);
			}
		}		
		return listOfFileRepresentingClasses;
	}
	
	/**
	 * @param listOfClassNames
	 * @return List&lt;String&gt;
	 * 
	 * <p>
	 * 	Searches through a list of file names of classes and creates a list 
	 * 	of all file names that don't have a $ in it. Files with a $ in it
	 * 	are class files for inner classes.
	 * </p>
	 */
	public static List<String> noInnerClassFiles(List<String> listOfClassNames) {
		
		List<String> ListOfFileNamesWithoutInnerClasses = new ArrayList<String>();
		
		for (String currentFileName : listOfClassNames) {
		
			if (currentFileName.indexOf("$") > 0) {
				continue;
			}
			ListOfFileNamesWithoutInnerClasses.add(currentFileName);
		}		
		return ListOfFileNamesWithoutInnerClasses;
	}
}
