/*
  Copyright (C) 2003 EBI, GRL
 
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.
 
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.ensembl.healthcheck;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import org.ensembl.healthcheck.testcase.*;
import org.ensembl.healthcheck.util.ClassFileFilenameFilter;
import org.ensembl.healthcheck.util.Utils;

/**
 * Hold information about tests. Can aldo find tests in a particular location.
 */
public class TestRegistry {

	protected static Logger logger = Logger.getLogger("HealthCheckLogger");

	List allTests; // a list of EnsTestCase objects

	private final static String BASE_TESTCASE_PACKAGE = "org.ensembl.healthcheck.testcase";

	// -----------------------------------------------------------------
	/**
	 * Create a new TestRegistry.
	 * 
	 * @param locations A list of strings representing locations in which to look for tests. Can be directories or jar files.
	 */
	public TestRegistry() {

		allTests = findAllTests();

	}

	// -----------------------------------------------------------------
	/**
	 * @return All the currently defined (single and multiple database) tests.
	 */
	public List getAll() {

		return allTests;

	} // getAll

	// -----------------------------------------------------------------
	/**
	 * @return All the single-database tests.
	 */
	public List getAllSingle() {

		List allSingle = new ArrayList();
		Iterator it = allTests.iterator();
		while (it.hasNext()) {
			Object test = it.next();
			if (test instanceof SingleDatabaseTestCase) {
				allSingle.add(test);
			}
		}

		return allSingle;

	} // getAllSingle

	//	-----------------------------------------------------------------
	/**
	 * @return All the multi-database tests.
	 */
	public List getAllMulti() {

		List allMulti = new ArrayList();
		Iterator it = allTests.iterator();
		while (it.hasNext()) {
			Object test = it.next();
			if (test instanceof MultiDatabaseTestCase) {
				allMulti.add(test);
			}
		}

		return allMulti;

	} // getAllMulti

	//	-----------------------------------------------------------------
	/**
	 * @return All the ordered database tests.
	 */
	public List getAllOrdered() {

		List allOrdered = new ArrayList();
		Iterator it = allTests.iterator();
		while (it.hasNext()) {
			Object test = it.next();
			if (test instanceof OrderedDatabaseTestCase) {
				allOrdered.add(test);
			}
		}

		return allOrdered;

	} // getAllOrdered

	// -----------------------------------------------------------------
	/**
	 * Get a list of all the single-database test cases that match certain conditions. 
	 * @param groups A list of test case groups
	 * @param type The type of databases the result tests should apply to.
	 * @return All the single-database tests that are in at least one of groups, and apply to type.
	 */
	public List getAllSingle(List groups, DatabaseType type) {

		List result = new ArrayList();

		Iterator it = getAllSingle().iterator();
		while (it.hasNext()) {
			SingleDatabaseTestCase test = (SingleDatabaseTestCase)it.next();
			if (test.inGroups(groups) && test.appliesToType(type)) {
				result.add(test);
			}
		}

		return result;

	}

	// -----------------------------------------------------------------
	/**
	 * Get a list of all the multi-database test cases that match certain conditions. 
	 * Note the database type that the test applies to is /not/ checked here.
	 * @param groups A list of test case groups
	 * @return All the multi-database tests that are in at least one of groups.
	 */
	public List getAllMulti(List groups) {

		List result = new ArrayList();

		Iterator it = getAllMulti().iterator();
		while (it.hasNext()) {
			MultiDatabaseTestCase test = (MultiDatabaseTestCase)it.next();
			if (test.inGroups(groups)) {
				result.add(test);
			}
		}

		return result;

	}

	//	-----------------------------------------------------------------
	/**
	 * Get a list of all the ordered database test cases that match certain conditions. 
	 * Note the database type that the test applies to is /not/ checked here.
	 * @param groups A list of test case groups
	 * @return All the ordered-database tests that are in at least one of groups.
	 */
	public List getAllOrdered(List groups) {

		List result = new ArrayList();

		Iterator it = getAllOrdered().iterator();
		while (it.hasNext()) {
			OrderedDatabaseTestCase test = (OrderedDatabaseTestCase)it.next();
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
	 * A test case is a class that extends EnsTestCase. Test case classes found in more
	 * than one location are only added once.
	 * 
	 * @return A List containing objects of the test case classes found.
	 */
	public List findAllTests() {

		ArrayList allTests = new ArrayList();

		// --------------------------------------
		// Look for class files located in the appropriate package in the build/ directory.

		// find all subdirectories 
		String startDir = System.getProperty("user.dir") + File.separator + "build" + File.separator + BASE_TESTCASE_PACKAGE.replace('.', File.separatorChar);
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
		String jarFileName = System.getProperty("user.dir") + File.separator + "lib" + File.separator + "ensj-healthcheck.jar";
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
	 * @param dir The base directory to look in.
	 * @param packageName The package name to look for.
	 * @return A list of tests in dir.
	 */
	public List findTestsInDirectory(String dir, String packageName) {

		logger.finest("Looking for tests in " + dir);

		ArrayList tests = new ArrayList();
		File f = new File(dir);

		// find all classes that extend org.ensembl.healthcheck.EnsTestCase
		ClassFileFilenameFilter cnff = new ClassFileFilenameFilter();
		File[] classFiles = f.listFiles(cnff);
		if (classFiles.length > 0) {
			logger.finer("Examining " + classFiles.length + " class file" + (classFiles.length > 1 ? "s" : "") + " in " + dir);
		}

		Object obj = new Object();

		for (int i = 0; i < classFiles.length; i++) {

			String baseClassName = classFiles[i].getName().substring(0, classFiles[i].getName().lastIndexOf("."));

			try {

				Class newClass = Class.forName(packageName + "." + baseClassName);
				String className = newClass.getName();
				obj = newClass.newInstance();

			} catch (InstantiationException ie) {
				logger.warning(baseClassName + " does not seem to be a test case class");
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (obj instanceof EnsTestCase && !tests.contains(obj)) {

				// set the test's type based upon the directory, if required
				EnsTestCase testCase = (EnsTestCase)obj;
				String[] bits = testCase.getName().split("\\.");
				String dirName = bits[bits.length - 2];

				testCase.setTypeFromDirName(dirName);

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
	 * @param jarFileName The name of the jar file to search.
	 * @param packageName The package name of the tests.
	 * @return The list of tests in the jar file.
	 */
	public List findTestsInJar(String jarFileName, String packageName) {

		logger.finest("Looking for tests in " + jarFileName);

		ArrayList tests = new ArrayList();

		try {

			JarFile jarFile = new JarFile(jarFileName);

			for (Enumeration enum = jarFile.entries(); enum.hasMoreElements();) {

				JarEntry entry = (JarEntry)enum.nextElement();
				String entryName = entry.getName().replace(File.separatorChar, '.');

				Object obj = new Object();

				// if entryName matches base package name, extract test name and subdir
				if (!entry.isDirectory() && entryName.indexOf(packageName) > -1) {

					String[] bits = entryName.split("\\.");
					String className = bits[bits.length - 2];
					String dirName = bits[bits.length - 3];
					String extension = bits[bits.length - 1];
					if (extension.equalsIgnoreCase("class") && !dirName.equals("testcase")) {
						try {

							Class newClass = Class.forName(packageName + "." + dirName + "." + className);
							obj = newClass.newInstance();

						} catch (InstantiationException ie) {
							logger.warning(className + " does not seem to be a test case class");
						} catch (Exception e) {
							e.printStackTrace();
						}

						if (obj instanceof org.ensembl.healthcheck.testcase.EnsTestCase && !tests.contains(obj)) {

							// set the test's type based upon the directory, if required
							EnsTestCase testCase = (EnsTestCase)obj;
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
	 * Add all tests in subList to mainList, <em>unless</em> the test is already a member
	 * of mainList.
	 * 
	 * @param mainList The list to add to.
	 * @param subList The list to be added.
	 */
	public void addUniqueTests(List mainList, List subList) {

		Iterator it = subList.iterator();
		while (it.hasNext()) {

			EnsTestCase test = (EnsTestCase)it.next();
			// can't really use List.contains() as the lists store objects which may be different
			if (!testInList(test, mainList)) {
				mainList.add(test);
				logger.fine("Added " + test.getShortTestName() + " to the list of tests");
			} else {
				logger.fine("Skipped " + test.getShortTestName() + " as it is already in the list of tests");
			}
		}

	} // addUniqueTests

	//	-------------------------------------------------------------------------
	/**
	 * Check if a particular test is in a list of tests. The check is done by test name.
	 * 
	 * @param test The test case to check.
	 * @param list The list to search.
	 * @return true if test is in list.
	 */
	public boolean testInList(EnsTestCase test, List list) {

		boolean inList = false;
		Iterator it = list.iterator();

		while (it.hasNext()) {
			EnsTestCase thisTest = (EnsTestCase)it.next();
			if (thisTest.getTestName().equals(test.getTestName())) {
				inList = true;
			}
		}

		return inList;

	} // testInList

	// -----------------------------------------------------------------

} // TestRegistry