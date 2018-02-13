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

package org.ensembl.healthcheck.configurationmanager;

import java.lang.reflect.InvocationHandler;
import java.util.Map;
import java.util.Set;

import uk.co.flamingpenguin.jewel.cli.OptionNotPresentException;

/**
 * <p>
 * Configuration objects should extend this abstract class. It provides
 * methods for dealing with configuration objects from the ConfigurationProcessor
 * and makes them implement the InvocationHandler interface.
 * </p>
 * 
 * <p>
 * Configuration objects that extend this class will most likely make heavy   
 * use of reflection and look like they are implementing the configuration 
 * interface like the one the uk.co.flamingpenguin.jewel.cli stuff uses.
 * </p>
 * 
 * <p>
 * Any implementation of this will probably be created using 
 * java.lang.reflect.Proxy.newProxyInstance
 * </p>
 * 
 * <pre>
 *     InvocationHandler handler = new ConfigurationByProperties(propertyFile);
 * 
 *     ConfigurationUserParameters configuration = (ConfigurationUserParameters)
 *     	java.lang.reflect.Proxy.newProxyInstance(
 * 	    	ConfigurationUserParameters.class.getClassLoader(),
 *     		new Class[] { ConfigurationUserParameters.class },
 *     		handler
 *     );
 *</pre>
 *
 */
public abstract class AbstractConfigurationBacking<T> 
	extends ConfigurationProcessor<T> implements InvocationHandler {}



