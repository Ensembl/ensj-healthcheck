/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2018] EMBL-European Bioinformatics Institute
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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Iterator;

import org.ensembl.healthcheck.testcase.EnsTestCase;

/**
 * <p>
 * 	This is the base class for groups of tests, it provides functionality
 * for adding and removing tests and groups of tests using the addTest
 * and removeTest methods.
 * </p>
 * 
 */
public class GroupOfTests {
	
	final protected List<GroupOfTests> sourceGroups;
	
	/**
	 * <p>
	 * 	The name of this group of tests. By default getName() will return the
	 * name of the class, but if this is set, then the value of "name" is 
	 * returned instead.
	 * </p>
	 * 
	 * <p>
	 * 	The name is used for the GUI.
	 * </p>
	 * 
	 * <p>
	 * 	Setting it explicitly is necessary when groups are created on the fly. 
	 * Otherwise any group created on the fly would have the name 
	 * "GroupOfTests".
	 * </p>
	 */
	protected String name = "";
	
	protected String description = "";
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 * If name attribute is not set, will return the simple name of the class.
	 * 
	 */
	public String getName() {

		if (name.isEmpty()) {
			return this.getClass().getSimpleName();
		} else {
			return name;
		}
	}

	public List<GroupOfTests> getSourceGroups() {
		
		Collections.sort(sourceGroups, new GroupOfTestsComparator());
		return sourceGroups;
	}

	final protected Set<Class<? extends EnsTestCase>> setOfTests;

	public Set<Class<? extends EnsTestCase>> getSetOfTests() {
		return setOfTests;
	}

	public GroupOfTests() {
		this.setOfTests   = new HashSet<Class<? extends EnsTestCase>>();
		this.sourceGroups = new ArrayList<GroupOfTests>();
	}
	
	public boolean hasTest(Class<? extends EnsTestCase> ensTestCase) {
		
		return setOfTests.contains(ensTestCase);	
	}

	public Set<Class<? extends EnsTestCase>> getTestClasses() {
		return setOfTests;
	}
	
	/**
	 * <p>
	 * 	Creates and returns a list of testclasses that are in this testgroup.
	 * </p>
	 * 
	 * @return List<Class<? extends EnsTestCase>>
	 * 
	 */
	public List<Class<? extends EnsTestCase>> getListOfTests() {
		
		Iterator<Class<? extends EnsTestCase>> i = this.getSetOfTests().iterator();
		
		List<Class<? extends EnsTestCase>> list
			= new LinkedList<Class<? extends EnsTestCase>>();
		
		while(i.hasNext()) {
			
			Class<? extends EnsTestCase> etc = i.next();
			list.add(etc);
		}
		Collections.sort(list, new EnsTestCaseComparator());
		return list;
	}

	/**
	 * @param nameOfTestClass
	 * @throws ClassNotFoundException
	 * 
	 * <p>
	 * 	Uses the classloader to fetch the class specifies in nameOfTestClass
	 * and remove it from the set of tests for this group.
	 * </p>
	 * 
	 */
	public void removeTest(String... nameOfTestClass) throws ClassNotFoundException {
		
		for (String currentNameOfTestClass : nameOfTestClass) {
			Class<? extends EnsTestCase> testClass = (Class<? extends EnsTestCase>) Class.forName(currentNameOfTestClass);
			this.setOfTests.remove(testClass);
		}
	}

	/**
	 * @param groupOfTests
	 * 
	 * Adds a group of tests to the set of tests in this group.
	 * 
	 */
	public void addTest(GroupOfTests groupOfTests) {
		this.setOfTests.addAll(groupOfTests.getTestClasses());
	}
	
	/**
	 * <p>
	 * Adds the tests from a single test class to the set of tests to be
	 * run. Additionally it keeps track of which groups were added. This
	 * may be used by the GUI in the future to show the user of which
	 * subgroups a group comprises.
	 * </p>
	 * 
	 * @param groupOfTestsClass
	 * 
	 */
	public void addTest(Class<? extends GroupOfTests> groupOfTestsClass) {
		
		GroupOfTests g;
		try {
			
			g = groupOfTestsClass.newInstance();
			
		}
		// Instantiation should always work. If not, we want to deal with
		// failures here rather than forcing every group definition file to 
		// deal with these problems.
		//
		catch (InstantiationException e) { throw new RuntimeException(e); }
		//
		// This would happen, if the constructor of the healthcheck was 
		// private or protected.
		//
		catch (IllegalAccessException e) { throw new RuntimeException(e); }
		
		sourceGroups.add(g);		
		addTest(g);
	}
	
	public void addTest(List<Class<EnsTestCase>> listOfTestClasses) {
		
		addTest( listOfTestClasses.toArray(new Class[]{}) );
	}

	/**
	 * <p>
	 * 	Use this to add tests and groups of tests to the set of tests 
	 * that you want to run.
	 * </p>
	 * 
	 * @param testCasesOrGroupsOfTests
	 * 
	 */
	public void addTest(Class... testCasesOrGroupsOfTests) {
		
		for (Class testCaseOrGroupOfTests : testCasesOrGroupsOfTests) {
	
				/** 
				 * The user could have passed a class that is neither a 
				 * GroupOfTests nor an EnsTestCase. In that case the class
				 * can't be handled this variable will indicate that.
				 */
				boolean canHandleThisClass = false;
			
				if (GroupOfTests.class.isAssignableFrom(testCaseOrGroupOfTests)) {
					
					addTest(testCaseOrGroupOfTests);
					canHandleThisClass = true;
				}
				if (EnsTestCase.class.isAssignableFrom(testCaseOrGroupOfTests)) {
					
					// Make sure we are not adding abstract classes. We only 
					// want tests that can be run. 
					//
					if (!Modifier.isAbstract(testCaseOrGroupOfTests.getModifiers())) {
						
						setOfTests.add(testCaseOrGroupOfTests);
					}
					canHandleThisClass = true;
				}
				if (!canHandleThisClass) {
					throw new RuntimeException(
						"Don't know what to do with class "
						+ testCaseOrGroupOfTests.getCanonicalName()
					);
				}
		}
	}
	
	/**
	 * @param groupOfTests
	 * 
	 * Removes a group of tests to the set of tests in this group.
	 * 
	 */
	public void removeTest(GroupOfTests groupOfTests) {
		this.setOfTests.removeAll(groupOfTests.getTestClasses());
	}

	/**
	 * @param tests
	 * 
	 * Removes a list of test classes from the set of tests in this group.
	 * 
	 */
	public void removeTest(Class<? extends EnsTestCase>... tests) {
		for(Class<? extends EnsTestCase> test: tests) {
			setOfTests.remove(test);
		}
	}
	
	/**
	 * <p>
	 * 	Returns instance of all the tests in this group. Tests are instantiated
	 * on the fly.
	 * </p> 
	 * @return set of tests in group
	 */
	public Set<EnsTestCase> getTests() {
		Set<EnsTestCase> tests = new HashSet<EnsTestCase>();
		for(Class<? extends EnsTestCase> clazz: this.setOfTests) {
			try {
				EnsTestCase test = clazz.newInstance();
				test.setTypeFromPackageName();
				test.types();
				tests.add(test);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return tests;
	}
	
	/**
	 * <p>
	 * 	Useful for debugging. Prints the name of the class and which test 
	 * classes were defined to run.
	 * </p>
	 * 
	 */
	public String toString() {
		
		Iterator<Class<? extends EnsTestCase>> testIterator = this.getTestClasses().iterator();
		StringBuffer asString = new StringBuffer(); 
		
		asString.append("Class: " + this.getClass().getName() + "\n");
		
		asString.append("Has all tests from the following groups:\n");
		
		for (GroupOfTests sourceGroups : getSourceGroups()) {
			
			asString.append(" - " + sourceGroups.getClass().getCanonicalName() + "\n");
		}
		
		asString.append("Tests defined:\n");
		
		while (testIterator.hasNext()) {
			asString.append(" - " + testIterator.next().getName() + "\n");
		}		
		return asString.toString();
	}	
}

class EnsTestCaseComparator implements Comparator<Class<? extends EnsTestCase>> {
	
	public int compare(Class<? extends EnsTestCase> arg0, Class<? extends EnsTestCase> arg1) {				
		return arg0.getSimpleName().compareTo(arg1.getSimpleName());
	}
}




