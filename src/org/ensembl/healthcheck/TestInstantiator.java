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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.ensembl.PackageScan;
import org.ensembl.healthcheck.testcase.EnsTestCase;

/**
 * <p>
 * 	A class for instantiating tests and groups of tests.
 * </p>
 * 
 * </p>
 * 	Tests can be referred to by the user by their class name or by an 
 * alias. That is why the name can't be passed directly to the the
 * classloader for instantiation. This class creates a map that maps
 * the possible names of tests to their classname. This is what
 * the public "forName" method uses to create instances of tests or
 * testgroups. 
 * </p>
 *
 */
public class TestInstantiator {

	static final Logger log = Logger.getLogger(TestInstantiator.class.getCanonicalName());
	
	/**
	 * <p>
	 * 	An array of packages that will be scanned to find testcases. Testcases 
	 * are classes which inherit from EnsTestCase.
	 * </p>
	 */
	private final String[] packageToScan;

	/**
	 * <p>
	 * A map that maps the names of testcases to their class names. Testcases
	 * can be known under different names. The names under which they are 
	 * known are the ones as which they register themselves. The default 
	 * behaviour is to use the classes simple name which is inherited from
	 * EnsTestCase. Any testcase can overwrite that, so to find the names, 
	 * each testcase must be instantiated.
	 * </p>
	 */
	private final Map<String,String> aliasToClassName;
	
	/**
	 * Getter for the aliasToClassName attribute.
	 * 
	 */
	public Map<String, String> getAliasToClassName() {
		return aliasToClassName;
	}

	public TestInstantiator(String... packageToScan) {
		
		this.packageToScan = packageToScan;
		aliasToClassName   = createMap(packageToScan);
	}

	/**
	 * <p>
	 * Takes a testName as a parameter and returns the class for which it
	 * stands.
	 * </p> 
	 * 
	 * <p>
	 * The testName can be the full name of a class. This method tries to load
	 * such a method using Class.forName(testName). If that fails, it checks,
	 * if testName is an alias and tries to instantiate the class to which the
	 * alias maps. If this succeeds, the class is returned, if this fails too,
	 * a RuntimeException is thrown.
	 * </p>
	 * 
	 * @param testName
	 * @return Class<EnsTestCase>
	 * 
	 */
	public Class<?> forName(String testName) {
		
		Class<?> testClass = null;
		
		try {
			testClass = (Class<?>) Class.forName(testName);
			log.fine(testName + " is a class name, was able to instantiate it directly.");
			
		} catch (ClassNotFoundException e) {
			
			if (!aliasToClassName.containsKey(testName)) {
				throw new RuntimeException(
					"Could not find " + testName + "! It is neither a name of"
					+ " a class nor a valid alias.\n" + aliasToClassName
				);
			}
			String className = aliasToClassName.get(testName);
			log.fine(testName + " maps to " + className.getClass().getName());
			
			try {
				testClass = (Class<?>) Class.forName(className);
			} catch (ClassNotFoundException f) {
				throw new RuntimeException(f);
			}
		}
		
		if (testClass == null) {
			throw new NullPointerException();
		}
		
		return testClass;
	}
	
	/**
	 * 
	 * <p>
	 * 	Creates a new instance of a class. Catches exceptions and rethrows
	 * them as RuntimeExceptions.
	 * </p>
	 * 
	 * @param <T>
	 * @param loadedClass
	 * @return new instance
	 * 
	 */
	protected <T> T newInstance(Class<T> loadedClass) {
		
		T testcase = null;
		
		try {
			testcase = loadedClass.newInstance();
			
		} catch (InstantiationException e) { throw new RuntimeException(e);				
		} catch (IllegalAccessException e) { throw new RuntimeException(e);
		}
		
		return testcase;
	}
	
	/**
	 * <p>
	 * 	Loads a test class denoted by testName and instantiates it. Does no 
	 * type checking of the loaded class. If you want type checking, use
	 * </p>
	 * 
	 * <code>
	 * instanceByName(String testName, Class<T> expectedType)
	 * </code>
	 * 
	 * <p>
	 * 	instead.
	 * </p>
	 * 
	 * @param <T>
	 * @param testName
	 * @return instance of test
	 */
	public <T> T instanceByName(String testName) {
		
		Class<T> loadedClass = (Class<T>) forName(testName);
		return newInstance(loadedClass);
	}
	
	/**
	 * <p>
	 * 	Loads a test class denoted by testName and instantiates it. Checks,
	 * if the loaded class is of the expectedType or a subclass of it.
	 * </p>
	 * 
	 * @param <T>
	 * @param testName
	 * @param expectedType
	 * @return instance of test
	 */
	public <T> T instanceByName(String testName, Class<T> expectedType) {
		
		Class<T> loadedClass = (Class<T>) forName(testName);
		
		if (expectedType.isAssignableFrom(loadedClass)) {			
			return (T)instanceByName(testName);
		}
		throw new RuntimeException(testName + " is not a test case!");
	}
	
	/**
	 * <p>
	 * 	Returns a list of names as which the given class registers.
	 * </p>
	 */
	protected static List<String> knownNamesFor(Class<?> s) {
		
		List<String> result = new ArrayList<String>();
		
		Object theClassInstance = null;
		
		try {
			boolean isAbstractClass = Modifier.isAbstract(s.getModifiers());
			
			if (isAbstractClass) {
				return result;
			}
			theClassInstance = s.newInstance();

		} catch (InstantiationException e) {
			log.config("Could not instantiate " + s + ", got an InstantiationException!");
		} catch (IllegalAccessException e) {
			log.config("Could not instantiate " + s + ", got an IllegalAccessException!");
		}
		
		if (theClassInstance == null) {
			return result;
		}
		
		if (EnsTestCase.class.isAssignableFrom(theClassInstance.getClass())) {
			result.addAll(knownNamesForEnsTestCase((EnsTestCase) theClassInstance));
		}
		
		if (GroupOfTests.class.isAssignableFrom(theClassInstance.getClass())) {
			result.addAll(knownNamesForGroupOfTests((GroupOfTests) theClassInstance));
		}
		
		return result;
	}
	
	/**
	 * <p>
	 * 	Groups of tests can be called with their short class names. So this 
	 * class returns just that short name.
	 * </p> 
	 */
	protected static List<String> knownNamesForGroupOfTests(GroupOfTests g) {
		
		List<String> result = new ArrayList<String>();
		
		if (g == null) {
			return result;
		}
			
		result.add(g.getName());
		
		return result;
	}

	/**
	 * <p>
	 * 	Returns a list of names as which this testcase can be called. The names
	 * are found by calling the getShortTestName and the getVeryShortTestName
	 * methods on the test.
	 * </p>
	 */
	protected static List<String> knownNamesForEnsTestCase(EnsTestCase etc) {
		
		List<String> result = new ArrayList<String>();
		
		if (etc == null) {
			return result;
		}
			
		result.add(etc.getShortTestName());
		
		boolean veryShortTestNameCanBeCalled = true;
		
		try {
			// The method creates the very short test name by trying to cut
			// off the substring "TestCase" at the end of the name like this:
			//
			// return name.substring(0, name.lastIndexOf("TestCase"));
			//
			// If the name of the test does not contain the string "TestCase"
			// and it usually does not, then lastIndexOf returns -1 and sthe
			// substring method returns a StringIndexOutOfBoundsException.
			//
			etc.getVeryShortTestName();

		} catch (StringIndexOutOfBoundsException e) {
			//
			// which is caught here.
			//
			veryShortTestNameCanBeCalled = false;
		}

		if (veryShortTestNameCanBeCalled) {
			if (!etc.getShortTestName().equals( etc.getVeryShortTestName() )) {

				result.add(etc.getVeryShortTestName());
			}
		}
		
		return result;
	}
	
	/**
	 * <p>
	 * Adds a {@see: keyValuePair} to a map like the aliasToClassName 
	 * attribute of this class.
	 * </p>
	 * 
	 * <p>
	 * It checks, if the keyValuePair has already been added and if so, if
	 * it contradicts what has already been stored. If so, prints out a 
	 * warning, but adds it anyway.
	 * </p> 
	 * 
	 */
	public static void addToMapWithCheck(Map simpleNameToClass, keyValuePair currentKeyValuePair) {
		
		boolean aliasAlreadyMappedToOtherClass 
			= 
				// has the alias already been added
				simpleNameToClass.containsKey(currentKeyValuePair.key)
				// and
				&&
				// is the class to which it is mapped different
				// from what we want to add now 
				( !currentKeyValuePair.value.equals(simpleNameToClass.get(currentKeyValuePair.key) ) );  
		
		// If so, we have an ambiguous short name
		//
		if (aliasAlreadyMappedToOtherClass) {
			log.config(
			      currentKeyValuePair.key   + " is an ambiguous alias!\n"
				+ currentKeyValuePair.value + "\n"
				+ simpleNameToClass.get(currentKeyValuePair.key) + "\n"
			);
		}
		simpleNameToClass.put(
			currentKeyValuePair.key, 
			currentKeyValuePair.value
		);
	}
	
	/**
	 * <p>
	 * Scans an array of packages for classes that are subclasses of 
	 * EnsTestCase or GroupsOfTests.
	 * </p>
	 * 
	 */
	public static Map<String,String> createMap(String[] packagesToScan) {
		
		Map<String,String> nameMap = new HashMap<String,String>(); 
		
		for (String packageToScan : packagesToScan) {
			
			log.config("Scanning package " + packageToScan);
			
			Map<String,String> currentNameMap = createMap(packageToScan);
			
			// Duplicate keys would override each other in the final hash
			// without warning.
			//
			nameMap.putAll(currentNameMap);
		}
		return nameMap;
	}
	
	/**
	 * Scans a given package for classes that are subclasses of EnsTestCase.
	 * 
	 * @param packageToScan
	 * 
	 */
	public static Map<String,String> createMap(String packageToScan) {
		
		Map<String,String> simpleNameToClass = new HashMap();
		List<Class<?>> classesInPackage = null;
		
		try {
			classesInPackage = PackageScan.getClassesForPackage(packageToScan, true);
			
		} catch(ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		if (classesInPackage == null) {
			throw new NullPointerException();
		}
		
		for (Class<?> s : classesInPackage) {

			List<String> testNames = null;
			
			testNames = knownNamesFor(s);
			
			if (testNames==null) {
				log.warning("I don't know what " + s.getName() + " is. It does not look like a test or a group, so skipping.");
			} else {			
				for (String testName : testNames) {
					
					addToMapWithCheck(
						simpleNameToClass, 
						new keyValuePair(
							testName, 
							s.getName()
						)
					);
				}
			}
		}

		return simpleNameToClass;		
	}

	/**
	 * 
	 * Summarises the TestInstantiator.
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		
		StringBuffer scannedPackages = new StringBuffer();
		
		for (String p : packageToScan) {
			scannedPackages.append(" - " + p + "\n");
		}
		
		return
			  "Summary of "          + TestInstantiator.class.getCanonicalName() + "\n\n"
			+ "Scanned packages: \n" + scannedPackages + "\n\n"
			+ "Aliases\n\n"          + aliasToClassNametoString(this.aliasToClassName);
	}
	
	/**
	 * Converts a map like the aliasToClassName instance variable to a string 
	 * representation.
	 * 
	 * @param aliasToClassName
	 * 
	 */
	public String aliasToClassNametoString(Map<String,String> aliasToClassName) {
		
		StringBuffer result = new StringBuffer();
		
		Iterator<String> i = aliasToClassName.keySet().iterator();
		
		while (i.hasNext()) {
			
			String s = i.next();
			result.append("  - " + String.format("%1$-40s",s) + " -> " + aliasToClassName.get(s).toString() + "\n");
		}
		return result.toString();
	}
}

class keyValuePair {

	public keyValuePair(String key, String value) {
		this.key   = key;
		this.value = value;
	}
	
	public String key;
	public String value;	
	
}

