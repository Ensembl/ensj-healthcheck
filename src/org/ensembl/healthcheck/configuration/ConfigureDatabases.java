package org.ensembl.healthcheck.configuration;

import java.util.List;

import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * 
 * Interface for the parameters with which the database is specified on which
 * the tests will be run.
 * 
 * @author michael
 * 
 */
public interface ConfigureDatabases {

	// The databases on which healthchecks will be run
	//
	// Update 11/10/2010: Changed from output.databases to test.databases in
	// order to prevent confusion as requested by Dan
	//
	@Option(shortName = "d", longName = "test_databases", description = "Name of databases that should be tested (e.g.: "
			+ "ensembl_compara_bacteria_5_58). If there is more than one "
			+ "database, separate with spaces. Any configured tests will "
			+ "be run on these databases. Does not support same format as output.databases!")
	List<String> getTestDatabases();

	boolean isTestDatabases();

	@Option(shortName = "D", longName = "test_divisions", description = "Names of division to which databases to test should belong e.g. EPl or EnsemblPlants. " +
			"This option requires the production database to be set up.")
	List<String> getDivisions();
	boolean isDivisions();

        @Option(longName = "species", description = "If set, this will be used as the species for all databases, overriding anything the" +
                        "name or meta table of the database may indicate.")
        String getSpecies();
        boolean isSpecies();

        @Option(shortName = "t", longName = "dbtype", description = "If set, this will be used as the type for all databases.")
        String getDbType();
        boolean isDbType();
	
}
