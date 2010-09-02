package org.ensembl.healthcheck.configuration;

import java.util.List;

import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * 
 * Interface for the parameters with which the database is specified on
 * which the tests will be run.
 * 
 * @author michael
 *
 */
public interface ConfigureDatabases {

	// The databases on which healthchecks will be run
	@Option(longName="output.databases")
	List<String> getOutputDatabases();
	boolean isOutputDatabases();
	
}
