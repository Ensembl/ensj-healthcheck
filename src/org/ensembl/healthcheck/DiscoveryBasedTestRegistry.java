/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2021] EMBL-European Bioinformatics Institute
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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ensembl.healthcheck.testcase.EnsTestCase;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;
import org.ensembl.healthcheck.testcase.OrderedDatabaseTestCase;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.ClassFileFilenameFilter;
import org.ensembl.healthcheck.util.Utils;

/**
 * Hold information about tests. Can also find tests in a particular location.
 */
public class DiscoveryBasedTestRegistry implements TestRegistry {

	private static Logger logger = Logger.getLogger("HealthCheckLogger");

	private List<EnsTestCase> allTests; // a list of EnsTestCase objects

	private static final String BASE_TESTCASE_PACKAGE = "org.ensembl.healthcheck.testcase";

	// -----------------------------------------------------------------
	/**
	 * Create a new TestRegistry.
	 */
	public DiscoveryBasedTestRegistry() {

		allTests = findAllTests();

	}

	// -----------------------------------------------------------------
	/**
	 * @return All the currently defined (single and multiple database) tests.
	 */
	public List<EnsTestCase> getAll() {

		return allTests;

	} // getAll

	// -----------------------------------------------------------------
	/**
	 * @return All the single-database tests.
	 */
	public List<SingleDatabaseTestCase> getAllSingle() {

		List<SingleDatabaseTestCase> allSingle = new ArrayList<SingleDatabaseTestCase>();
		for(EnsTestCase test: allTests) {
			if (test instanceof SingleDatabaseTestCase) {
				allSingle.add((SingleDatabaseTestCase) test);
			}
		}

		return allSingle;

	} // getAllSingle

	// -----------------------------------------------------------------
	/**
	 * @return All the multi-database tests.
	 */
	public List<MultiDatabaseTestCase> getAllMulti() {

		List<MultiDatabaseTestCase> allMulti = new ArrayList<MultiDatabaseTestCase>();
		for(EnsTestCase test: allTests) {
			if (test instanceof MultiDatabaseTestCase) {
				allMulti.add((MultiDatabaseTestCase) test);
			}
		}

		return allMulti;

	} // getAllMulti

	// -----------------------------------------------------------------
	/**
	 * @return All the ordered database tests.
	 */
	public List<OrderedDatabaseTestCase> getAllOrdered() {

		List<OrderedDatabaseTestCase> allOrdered = new ArrayList<OrderedDatabaseTestCase>();
		for(EnsTestCase test: allTests) {
			if (test instanceof OrderedDatabaseTestCase) {
				allOrdered.add((OrderedDatabaseTestCase) test);
			}
		}

		return allOrdered;

	} // getAllOrdered

	// -----------------------------------------------------------------
	/**
	 * Get a list of all the single-database test cases that match certain conditions.
	 * 
	 * @param groups
	 *          A list of test case groups
	 * @param type
	 *          The type of databases the result tests should apply to.
	 * @return All the single-database tests that are in at least one of groups, and apply to type.
	 */
	public List<SingleDatabaseTestCase> getAllSingle(List<String> groups, DatabaseType type) {

		List<SingleDatabaseTestCase> result = new ArrayList<SingleDatabaseTestCase>();

		for(SingleDatabaseTestCase test: getAllSingle()) {
			if (test.inGroups(groups) && test.appliesToType(type)) {
				result.add(test);
			}
		}

		return result;

	}

	// -----------------------------------------------------------------
	/**
	 * Get a list of all the multi-database test cases that match certain conditions. Note the database type that the test applies to
	 * is /not/ checked here.
	 * 
	 * @param groups
	 *          A list of test case groups
	 * @return All the multi-database tests that are in at least one of groups.
	 */
	public List<MultiDatabaseTestCase> getAllMulti(List<String> groups) {

		List<MultiDatabaseTestCase> result = new ArrayList<MultiDatabaseTestCase>();

		for(MultiDatabaseTestCase test: getAllMulti()) {
			if (test.inGroups(groups)) {
				result.add(test);
			}
		}
		return result;
	}

	// -----------------------------------------------------------------
	/**
	 * Get a list of all the ordered database test cases that match certain conditions. Note the database type that the test applies
	 * to is /not/ checked here.
	 * 
	 * @param groups
	 *          A list of test case groups
	 * @return All the ordered-database tests that are in at least one of groups.
	 */
	public List<OrderedDatabaseTestCase> getAllOrdered(List<String> groups) {

		List<OrderedDatabaseTestCase> result = new ArrayList<OrderedDatabaseTestCase>();

		for(OrderedDatabaseTestCase test: getAllOrdered()) {
			if (test.inGroups(groups)) {
				result.add(test);
			}
		}
		return result;
	}

	// -----------------------------------------------------------------

	/**
	 * Finds all tests.
	 * 
	 * A test case is a class that extends EnsTestCase. Test case classes found in more than one location are only added once.
	 * 
	 * @return A List containing objects of the test case classes found.
	 */
	public List<EnsTestCase> findAllTests() {

		allTests = new ArrayList<EnsTestCase>();

		// --------------------------------------
		// Look for class files located in the appropriate package in the build/ directory.

		// find all subdirectories
		String startDir = System.getProperty("user.dir") + File.separator + "target" + File.separator + "build" + File.separator + BASE_TESTCASE_PACKAGE.replace('.', File.separatorChar);
		String[] subdirs = Utils.getSubDirs(startDir);

		// look for tests in each
		for (int i = 0; i < subdirs.length; i++) {

			String subdir = subdirs[i];

			// check dir corresponds to a known database type
			if (!subdir.equals("multi") && subdir.equalsIgnoreCase("generic") || DatabaseType.resolveAlias(subdir) != DatabaseType.UNKNOWN) {

				String directoryName = startDir + File.separator + subdir;
				String packageName = BASE_TESTCASE_PACKAGE + "." + subdir;
				addUniqueTests(allTests, findTestsInDirectory(directoryName, packageName));

			} else {

				logger.warning("Subdirectory " + subdir + " cannot be related to a database type");

			}

		} // foreach subdir

		// --------------------------------------
		// Look inside lib/ensj-healthcheck.jar
		// This is done second as if there is a class file for this test case in the build dir
		// then that should be used instead of the one in the jar file.
		// (addUniqueTests doesn't add a test if it's already in the list)
		String jarFileName = System.getProperty("user.dir") + File.separator + "target" + File.separator + "dist" + File.separator + "ensj-healthcheck.jar";
		if ((new File(jarFileName)).exists()) {
			addUniqueTests(allTests, findTestsInJar(jarFileName, BASE_TESTCASE_PACKAGE));
		}

		// --------------------------------------

		logger.finer("Found " + allTests.size() + " unique test case class" + (allTests.size() > 1 ? "es" : ""));

		return allTests;

	} // findAllTests

	// -------------------------------------------------------------------------
	/**
	 * Find all the tests (ie classes that extend EnsTestCase) in a directory.
	 * 
	 * @param dir
	 *          The base directory to look in.
	 * @param packageName
	 *          The package name to look for.
	 * @return A list of tests in dir.
	 */
	public List<EnsTestCase> findTestsInDirectory(String dir, String packageName) {

		logger.finest("Looking for tests in " + dir);

		List<EnsTestCase> tests = new ArrayList<EnsTestCase>();
		File f = new File(dir);

		// find all classes that extend org.ensembl.healthcheck.EnsTestCase
		ClassFileFilenameFilter cnff = new ClassFileFilenameFilter();
		File[] classFiles = f.listFiles(cnff);
		if (classFiles.length > 0) {
			logger.finer("Examining " + classFiles.length + " class file" + (classFiles.length > 1 ? "s" : "") + " in " + dir);
		}

		Object obj = null;

		for (File classFile : classFiles) {

			String baseClassName = classFile.getName().substring(0, classFile.getName().lastIndexOf("."));

			if (baseClassName.indexOf("$") > 0) {
				logger.finest("Skipping " + baseClassName + " since it appears to be an auto-generated anonymous inner class");
				continue;
			}
			
			try {
				Class<?> newClass = Class.forName(packageName + "." + baseClassName);
				boolean isEnsTestCase = EnsTestCase.class.isAssignableFrom(newClass);
				boolean isAbstract = Modifier.isAbstract(newClass.getModifiers());
				if (! isAbstract) {
          if(isEnsTestCase) {
            obj = newClass.newInstance();
          }
          else {
            logger.fine("The class "+baseClassName+" is in the test package but appears not to implement "+EnsTestCase.class);
          }
        }
			} catch (IllegalAccessException ie) {
				// EG: Catch and log reflection warnings
				logger.log(Level.WARNING, baseClassName + " has an issue when trying to create an instance", ie);
			} catch (InstantiationException ie) {
			  logger.log(Level.WARNING, baseClassName + " has an issue when trying to create an instance", ie);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if (obj != null && !tests.contains(obj)) {

				// set the test's type based upon the directory, if required
				EnsTestCase testCase = (EnsTestCase) obj;
				String[] bits = testCase.getName().split("\\.");
				String dirName = bits[bits.length - 2];

				testCase.setTypeFromDirName(dirName);

				// call the test's type method
				testCase.types();

				// store the test instance
				tests.add(testCase);

			}
		} // for classFiles

		return tests;

	} // findTestsInDirectory

	// -------------------------------------------------------------------------
	/**
	 * Find tests in a jar file.
	 * 
	 * @param jarFileName
	 *          The name of the jar file to search.
	 * @param packageName
	 *          The package name of the tests.
	 * @return The list of tests in the jar file.
	 */
	public List<EnsTestCase> findTestsInJar(String jarFileName, String packageName) {

		logger.finest("Looking for tests in " + jarFileName);

		ArrayList<EnsTestCase> tests = new ArrayList<EnsTestCase>();

		try {

			JarFile jarFile = new JarFile(jarFileName);

			for (Enumeration en = jarFile.entries(); en.hasMoreElements();) {

				JarEntry entry = (JarEntry) en.nextElement();
				String entryName = entry.getName().replace(File.separatorChar, '.');

				Object obj = null;

				// if entryName matches base package name, extract test name and subdir
				if (!entry.isDirectory() && entryName.indexOf(packageName) > -1) {

					String[] bits = entryName.split("\\.");
					String className = bits[bits.length - 2];
					String dirName = bits[bits.length - 3];
					String extension = bits[bits.length - 1];
					
					if (className.indexOf("$") > 0) {
						logger.finest("Skipping " + className + " since it appears to be an auto-generated anonymous inner class");
						continue;
					}
					
					if (extension.equalsIgnoreCase("class") && !dirName.equals("testcase")) {
					  
			      try {
			        Class<?> newClass = Class.forName(packageName + "." + dirName + "." + className);
			        boolean isEnsTestCase = EnsTestCase.class.isAssignableFrom(newClass);
			        boolean isAbstract = Modifier.isAbstract(newClass.getModifiers());
			        if (! isAbstract) {
			          if(isEnsTestCase) {
			            obj = newClass.newInstance();
			          }
			          else {
			            logger.fine("The class "+className+" is in the test package but appears not to implement "+EnsTestCase.class);
			          }
			        }
			      } catch (IllegalAccessException ie) {
			        // EG: Catch and log reflection warnings
			        logger.log(Level.WARNING, className + " had an issue whilst trying to create an instance", ie);
			      } catch (InstantiationException ie) {
			        logger.log(Level.WARNING, className + " has an issue whilst trying to create an instance", ie);
			      } catch (Exception e) {
			        e.printStackTrace();
			      }

						if (obj != null && !tests.contains(obj)) {

							// set the test's type based upon the directory, if required
							EnsTestCase testCase = (EnsTestCase) obj;
							testCase.setTypeFromDirName(dirName);

							// store the test instance
							tests.add(testCase);
						}
					}
				}
			}
		} catch (IOException ioe) {

			ioe.printStackTrace(System.err);

		}

		return tests;

	} // findTestsInJar

	// -------------------------------------------------------------------------
	/**
	 * Add all tests in subList to mainList, <em>unless</em> the test is already a member of mainList.
	 * 
	 * @param mainList
	 *          The list to add to.
	 * @param subList
	 *          The list to be added.
	 */
	public void addUniqueTests(List<EnsTestCase> mainList, List<EnsTestCase> subList) {

		for(EnsTestCase test: subList) {

			// can't really use List.contains() as the lists store objects which may be different
			if (!testInList(test, mainList)) {
				mainList.add(test);
				logger.fine("Added " + test.getShortTestName() + " to the list of tests");
			} else {
				logger.fine("Skipped " + test.getShortTestName() + " as it is already in the list of tests");
			}
		}

	} // addUniqueTests

	// -------------------------------------------------------------------------
	/**
	 * Check if a particular test is in a list of tests. The check is done by test name.
	 * 
	 * @param test
	 *          The test case to check.
	 * @param list
	 *          The list to search.
	 * @return true if test is in list.
	 */
	public boolean testInList(EnsTestCase test, List<EnsTestCase> list) {

		boolean inList = false;
		for(EnsTestCase thisTest: list) {
			if (thisTest.getTestName().equals(test.getTestName())) {
				inList = true;
			}
		}

		return inList;

	} // testInList

	// -----------------------------------------------------------------

	/**
	 * Get a list of the union of types of databases that all the tests apply to.
	 * 
	 * @return An array containing each DatabaseType found in the registry.
	 */
	public DatabaseType[] getTypes() {

		List<DatabaseType> types = new ArrayList<DatabaseType>();

		for(EnsTestCase test: allTests) {
			DatabaseType[] testTypes = test.getAppliesToTypes();
			for (int i = 0; i < testTypes.length; i++) {
				if (!types.contains(testTypes[i])) {
					types.add(testTypes[i]);
				}
			}
		}

		return types.toArray(new DatabaseType[types.size()]);

	}

	// -------------------------------------------------------------------------
	/**
	 * Get a list of the union of groups of all the tests.
	 * 
	 * @return An array of all the group names.
	 */
	public String[] getGroups() {

		List<String> groups = new ArrayList<String>();

		for(EnsTestCase test: allTests) {
			for(String group: test.getGroups()) {
				// filter out test names
				if (!isTestName(group) && !groups.contains(group)) {
					groups.add(group);
				}
			}
		}

		return groups.toArray(new String[groups.size()]);

	}

	// -------------------------------------------------------------------------
	/**
	 * Get all the tests in a particular group.
	 * 
	 * @param group
	 *          the group to run.
	 * @return The array of tests in group.
	 */
	public EnsTestCase[] getTestsInGroup(String group) {

		List<EnsTestCase> result = new ArrayList<EnsTestCase>();

		for(EnsTestCase test: allTests) {
			if (test.inGroup(group)) {
				result.add(test);
			}
		}

		return result.toArray(new EnsTestCase[result.size()]);

	}

	// -------------------------------------------------------------------------
	/**
	 * Get a list of the union of groups of all the tests that apply to a particular type of database.
	 * 
	 * @param type
	 *          The type to check.
	 * @return The union of groups of all the tests that apply to type.
	 */
	public String[] getGroups(DatabaseType type) {

		List<String> groups = new ArrayList<String>();

		for(EnsTestCase test: allTests) {
			if (test.appliesToType(type)) {
				for(String group: test.getGroups()) {
					// filter out test names
					if (!isTestName(group) && !groups.contains(group)) {
						groups.add(group);
					}
				}
			}
		}

		return groups.toArray(new String[groups.size()]);

	}

	// -------------------------------------------------------------------------
	/**
	 * Get all the tests in a particular group that apply to a particular type of database.
	 * 
	 * @param group
	 *          The group to check.
	 * @param type
	 *          The type to check.
	 * @return All the tests in group that apply to type.
	 */
	public EnsTestCase[] getTestsInGroup(String group, DatabaseType type) {

		List<EnsTestCase> result = new ArrayList<EnsTestCase>();

		for(EnsTestCase test: allTests) {
			if (test.inGroup(group) && test.appliesToType(type)) {
				result.add(test);
			}
		}

		return result.toArray(new EnsTestCase[result.size()]);

	}

	// -------------------------------------------------------------------------
	/**
	 * Check if a string (e.g. the name of a group) is actually the name of a known test case.
	 */
	private boolean isTestName(String s) {

		for(EnsTestCase test: allTests) {
			if (test.getShortTestName().equals(s)) {
				return true;
			}
		}

		return false;

	}
	// -------------------------------------------------------------------------

} // TestRegistry
