/**
 * File: AbstractPerlModuleBasedTestCase.java
 * Created by: dstaines
 * Created on: Mar 23, 2010
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseServer;
import org.ensembl.healthcheck.util.TemplateBuilder;

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

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.AbstractPerlBasedTestCase#processOutput(java.lang.String, java.lang.String)
	 */
	@Override
	protected void processOutput(String output, String error) {
		System.err.println(error);
	}

}
