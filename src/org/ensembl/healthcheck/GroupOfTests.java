package org.ensembl.healthcheck;

import java.util.ArrayList;
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
	
	public List<GroupOfTests> getSourceGroups() {
		return sourceGroups;
	}

	final protected Set<Class<? extends EnsTestCase>> setOfTests;

	public Set<Class<? extends EnsTestCase>> getSetOfTests() {
		return setOfTests;
	}

	public String getShortName() {		
		return this.getClass().getSimpleName();		
	}
	
	public GroupOfTests() {
		this.setOfTests   = new HashSet<Class<? extends EnsTestCase>>();
		this.sourceGroups = new ArrayList<GroupOfTests>();
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

	/**
	 * <p>
	 * 	Use this to add tests and groups of tests to the set of tests 
	 * that you want to run.
	 * </p>
	 * 
	 * @param testCasesOrGroupsOfTests
	 * @throws ClassNotFoundException
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
					
					setOfTests.add(testCaseOrGroupOfTests);
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
	 * 
	 * @return
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






