/**
 * File: AbstractPerlModuleBasedTestCase.java
 * Created by: dstaines
 * Created on: Mar 23, 2010
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase;

import java.io.IOException;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseServer;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.TemplateBuilder;

import java.sql.Connection;

/**
 * @author dstaines
 *
 */
public abstract class AbstractPerlModuleBasedTestCase extends AbstractPerlBasedTestCase {

	private static final String SCRIPT = "./perl/run_healthcheck.pl -host $host$ -port $port$ -user $user$ -pass $pass$ -dbname $dbname$ -species_id $species_id$ -module $module$";
	
	public AbstractPerlModuleBasedTestCase() {
		super();
	}
	
	/**
	 * 
	 * Returns the name of the perl module with the healthcheck test.
	 * 
	 * @return
	 * 
	 */
	protected abstract String getModule();

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.AbstractPerlBasedTestCase#getPerlScript()
	 */
	@Override
	protected String getPerlScript(
			DatabaseRegistryEntry dbre, 
			int speciesId
	) {
		DatabaseServer srv = dbre.getDatabaseServer();
		return TemplateBuilder.template(SCRIPT, "host", srv.getHost(),
				"port", srv.getPort(),
				"user", srv.getUser(),
				"pass", srv.getPass(),
				"dbname", dbre.getName(),
				"module", getModule(),
				"species_id", speciesId);
	}


	protected Appendable createStdoutProcessor(final EnsTestCase e, final Connection c) {

		return new OutputGobbler(
				new ReportManagerCaller() {

					@Override public void report(String message) {
						ReportManager.correct(e, c, message.trim());
					}
				}
		);
	}

	protected Appendable createStderrProcessor(final EnsTestCase e, final Connection c) {
		
		return new OutputGobbler(
				new ReportManagerCaller() {

					@Override public void report(String message) {
						
						// Only count messages as a problem, if they are 
						// prefixed with PROBLEM. The prefix is created by 
						// Bio::EnsEMBL::Healthcheck.
						//
						// Anything else on STDERR might not be indicating
						// a problem with the database. It might just be
						// the API complaining that the wrong version has
						// been used.
						//
						if (message.startsWith("PROBLEM")) {
							ReportManager.problem(e, c, message.trim());
						} else {							
							ReportManager.correct(e, c, message.trim());								
						}
					}
				}
		);
	}
}

class OutputGobbler implements Appendable {
	
	private final ReportManagerCaller reportManagerCall;
	
	public OutputGobbler(ReportManagerCaller reportManagerCall) {
		
		this.reportManagerCall = reportManagerCall;
	}
	
	@Override
	public Appendable append(final CharSequence csq) throws IOException {
		
		reportManagerCall.report(csq.toString());
		return this;
	}

	@Override
	public Appendable append(final char c) throws IOException {

		reportManagerCall.report(Character.toString(c));
		return this;
	}

	@Override
	public Appendable append(CharSequence csq, int start, int end)
			throws IOException {
		
		throw new NoSuchMethodError("This method should not be needed at the moment.");
	}	
}
