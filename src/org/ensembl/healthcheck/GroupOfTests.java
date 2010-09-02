package org.ensembl.healthcheck;

import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import org.ensembl.healthcheck.testcase.EnsTestCase;

/**
 * 
 * This is the base class for groups of tests, it provides functionality
 * for adding and removing tests and groups of tests using the addTest
 * and removeTest methods.
 * 
 */
public class GroupOfTests {
	
	public String getShortName() {
		
		return this.getClass().getSimpleName();		
	}
	
	public Set<Class<? extends EnsTestCase>> getSetOfTests() {
		return setOfTests;
	}

	public void setSetOfTests(Set<Class<? extends EnsTestCase>> setOfTests) {
		this.setOfTests = setOfTests;
	}

	protected Set<Class<? extends EnsTestCase>> setOfTests;
	
	protected Set<Class<? extends EnsTestCase>> getListOfTests() {
		return setOfTests;
	}

	protected void setListOfTests(Set<Class<? extends EnsTestCase>> listOfTests) {
		this.setOfTests = listOfTests;
	}

	public GroupOfTests() {
		this.setOfTests = new HashSet<Class<? extends EnsTestCase>>();
	}
	
	/**
	 * @param nameOfTestClass
	 * @throws ClassNotFoundException
	 * 
	 * Uses the classloader to fetch the class specifies in nameOfTestClass
	 * and add it to the set of tests for this group.
	 * 
	 */
	public void addTest(String... nameOfTestClass) throws ClassNotFoundException {
		
		for (String currentNameOfTestClass : nameOfTestClass) {
				Class<? extends EnsTestCase> testClass = (Class<? extends EnsTestCase>) Class.forName(currentNameOfTestClass);		
				this.setOfTests.add(testClass);
		}
	}

	/**
	 * @param nameOfTestClass
	 * @throws ClassNotFoundException
	 * 
	 * Uses the classloader to fetch the class specifies in nameOfTestClass
	 * and remove it from the set of tests for this group.
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
		this.setOfTests.addAll(groupOfTests.getListOfTests());
	}
	
	/**
	 * @param groupOfTests
	 * 
	 * Removes a group of tests to the set of tests in this group.
	 * 
	 */
	public void removeTest(GroupOfTests groupOfTests) {
		this.setOfTests.removeAll(groupOfTests.getListOfTests());
	}

	/**
	 * @param testClass
	 * 
	 * Adds a test class to the set of tests in this group.
	 * 
	 */
	public void addTest(Class<? extends EnsTestCase>... testClass) {
		for(Class<? extends EnsTestCase> test: testClass) {
			setOfTests.add(test);
		}
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
	 * Returns instance of all the tests in this group. Tests are instantiated
	 * on the fly. 
	 * 
	 * @return
	 */
	public Set<EnsTestCase> getTests() {
		Set<EnsTestCase> tests = new HashSet<EnsTestCase>();
		for(Class<? extends EnsTestCase> clazz: this.setOfTests) {
			try {
				tests.add(clazz.newInstance());
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return tests;
	}
	
	/**
	 * Useful for debugging. Prints the name of the class and which test 
	 * classes were defined to run.
	 */
	public String toString() {
		
		Iterator<Class<? extends EnsTestCase>> testIterator = this.getListOfTests().iterator();
		StringBuffer asString = new StringBuffer(); 
		
		asString.append("Class: " + this.getClass().getName() + "\n");
		asString.append("Tests defined:\n");
		
		while (testIterator.hasNext()) {
			asString.append(" - " + testIterator.next().getName() + "\n");
		}		
		return asString.toString();
	}	
}






