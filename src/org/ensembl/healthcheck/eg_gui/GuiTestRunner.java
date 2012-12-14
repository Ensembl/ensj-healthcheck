package org.ensembl.healthcheck.eg_gui;

import java.awt.BorderLayout;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportLine;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.eg_gui.GuiTestResultWindowTab;
import org.ensembl.healthcheck.eg_gui.TestProgressDialog;
import org.ensembl.healthcheck.testcase.AbstractPerlBasedTestCase;
import org.ensembl.healthcheck.testcase.EnsTestCase;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;
import org.ensembl.healthcheck.testcase.OrderedDatabaseTestCase;
import org.ensembl.healthcheck.testcase.PerlScriptConfig;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

import com.mysql.jdbc.Connection;

import java.util.logging.Handler;
import java.util.logging.Logger;

public class GuiTestRunner {

	/**
	 * <p>
	 * 	Creates a logger that will forward any logged messages to the Report
	 * Manager.
	 * </p>
	 * 
	 * @param e
	 * @return Logger
	 * 
	 */
	protected static Logger createGuiLogger(Handler h) {
		
		Logger logger = Logger.getAnonymousLogger();
		
		for (Handler currentHandler : logger.getHandlers()) {
			logger.removeHandler(currentHandler);
		}

		// Otherwise messages will be sent to the screen.
		//
		logger.setUseParentHandlers(false);
		logger.addHandler(h);
		

		if (logger.getLevel()==null) {
			logger.setLevel(Constants.defaultLogLevel);
		}
		
		return logger;
	}

    /**
     * <p>
     * 	Run all the tests in a list.
     * </p>
     * 
     * @param ltests The tests to run.
     * @param ldatabases The databases to run the tests on.
     * @param lgtrf The test runner frame in which to display the results.
     */
    public static Thread runAllTests(
    		final List<Class<? extends EnsTestCase>> tests,
    		final DatabaseRegistryEntry[] databases,
    		final TestProgressDialog testProgressDialog,
    		final JComponent resultDisplayComponent,
    		final String PERL5LIB,
    		final PerlScriptConfig psc,
    		final GuiLogHandler guiLogHandler
    ) {

        // Tests are run in a separate thread
        //
        Thread t = new Thread() {

        	public void run() {
        		
        		PrintStream stderrSaved = System.err;

        		testProgressDialog.reset();
            	testProgressDialog.setVisible(true);

                int totalTestsToRun = tests.size() * databases.length;

                testProgressDialog.setMaximum(totalTestsToRun);

                int testsRun = 0;
                
                // for each test, if it's a single database test we run it against each
                // selected database in turn
                // for multi-database tests, we create a new DatabaseRegistry containing
                // the selected tests and use that
                //
                for (Class<? extends EnsTestCase> currentTest : tests) {
                	
                	// If there was an interrupt request for this thread, no
                	// more tests are executed.
                	//
                	if (isInterrupted()) {
                		break;
                	}
					
					// Create a logger with this handler
					Logger guiLogger   = createGuiLogger(guiLogHandler);

					// Inject into the current testcase. The logger property 
					// is static. It should be set before instantiation in
					// case something is done with the logger in the 
					// constructor. (As in AbstractPerlModuleBasedTestCase)
					//
					EnsTestCase.setLogger(guiLogger);
                	
                    EnsTestCase testCase = null;
					try {
						testCase = currentTest.newInstance();
					} 
					catch (InstantiationException e) { throw new RuntimeException(e); } 
					catch (IllegalAccessException e) {
						e.printStackTrace();
						throw new RuntimeException(e); 
					}

					// Inject a logger that will forward all logging messages 
					// to the gui.
					//
					Logger savedLogger = testCase.getLogger();
					
					// Tell the guiloghandler to associate all log messages 
					// with the current testcase.
					//
					guiLogHandler.setEnsTestCase(testCase);
					
					// System properties are set by the GUI. This prevents
					// EnsTestCase.importSchema from overwriting settings
					// by the user with the defaults from the configuration
					// file.
					//
					testCase.setSetSystemProperties(false);
					
					// Stack traces are written to stderr. They indicate a 
					// serious error. They are rerouted to the ReportManager
					// as problems and the test fails.
					//
					System.setErr(new ReporterPrintStream(testCase));
					
					// If PERL5LIB parameter has been set and this is a perl 
					// based test case, then set the PERL5LIB attribute.
					//
                    if (testCase instanceof AbstractPerlBasedTestCase) {
                    	
                    	AbstractPerlBasedTestCase at = (AbstractPerlBasedTestCase) testCase; 
                    	
                    	if (PERL5LIB != null) {                    	
                    		at.setPERL5LIB(PERL5LIB);                    	
                    	}
                    	
                    	if (psc != null) {
                    		at.setConfig(psc);
                    	}
                    }

                    boolean passed = false;
                    
                    if (testCase instanceof SingleDatabaseTestCase) {

                        for (DatabaseRegistryEntry currentDbre : databases) {
                        	
                            String message = testCase.getShortTestName() + ": " + currentDbre.getName();
                            
                            ReportManager.startTestCase(testCase, currentDbre);
                            
                            testProgressDialog.setNote(message);
                            
                            testCase.types();                            
                            
                            try {
                            	passed = ((SingleDatabaseTestCase) testCase).run(currentDbre);
                            }
                            catch (Exception e) {
                            	
                            	ReportManager.report(
                            			testCase, 
                            			currentDbre.getConnection(), 
                            			ReportLine.PROBLEM,
                            			testCase.getShortTestName() + " threw an exception:"
                            			+ e.getClass().getCanonicalName() + "\n\n"
                            			+ stackTraceToString(e.getStackTrace()) + "\n\n" 
                            			+ e.getMessage()
                            	);
                            }
                            catch (java.lang.Error e) {
                            	
                            	String errorMsg = testCase.getShortTestName() + " threw a java error:"
                            			+ e.getClass().getCanonicalName() + "\n\n"
                            			+ stackTraceToString(e.getStackTrace()) + "\n\n" 
                            			+ e.getMessage();
                            	
                            	System.err.println(errorMsg);
                            	System.out.println(errorMsg);
                            	stderrSaved.println(errorMsg);
                            	
                            	ReportManager.report(
                            			testCase, 
                            			currentDbre.getConnection(), 
                            			ReportLine.PROBLEM,
                            			errorMsg
                            	);
                            	
                            }
                            // If a test has not reported anything to the 
                            // report manager, there will not be any report. 
                            // The user may think that the test was not run.
                            // So in this case a standard line is generated.
                            //
                            boolean testHasReportedSomething = ReportManager.getAllReportsByTestCase().containsKey(testCase.getTestName()); 
                            
                            if (!testHasReportedSomething) { 
	                            if (passed) {
	                            	ReportManager.report(
	                            			testCase, 
	                            			currentDbre.getConnection(), 
	                            			ReportLine.INFO,
	                            			testCase.getShortTestName() + " did not produce any output, but reported that the database has passed."
	                            	);
	                            } else {
	                            	ReportManager.report(
	                            			testCase, 
	                            			currentDbre.getConnection(), 
	                            			ReportLine.PROBLEM,
	                            			testCase.getShortTestName() + " did not produce any output, but reported that the database has failed."
	                            	);
	                            }
                            }
                            
                            ReportManager.finishTestCase(testCase, passed, currentDbre);
                            
                            testsRun += 1;
                            
                            testProgressDialog.setProgress(testsRun);
                            testProgressDialog.repaint();
                        }

                    } else if (testCase instanceof MultiDatabaseTestCase) {

                        DatabaseRegistry dbr = new DatabaseRegistry(databases);
                        
                        ReportManager.startTestCase(testCase, null);
                        
                        String message = testCase.getShortTestName() + " ( " + dbr.getEntryCount() + " databases)";
                        
                        testProgressDialog.setNote(message);
                        
                        testCase.types();
                                              
                        try {
                        	passed = ((MultiDatabaseTestCase) testCase).run(dbr);
	                    }
	                    catch (Exception e) {
	                    	
	                    	ReportManager.report(
	                    			testCase, 
	                    			(Connection) null, 
	                    			ReportLine.PROBLEM,
	                    			testCase.getShortTestName() + " threw an exception:"
	                    			+ e.getClass().getCanonicalName() + "\n\n"
	                    			+ stackTraceToString(e.getStackTrace()) + "\n\n" 
	                    			+ e.getMessage()
	                    	);
	                    }

                        ReportManager.finishTestCase(testCase, passed, null);
                        
                        // If a test has not reported anything to the 
                        // report manager, there will not be any report. 
                        // The user may think that the test was not run.
                        // So in this case a standard line is generated.
                        //
                        boolean testHasReportedSomething = ReportManager.getAllReportsByTestCase().containsKey(testCase.getTestName()); 
                        
                        if (passed && !testHasReportedSomething) {
                        	ReportManager.report(
                        			testCase, 
                        			dbr.getAll()[0].getConnection(), 
                        			ReportLine.INFO,
                        			testCase.getShortTestName() + " did not produce any output, but reported that the database has passed."
                        	);
                        }

                        testsRun += dbr.getEntryCount();
                        
                        testProgressDialog.setProgress(testsRun);
                        testProgressDialog.repaint();

                    } else if (testCase instanceof OrderedDatabaseTestCase) {
                    	
                    	JOptionPane.showMessageDialog(
                    		testProgressDialog, 
                    		"Functionality for running OrderedDatabaseTestCases has not been implemented!", 
                    		"Error",
                            JOptionPane.ERROR_MESSAGE
                         );
                    }
                    
                    // Retore the original logger. Actually unnecessary, 
                    // because the testcase will not be used anymore.
                    //
                    testCase.setLogger(savedLogger);
                    
                    // Restore stderr
                    //
                    System.setErr(stderrSaved);
                        
                    ReportManager.finishTestCase(
                        	testCase, 
                        	passed, 
                        	null
                        );
                        
                }
                testProgressDialog.setVisible(false);
                
                // Open in the legacy result window, because it is really 
                // nice.
                //
                resultDisplayComponent.removeAll();
                resultDisplayComponent.add(
                	new GuiTestResultWindowTab("All", ReportLine.ALL), 
                	BorderLayout.CENTER
                );
                
                // The above will have no visible effect. In order for this 
                // to work, revalidate must be called.
                //
                // See: http://www.iam.ubc.ca/guides/javatut99/uiswing/overview/threads.html
                //
                resultDisplayComponent.revalidate();
            }
        };
        testProgressDialog.setRunner(t);
        
        t.setName("GuiTestRunner");
        
        UncaughtExceptionHandler eh = 
	        new UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread t, Throwable e) {
	            	String errorMsg = t.getName() + " threw a java error:"
	            			+ e.getClass().getCanonicalName() + "\n\n"
	            			+ stackTraceToString(e.getStackTrace()) + "\n\n" 
	            			+ e.getMessage();
	            	
	            	System.err.println(errorMsg);
	            	System.out.println(errorMsg);				
				}
	        };
        
        t.setUncaughtExceptionHandler(eh);
        Thread.setDefaultUncaughtExceptionHandler(eh);
        
        t.start();
        
        return t;
    }

	protected static String stackTraceToString(StackTraceElement[] stackTraceElement) {
		
		StringBuffer stacktrace = new StringBuffer();
		
		for (StackTraceElement ste : stackTraceElement) {
			
			stacktrace.append(ste.toString());
			stacktrace.append("\n");
			
		}
		return stacktrace.toString();
	}
}

/**
 * 
 * <p>
 * 	A PrintStream that forwards print statements to the ReportManager as 
 * problems which will make the current test fail.
 * </p>
 * 
 * <p>
 * 	Used to capture printStackTraceEvents that happen during testruns and 
 * would be ignored otherwise.
 * </p>
 * 
 * @author michael
 *
 */
class ReporterPrintStream extends PrintStream {

	protected EnsTestCase e;
	
	public ReporterPrintStream(EnsTestCase e) {
		super(System.out);
		this.e = e;
	}
	
	public void print(String s) {		
		ReportManager.problem(e, (Connection) null, s);
	}

	public void println(String s) {		

		// No newline added, messages to the ReportManager don't need
		// a newline.
		//
		this.print(s);
	}
	public void println() {
		
		// Explicit newlines will be added.
		//
		this.print("\n");
	}
}