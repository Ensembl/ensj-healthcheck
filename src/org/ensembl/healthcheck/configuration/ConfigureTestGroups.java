package org.ensembl.healthcheck.configuration;

import java.util.List;
import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * 
 * Interface for providing options for specifying groups and tests that should
 * or should not run.
 * 
 * @author michael
 *
 */
public interface ConfigureTestGroups {

	// Specify one or many groups of tests that will be run.
	@Option(
		shortName   = "g",
		description = "Specify which groups of tests should be run. Fully "
			+ "qualified class names can be used as well as their short "
			+ "names."
	) 
	List<String> getGroup();
	boolean isGroup();

	// A list of names of groups of tests that should not be run
	// although they may be referenced in a test group
	@Option(
		shortName   = "l",
		description = "Specify which groups of tests should not be run. Fully "
			+ "qualified class names can be used as well as their short "
			+ "names."
	) 
	List<String> getLess();
	boolean isLess();

	// A list of individual tests that should be run
	@Option(
		shortName="t",
		description = "Specify which tests should be run. Fully "
			+ "qualified class names can be used as well as their short "
			+ "names."
	) 
	List<String> getTest();
	boolean isTest();

	// A list of individual tests that should not be run
	@Option(
		shortName="n",
		description = "Specify which tests should not be run. Fully "
			+ "qualified class names can be used as well as their short "
			+ "names."
	) 
	List<String> getNotest();
	boolean isNotest();
}
