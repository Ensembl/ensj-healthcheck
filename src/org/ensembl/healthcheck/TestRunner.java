/*
  Copyright (C) 2003 EBI, GRL
 
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Publicpr
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

import java.util.*;
import java.util.logging.*;
import java.sql.*;
import java.io.*;
import java.util.regex.*;

import junit.framework.*;

import org.ensembl.healthcheck.util.*;

/**
 * <p>TestRunner is a base class that provides utilities for running tests -
 * logging, the ability to find and run tests from certain locations, etc.</p>
 */

public class TestRunner {
  
  /** List that holds an instance of each test. */
  protected List allTests;            
  /** The List of group names (as Strings) that will be run. */
  protected List groupsToRun;
  /** Contains database connection parameters read in from database.properties */
  protected Properties dbProps;
  /** If set, database names are filtered with this regular expression before the regexp built into the tests. */
  protected String preFilterRegexp;
  
  protected static Logger logger = Logger.getLogger("HealthCheckLogger");
  
  // -------------------------------------------------------------------------
  /** Creates a new instance of TestRunner */
  
  public TestRunner() {
    
    groupsToRun = new ArrayList();
    
  } // TestRunner
  
  // -------------------------------------------------------------------------
  /**
   * Read the <code>database.properties</code> file into dbProps.
   */
  protected void readPropertiesFile() {
    
    String propsFile = System.getProperty("user.dir") + System.getProperty("file.separator") + "database.properties";
    dbProps = Utils.readPropertiesFile(propsFile);
    logger.fine("Read database properties from " + propsFile);
    Enumeration e = dbProps.propertyNames();
    String propName;
    while (e.hasMoreElements()) {
      propName = (String)e.nextElement();
      logger.finer("\t" + propName + " = " + dbProps.getProperty(propName));
    }
    
  } // readPropertiesFile
  
  // -------------------------------------------------------------------------
  /**
   * Get a list of database names that match a particular regular expression.
   * @param regexp The regular expression to match.
   * @return An array of the matching database names (may be empty if none matched).
   */
  public String[] getListOfDatabaseNames(String regexp) {
    
    Connection conn;

    String[] databaseNames = null;
    
    // open connection
    try {
      
      conn = DBUtils.openConnection(dbProps.getProperty("driver",      "org.gjt.mm.mysql.Driver"),
      dbProps.getProperty("databaseURL", "kaka.sanger.ac.uk"),
      dbProps.getProperty("user",        "anonymous"),
      dbProps.getProperty("password",    ""));
      
      logger.fine("Opened connection to " + dbProps.getProperty("databaseURL", "kaka.sanger.ac.uk") + " as " + dbProps.getProperty("user", "anonymous"));
      
      databaseNames = DBUtils.listDatabases(conn, regexp, preFilterRegexp);
      
      if (databaseNames.length == 0) {
        logger.info("No database names matched");
      }
      
      conn.close();
      
      logger.fine("Connection closed");
      
    } catch (Exception e) {
      
      e.printStackTrace();
      System.exit(1);
      
    }
    
    return databaseNames;
    
  } // getDatabaseList
  
  // -------------------------------------------------------------------------
  /**
   * Prints, to stdout, a list of the database names that match a given regular expression.
   * @param regexp The regular expression to match.
   */
  protected void showDatabaseList(String regexp) {
    
    logger.fine("Listing databases matching " + regexp + " :\n");
    
    String[] databaseList = getListOfDatabaseNames(regexp);
    
    for (int i = 0; i < databaseList.length; i++) {
      logger.fine("\t" + databaseList[i]);
    }
    
  } // showDatabaseList
  
  // -------------------------------------------------------------------------
  /**
   * Finds all tests in several locations.
   * A test case is a class that extends EnsTestCase.
   * Test case classes found in more than one location are only added once.
   * @return A List containing objects of the test case classes found.
   */
  protected List findAllTests() {
    
    ArrayList allTests = new ArrayList();
    
    // some variables that will come in useful later on
    String thisClassName = this.getClass().getName();
    String packageName = thisClassName.substring(0, thisClassName.lastIndexOf("."));
    String directoryName = packageName.replace('.', File.separatorChar);
    logger.finest("Package name: " + packageName + " Directory name: " + directoryName);
    String runDir = System.getProperty("user.dir") + File.separator;
    
    // places to look
    ArrayList locations = new ArrayList();
    locations.add(runDir + "src" + File.separator + directoryName);       // same directory as this class
    locations.add(runDir + "build" + File.separator + directoryName);     // ../build/<packagename>
    
    // look in each of the locations defined above
    Iterator it = locations.iterator();
    while (it.hasNext()) {
      String location = (String)it.next();
      File dir = new File(location);
      if (dir.exists()) {
        if (location.lastIndexOf('.') > -1 && location.substring(location.lastIndexOf('.')).equalsIgnoreCase("jar")) { // ToDo -check this
          addUniqueTests(allTests, findTestsInJar(location, packageName));
        } else {
          addUniqueTests(allTests, findTestsInDirectory(location, packageName));
        }
      } else {
        logger.info(dir.getAbsolutePath() + " does not exist, skipping.");
      } // if dir.exists
    }
    
    // --------------------------------
    logger.finer("Found " + allTests.size() + " unique test case classes.");
    
    /*Iterator it = allTests.iterator();
    while (it.hasNext()) {
      EnsTestCase tc = (EnsTestCase)it.next();
      System.out.println("#####" + tc.getTestName());
    }
     */
    
    return allTests;
    
  } // findAllTests
  
  // -------------------------------------------------------------------------
  /**
   * Run all the tests in a list. 
   * @param allTests The tests to run, as objects.
   * @param forceDatabases If true, use only the database name pattern specified 
   * on the command line, <em>not</em> the regular expression built in to the test case.
   */
  protected void runAllTests(List allTests, boolean forceDatabases) {
    
    // check if allTests() has been populated
    if (allTests == null) {
      logger.warning("No tests to run! Call findAllTests() first?");
    }
    
    if (allTests.size() == 0) {
      logger.warning("Warning: no tests found!");
      return;
    }
    
    Iterator it = allTests.iterator();
    while (it.hasNext()) {
      
      EnsTestCase testCase = (EnsTestCase)it.next();
            
      if (testCase.inGroups(groupsToRun)) {
        logger.warning("\nRunning test of type " + testCase.getClass().getName());
        if (preFilterRegexp != null) {
          testCase.setPreFilterRegexp(preFilterRegexp);
        }
        
        if (forceDatabases) {
          // override built-in database regexp with the one specified on the command line
          testCase.setDatabaseRegexp(preFilterRegexp);
        }
        
        TestResult tr = testCase.run();
        
        String passFail = tr.getResult() ? "PASSED" : "FAILED";
        logger.warning(tr.getName() + " " + passFail + " " + tr.getMessage());

      }
    }
    
  } // runAllTests
  
  // -------------------------------------------------------------------------
  /**
   * Get an iterator that will iterate over database connections whose names 
   * match a particular regular expression.
   * @param databaseRegexp The regular expression to match.
   * @return A DatabaseConnectionIterator object that will iterate over database 
   * Connections for databases whose names match the regular expression.
   */
  public DatabaseConnectionIterator getDatabaseConnectionIterator(String databaseRegexp) {
    
    return new DatabaseConnectionIterator(dbProps.getProperty("driver"),
    dbProps.getProperty("databaseURL"),
    dbProps.getProperty("user"),
    dbProps.getProperty("password"),
    getListOfDatabaseNames(databaseRegexp));
    
  } // getDatabaseConnectionIterator
  
  // -------------------------------------------------------------------------
  /**
   * Find all the tests (ie classes that extend EnsTestCase) in a directory.
   * @param dir The base directory to look in.
   * @param packageName The EnsTestCase package name.
   * @return A list of tests in dir.
   */
  public List findTestsInDirectory(String dir, String packageName) {
    
    logger.info("Looking for tests in " + dir);
    
    ArrayList tests = new ArrayList();
    
    File f = new File(dir);
    
    // find all classes that extend org.ensembl.healthcheck.EnsTestCase
    ClassFileFilenameFilter cnff = new ClassFileFilenameFilter();
    File[] classFiles = f.listFiles(cnff);
    logger.finer("Examining " + classFiles.length + " class files ...");
    
    // check if each class file extends EnsTestCase by checking its type
    // need to avoid trying to instantiate the abstract class EnsTestCase iteslf
    Class newClass;
    Object obj = new Object();
    String baseClassName;
    
    for (int i = 0; i < classFiles.length; i++) {
      
      logger.finest(classFiles[i].getName());
      baseClassName = classFiles[i].getName().substring(0, classFiles[i].getName().lastIndexOf("."));
      
      try {
        newClass = Class.forName(packageName + "." + baseClassName);
        String className = newClass.getName();
        if (!className.equals("org.ensembl.healthcheck.EnsTestCase") &&
        !className.substring(className.length()-4).equals("Test")  ) {  // ignore JUnit tests
          obj = newClass.newInstance();
        }
      } catch (InstantiationException ie) {
        // normally it is a BAD THING to just ignore exceptions
        // however InstantiationExceptions may be thrown often in this case, 
        // so we deliberately chose to suppres this particular exception
      } catch (Exception e) {
        e.printStackTrace();
      }
      
      if (obj instanceof org.ensembl.healthcheck.EnsTestCase && !tests.contains(obj)) {
        ((EnsTestCase)obj).init(this);
        tests.add(obj); // note we store an INSTANCE of the test, not just its name
        //logger.info("Added test case " + obj.getClass().getName());
      }
      
    } // for classFiles
    
    return tests;
    
  } // findTestsInDirectory
  
  // -------------------------------------------------------------------------
  /** 
   * Find tests in a jar file.
   * @param jarFileName The name of the jar file to search.
   * @param packageName The package name of the tests.
   * @return The list of tests in the jar file.
   */ 
  public List findTestsInJar(String jarFileName, String packageName) {
    
    ArrayList tests = new ArrayList();
    
    //throw new NotImplementedException();

    return tests;
    
  } // findTestsInJar
  
  // -------------------------------------------------------------------------
  /** 
   * Add all tests in subList to mainList, <em>unless</em> the test is already a member of mainList.
   * @param mainList The list to add to.
   * @param subList The list to be added.
   */
  public void addUniqueTests(List mainList, List subList) {
    
    Iterator it = subList.iterator();
    
    while (it.hasNext()) {
      
      EnsTestCase test = (EnsTestCase)it.next();
      if (!testInList(test, mainList)) { // can't really use List.contains() as the lists store objects which may be different
        mainList.add(test);
        logger.info("Added " + test.getShortTestName() + " to the list of tests");
      } else {
        logger.fine("Skipped " + test.getShortTestName() + " as it is already in the list of tests");
      }
    }
    
  } // addUniqueTests
  
  // -------------------------------------------------------------------------
  /**
   * Check if a particular test is in a list of tests. The check is done by test name.
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
  // -------------------------------------------------------------------------
  
} // TestRunner

// -------------------------------------------------------------------------


// -------------------------------------------------------------------------
