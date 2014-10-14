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

package org.ensembl.healthcheck;

import org.ensembl.healthcheck.configuration.ConfigurationUserParameters;
import org.ensembl.healthcheck.util.SqlTemplate;

import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * Copies certain configuration properties into system properties. They are 
 * used by some healthchecks, but also by modules like the {@link ReportManager}.
 * 
 * {@link org.ensembl.healthcheck.util.DBUtils} used to use system properties 
 * as well, but has been refactored to take in a configuration object and use 
 * that instead.
 *
 */
public class SystemPropertySetter {

	protected final ConfigurationUserParameters configuration;
	
	public SystemPropertySetter(ConfigurationUserParameters configuration) {
		this.configuration = configuration;
	}

	public void setPropertiesForReportManager_createDatabaseSession() {

		System.setProperty("output.password",    configuration.getOutputPassword());
		System.setProperty("host",           configuration.getHost() );
		System.setProperty("port",           configuration.getPort() );
		System.setProperty("output.release", configuration.getOutputRelease() );

                if (configuration.isTestDatabases()) {
                     String test_databases = "";
                     for (String database : configuration.getTestDatabases()) {
                         test_databases += database + ";";
                     }
                     System.setProperty("test_databases", test_databases);
                }
                if (configuration.isGroups()) {
                     String test_groups = "";
                     for (String group : configuration.getGroups()) {
                         test_groups += group + ";";
                     }
                     System.setProperty("test_groups", test_groups);
                }

	}

	public void setPropertiesForReportManager_connectToOutputDatabase() {
		
		System.setProperty("output.driver",      configuration.getDriver());
		System.setProperty(
			"output.databaseURL", 
			"jdbc:mysql://"
				+ configuration.getOutputHost()
				+ ":"
				+ configuration.getOutputPort()
				+ "/"
		);
		
		System.setProperty("output.database",    configuration.getOutputDatabase());
		System.setProperty("output.user",        configuration.getOutputUser());
		System.setProperty("output.password",    configuration.getOutputPassword());
	}
	
	/**
	 * Sets system properties for the healthchecks.
	 * 
	 */
	public void setPropertiesForHealthchecks() {
		

		if (configuration.isIgnorePreviousChecks()) {
			// Used in:
			//
			// org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionExonCoords
			// org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionBase
			// org.ensembl.healthcheck.testcase.generic.GeneStatus
			//
			System.setProperty("ignore.previous.checks",    configuration.getIgnorePreviousChecks());
		}

		if (configuration.isRepair()) {
			// Used in:
			//
			// org.ensembl.healthcheck.testcase.EnsTestCase
			//
			System.setProperty("repair",    configuration.getRepair());
		}

		if (configuration.isSchemaFile()) {
			// Used in:
			//
			// org.ensembl.healthcheck.testcase.generic.CompareSchema
			//		
			System.setProperty("schema.file",    configuration.getSchemaFile());
		}
		
		if (configuration.isVariationSchemaFile()) {
			// Used in:
			//
			// org.ensembl.healthcheck.testcase.variation.CompareVariationSchema
			//		
			System.setProperty("variation_schema.file",    configuration.getVariationSchemaFile());
		}
		
		if (configuration.isFuncgenSchemaFile()) {
			// Used in:
			//
			// org.ensembl.healthcheck.testcase.funcgen.CompareFuncgenSchema
			//		
			System.setProperty("funcgen_schema.file",    configuration.getFuncgenSchemaFile());
		}
		
		if (configuration.isMasterSchema()) {
			// Used in:
			//
			// org.ensembl.healthcheck.testcase.generic.CompareSchema
			// org.ensembl.healthcheck.testcase.funcgen.CompareFuncgenSchema
			//
			System.setProperty("master.schema",    configuration.getMasterSchema());
		}
		
		
		if (configuration.isPerl()) {
			// Used in:
			//
			// org.ensembl.healthcheck.testcase.AbstractPerlBasedTestCase
			//	
			System.setProperty(
				org.ensembl.healthcheck.testcase.AbstractPerlBasedTestCase.PERL, 
				configuration.getPerl()
			);
		}
		
		if (configuration.isMasterVariationSchema()) {
			// Used in:
			//
			// org.ensembl.healthcheck.testcase.variation.CompareVariationSchema
			//	
			System.setProperty("master.variation_schema",    configuration.getMasterVariationSchema());
		}
		
		if (configuration.isUserDir()) {
			// Used in:
			//
			// org.ensembl.healthcheck.testcase.EnsTestCase
			//
			System.setProperty("user.dir",    configuration.getUserDir());
		}
		
		if (configuration.isFileSeparator()) {
			// Used in:
			//
			// org.ensembl.healthcheck.testcase.EnsTestCase
			//
			System.setProperty("file.separator",    configuration.getFileSeparator());
		}
		
		if (configuration.isDriver()) {
			// Used in:
			//
			// org.ensembl.healthcheck.testcase.EnsTestCase
			//
			System.setProperty("driver",    configuration.getDriver());
		}
		
		if (configuration.isDatabaseURL()) {
			// Used in:
			//
			// org.ensembl.healthcheck.testcase.EnsTestCase
			//
			System.setProperty("databaseURL",    configuration.getDatabaseURL());
		} else {
			// Used in:
			//
			// org.ensembl.healthcheck.testcase.EnsTestCase importSchema
			//
			// when running CompareSchema tests. I have absolutely no idea where
			// the this is supposed to get set in the legacy code.
			//
			System.setProperty(
				"databaseURL",    
				"jdbc:mysql://"
				+ configuration.getHost()
				+ ":"
				+ configuration.getPort()
				+ "/"
			);
		}
		
		if (configuration.isUser()) {
			// Used in:
			//
			// org.ensembl.healthcheck.testcase.EnsTestCase
			//
			System.setProperty("user",    configuration.getUser());
		}
		
		if (configuration.isPassword()) {
			// Used in:
			//
			// org.ensembl.healthcheck.testcase.EnsTestCase
			//
			System.setProperty("password",    configuration.getPassword());
		}
		if (configuration.isMasterFuncgenSchema()) {
			// Used in:
			//
			// org.ensembl.healthcheck.testcase.funcgen.CompareFuncgenSchema
			//
			System.setProperty("master.funcgen_schema",    configuration.getMasterFuncgenSchema());
		}
	}
}
