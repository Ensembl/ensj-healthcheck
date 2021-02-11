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

package org.ensembl.healthcheck.configurationmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import uk.co.flamingpenguin.jewel.cli.OptionNotPresentException;

/**
 * A configuration object that gets its information from a property file.
 * 
 * Use ConfigurationFactory for instantiation or one of the many constructors
 * 
 */
public class ConfigurationByProperties<T> extends
		AbstractAliasAwareWithStanardInvocationHanderConfigurationBacking<T> {

	static final Logger log = Logger.getLogger(ConfigurationByProperties.class
			.getCanonicalName());

	/**
	 * A list of strings may be expected as a configuration value. In properties
	 * file however, there are only strings. In case a list of strings is
	 * requested in the configuration interface the string found in the property
	 * will be split on the value in the listSeparatorInProperyFile variable to
	 * generate a list of values.
	 * 
	 * Update 11/10/2010: Changed from comma (",") to space (" ") as requested
	 * by Dan
	 * 
	 */
	private final static String LIST_SEPARATOR_IN_PROPERTY_FILE = "\\s+";

	private final Properties properties;

	protected Map<String, Set<String>> parameterAliasesMap;

	/**
	 * 
	 * Construct a ConfigurationByProperties object that proxies a
	 * configurationInterfaceToProxy. Initialised by passing properties.
	 * 
	 * @param configurationInterfaceToProxy
	 * @param properties
	 * @return {@link ConfigurationByProperties}
	 */
	public static Object newInstance(Class configurationInterfaceToProxy,
			Properties properties) {

		return createProxyUsingConfigurationObject(
				configurationInterfaceToProxy, new ConfigurationByProperties(
						configurationInterfaceToProxy, properties));
	}

	/**
	 * 
	 * Construct a ConfigurationByProperties object that proxies a
	 * configurationInterfaceToProxy. Initialised by passing the name of a
	 * property file.
	 * 
	 * @param configurationInterfaceToProxy
	 * @param propertyFile
	 * @return {@link ConfigurationByProperties}
	 * @throws IOException
	 */
	public static Object newInstance(Class configurationInterfaceToProxy,
			String propertyFile) throws IOException {

		return createProxyUsingConfigurationObject(
				configurationInterfaceToProxy, new ConfigurationByProperties(
						configurationInterfaceToProxy, propertyFile));
	}

	/**
	 * 
	 * Creates a proxy of type configurationInterfaceToProxy for a handler which
	 * is an instance of this object.
	 * 
	 * @param configurationInterfaceToProxy
	 * @param handler
	 * @return proxy
	 */
	private static Object createProxyUsingConfigurationObject(
			Class configurationInterfaceToProxy, InvocationHandler handler) {
		Object configuration = java.lang.reflect.Proxy.newProxyInstance(
				configurationInterfaceToProxy.getClassLoader(),
				new Class[] { configurationInterfaceToProxy }, handler);
		return configuration;
	}

	/**
	 * Constructor using a filename for finding the properties file
	 * 
	 * @param propertyFile
	 * @throws IOException
	 * 
	 */
	public ConfigurationByProperties(Class<T> configurationClass,
			String propertyFile) throws IOException {
		this(configurationClass, new File(propertyFile));
	}

	/**
	 * Constructor using a File object for finding the properties file
	 * 
	 * @param propertyFile
	 * @throws IOException
	 * 
	 */
	public ConfigurationByProperties(Class<T> configurationClass,
			File propertyFile) throws IOException {
		this(configurationClass, new FileInputStream(propertyFile));
	}

	/**
	 * Constructor using a FileInputStream object for reading the properties
	 * file.
	 * 
	 * @param propertyFis
	 * @throws IOException
	 * 
	 */
	public ConfigurationByProperties(Class<T> configurationClass,
			FileInputStream propertyFis) throws IOException {

		super(configurationClass);

		Properties properties = new Properties();
		properties.load(propertyFis);
		propertyFis.close();

		this.properties = properties;
		parameterAliasesMap = this
				.createParameterAliasesMap(configurationClass);

	}

	/**
	 * @param properties
	 * 
	 *            Constructor that takes the properties directly.
	 * 
	 */
	public ConfigurationByProperties(Class<T> configurationClass,
			Properties properties) {

		super(configurationClass);

		this.properties = properties;
		parameterAliasesMap = this
				.createParameterAliasesMap(configurationClass);
	}

	protected Object mockDirectGetMethod(String varRequested) {

		// Result may be null
		String configValue = this.properties.getProperty(varRequested);

		// Requested property might not have been set. If so
		if (configValue == null) {
			throw new OptionNotPresentException(
					"No configuration setting found for " + varRequested);
		}

		log.fine("varRequested: " + varRequested);

		configurationDataType dataTypeExpected = canonicalVarName2DataType
				.get(alias2CanonicalVarName.get(varRequested));

		if (dataTypeExpected == null) {
			throw new NullPointerException("Unknown return data type for "
					+ varRequested);
		}

		if (dataTypeExpected == configurationDataType.String) {
			
			// Return trimmed version of the String so trailing spaces will 
			// not be returned as part of the value. 
			//
			return configValue.trim();
		}
		if (dataTypeExpected == configurationDataType.List_Of_Strings) {

			List<String> returnValue = new ArrayList<String>();

			for (String currentValue : configValue
					.split(LIST_SEPARATOR_IN_PROPERTY_FILE)) {
				
				// The trimmed values are returned. Actually the call to trim 
				// does not have any effect here anymore, because the split 
				// on LIST_SEPARATOR_IN_PROPERTY_FILE already gets rid of 
				// whitespaces around the values.
				//
				returnValue.add(currentValue.trim());
			}
			return returnValue;
		}

		throw new RuntimeException("Unknown return type " + dataTypeExpected
				+ " for " + varRequested + "!");
	}

	protected boolean mockDirectIsMethod(String varRequested) {

		try {
			// mockGetMethod(varRequested);
			mockDirectGetMethod(varRequested);
		} catch (OptionNotPresentException e) {
			return false;
		}
		return true;
	}

	public String toString() {
		return this.getClass().getSimpleName() + " " + properties.toString();
	}

}
