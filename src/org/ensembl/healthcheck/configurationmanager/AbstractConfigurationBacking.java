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



