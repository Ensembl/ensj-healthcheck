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

	@Option(longName="master.schema")
	String getMasterSchema();
	boolean isMasterSchema();

	@Option(longName="master.variation_schema")
	String getMasterVariation();
	boolean isMasterVariation();

	@Option(longName="master.funcgen_schema")
	String getMasterFuncgen_schema();
	boolean isMasterFuncgen_schema();
}
