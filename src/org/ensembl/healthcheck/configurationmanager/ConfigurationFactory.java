/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2020] EMBL-European Bioinformatics Institute
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

package org.ensembl.healthcheck.configurationmanager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.CliFactory;
import java.util.List;
import java.util.ArrayList;

/**
 * Use for creating different kinds of configuration objects, supports
 * 
 * - Properties 
 * - Commandline and
 * - Cascading
 * 
 * from the static enum ConfigurationType in this class.
 * 
 * The interface of the configuration objects returned follows the example 
 * of uk.co.flamingpenguin.jewel.cli. The concrete interface that this 
 * factories configuration objects should implement must be passed to the
 * constructor as a class together with the command line arguments. 
 *
 * @author mnuhn
 * 
 */
public class ConfigurationFactory <T> {

	/**
	 * An enumeration of the kinds of configuration objects that the 
	 * configuration object can produce.
	 *
	 */
	public static enum ConfigurationType {
		Properties, Commandline, Cascading
	}
	
	/**
	 * Configuration objects will return an object of this type.
	 */
	protected final Class<T>   configurationInterface;
	/**
	 * A list of configuration objects that will be used for producing a
	 * Cascading configuration object
	 */
	protected final List<T>    configurationObjects;
	/**
	 * A list of property files from which configuration objects based on
	 * Properties are produced.
	 */
	protected final List<File> propertyFile;
	/**
	 * The command line arguments as they would be given in to the main method
	 * of any java program.
	 * 
	 */
	protected final String[] commandLineArguments;
	
	/**
	 * @param configurationInterface The class object of the interface that 
	 *     should be mocked by the produced configuration objects
	 *     
	 * @param args The command line parameters that were given. These will
	 *     be used to initialise the created objects and to find properties
	 *     files that the user specified.
	 */
	public ConfigurationFactory(Class<T> configurationInterface, Object ... args) {
		this.configurationInterface = configurationInterface;
		
		List<String> commandLineArguments = new ArrayList<String>();
		List<T>      configurationObjects = new ArrayList<T>();
		List<File>   propertyFiles        = new ArrayList<File>();
		// Can be a configuration object or a String
		for (Object arg : args) {
			
			if (arg != null) {
			
				if (arg instanceof String) {
					// Every string in the constructor is a command line argument
					commandLineArguments.add((String) arg);
				} else {
					if (arg instanceof String[]) {
						//
						// The constructor accepts arrays of Strings as well like
						// the args[] that are passed to the main method. These
						// are added to the list of command line arguments.
						//
						String[] argList = (String[]) arg;
						
						for (String a : argList) {
							commandLineArguments.add(a);
						}
					} else {
						if (arg instanceof List<?>) {
							propertyFiles.addAll( (List<File>) arg );
						} else {
							configurationObjects.add((T) arg);
						}
					}
				}
			}
		}		
		this.commandLineArguments = commandLineArguments.toArray(new String[]{});
		this.configurationObjects = configurationObjects;
		this.propertyFile         = propertyFiles;
	}
	
	/**
	 * @param configurationType element of the enum ConfigurationTypes specifying what 
	 *     configuration object you want.
	 * @return ConfigurationUserParameters
	 * 
	 * The factory method that creates configuration objects.
	 * 
	 */
	public T getConfiguration(ConfigurationType configurationType) {

		T conf = null;
		
		if (configurationType == configurationType.Properties) {
			if (propertyFile.size() == 0) {
				throw new RuntimeException("No property file was given!");
			}
			if (propertyFile.size() == 1) {
				return getConfigurationByProperties(this.propertyFile.get(0));
			}
			if (propertyFile.size() > 1) {
				throw new RuntimeException("More than one property file was given, this is ambiguous!");
			}
		}
		if (configurationType == configurationType.Commandline) {
			return getConfigurationByCommandline();
		}		
		if (configurationType == configurationType.Cascading) {
			return getConfigurationByCascading();
		}		
		throw new IllegalArgumentException();
	}

	/**
	 * @return T
	 * 
	 * Create a configuration object that returns parameters read from a 
	 * property file.
	 * 
	 */
	protected List<T> getConfigurationByProperties(List<File> propertyFiles) {
		
		List<T> configurationList = new ArrayList<T>();
		
		for (File file : propertyFiles) {
			configurationList.add(getConfigurationByProperties(file));
		}
		
		return configurationList;		
	}
	protected T getConfigurationByProperties(File propertyFile) {
		
		InvocationHandler handler;

		try {
			handler = new ConfigurationByProperties(configurationInterface, propertyFile);
		} catch (IOException e) {
			throw new ConfigurationException(
				"There was a problem reading properties from file " + propertyFile.getName(), 
				e
			);
		}

		@SuppressWarnings("unchecked")
		T configuration = (T)
			java.lang.reflect.Proxy.newProxyInstance(
				this.configurationInterface.getClassLoader(),
				new Class[] { this.configurationInterface },
				handler
		);

		return configuration;
	}

	/**
	 * 
	 * Create a configuration object that returns parameters read from a 
	 * property file.
	 * 
	 * @return T
	 */
	protected T getConfigurationByCascading() {
		
		List<T> configurationObjectList = new ArrayList<T>();
		
		// Add the configuration objects in the order they should override 
		// each other. Command line options override property file information
		// which in turn override any default configuration objects that may
		// have been given.
		//
		configurationObjectList.add    (getConfigurationByCommandline());
		configurationObjectList.addAll (getConfigurationByProperties(this.propertyFile));
		configurationObjectList.addAll (configurationObjects);
		
		InvocationHandler handler = new ConfigurationByCascading<T>(configurationObjectList);

		@SuppressWarnings("unchecked")		
		T configuration = (T)
			java.lang.reflect.Proxy.newProxyInstance(
				this.configurationInterface.getClassLoader(),
				new Class[] { this.configurationInterface },
				handler
		);

		return configuration;
	}

	/**
	 * Create a configuration object that returns parameters read from the 
	 * command line.
	 * 
	 * @return T
	 * 
	 */
	protected T getConfigurationByCommandline() {
		
		T result = null;
		
		String[] args = this.commandLineArguments; 
		
		if (args == null) {
			throw new NullPointerException("Command line arguments have not been set in factory!");
		}
		
		try {
			result = (T) CliFactory.parseArguments(
				this.configurationInterface, args
			);
		} catch (ArgumentValidationException e) {
			throw new ConfigurationException(e.getMessage());
		}
		
		return result;
	}
}


