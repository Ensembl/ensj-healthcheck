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

/**
 * <p>Title: TestRunner.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Organisation: EMBL</p>
 * <p>Created on March 11, 2003, 1:55 PM</p>
 * @author Glenn Proctor <glenn@ebi.ac.uk>
 * @version $Revision$
 */

package org.ensembl.healthcheck;

import java.util.*;
import java.util.logging.*;
import java.sql.*;
import java.io.*;
import java.util.regex.*;

import junit.framework.*;

import org.ensembl.healthcheck.util.*;

public class TestRunner {
  
  private static String version = "$Id$";
  private ArrayList allTests;            // will hold an instance of each test
  private ArrayList groupsToRun;
  private Properties dbProps;
  private String commandLineRegexp = null;
  private boolean forceDatabases = false;
  
  private static Logger logger = Logger.getLogger("HealthCheckLogger");
  
  // -------------------------------------------------------------------------
  /** Creates a new instance of TestRunner */
  
  public TestRunner() {
    
    groupsToRun = new ArrayList();
    
  } // TestRunner
  
  // -------------------------------------------------------------------------
  
  public static void main(String[] args) {
    
    TestRunner tr = new TestRunner();
    
    System.out.println(tr.getVersion());
    
    tr.parseCommandLine(args);
    
    tr.setupLogging();
    
    tr.readPropertiesFile();
    
    //tr.showDatabaseList();
    
    tr.runAllTests(tr.findAllTests());
    
  } // main
  
  // -------------------------------------------------------------------------
  
  private void readPropertiesFile() {
    
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
  
  
  private void parseCommandLine(String[] args) {
    
    if (args.length == 0) {
      
      printUsage();
      System.exit(1);
      
    } else {
      
      for (int i=0; i < args.length; i++) {
	
	if (args[i].equals("-h")) {
	  
	  printUsage();
	  System.exit(0);
	  
	} else if (args[i].equals("-d")) {
	  
	  i++;
	  commandLineRegexp = args[i];
	  System.out.println("Will pre-filter database names on " + commandLineRegexp);
	  
	} else if (args[i].equals("-force")) {
	  
	  forceDatabases = true;
	  System.out.println("Will use ONLY databases specified by -d");
	  
	} else {
	  groupsToRun.add(args[i]);
	  System.out.println("Will run tests in group " + args[i]);
	}
      }
      
      if (forceDatabases && commandLineRegexp == null) {
	System.err.println("You have requested -force but not specified a database name regular expression with -d");
	System.exit(1);
      }
    }
    
  } // parseCommandLine
  
  // -------------------------------------------------------------------------
  
  private void setupLogging() {
    
    logger.setUseParentHandlers(false); // stop parent logger getting the message
    Handler myHandler = new MyStreamHandler(System.out, new LogFormatter());
    logger.addHandler(myHandler);
    logger.setLevel(Level.FINEST);
    logger.info("Set logging level to " + logger.getLevel().getName());
    
  } // setupLogging
  
  // -------------------------------------------------------------------------
  
  private void printUsage() {
    
    System.out.println("\nUsage: TestRunner {-d regexp} {-force} {group1} {group2} ...\n");
    System.out.println("Options:");
    System.out.println("  -d regexp  Use the given regular expression to decide which databases to use.");
    System.out.println("  -force     Run the named tests on the databases matched by -d, without ");
    System.out.println("             taking into account the regular expressions built into the tests themselves.");
    System.out.println("  group1     Names of groups of test cases to run.");
    System.out.println("             Note each test case is in a group of its own with the name of the test case.");
    System.out.println("             This allows individual tests to be run if required.");
    System.out.println("");
    
  } // printUsage
  
  // -------------------------------------------------------------------------
  
  public String getVersion() {
    
    // strip off first and last few chars of version since these are only used by CVS
    return version.substring(5, version.length()-2);
    
  } // getVersion
  
  // -------------------------------------------------------------------------
  
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
  
  private void showDatabaseList(String regexp, String preFilterRegexp) {
    
    logger.fine("Listing databases matching " + regexp + " :\n");
    
    String[] databaseList = getListOfDatabaseNames(regexp, preFilterRegexp);
    
    for (int i = 0; i < databaseList.length; i++) {
      logger.fine("\t" + databaseList[i]);
    }
    
  } // showDatabaseList
  
  // -------------------------------------------------------------------------
  
  private ArrayList findAllTests() {
    
    ArrayList allTests = new ArrayList();
    
    // --------------------------------
    // look for tests in the same directory as this class
    String thisClassName = this.getClass().getName();
    String packageName = thisClassName.substring(0, thisClassName.lastIndexOf("."));
    String directoryName = packageName.replace('.', File.separatorChar);
    logger.finest("Package name: " + packageName + " Directory name: " + directoryName);
    String fullDirectory = System.getProperty("user.dir") + File.separator + "src" + File.separator + directoryName;
    allTests.addAll(findTestsInDirectory(fullDirectory, packageName));
    
    // --------------------------------
    
    
    
    // --------------------------------
    logger.finer("Found " + allTests.size() + " test case classes.");
    
    /*Iterator it = allTests.iterator();
    while (it.hasNext()) {
      EnsTestCase tc = (EnsTestCase)it.next();
      System.out.println("#####" + tc.getTestName());
    }
     */
    
    return allTests;
    
  } // findAllTests
  
  // -------------------------------------------------------------------------
  
  private void runAllTests(ArrayList allTests) {
    
    Pattern pattern;
    Matcher matcher;
    
    // check if allTests() has been populated; if not, run findAllTests()
    if (allTests == null) {
      allTests = findAllTests();
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
	if (commandLineRegexp != null) {
	  testCase.setPreFilterRegexp(commandLineRegexp);
	}
	
	if (forceDatabases) {
	  // override built-in database regexp with the one specified on the command line
	  testCase.setDatabaseRegexp(commandLineRegexp);
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
  
  public ArrayList findTestsInDirectory(String dir, String packageName) {
    
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
	logger.info("Added test case " + obj.getClass().getName());
      }
      
    } // for classFiles
    
    return tests;
    
  } // findAllTestsByPath
  
  // -------------------------------------------------------------------------
  
  
  
} // TestRunner

// -------------------------------------------------------------------------


// -------------------------------------------------------------------------