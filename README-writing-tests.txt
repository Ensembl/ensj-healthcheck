
  
WRITING YOUR OWN TESTS
======================

The TestRunner class is able to "discover" test case classes at runtime, provided they fulfil certain criteria:

1. They must be in the package org.ensembl.healthcheck.testcase

2. The .class files must be in src/org/ensembl/healthcheck/testcase or build/org/ensembl/healthcheck/testcase [2]

3. The test case classes must extend org.ensembl.healthcheck.EnsTestCase

See the Javadoc documentation for more details, particularly for the following classes:

 EnsTestCase                Base class containing many methods that can be used in tests.
 TestResult			        The object that the run() method can return after the test.
 ReportManager              Can be used to store information about the outcome of the tests.
 DatabaseConnectionIterator Implements java.lang.Iterator to facilitate obtaining database connections.
 DBUtils                    Various (static) methods for doing common database-oriented tasks.
 Utils				        Non-database utilities.

The EnsTestCase class is the base class for all test cases in the EnsEMBL Healthcheck system. It is not 
intended to be instantiated directly; subclasses should implement the run() method and use that to provide 
test-case specific behaviour.

EnsTestCase provides a number of methods which are intended to make writing test cases simple; in many 
cases an extension test case will involve little more than calling one of the methods in this class and 
setting some return value based on the result. For example, the following test case gets a 
DatabaseConnectionIterator, then uses it to loop over each affected database and call the countOrphans() 
method in EnsTestCase. In this particular situation, if there are any orphans in any of the databases, 
the test fails. The problem and correct methods of the ReportManager class are used to store information
about which databases pass or fail the test.

public class OrphanTestCase extends EnsTestCase {
  
  public OrphanTestCase() {
    databaseRegexp = "^homo_sapiens_core_12.*";
    addToGroup("group2
  }
  
  TestResult run() {
    
    boolean result = true;
    
    DatabaseConnectionIterator it = getDatabaseConnectionIterator();
        
    while (it.hasNext()) {
      
      Connection con = (Connection)it.next();
      int orphans = super.countOrphans(con, "gene", "gene_id", "gene_stable_id", "gene_id", false);
      
      if (orphans == 0) {
        ReportManager.correct(this, con, "No orphans between gene and gene_stable_id");
      } else {
        ReportManager.problem(this, con, orphans + " orphans between gene and gene_stable_id");
      }
      result &= (orphans == 0);
    }
    
    return new TestResult(getShortTestName(), result);
    
  }
  
} // OrphanTestCase

Most test cases you write will take this form:

1. Set up the database regexp [3] and any groups that this test case is a member of [4].

2. Implement the run() method; normally this will involve getting a DatabaseConnectionIterator, 
   calling one or more superclass methods on each database connection.

3. Create and return a TestResult object according to the results of your tests.

Notes
-----

[1] The CVS checkout currently includes some directories and files that are not necessary 
    (e.g. build/ classes/ etc).
[2] More flexible "discovery" of classes at runtime, e.g. from jar files and other directory 
    locations, is reasonably straightforward to add by modifying or overriding findAllTests() 
    in TestRunner.
[3] A utility class called DatabaseNameMatcher is provided that will take a regular expression 
    on the command line and list any database names that match it. This is useful for 
    developing/debugging the regular expressions. There is a script called database-name-matcher.sh that 
    can be used as a wrapper for this class.
[4] Note that all tests by default are members of a group called "all". There is no need to explicitly 
    add your new test to this group.
