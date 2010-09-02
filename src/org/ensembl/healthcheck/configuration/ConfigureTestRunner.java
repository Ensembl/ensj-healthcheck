package org.ensembl.healthcheck.configuration;

import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * 
 * Interface for providing options for specifying which test registry and
 * which reporter type should be used.
 * 
 * @author michael
 *
 */
public interface ConfigureTestRunner {

	@Option
	String getTestRegistryType();
	boolean isTestRegistryType();
	
	@Option
	String getReporterType();
	boolean isReporterType();
}
