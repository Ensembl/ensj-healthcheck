
WRITING YOUR OWN TESTS
======================

Before you start
----------------

There are two things you need to decide when starting to write a healthcheck:

1. What type of database does it apply do? Core, EST, Mart etc

   This will affect where you put your .java file. It should be in a directory
   like

     src/org/ensembl/healthcheck/testcase/{type}
   
   where {type} is one of:

     generic - for core, est, estgene and vega databases
     compara - for Compara-type databases
     mart    - for Marts
     etc

   The package statement at the top of the .java file should correspond to the
   directory, e.g. generic tests should have a package statement like

     package org.ensembl.healthcheck.testcase.generic;


2. Does it look at one database at a time, or several. If the latter, is the
   order important?

   This determines which of SingleDatabaseTestCase, MultiDatabaseTestCase or
   OrderedDatabaseTestCase you subclass in order to write your tests. Each of
   these has an abstract run() method with a different signature which you must
   override in your new test class.


Anatomy of a test
-----------------

Your test should be in an appropriately-named .java source file in the directory
decided upon in step 1 above. It has the following components:

  Constructor - a public constructor which should call addToGroup() to specify
  which test group(s) this test is a part of, and setDescription(). Neither of
  these is mandatory but all tests should be in at least one test group, and it
  is good practice to set a description.
  
  A types() method - by default the test will run on the types of databases that
  match the directory that the test is in - tests in the compara subdirectory
  will run on compara databases etc. Tests in the "generic" subdirectory will
  run by default on core, est, estgene and Vega databases, since they all have
  the same schema. You can override the default by implementing a types() method
  that calls addAppliesToType() and/or removeAppliesToType() to modify the
  internal list of types that this test applies to.

  A run() method - this is where the logic of the test takes place. The
  signature of this method is different depending on what type of test case
  (single, multi or ordered) is being run:

    Tests that extend SingleDatabaseTestCase have a run method that takes a
    single DatabaseRegistryEntry, which contains information about a single
    database.

    Tests that extend MultiDatabaseTestCase have a run method that takes a whole
    DatabaseRegistry - it is up to the tests themselves to decide which
    databases in the registry to run on.

    Tests that extend OrderedDatabaseTestCase have a run method that takes an
    array of DatabaseRegistryEntries - the order of DatabaseRegistryEntries in
    the array is the same as was specified on the command line.

  The run method should return true if the test passed, and false if it
  failed. Tests should use the problem() and correct() methods of the
  ReportManager class to store any problems they find and to indicate that no
  problems of a particular kind have been found.

The best way to learn how to write tests is to look at existing tests - some
recommended examples to get you started:

   generic/AssemblyException.java - a straightforward example of a
   SingleDatabaseTestCase. Note the use of the getRowCount() utility method
   (defined in EnsTestCase) and the use of ReportManager.

   generic/EmptyTables.java - a much more complicated example of a
   SingleDatabaseTestCase that makes extensive use of checks on database types
   and species.

   generic/MetaCrossSpecies.java - an example of a MultiDatabaseTestCase.

   mart/CompareOldNew.java - an example of an OrderedDatabaseTestCase.

See the Javadoc documentation for more details, particularly for the following
classes:

 EnsTestCase

  Base class containing many methods that can be used in tests.

 SingleDatabaseTestCase     :
 MultiDatabaseTestCase      : - see above
 OrderedDatabaseTestCase    :

 DatabaseRegistry 
 
  Holds a list of all the databases that were specified on the command line. 
  Used by MultiDatabaseTestCases. Has several methods for accessing the entries.

 DatabaseRegistryEntry

  Represents a single database. Holds the name, a JDBC Connection, and the
  species and type of the database.

 DatabaseType
  
  A type-safe enumeration of database types.

 Species
  
  A type-safe enumeration of species.

 ReportManager
 
  Can be used to store information about the outcome of the tests.

 DBUtils

  Various (static) methods for doing common database-oriented tasks.

 Utils

 Non-database utilities.

Note that for compactness the Javadoc is stored in CVS as a zip file (ind
doc/javadoc/ensj-healthcheck.zip) and will need to be unzipped if you want to
read it.


Compiling your test
-------------------

You will need to compile your .java file to a .class file in the build/
directory before you can run it. Note that since the healthcheck system now runs
from a Java jar file (lib/ensj-healthcheck.jar) this must be updated if the test
is to be found. There are several ways of compiling:

1. Use the compile-healthcheck.sh script; this will compile anything that needs
   it, and create the jar file.

2. Use Ant (see the Useful tools section) - the "jar" target in the healthcheck
   build.xml will compile any sources that require it and create the jar file.


Running your new test
---------------------

The text and GUI test runners are able to "discover" tests at runtime without
any extra configuration; provided your test has been compiled to a .class file,
extends one of the *DatabaseTestCase base classes, and is in the appropriate
subdirectory under build/ or in lib/ensj-healthcheck.jar, it should be picked up
automatically.


Useful tools
------------

You can of course just write your .java file in a text editor and compile and
run it from the command-line. However I recommend the following Open Source
tools if you're going to do any more serious Java development:

 - Ant http://ant.apache.org/ - powerful build system for Java, like make but
 easier to use! The healthcheck system comes with an Ant build.xml file which
 has a number of targets for compiling, creating jars etc.

 - Eclipse http://www.eclipse.org/ - an excellent IDE for developing in Java
 (and other languages). Makes programming a lot easier and quicker.
