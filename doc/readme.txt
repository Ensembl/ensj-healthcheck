EnsEMBL HealthCheck
===================


REQUIREMENTS
============

1. JDK 1.4 or later (see http://java.sun.com/ for downloads); this is already installed on the Sanger system in /usr/opt/java141/.
   Make sure that your JAVA_HOME environment variable is pointing to the correct directory and that the *correct* Java executables are in your path; put something like the following in your .cshrc:

  setenv JAVA_HOME /usr/opt/java141
  setenv PATH ${JAVA_HOME}/bin:${PATH}


INSTALLATION
============

1. Obtain the source files by checking out the ensj-healthcheck module from CVS.  Use the -r option to check out a specific tag if required. 

2. cd ensj-healthcheck

3. Edit database.properties to contain values that correspond to the database server which you want to connect to. Note that you only specify the *host* here - the actual databases are specified on the command line when running tests.


RUNNING
=======

A number of shell scripts (with a .sh extension) are provided to aid in running healthchecks. These are summarised below; the main one you will use is called run-healthcheck.sh; note that this script actually passes all of its command-line options through to the TextTestRunner class.

 Usage: TextTestRunner {options} {group1} {group2} ...
 
 Options:
   -d regexp       Use the given regular expression to decide which databases to use.
   -force          Run the named tests on the databases matched by -d, without
                   taking into account the regular expressions built into the tests themselves.
   -h              This message.
   -output level   Set output level; level can be one of
                   none      nothing is printed
                   problem   only problems are reported
                   correct   only correct results (and problems) are reported
                   summary   only summary info (and problems, and correct reports) are reported
                   info      info (and problem, correct, summary) messages reported
                   all       everything is printed
   -config file    Read config from file (in ensj-healthcheck dir) rather than database.properties
   -debug          Print debugging info (for developers only)
   -repair         If appropriate, carry out repair methods on test cases that support it
   -showrepair     Like -repair, but the repair is NOT carried out, just reported.
   -length n       Break output lines at n columns; default is 65. 0 means never break
   -schemainfo     Cache schema info at startup; required for SchemasMatch testcase
   -refreshschemas Rebuild the stored schema info; this is rather slow as every schema must be examined, but should be used when a schema structure change has occurred.
   group1          Names of groups of test cases to run.
                   Note each test case is in a group of its own with the name of the test case.
                   This allows individual tests to be run if required.

If no tests or test groups are specified, and a database regular expression is given with -d, the matching databases are shown.

Test Groups
-----------

There are a number of test groups which contain one or more tests, e.g. post_genebuild, pre_release. Each test is also a member (in fact the only member) of a test group with the same name as the test case. This allows individual tests to be specified by name on the command line if necessary.

Other Utilities
---------------

Run each of these with the -h option to show usage.
   
  show-groups.sh 		Shows all the groups and the tests belonging to them. No command-line options required.
  database-name-matcher.sh	Shows which database names match a particular regular expression.
  list-tests-in-group.sh	Shows all the tests in a particular group, with descriptions if required.
  run-healthcheck-gui.sh	Starts the healthcheck GUI. 
  compile-healthcheck.sh	Only used if you've made changes to the source, e.g. when writing your own tests.


WRITING YOUR OWN TESTS
======================

If you want to write your own healthchecks, rather than running the pre-defined ones, see the file README-writing-tests.txt.

----------------------------------------------------------------------

