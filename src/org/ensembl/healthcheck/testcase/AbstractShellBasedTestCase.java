/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2020] EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ensembl.healthcheck.testcase;

import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	 * Specify here, if the test implementing this class uses speciesIds. If 
	 * true, the method
	 * </p>
	 * 
	 * createCommandLine(dbre, speciesId);
	 * 
	 * <p>
	 * will be called for creating commands. If the test implementing this 
	 * class doesn't use speciesId, set to false and
	 * </p>
	 * 
	 * createCommandLine(dbre);
	 * 
	 * <p>
	 * will be called to get the command for executing the shell based test.
	 * </p>
	 */
	boolean isSpeciesIdAware = false;

	public boolean isSpeciesIdAware() {
		return isSpeciesIdAware;
	}

	public void setSpeciesIdAware(boolean iteratesOverSpeciesIds) {
		this.isSpeciesIdAware = iteratesOverSpeciesIds;
	}

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
	 * 	Use this, if your test uses species ids.
	 * 
	 * @param dbre
	 * @param speciesId
	 * @return command line
	 */
	protected String createCommandLine(final DatabaseRegistryEntry dbre, int speciesId) { return null; }
	
	/**
	 * 	Use this, if your test is independent of species ids.
	 * 
	 * @param dbre
	 * @return command line
	 */
	protected String createCommandLine(final DatabaseRegistryEntry dbre) { return null; }
	
	/**
	 * 	Preservers the system environment by default. If your test requires
	 * certain environment variables to be set, override this method.
	 * 
	 * @return hash of environment variables
	 */
	protected Map<String,String> environmentVarsToSet() {
		
		Map<String,String> environmentVars = new HashMap<String,String>(System.getenv());
		
		return environmentVars;
	}

	public boolean runShellTest(final DatabaseRegistryEntry dbre, int speciesId, boolean useSpeciesId) {
		
		boolean passes = true;
		
		final EnsTestCase currentTestCase = this;
		
		Appendable out = createStdoutProcessor(currentTestCase, dbre.getConnection());
		Appendable err = createStderrProcessor(currentTestCase, dbre.getConnection());
		
		String lastCmdThatWasRun = "";
		
		try {
			
			String shellCmd;
			
			if (useSpeciesId) {
				shellCmd = createCommandLine(dbre, speciesId);
			} else {
				shellCmd = createCommandLine(dbre);
			}

			logger.info(
				"Running: "
				+ shellCmd
			);
				

			int exit = 1;
			
			if (!StringUtils.isEmpty(shellCmd)) {
				
				//
				// Running the command by creating an array avoids the 
				// problem of java breaking the command down at spaces to
				// divide it into command and arguments.
				//
				// The command is passed to the bash so things like pipes and
				// backticks are interpreted. 
				//
				String[] cmdLineItems = new String[] {
						"/bin/bash",
						"-c",
						shellCmd,
				};
				
				exit = ProcessExec.exec(
					cmdLineItems, 
					out, 
					err, 
					false, 
					environmentVarsToSet()
				);
				
				lastCmdThatWasRun = shellCmd;
				
			} else {
				throw new RuntimeException("Shell based test "+this.getName()+" has not returned a valid command line");
			}
			
			if (exit == 0) {
				ReportManager.correct(
						this, 
						dbre.getConnection(), 
						"Command \n"
						+ lastCmdThatWasRun 
						+ "\ncompleted successfully"
				);
			} else {
				ReportManager.problem(
						this, 
						dbre.getConnection(), 
						"Command \n"
						+ lastCmdThatWasRun 
						+ "\ndid not complete successfully"
				);
				passes = false;
			}
		} catch (IOException e) {
			ReportManager.problem(
				this, 
				dbre.getConnection(),
				"Could not execute " 
				+ lastCmdThatWasRun 
				+ "\nGot the following error: "
				+ e.getMessage()
			);
			passes = false;
		} 
		return passes;
	}
	
	@Override
	public boolean run(final DatabaseRegistryEntry dbre) {
		
		boolean passes = true;
		
		if (isSpeciesIdAware) {
			
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
				
				// Once a test has failed, should not do anymore tests on  
				// other species ids.
				//
				passes = passes && runShellTest(dbre, speciesId, true);
			}

		} else {
			passes = runShellTest(dbre, 0, false);
		}
		return passes;
	}
};
