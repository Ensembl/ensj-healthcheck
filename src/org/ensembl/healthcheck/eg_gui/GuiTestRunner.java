package org.ensembl.healthcheck.eg_gui;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

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
import java.util.logging.LogRecord;
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
	protected static Logger createGuiLogger(final EnsTestCase e) {
		
		Logger logger = Logger.getAnonymousLogger();
		
		for (Handler currentHandler : logger.getHandlers()) {
			logger.removeHandler(currentHandler);
		}

		// Otherwise messages will be sent to the screen.
		//
		logger.setUseParentHandlers(false);
		logger.addHandler(createLogHandlerToReportManager(e));		
		return logger;
	}

	/**
	 * <p>
	 * 	Creates a Handler for a logger that will forward everything to the
	 * ReportManager.
	 * </p>
	 * 
	 * @param e
	 * @return Handler
	 * 
	 */
	protected static Handler createLogHandlerToReportManager(final EnsTestCase e) {
		
		Handler guiLoggingHandler = new Handler() {
    		public void publish(LogRecord logRecord) {
  	    	  
    			ReportManager.correct(
    				e, 
    				(Connection) null, 
    				logRecord.getLevel()
    				+ ": " 
    				+ logRecord.getSourceClassName() 
    				+ ":\n" 
    				+ logRecord.getSourceMethodName() 
    				+ ": " 
    				+ logRecord.getMessage() + "\n" 
    			);
    		}

			@Override public void close() throws SecurityException {}
			@Override public void flush() {}
    	};
    	return guiLoggingHandler;
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
    		final PerlScriptConfig psc
    ) {

        // Tests are run in a separate thread
        //
        Thread t = new Thread() {

        	public void run() {

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
					Logger guiLogger   = createGuiLogger(testCase);

					testCase.setLogger(guiLogger);
					
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

                    if (testCase instanceof SingleDatabaseTestCase) {

                        for (DatabaseRegistryEntry currentDbre : databases) {
                        	
                            String message = testCase.getShortTestName() + ": " + currentDbre.getName();
                            
                            testProgressDialog.setNote(message);
                            
                            ((SingleDatabaseTestCase) testCase).run(currentDbre);
                            
                            // If a test has not reported anything to the 
                            // report manager, there will not be any report. 
                            // The user may think that the test was not run.
                            // So in this case a standard line is generated
                            // to show that the test has probably completed
                            // and all is well.
                            //
                            boolean testHasReportedSomething = ReportManager.getAllReportsByTestCase().containsKey(testCase.getTestName()); 
                            
                            if (!testHasReportedSomething) {
                            	ReportManager.report(
                            			testCase, 
                            			currentDbre.getConnection(), 
                            			ReportLine.INFO,
                            			testCase.getShortTestName() + " did not produce any output. This usually means that there were no problems."
                            	);
                            }
                            
                            testsRun += 1;
                            
                            testProgressDialog.setProgress(testsRun);
                            testProgressDialog.repaint();
                        }

                    } else if (testCase instanceof MultiDatabaseTestCase) {

                        DatabaseRegistry dbr = new DatabaseRegistry(databases);
                        
                        String message = testCase.getShortTestName() + " ( " + dbr.getEntryCount() + " databases)";
                        
                        testProgressDialog.setNote(message);
                        
                        ((MultiDatabaseTestCase) testCase).run(dbr);

                        // If a test has not reported anything to the 
                        // report manager, there will not be any report. 
                        // The user may think that the test was not run.
                        // So in this case a standard line is generated
                        // to show that the test has probably completed
                        // and all is well.
                        //
                        boolean testHasReportedSomething = ReportManager.getAllReportsByTestCase().containsKey(testCase.getTestName()); 
                        
                        if (!testHasReportedSomething) {
                        	ReportManager.report(
                        			testCase, 
                        			dbr.getAll()[0].getConnection(), 
                        			ReportLine.INFO,
                        			testCase.getShortTestName() + " did not produce any output. This usually means that there were no problems."
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
                    
                    boolean currentTestReportedNoProblems 
                    	= ReportManager.getReportsByTestCase(
                    		testCase.getTestName(), 
                    		ReportLine.PROBLEM
                    	).size()==0; 
                    
                    ReportManager.finishTestCase(
                    	testCase, 
                    	currentTestReportedNoProblems, 
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
        t.start();
        return t;
    }
}
