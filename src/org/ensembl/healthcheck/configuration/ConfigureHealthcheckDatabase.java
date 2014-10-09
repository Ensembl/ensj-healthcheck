/*
 * Copyright [1999-2014] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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
 * Interface for specifying the connection details and database name to
 * which the results of healthchecks will be written, if the reporterType
 * is "database".
 * 
 * @author michael
 *
 */
public interface ConfigureHealthcheckDatabase {

		// The database where the results of healthchecks are written to, if
		// the ReporterType is set to database
		//
		@Option(
			longName    = "output.database",
			description = "The name of the database where the results of the "
				+ "healthchecks are written to, if the database reporter is "
				+ "used."
		)
		String getOutputDatabase();
		boolean isOutputDatabase();

                @Option(
                        longName    = "sessionID",
                        description = "The session to add these results for"
                                + "Reuse same session in parallel run"
                )
                String getSessionID();
                boolean isSessionID();

		@Option(
			longName    = "output.host",
			description = "The name of the database where the results of the "
				+ "healthchecks are written to, if the database reporter is "
				+ "used."
		)
		String getOutputHost();
		boolean isOutputHost();
		
		@Option(
			longName    = "output.port",
			description = "The port of the database where the results of the "
				+ "healthchecks are written to, if the database reporter is "
				+ "used."
		)
		String getOutputPort();
		boolean isOutputPort();
		
		@Option(
			longName    = "output.user",
			description = "The user name for the database where the results "
				+ "of the healthchecks are written to, if the database reporter "
				+ "is used."
		)
		String getOutputUser();
		boolean isOutputUser();
		
		@Option(
			longName    = "output.password",
			description = "The password for the database where the results "
				+ "of the healthchecks are written to, if the database "
				+ "reporter is used."
		)
		String getOutputPassword();
		boolean isOutputPassword();
		
		@Option(
			longName    = "output.driver",
			description = "The driver for the database where the results "
				+ "of the healthchecks are written to, if the database "
				+ "reporter is used."
		)
		String getOutputDriver();
		boolean isOutputDriver();
		
		@Option(
			longName    = "output.release",
			description = "Gets written into the session table for describing"
				+ " the test session, if the database reporter is used."
		)
		String getOutputRelease();
		boolean isOutputRelease();	

		@Option(
			longName    = "output.schemafile",
			description = "If output.database does not exist, it will be "
				+ "created automatically. This file should have the SQL "
				+ "commands to create the schema. Please remember that hashes "
				+ "(#) are not allowed to start comments in SQL. Use two "
				+ "dashes \"--\" at the beginning of a line instead. If the "
				+ "configuratble testrunner can't find this file from the "
				+ "current working directory, it will search for it in the "
				+ "classpath."
		)
		String getOutputSchemafile();
		boolean isOutputSchemafile();	
		
		@Option(
			longName    = "production.database",
			description = "The name of the Ensembl production database to use to retrieve division information. " +
					"Assumed to be on the same server as the output databases."
		)
		String getProductionDatabase();
		boolean isProductionDatabase();
		
}
