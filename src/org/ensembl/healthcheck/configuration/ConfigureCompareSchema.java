package org.ensembl.healthcheck.configuration;

import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * 
 * Interface for the parameters used by tests comparing schemas.
 * 
 * @author michael
 * 
 */
public interface ConfigureCompareSchema {

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.generic.CompareSchema
	// org.ensembl.healthcheck.testcase.funcgen.CompareFuncgenSchema
	//
	@Option(longName = "master.schema", description = "Parameter used only in "
			+ "org.ensembl.healthcheck.testcase.generic.CompareSchema, "
			+ "and org.ensembl.healthcheck.testcase.funcgen.CompareFuncgenSchema")
	String getMasterSchema();

	boolean isMasterSchema();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.variation.CompareVariationSchema
	//
	@Option(longName = "master.variation_schema", description = "Parameter used only in "
			+ "master.variation_schema")
	String getMasterVariationSchema();

	boolean isMasterVariationSchema();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.funcgen.CompareFuncgenSchema
	//
	@Option(longName = "master.funcgen_schema", description = "Parameter used in "
			+ "org.ensembl.healthcheck.testcase.funcgen.CompareFuncgenSchema")
	String getMasterFuncgenSchema();

	boolean isMasterFuncgenSchema();

}
