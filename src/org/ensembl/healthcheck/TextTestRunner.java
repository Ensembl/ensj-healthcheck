/*
  Copyright (C) 2004 EBI, GRL
 
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

import org.ensembl.healthcheck.util.*;
import org.ensembl.healthcheck.testcase.*;

import java.util.*;
import java.util.logging.*;

/**
 * Subclass of TestRunner intended for running tests from the command line.
 */
public class TextTestRunner extends TestRunner implements Reporter {
  
  private static String version = "$Id$";
  private boolean forceDatabases = false;
  private boolean debug = false;
  private boolean useSchemaInfo = true;
  private boolean rebuildSchemaInfo = false;
  public ArrayList outputBuffer ;
  private String lastDatabase = "";
  
  // -------------------------------------------------------------------------
  
  /**
   * Command-line run method.
   * @param args The command-line arguments.
   */
  public static void main(String[] args) {
    
    TextTestRunner ttr = new TextTestRunner();
    
    System.out.println(ttr.getVersion());
    
    ttr.readPropertiesFile();
    
    ttr.parseCommandLine(args);
    ttr.outputBuffer = new ArrayList();
    
    ttr.setupLogging();
    
    if (ttr.useSchemaInfo) {
      ttr.buildSchemaList(true);
    }
    
    //if (ttr.useSchemaInfo && !ttr.rebuildSchemaInfo) { // if buildSchemaList has been called, SchemaManager will already have been populated
    //  ttr.readStoredSchemaInfo();
    //}
    ReportManager.setReporter(ttr);
    
    ttr.runAllTests(ttr.findAllTests(), ttr.forceDatabases);
    
    ConnectionPool.closeAll();
    
  }
  
  // main
  // -------------------------------------------------------------------------
  private void printUsage() {
    
    System.out.println("\nUsage: TextTestRunner {options} {group1} {group2} ...\n");
    System.out.println("Options:");
    System.out.println("  -d regexp       Use the given regular expression to decide which databases to use.");
    System.out.println("  -force          Run the named tests on the databases matched by -d, without ");
    System.out.println("                  taking into account the regular expressions built into the tests themselves.");
    System.out.println("  -h              This message.");
    System.out.println("  -output level   Set output level; level can be one of ");
    System.out.println("                  none      nothing is printed");
    System.out.println("                  problem   only problems are reported");
    System.out.println("                  correct   only correct results (and problems) are reported");
    System.out.println("                  summary   only summary info (and problems, and correct reports) are reported");
    System.out.println("                  info      info (and problem, correct, summary) messages reported");
    System.out.println("                  all       everything is printed");
    System.out.println("  -config file    Read config from file (in ensj-healthcheck dir) rather than database.properties");
    System.out.println("  -debug          Print debugging info (for developers only)");
    System.out.println("  -repair         If appropriate, carry out repair methods on test cases that support it");
    System.out.println("  -showrepair     Like -repair, but the repair is NOT carried out, just reported.");
    System.out.println("  -noschemainfo   Do not cache schema info at startup. Quicker, but may cause some tests not to work. Use with caution.");
    System.out.println("  -refreshschemas Rebuild the stored schema info; this is rather slow as every schema must be examined, but should be used when a schema structure change has occurred.");
    System.out.println("  group1          Names of groups of test cases to run.");
    System.out.println("                  Note each test case is in a group of its own with the name of the test case.");
    System.out.println("                  This allows individual tests to be run if required.");
    System.out.println("");
    System.out.println("If no tests or test groups are specified, and a database regular expression is given with -d, the matching databases are shown. ");
    System.out.println("");
    System.out.println("Currently available tests:");
    
    List tests = findAllTests();
    Collections.sort(tests, new TestComparator());
    Iterator it = tests.iterator();
    while (it.hasNext()) {
      EnsTestCase test = (EnsTestCase)it.next();
      System.out.print(test.getShortTestName() + " ");
    }
    
    System.out.println("");
    
  }
  
  /**
   * Return the CVS version string for this class.
   * @return The version.
   */
  public String getVersion() {
    
    // strip off first and last few chars of version since these are only used by CVS
    return version.substring(5, version.length() - 2);
    
  }
  
  // -------------------------------------------------------------------------
  private void parseCommandLine(String[] args) {
    
    if (args.length == 0) {
      
      printUsage();
      System.exit(1);
      
    } else {
      
      for (int i = 0; i < args.length; i++) {
        
        if (args[i].equals("-h")) {
          
          printUsage();
          System.exit(0);
          
        } else if (args[i].equals("-output")) {
          
          setOutputLevel(args[++i]);
          // System.out.println("Set output level to " + outputLevel);
          
        } else if (args[i].equals("-debug")) {
          
          debug = true;
          
        } else if (args[i].equals("-repair")) {
          
          doRepair = true;
          
        } else if (args[i].equals("-showrepair")) {
          
          showRepair = true;
          
        } else if (args[i].equals("-d")) {
          
          i++;
          preFilterRegexp = args[i];
          // System.out.println("Will pre-filter database names on " + preFilterRegexp);
          
        } else if (args[i].equals("-force")) {
          
          forceDatabases = true;
          // System.out.println("Will use ONLY databases specified by -d");
          
        } else if (args[i].equals("-noschemainfo")) {
          
          useSchemaInfo = false;
          // System.out.println("Will NOT read schema info at startup");
          
        } else if (args[i].equals("-refreshschemas")) {
          
          rebuildSchemaInfo = true;
          // System.out.println("Will rebuild and store schema info at startup");
          
        } else if (args[i].equals("-config")) {
          
          i++;
          propertiesFileName = args[i];
          // System.out.println("Will read properties from " + propertiesFileName);
          
        } else {
          
          groupsToRun.add(args[i]);
          // System.out.println("Will run tests in group " + args[i]);
          
        }
        
      }
      
      if (forceDatabases && (preFilterRegexp == null)) {
        
        System.err.println("You have requested -force but not specified a database name regular expression with -d");
        System.exit(1);
        
      }
      
      // print matching databases if no tests specified
      if (groupsToRun.size() == 0 && preFilterRegexp != null) {
        System.out.println("Databases that match the regular expression '" + preFilterRegexp + "':");
        String[] names = getListOfDatabaseNames(".*");
        for (int i = 0; i < names.length; i++) {
          System.out.println("  " + names[i]);
        }
      }
      
    }
    
  }
  
  // parseCommandLine
  // -------------------------------------------------------------------------
  private void setupLogging() {
    
    logger.setUseParentHandlers(false);// stop parent logger getting the message
    
    Handler myHandler = new MyStreamHandler(System.out, new LogFormatter());
    
    logger.addHandler(myHandler);
    logger.setLevel(Level.WARNING);// default - only print important messages
    
    if (debug) {
      
      logger.setLevel(Level.FINEST);
      
    }
    
    //logger.info("Set logging level to " + logger.getLevel().getName());
  }
  
  // setupLogging
  // -------------------------------------------------------------------------
  
  public void message( ReportLine reportLine ) {
    String level = "ODD    ";
    
    System.out.print( "." );
    System.out.flush();
    
    if( reportLine.getLevel() < outputLevel ) {
      return;
    }
    
    if( ! reportLine.getDatabaseName().equals( lastDatabase )) {
      outputBuffer.add( "  " + reportLine.getDatabaseName() );
      lastDatabase = reportLine.getDatabaseName();
    }
    
    switch( reportLine.getLevel() ) {
      case( ReportLine.PROBLEM ) :
        level = "PROBLEM";
        break;
      case( ReportLine.WARNING ) :
        level = "WARNING";
        break;
      case( ReportLine.INFO ) :
        level = "INFO   ";
        break;
      case( ReportLine.CORRECT ) :
        level = "CORRECT";
        break;
    }
    
    outputBuffer.add( "    " + level + ":  " +
    lineBreakString( reportLine.getMessage(), 65, "              " ));
  }
  
  public void startTestCase( EnsTestCase testCase ) {
    String name;
    name = testCase.getClass().getName();
    name = name.substring( name.lastIndexOf( "." ) + 1 );
    System.out.print( name + " " );
    System.out.flush();
  }
  
  public void finishTestCase( EnsTestCase testCase, TestResult result ) {
    
    if (result.getResult()) {
      System.out.println(" PASSED");
    } else {
      System.out.println(" FAILED");
    }
    
    lastDatabase = "";
    Iterator it = outputBuffer.iterator();
    while( it.hasNext() ) {
      System.out.println( (String) it.next() );
    }
    outputBuffer.clear();
    
  }
  
  private String lineBreakString( String mesg, int maxLen, String indent ) {
    if( mesg.length() <= maxLen ) {
      return mesg;
    }
    
    int lastSpace = mesg.lastIndexOf( " ", maxLen );
    if( lastSpace > 15 ) {
      return mesg.substring(0, lastSpace) + "\n" + indent +
      lineBreakString(mesg.substring(lastSpace+1), maxLen, indent);
    } else {
      return mesg.substring(0, maxLen) + "\n" + indent +
      lineBreakString(mesg.substring(maxLen), maxLen, indent);
    }
  }
}


// TextTestRunner
