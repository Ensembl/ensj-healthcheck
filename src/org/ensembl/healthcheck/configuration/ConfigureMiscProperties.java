/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2018] EMBL-European Bioinformatics Institute
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

import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * These are configuration parameters that I found by grepping through the
 * healthchecks for System.getProperty. They are here so they can be set on on
 * the command line or property files.
 * 
 */
public interface ConfigureMiscProperties {

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionExonCoords
	// org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionBase
	// org.ensembl.healthcheck.testcase.generic.GeneStatus
	//
	@Option(longName = "ignore.previous.checks", description = "Parameter used only in "
			+ "org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionExonCoords, "
			+ "org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionBase "
			+ "and org.ensembl.healthcheck.testcase.generic.GeneStatus")
	String getIgnorePreviousChecks();

	boolean isIgnorePreviousChecks();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase. todo
	//
	@Option(longName = "datafile_base_path", description = "Location of the "
			+ "external files from the current release"
	)
	String getDataFileBasePath();
	boolean isDataFileBasePath();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.generic.CompareSchema
	//
	@Option(longName = "schema.file", description = "Parameter used only in "
			+ "org.ensembl.healthcheck.testcase.generic.CompareSchema, "
	)
	String getSchemaFile();
	boolean isSchemaFile();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.funcgen.CompareFuncgenSchema
	//
	@Option(longName = "funcgen_schema.file", description = "Parameter used only in "
		+ "and org.ensembl.healthcheck.testcase.funcgen.CompareFuncgenSchema"
	)
	String getFuncgenSchemaFile();
	boolean isFuncgenSchemaFile();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.variation.CompareVariationSchema
	//
	@Option(longName = "variation_schema.file", description = "Parameter used only in "
		+ "org.ensembl.healthcheck.testcase.variation.CompareVariationSchema "
	)
	String getVariationSchemaFile();
	boolean isVariationSchemaFile();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.AbstractPerlBasedTestCase
	//
	@Option(longName = "perl", description = "Parameter used only in "
			+ "org.ensembl.healthcheck.testcase.AbstractPerlBasedTestCase")
	String getPerl();

	boolean isPerl();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.EnsTestCase
	//
	@Option(longName = "user.dir", description = "Parameter used in "
			+ "org.ensembl.healthcheck.testcase.EnsTestCase")
	String getUserDir();

	boolean isUserDir();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.EnsTestCase
	//
	@Option(longName = "file.separator", description = "Parameter used in "
			+ "org.ensembl.healthcheck.testcase.EnsTestCase")
	String getFileSeparator();

	boolean isFileSeparator();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.EnsTestCase
	//
	@Option(longName = "driver", description = "Parameter used in "
			+ "org.ensembl.healthcheck.testcase.EnsTestCase")
	String getDriver();

	boolean isDriver();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.EnsTestCase
	//
	@Option(longName = "databaseURL", description = "Parameter used in "
			+ "org.ensembl.healthcheck.testcase.EnsTestCase")
	String getDatabaseURL();

	boolean isDatabaseURL();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.EnsTestCase
	//
	@Option(longName = "user", description = "Parameter used in "
			+ "org.ensembl.healthcheck.testcase.EnsTestCase")
	String getUser();

	boolean isUser();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.EnsTestCase
	//
	@Option(longName = "password", description = "Parameter used in "
			+ "org.ensembl.healthcheck.testcase.EnsTestCase")
	String getPassword();

	boolean isPassword();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.EnsTestCase
	//
	@Option(longName = "repair", description = "Allow the tests to try to repair the database (if they can)")
	String getRepair();

	boolean isRepair();

}
