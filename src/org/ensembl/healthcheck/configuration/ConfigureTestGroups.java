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
