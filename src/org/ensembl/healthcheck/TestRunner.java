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
  
  protected List allTests;            // will hold an instance of each test
  protected List groupsToRun;
  protected Properties dbProps;
  
  private static Logger logger = Logger.getLogger("HealthCheckLogger");

  // -------------------------------------------------------------------------
  /** Creates a new instance of TestRunner */
  
  public TestRunner() {
    
    groupsToRun = new ArrayList();
    
  } // TestRunner
  
  // -------------------------------------------------------------------------
  
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
  
 
  public String[] getListOfDatabaseNames(String regexp, String preFilterRegexp) {
    
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
	logger.warning("No database names matched");
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
  
  protected void showDatabaseList(String regexp, String preFilterRegexp) {
    
    logger.fine("Listing databases matching " + regexp + " :\n");
    
    String[] databaseList = getListOfDatabaseNames(regexp, preFilterRegexp);
    
    for (int i = 0; i < databaseList.length; i++) {
      logger.fine("\t" + databaseList[i]);
    }
    
  } // showDatabaseList
  
  // -------------------------------------------------------------------------
  
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
    logger.finer("Found " + allTests.size() + " test case classes of which .");
    
    /*Iterator it = allTests.iterator();
    while (it.hasNext()) {
      EnsTestCase tc = (EnsTestCase)it.next();
      System.out.println("#####" + tc.getTestName());
    }
     */
    
    return allTests;
    
  } // findAllTests
  
  // -------------------------------------------------------------------------
  
  protected void runAllTests(List allTests, String preFilterRegexp, boolean forceDatabases) {
    
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
	logger.info("\tRunning test of type " + testCase.getClass().getName());
	if (preFilterRegexp != null) {
	  testCase.setPreFilterRegexp(preFilterRegexp);
	}
	
	if (forceDatabases) {
	  // override built-in database regexp with the one specified on the command line
	  testCase.setDatabaseRegexp(preFilterRegexp);
	}
	TestResult tr = testCase.run();
	System.out.println("\n" + tr.getName() + " " + tr.getResult() + " " + tr.getMessage() + "\n");
	// TBC
      }
    }
    
  } // runAllTests
  
  // -------------------------------------------------------------------------
  
  public DatabaseConnectionIterator getDatabaseConnectionIterator(String[] databaseNames) {
    
    return new DatabaseConnectionIterator(dbProps.getProperty("driver"),
    dbProps.getProperty("databaseURL"),
    dbProps.getProperty("user"),
    dbProps.getProperty("password"),
    databaseNames);
    
  } // getDatabaseConnectionIterator
  
  // -------------------------------------------------------------------------
  
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
  
  public List findTestsInJar(String jarFileName, String packageName) {
    
    ArrayList tests = new ArrayList();
    
    // TBC
    
    return tests;
    
  } // findTestsInJar
  
  // -------------------------------------------------------------------------
  /**
   * Add all tests in subList to mainList, <em>unless</em> the test is already a member of mainList.
   */
  public void addUniqueTests(List mainList, List subList) {
    
    Iterator it = subList.iterator();
    
    while (it.hasNext()) {
      
      EnsTestCase test = (EnsTestCase)it.next();
      if (!testInList(test, mainList)) { // can't really use List.contains() as the lists store objects which may be different
	mainList.add(test);
	logger.info("Added " + test.getShortTestName() + " to the list of tests to run");
      } else {
	logger.fine("Skipped " + test.getShortTestName() + " as it is already in the list of tests to run");
      }
    }
    
  } // addUniqueTests
  
  // -------------------------------------------------------------------------
  
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