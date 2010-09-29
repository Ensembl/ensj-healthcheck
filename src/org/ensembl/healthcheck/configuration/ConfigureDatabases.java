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
	@Option(
		longName    = "output.databases",
		description = "Name of database that should be tested (e.g.: "
			+ "ensembl_compara_bacteria_5_58). Any configured tests will "
			+ "be run on this database. Not to be confused with "
			+ "--output.database."
	)
	List<String> getOutputDatabases();
	boolean isOutputDatabases();
	
}
