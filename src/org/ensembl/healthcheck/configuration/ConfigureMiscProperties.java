package org.ensembl.healthcheck.configuration;

import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * These are configuration parameters that I found by grepping through the 
 * healthchecks for System.getProperty. They are here so they can be set on
 * on the command line or property files.
 *
 */
public interface ConfigureMiscProperties {

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.generic.Biotypes
	//
	@Option(
		longName    = "biotypes.file",
		description = "Parameter used only in the test org.ensembl.healthcheck.testcase.generic.Biotypes" 
	)
	String getBiotypesFile();
	boolean isBiotypesFile();
	
	// Used in:
	//
	// org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionExonCoords
	// org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionBase
	// org.ensembl.healthcheck.testcase.generic.GeneStatus
	//
	@Option(
		longName    = "ignore.previous.checks",
		description = "Parameter used only in "
			+ "org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionExonCoords, "
			+ "org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionBase "
			+ "and org.ensembl.healthcheck.testcase.generic.GeneStatus"
	)
	String getIgnorePreviousChecks();
	boolean isIgnorePreviousChecks();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.generic.CompareSchema
	// org.ensembl.healthcheck.testcase.variation.CompareVariationSchema
	// org.ensembl.healthcheck.testcase.funcgen.CompareFuncgenSchema
	//
	@Option(
		longName    = "schema.file",
		description = "Parameter used only in "
			+ "org.ensembl.healthcheck.testcase.generic.CompareSchema, "
			+ "org.ensembl.healthcheck.testcase.variation.CompareVariationSchema "
			+ "and org.ensembl.healthcheck.testcase.funcgen.CompareFuncgenSchema"
	)
	String getSchemaFile();
	boolean isSchemaFile();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.generic.CompareSchema
	// org.ensembl.healthcheck.testcase.funcgen.CompareFuncgenSchema
	//
	@Option(
		longName    = "master.schema",
		description = "Parameter used only in "
			+ "org.ensembl.healthcheck.testcase.generic.CompareSchema, "
			+ "and org.ensembl.healthcheck.testcase.funcgen.CompareFuncgenSchema"
	)
	String getMasterSchema();
	boolean isMasterSchema();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.generic.LogicNamesDisplayable
	//
	@Option(
		longName    = "logicnames.file",
		description = "Parameter used only in "
			+ "org.ensembl.healthcheck.testcase.generic.LogicNamesDisplayable"
	)
	String getLogicnamesFile();
	boolean isLogicnamesFile();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.AbstractPerlBasedTestCase
	//
	@Option(
		longName    = "perl",
		description = "Parameter used only in "
			+ "org.ensembl.healthcheck.testcase.AbstractPerlBasedTestCase"
	)
	String getPerl();
	boolean isPerl();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.variation.CompareVariationSchema
	//
	@Option(
		longName    = "master.variation_schema",
		description = "Parameter used only in "
			+ "master.variation_schema"
	)
	String getMasterVariationSchema();
	boolean isMasterVariationSchema();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.EnsTestCase
	//
	@Option(
		longName    = "user.dir",
		description = "Parameter used in "
			+ "org.ensembl.healthcheck.testcase.EnsTestCase"
	)
	String getUserDir();
	boolean isUserDir();
	
	// Used in:
	//
	// org.ensembl.healthcheck.testcase.EnsTestCase
	//
	@Option(
		longName    = "file.separator",
		description = "Parameter used in "
			+ "org.ensembl.healthcheck.testcase.EnsTestCase"
	)
	String getFileSeparator();
	boolean isFileSeparator();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.EnsTestCase
	//
	@Option(
		longName    = "driver",
		description = "Parameter used in "
			+ "org.ensembl.healthcheck.testcase.EnsTestCase"
	)
	String getDriver();
	boolean isDriver();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.EnsTestCase
	//
	@Option(
		longName    = "databaseURL",
		description = "Parameter used in "
			+ "org.ensembl.healthcheck.testcase.EnsTestCase"
	)
	String getDatabaseURL();
	boolean isDatabaseURL();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.EnsTestCase
	//
	@Option(
		longName    = "user",
		description = "Parameter used in "
			+ "org.ensembl.healthcheck.testcase.EnsTestCase"
	)
	String getUser();
	boolean isUser();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.EnsTestCase
	//
	@Option(
		longName    = "password",
		description = "Parameter used in "
			+ "org.ensembl.healthcheck.testcase.EnsTestCase"
	)
	String getPassword();
	boolean isPassword();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.funcgen.CompareFuncgenSchema
	//
	@Option(
		longName    = "master.funcgen_schema",
		description = "Parameter used in "
			+ "org.ensembl.healthcheck.testcase.funcgen.CompareFuncgenSchema"
	)
	String getMasterFuncgenSchema();
	boolean isMasterFuncgenSchema();
}

