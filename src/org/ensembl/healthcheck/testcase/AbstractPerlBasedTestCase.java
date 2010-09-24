/**
 * File: AbstractPerlBasedTestCase.java
 * Created by: dstaines
 * Created on: Nov 13, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.configurationmanager.ConfigurationByCascading;
import org.ensembl.healthcheck.util.ProcessExec;

/**
 * Base class for invoking a perl script to carry out the test and parse the
 * output.
 * 
 * @author dstaines
 * 
 */
public abstract class AbstractPerlBasedTestCase extends SingleDatabaseTestCase {

	static final Logger log = Logger.getLogger(AbstractPerlBasedTestCase.class.getCanonicalName());

	public static final String PERLOPTS = "perlopts";
	public static final String PERL = "perl";

	/**
	 * 
	 * Internal class that holds perl configurations
	 *
	 */
	public class PerlScriptConfig {

		private final String perlBinary;
		private final String perlOptions;

		public PerlScriptConfig(String perlBinary, String perlOptions) {
			this.perlBinary = perlBinary;
			this.perlOptions = perlOptions;
		}

		public String getPerlBinary() {
			return perlBinary;
		}

		public String getPerlOptions() {
			return perlOptions;
		}
	};

	protected PerlScriptConfig config;

	protected PerlScriptConfig getConfig() {
		if(config==null) {
			config = new PerlScriptConfig(
					System.getProperty(PERL), 
					System.getProperty(PERLOPTS)
			);
		}
		return config;
	}

	protected void setConfig(PerlScriptConfig config) {
		this.config = config;
	}

	public AbstractPerlBasedTestCase() {
	}

	/**
	 * @return String perl script and relevant arguments to invoke with perl
	 *         binary and options from
	 *         {@link AbstractPerlBasedTestCase#getConfig()}
	 */
	protected abstract String getPerlScript(DatabaseRegistryEntry dbre, int speciesId);

	/**
	 * Process the output and error from the script specified in
	 * {@link AbstractPerlBasedTestCase#getPerlScript()} and invoke the
	 * {@link ReportManager} accordingly
	 * 
	 * @param output
	 *            string containing standard output from script
	 * @param error
	 *            string containing error from script
	 */
	protected abstract void processOutput(String output, String error);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.healthcheck.testcase.SingleDatabaseTestCase#run(org.ensembl
	 * .healthcheck.DatabaseRegistryEntry)
	 */
	@Override
	public boolean run(DatabaseRegistryEntry dbre) {
		boolean passes = true;
		
		List<Integer> dbre_speciesIds = dbre.getSpeciesIds();
		
		// Make sure species ids were configured. If not, the perl test will 
		// not be run. The warning message may be overlooked by a user, 
		// therefore the test is set to fail in order to get attention.
		//
		if (dbre_speciesIds.size() == 0) {
			
			log.warning(
				"No species ids! Perhaps no databases were configured?"
				+ " This test will not be run."
			);
			passes = false;
		}
		
		for (int speciesId : dbre_speciesIds) {
			String commandLine = getPerlScript(dbre, speciesId);
			StringBuffer out = new StringBuffer();
			StringBuffer err = new StringBuffer();
			try {
				int exit = ProcessExec.exec(commandLine, out, err);
				processOutput(out.toString(), err.toString());
				if (exit == 0) {
					ReportManager.correct(this, dbre.getConnection(), "Script "
							+ commandLine + " completed successfully");
				} else {
					ReportManager.problem(this, dbre.getConnection(), "Script "
							+ commandLine + " did not complete successfully");
					passes = false;
				}
			} catch (IOException e) {
				ReportManager.problem(this, dbre.getConnection(),
						"Could not execute " + commandLine + ": "
								+ e.getMessage());
				passes = false;
			}
		}
		return passes;
	}
}
