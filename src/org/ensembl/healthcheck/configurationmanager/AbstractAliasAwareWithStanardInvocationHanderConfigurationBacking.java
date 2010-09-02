package org.ensembl.healthcheck.configurationmanager;

import java.lang.reflect.Method;

/**
 * <p>
 * Provides a standard invocation handler for configuration objects.
 * </p>
 *
 * @param <T>
 */
public abstract class AbstractAliasAwareWithStanardInvocationHanderConfigurationBacking<T> 
	extends AbstractAliasAwareConfigurationBacking<T> {

	/**
	 * <p>
	 * Requires the class object of the configuration interface that it will 
	 * be backing. The class is necessary so annotations describing aliases
	 * for the configuration parameters can be read and the expected return 
	 * types.
	 * </p>
	 * 
	 * @param configurationClass
	 */
	AbstractAliasAwareWithStanardInvocationHanderConfigurationBacking(Class<T> configurationClass) {
		super(configurationClass);
	}
	
	/**
	 * <p>
	 * This method is called when any of the get* or is* methods are called on
	 * a configuration object inheriting from this and not overriding.
	 * </p>
	 * 
	 * <p>
	 * It dispatches calling of these methods to mockGetMethod and mockIsMethod
	 * which are inherited by AbstractAliasAwareConfigurationBacking. If 
	 * "toString()" is called, this is dispatched to the inheriting objects 
	 * toString method which should be overridden. 
	 * </p>
	 * 
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 * 
	 */
	public Object invoke(Object proxy, Method m, Object[] args) {
		
		// Implementations of this abstract class should override toString to 
		// create an adequate representation of the configuration object.
		//
		if (m.getName().equals("toString")) {
			return this.toString();
		}

		// If it is not toString that was called, then there must have been a 
		// get* or is* request for a variable.
		
		String varRequested = getVariableRequested(m);
		
		if (isGetMethod(m)) {
			return mockGetMethod(varRequested);
		}
		if (isIsMethod(m)) {
			return mockIsMethod(varRequested);
		}

		throw new IllegalArgumentException("This only supports get* and is* methods! Method tried was: " + m.getName());
	}
}
