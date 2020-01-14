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

import java.util.Map;
import java.util.Set;

import uk.co.flamingpenguin.jewel.cli.OptionNotPresentException;

/**
 * <p>
 * Abstract class that provides the methods mockGetMethod and mockIsMethod
 * which implement get and is functionality.
 * </p>
 * 
 * <p>
 * The user has to implement mockDirectGetMethod and mockDirectIsMethod which
 * don't deal with aliases for configuration variable names, but instead are
 * simple lookup methods in the underlying configuration source.
 * </p>
 * 
 * <p>
 * Parameters accessed by the configuration objects can appear under different
 * names. The reason for this are shortcuts like -c instead of --conf or 
 * alternative names like output.databases which can't be implemented as a
 * get method, because getoutput.databases is an invalid method name. In
 * the latter case the get method is called getOutputDatabases and an 
 * annotation is added to make the configuration object aware that the actual
 * name is output.databases. For an example of this, see:
 * </p>
 * 
 * @see org.ensembl.healthcheck.configuration.ConfigureDatabases
 * @author michael
 * @param <T>
 * 
 */
public abstract class AbstractAliasAwareConfigurationBacking<T> extends AbstractConfigurationBacking<T> {
	
	/**
	 * 
	 * A Map that maps the name of a variable as a string to a set of Strings 
	 * with all of the possible aliases it can have. 
	 * 
	 * The keys of the map are the variable names in the get methods of the
	 * configuration interface. So "getFoo" would produce the key "foo" in 
	 * this map.
	 * 
	 */
	protected final Map<String,Set<String>>           canonicalVarName2AllAliases;

	/**
	 * 
	 * A map that maps the name of a configuration variable to the data type
	 * it is supposed to be in. Supported data types are the elements of the
	 * enum configurationDataType:
	 * 
	 */
	protected final Map<String,configurationDataType> canonicalVarName2DataType;
	
	/**
	 * 
	 * A map that maps the possible aliases for parameters to their canonical
	 * name.
	 * 
	 */
	protected final Map<String,String>                alias2CanonicalVarName;	
	
	/**
	 * Enumeration of possible return data types for configuration values.
	 *
	 */
	public enum configurationDataType {
		String, List_Of_Strings
	}
	
	public AbstractAliasAwareConfigurationBacking (Class<T> configurationClass) {
		canonicalVarName2AllAliases  = createVarName2AllAliases     (configurationClass);
		canonicalVarName2DataType    = createVarName2DataType       (configurationClass);
		alias2CanonicalVarName       = createAlias2CanonicalVarName (configurationClass);
	}
	
	/**
	 * 
	 * A method that gets a variable of exactly this name. This method
	 * is wrapped by mockGetMethod which iterates through all known
	 * aliases for the variable.
	 * 
	 * @param varRequested
	 * @return variable for name
	 * 
	 */
	abstract protected Object mockDirectGetMethod(String varRequested);

	/**
	 * 
	 * A method that tests whether a variable of this name has been
	 * declared in the configuration. Again, to be useful, this method
	 * is wrapped by mockIsMethod which calls this methods for all
	 * possible aliases.
	 * 
	 * @param varRequested
	 * @return true if variable declared
	 */
	abstract protected boolean mockDirectIsMethod(String varRequested);
	
	/**
	 * <p>
	 * Performs the get method for a variable. When a configuration object is
	 * calles like config.getFoo then the call is dispatched to 
	 * mockGetMethod(foo) from the invoke method.
	 * </p>
	 * 
	 * <p>
	 * This method iterates over all possible names under which this vairable
	 * could be known by using the canonicalVarName2AllAliases map. For each
	 * alias it calls the mockDirectIsMethod and the mockDirectGetMethod to 
	 * obtain the actual values.
	 * </p>
	 * 
	 * <p>
	 * mockDirectGetMethod and mockDirectIsMethod are both variants of the 
	 * mockGetMethod and the mockIsMethod which just do a simple lookup in
	 * whatever the source of information the subclass uses. (e.g. property
	 * file, system property etc)
	 * </p>
	 * 
	 * @param varRequested
	 * @return mock method
	 */
	protected Object mockGetMethod(String varRequested) {
		
		Set<String> allAliases = canonicalVarName2AllAliases.get(varRequested);
		
		for (String currentAlias : allAliases) {
			
			if (mockDirectIsMethod(currentAlias)) {
				return mockDirectGetMethod(currentAlias);
			}
		}
		throw new OptionNotPresentException(varRequested + " not found!");
	}
	
	/**
	 * <p>
	 * This is the companion piece of the mockGetMethod for is* method calls.
	 * </p>
	 * 
	 * @param varRequested
	 * @return whether method has been mocked
	 * 
	 */
	protected boolean mockIsMethod(String varRequested) {
		
		Set<String> allAliases = canonicalVarName2AllAliases.get(varRequested);
		
		for (String currentAlias : allAliases) {
			
			if (mockDirectIsMethod(currentAlias)) {
				return true;
			}
		}
		return false;
	}
}
