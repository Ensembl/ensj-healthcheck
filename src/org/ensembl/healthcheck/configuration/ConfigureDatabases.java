/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
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

	@Option(longName = "dbtype", description = "If set, this will be used as the type for all databases.")
	String getDbType();
	boolean isDbType();
	
}
