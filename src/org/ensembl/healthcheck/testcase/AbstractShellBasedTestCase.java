package org.ensembl.healthcheck.testcase;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.ActionAppendable;
import org.ensembl.healthcheck.util.ProcessExec;

/**
 * <p>
 * 	Abstract class providing methods for running tests that are started from
 * the shell in a separate process.
 * </p>
 * 
 * @author mnuhn
 *
 */
public abstract class AbstractShellBasedTestCase extends SingleDatabaseTestCase {

	/**
	 * <p>
	 * 	Creates a default processor for output. Sends everything to 
	 * the method dispatchMessage.
	 * </p>
	 * 
	 */
	protected Appendable createOutputProcessor(final EnsTestCase e, final Connection c) {
		return 
			new ActionAppendable() {
				@Override public void process(String message) {
					dispatchMessage(e, c, message.trim());
				}
				
			};
	}
	
	/**
	 * <p>
	 * 	Forward any message received from the healthcheck to the ReportManager.
	 * Meant to be overridden with behaviour specific to the test.
	 * </p>
	 * 
	 * @param message
	 */
	protected void dispatchMessage(final EnsTestCase e, final Connection c, String message) {
		ReportManager.correct(e, c, message);
	}

	/**
	 * <p>
	 * 	Creates an Appendable object to which stdout is delegated. Overwrite 
	 * this method to determine what is to be done with output sent to stdout 
	 * from the subprocess.
	 * </p>
	 * 
	 */
	protected Appendable createStdoutProcessor(final EnsTestCase e, final Connection c) {
		return createOutputProcessor(e, c);
	}

	/**
	 * <p>
	 * 	Creates an Appendable object to which stderr is delegated. Overwrite 
	 * this method to determine what is to be done with output sent to stderr 
	 * from the subprocess.
	 * </p>
	 * 
	 */
	protected Appendable createStderrProcessor(final EnsTestCase e, final Connection c) {
		return createOutputProcessor(e, c);
	}
	
	/**
	 * <p>
	 * 	This should return the command line that starts your test. If you have 
	 * spaces in a path or an argument that has spaces, but should not be split
	 * into several arguments, don't use this method, but use 
	 * createCommandLineArray instead.
	 * </p>
	 * 
	 * @param dbre
	 * @param speciesId
	 * @return
	 */
	protected String createCommandLine(
			final DatabaseRegistryEntry dbre,
			int speciesId
	) {
		return "echo No shell command has been specified.";
	}
	
	/**
	 * <p>
	 * 	Java has issues when there are spaces in a command line. It breaks the
	 * command into the command and its arguments by splitting on spaces. This
	 * can be very bad, if you have spaces in a path or arguments that are 
	 * quoted. In this case, you have to create the array of commands and 
	 * arguments yourself to prevent java from doing this.
	 * </p>
	 * 
	 * @param dbre
	 * @param speciesId
	 * @return
	 */
	protected String[] createCommandLineArray(
			final DatabaseRegistryEntry dbre,
			int speciesId
	) {
		
		return new String[]{ createCommandLine(dbre, speciesId) };
	}
	
	/**
	 * <p>
	 * 	Does not set any environment variable by default. If your test requires
	 * certain environment variables to be set, override this method.
	 * </p>
	 * 
	 * @return
	 */
	protected String[] environmentVarsToSet() {
		return new String[]{};
	}

	@Override
	public boolean run(final DatabaseRegistryEntry dbre) {
		
		boolean passes = true;
		
		List<Integer> dbre_speciesIds = dbre.getSpeciesIds();
		
		// Make sure species ids were configured. If not, the perl test will 
		// not be run. The warning message may be overlooked by a user, 
		// therefore the test is set to fail in order to get attention.
		//
		if (dbre_speciesIds.size() == 0) {
			
			logger.warning(
				"No species ids! Perhaps no databases were configured?"
				+ " This test will not be run."
			);
			passes = false;
		}
		
		for (int speciesId : dbre_speciesIds) {
			
			String[] commandLineArray = createCommandLineArray(dbre, speciesId);
			
			final EnsTestCase currentTestCase = this;
			
			Appendable out = createStdoutProcessor(currentTestCase, dbre.getConnection());
			Appendable err = createStderrProcessor(currentTestCase, dbre.getConnection());
			
			try {

				int exit = ProcessExec.exec(
					commandLineArray, 
					out, 
					err, 
					false, 
					environmentVarsToSet()
				);
				
				if (exit == 0) {
					ReportManager.correct(
							this, 
							dbre.getConnection(), 
							"Command \n"
							+ StringUtils.join(commandLineArray, " ") 
							+ "\ncompleted successfully"
					);
				} else {
					ReportManager.problem(
							this, 
							dbre.getConnection(), 
							"Command \n"
							+ StringUtils.join(commandLineArray, " ") 
							+ "\ndid not complete successfully"
					);
					passes = false;
				}
			} catch (IOException e) {
				ReportManager.problem(
					this, 
					dbre.getConnection(),
					"Could not execute " 
					+ StringUtils.join(commandLineArray, " ") 
					+ "\nGot the following error: "
					+ e.getMessage()
				);
				passes = false;
			} 
		}
		return passes;
	}
}
