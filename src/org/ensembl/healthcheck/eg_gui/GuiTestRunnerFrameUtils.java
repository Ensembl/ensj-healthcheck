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

package org.ensembl.healthcheck.eg_gui;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.configuration.ConfigureHost;
import org.ensembl.healthcheck.configurationmanager.ConfigurationFactory;
import org.ensembl.healthcheck.configurationmanager.ConfigurationFactory.ConfigurationType;
import org.ensembl.healthcheck.util.DBUtils;

public class GuiTestRunnerFrameUtils {
	
	/** The logger to use for this class */
	protected static Logger logger = Logger.getLogger("HealthCheckLogger");

	/**
	 * <p>
	 * 	Creates a configuration object of type ConfigureHost from the File
	 * passed as a parameter and returns it.
	 * </p>
	 * 
	 * @param iniFile
	 * @return config object
	 */
	protected static ConfigureHost getHostConfiguration(File iniFile) {

		List<File> propertyFileNames = new ArrayList<File>();
		
		propertyFileNames.add(iniFile);
		
		ConfigurationFactory<ConfigureHost> confFact = new ConfigurationFactory(
			ConfigureHost.class, 
			propertyFileNames
		);		
		ConfigureHost configuration = confFact.getConfiguration(ConfigurationType.Properties);

		return configuration;
	}

	/**
	 * <p>
	 * 	Iterates over all files in the directories dirWithDbServerConfigs. 
	 * Assumes they are inifiles with data to populate a ConfigureHost object.
	 * Creates a ConfigureHost object for every inifile and returns a 
	 * List<ConfigureHost>.
	 * </p>
	 * 
	 * @param dirsWithDbServerConfigs
	 * @return list of configuration objects
	 */
	public static List<ConfigureHost> createDbDetailsConfigurations(String... dirsWithDbServerConfigs) {
		
		List<ConfigureHost> dbDetails = new ArrayList<ConfigureHost>();
		
		for (String dirWithDbServerConfigs : dirsWithDbServerConfigs) {
		
			File currentDir = new File(dirWithDbServerConfigs);
			
			if (currentDir.exists() && currentDir.canRead() && currentDir.isDirectory()) {

				for (
					File f : currentDir.listFiles(

						// Only use ini files. 
						//
						new FilenameFilter() {
							public boolean accept(File arg0, String arg1) {
								return arg1.endsWith(".ini");
							}
						}
					)
				) {
					ConfigureHost configuration = getHostConfiguration(f);
					dbDetails.add(configuration);
				}
			} else {
				logger.info("Skipping " + currentDir);
			}
		}
		return dbDetails;
	}

	/**
	 * 
	 * <p>
	 * 	Returns true, if it can connect to the database server specified in 
	 * the dbDetails parameter and finds at least one database one it, false 
	 * otherwise.
	 * </p>
	 * 
	 * @param dbDetails
	 * @return true if connection succeeds
	 */
	protected static boolean canConnectToDbServer(ConfigureHost dbDetails) {
		
		DBUtils.initialise();
		DBUtils.setHostConfiguration(dbDetails);
		
		List<String> regexps = new ArrayList<String>();
		regexps.add(".*");

		DatabaseRegistry databaseRegistry = new DatabaseRegistry(
			regexps, 
			null,
			null, 
			false
		);

		return databaseRegistry.getEntryCount()>0;		
	}
	
	/**
	 * <p>
	 * 	Iterates over the List<ConfigureHost> parameter which is a list of 
	 * dbDetails and returns a list of List<ConfigureHost> which are the ones
	 * to which it can connect and that have at least one database on them. 
	 * </p>
	 * 
	 * @param dbDetails
	 * @return configuration objects
	 */
	public static List<ConfigureHost> grepForAvailableServers(List<ConfigureHost> dbDetails) {
		
		List<ConfigureHost> availableDbDetails = new ArrayList<ConfigureHost>();
		
		for (ConfigureHost currentDbDetails : dbDetails) {
			
			if (canConnectToDbServer(currentDbDetails)) {
				availableDbDetails.add(currentDbDetails);
			} else {
				logger.warning("Can't connect to " + currentDbDetails.getHost() + ":" + currentDbDetails.getPort());
			}
		}
		return availableDbDetails;
	}
}
