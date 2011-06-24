/**
 * File: AbstractPerlBasedTestCase.java
 * Created by: dstaines
 * Created on: Nov 13, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase;

import java.io.IOException;
import java.util.List;


import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.ProcessExec;

import java.sql.Connection;;

/**
 * Base class for invoking a perl script to carry out the test and parse the
 * output.
 * 
 * @author dstaines
 * 
 */
public abstract class AbstractPerlBasedTestCase extends SingleDatabaseTestCase {

	public static final String PERLOPTS = "perlopts";
	public static final String PERL     = "perl";

	protected String PERL5LIB = null;
	
	public String getPERL5LIB() {
		return PERL5LIB;
	}

	public void setPERL5LIB(String pERL5LIB) {
		PERL5LIB = pERL5LIB;
	}

	protected PerlScriptConfig config;

	public PerlScriptConfig getConfig() {
		if(config==null) {
			config = new PerlScriptConfig(
					System.getProperty(PERL), 
					System.getProperty(PERLOPTS)
			);
		}
		return config;
	}

	public void setConfig(PerlScriptConfig config) {
		this.config = config;
	}

	public AbstractPerlBasedTestCase() {}
	
	/**
	 * @return String perl script and relevant arguments to invoke with perl
	 *         binary and options from
	 *         {@link AbstractPerlBasedTestCase#getConfig()}
	 */
	protected abstract String getPerlScript(DatabaseRegistryEntry dbre, int speciesId);

	/**
	 * Creates an Appendable object to which Stdout is delegated
	 * 
	 */
	protected abstract Appendable createStdoutProcessor(EnsTestCase e, Connection c);

	/**
	 * Creates an Appendable object to which Stderr is delegated
	 * 
	 */
	protected abstract Appendable createStderrProcessor(EnsTestCase e, Connection c);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.healthcheck.testcase.SingleDatabaseTestCase#run(org.ensembl
	 * .healthcheck.DatabaseRegistryEntry)
	 */
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
			
			String commandLine = getPerlScript(dbre, speciesId);
			
			if (config!=null) {
				
				if (!config.getPerlBinary().isEmpty()) {
					
					if (config.getPerlOptions().isEmpty()) {
						
						commandLine = config.getPerlBinary() + " " + commandLine;
						
					} else {
						
						commandLine = config.getPerlBinary() +  " " + config.getPerlOptions() + " " + commandLine;
					}
				}
			}
			
			final EnsTestCase currentTestCase = this;
			
			Appendable out = createStdoutProcessor(currentTestCase, dbre.getConnection());
			Appendable err = createStderrProcessor(currentTestCase, dbre.getConnection());
			
			try {
				
				int exit;
				
				if (getPERL5LIB() == null) {
					exit = ProcessExec.exec(commandLine, out, err);
				} else {
					exit = ProcessExec.exec(
						commandLine, 
						out, 
						err, 
						new String[] { 
							"PERL5LIB=" + getPERL5LIB() 
						}
					);
				}
				
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

