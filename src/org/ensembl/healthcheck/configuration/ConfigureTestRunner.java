package org.ensembl.healthcheck.configuration;

import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * 
 * Interface for providing options for specifying which test registry and which
 * reporter type should be used.
 * 
 * @author michael
 * 
 */
public interface ConfigureTestRunner {

	@Option(shortName = "r", description = "Specify the type of test registry that will be used. "
			+ "The allowed options are \"Discoverybased\" and "
			+ "\"ConfigurationBased\"")
	String getTestRegistryType();

	boolean isTestRegistryType();

	@Option(shortName = "R", description = "Specify the reporter type that will be used. "
			+ "The allowed options are \"Database\" and \"Text\".")
	String getReporterType();

	boolean isReporterType();
}
