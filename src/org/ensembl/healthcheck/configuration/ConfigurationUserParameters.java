/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2021] EMBL-European Bioinformatics Institute
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
 * <p>
 * Defines command line interface options for configuring the tests to be
 * run. This interface style of configuring is from the jewel project:
 * </p>
 * 
 * <p>
 * http://jewelcli.sourceforge.net/examples.html
 * </p>
 * 
 * <p>
 * This interface extends several other interface classes. The advantage of
 * this is that later the ConfigurationUserParameters can be cast into one
 * of these interfaces in order to limit the information a part of the program
 * has access to and thereby decoupling code from configuration information.
 * </p>
 * 
 * <p>
 * ATTENTION: Don't set any defaults for command line options! Doing so will
 * make jewel provide the default value, if none is given. The cascading  
 * configuration object will then think that the user provided this value and 
 * not look any further in other configuration files. Default values should be
 * set in a properties file which is always read and put in last in the 
 * hierarchy.
 * </p> 
 *
 */
public interface ConfigurationUserParameters 
	extends 
		ConfigureHost, 
		ConfigureDatabases, 
		ConfigureTestGroups,
		ConfigureConfiguration,
		ConfigureTestRunner,
		ConfigureHealthcheckDatabase,
		ConfigureCompareSchema,
		ConfigureMiscProperties {
	
	@Option(helpRequest = true, description = "display help", shortName = "h")
	boolean getHelp();
	
}
