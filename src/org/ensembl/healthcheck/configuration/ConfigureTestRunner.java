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

        @Option(shortName = "o", longName = "output", description = "Specify the level of output that will be used. "
                        + "The allowed options are \"All\", \"None\", \"Problem\", \"Current\", \"Warning\" and \"Info\", .")
        String getOutputLevel();
        boolean isOutputLevel();
}
