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
		longName   = "include_groups",
		description = "Specify which groups of tests should be run. Fully "
			+ "qualified class names can be used as well as their short "
			+ "names."
	) 
	List<String> getGroups();
	boolean isGroups();

	// A list of names of groups of tests that should not be run
	// although they may be referenced in a test group
	@Option(
			shortName   = "G",
			longName   = "exclude_groups",
		description = "Specify which groups of tests should not be run. Fully "
			+ "qualified class names can be used as well as their short "
			+ "names."
	) 
	List<String> getExcludeGroups();
	boolean isExcludeGroups();

	// A list of individual tests that should be run
	@Option(
		shortName="t",
		longName = "include_tests",
		description = "Specify which tests should be run. Fully "
			+ "qualified class names can be used as well as their short "
			+ "names."
	) 
	List<String> getTests();
	boolean isTests();

	// A list of individual tests that should not be run
	@Option(
		shortName="T",
		longName = "exclude_tests",
		description = "Specify which tests should not be run. Fully "
			+ "qualified class names can be used as well as their short "
			+ "names."
	) 
	List<String> getExcludeTests();
	boolean isExcludeTests();
}
