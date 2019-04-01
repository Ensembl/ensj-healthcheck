/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
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
 * Interface for specifying the parameters for the primary and secondary
 * database connection details.
 * 
 * @author michael
 * 
 */
public interface ConfigureHost {

	@Option(shortName = "h", longName = "host", description = "The host for the database server you wish to connect to.")
	String getHost();

	boolean isHost();

	@Option(shortName = "P", longName = "port", description = "The port for the database server you wish to connect to.")
	String getPort();

	boolean isPort();

	@Option(shortName = "u", longName = "user", description = "The user for the database server you wish to connect to.")
	String getUser();

	boolean isUser();

	@Option(shortName = "p", longName = "password", description = "The password (if required) for the database server you wish to connect to.")
	String getPassword();

	boolean isPassword();

	@Option(description = "Database driver to use")
	String getDriver();

	boolean isDriver();

	@Option(longName = "secondary.host", description = "Some tests require a second database containing the previous release. This "
			+ "configures the hostname of the second database server.")
	String getSecondaryHost();

	boolean isSecondaryHost();

	@Option(longName = "secondary.port", description = "Some tests require a second database containing the previous release. This "
			+ "configures the port of the second database server.")
	String getSecondaryPort();

	boolean isSecondaryPort();

	@Option(longName = "secondary.user", description = "Some tests require a second database containing the previous release. This "
			+ "configures the user name for the second database server.")
	String getSecondaryUser();

	boolean isSecondaryUser();

	@Option(longName = "secondary.password", description = "Some tests require a second database containing the previous release. This "
			+ "configures the password for the second database server.")
	String getSecondaryPassword();

	boolean isSecondaryPassword();

	@Option(longName = "secondary.driver", description = "Some tests require a second database containing the previous release. This "
			+ "configures the driver for the second database server.")
	String getSecondaryDriver();

	boolean isSecondaryDriver();

        @Option(longName = "secondary.database", description = "Some tests require a second database containing the previous release. This "
                        + "configures the database name for the second database server.")
        String getSecondaryDb();

        boolean isSecondaryDb();

	/* Additional methods for handling multiple database servers */

	@Option(description = "Host for server 1 (support for multiple staging servers in Ensembl)")
	String getHost1();

	boolean isHost1();

	@Option(description = "Port for server 1 (support for multiple staging servers in Ensembl)")
	String getPort1();

	boolean isPort1();

	@Option(description = "User for server 1 (support for multiple staging servers in Ensembl)")
	String getUser1();

	boolean isUser1();

	@Option(description = "Password for server 1 (support for multiple staging servers in Ensembl)")
	String getPassword1();

	boolean isPassword1();

	@Option(description = "Driver for server 1 (support for multiple staging servers in Ensembl)")
	String getDriver1();

	boolean isDriver1();

	@Option(description = "Host for server 2 (support for multiple staging servers in Ensembl)")
	String getHost2();

	boolean isHost2();

	@Option(description = "Port for server 2 (support for multiple staging servers in Ensembl)")
	String getPort2();

	boolean isPort2();

	@Option(description = "User for server 2 (support for multiple staging servers in Ensembl)")
	String getUser2();

	boolean isUser2();

	@Option(description = "Password for server 2 (support for multiple staging servers in Ensembl)")
	String getPassword2();

	boolean isPassword2();

	@Option(description = "Driver for server 2 (support for multiple staging servers in Ensembl)")
	String getDriver2();

	boolean isDriver2();

	@Option(longName = "release", description = "Release number which will apply once the data you're testing is released")
	String getRelease();
	boolean isRelease();
}
