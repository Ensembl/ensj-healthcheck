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
	@Option(longName="biotypes.file")
	String getBiotypesFile();
	boolean isBiotypesFile();
	
	// Used in:
	//
	// org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionExonCoords
	// org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionBase
	// org.ensembl.healthcheck.testcase.generic.GeneStatus
	//
	@Option(longName="ignore.previous.checks")
	String getIgnorePreviousChecks();
	boolean isIgnorePreviousChecks();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.generic.CompareSchema
	// org.ensembl.healthcheck.testcase.variation.CompareVariationSchema
	// org.ensembl.healthcheck.testcase.funcgen.CompareFuncgenSchema
	//
	@Option(longName="schema.file")
	String getSchemaFile();
	boolean isSchemaFile();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.generic.CompareSchema
	// org.ensembl.healthcheck.testcase.funcgen.CompareFuncgenSchema
	//
	@Option(longName="master.schema")
	String getMasterSchema();
	boolean isMasterSchema();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.generic.LogicNamesDisplayable
	//
	@Option(longName="logicnames.file")
	String getLogicnamesFile();
	boolean isLogicnamesFile();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.AbstractPerlBasedTestCase
	//
	@Option(longName="perl")
	String getPerl();
	boolean isPerl();

	//config = new PerlScriptConfig(System.getProperty(PERL), System

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.variation.CompareVariationSchema
	//
	@Option(longName="master.variation_schema")
	String getMasterVariationSchema();
	boolean isMasterVariationSchema();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.EnsTestCase
	//
	@Option(longName="user.dir")
	String getUserDir();
	boolean isUserDir();
	
	// Used in:
	//
	// org.ensembl.healthcheck.testcase.EnsTestCase
	//
	@Option(longName="file.separator")
	String getFileSeparator();
	boolean isFileSeparator();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.EnsTestCase
	//
	@Option(longName="driver")
	String getDriver();
	boolean isDriver();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.EnsTestCase
	//
	@Option(longName="databaseURL")
	String getDatabaseURL();
	boolean isDatabaseURL();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.EnsTestCase
	//
	@Option(longName="user")
	String getUser();
	boolean isUser();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.EnsTestCase
	//
	@Option(longName="password")
	String getPassword();
	boolean isPassword();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.funcgen.CompareFuncgenSchema
	//
	@Option(longName="master.funcgen_schema")
	String getMasterFuncgenSchema();
	boolean isMasterFuncgenSchema();
}

