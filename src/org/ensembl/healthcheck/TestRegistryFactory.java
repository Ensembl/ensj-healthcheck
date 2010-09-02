package org.ensembl.healthcheck;

import java.net.URL;
import java.net.URLClassLoader;

import org.ensembl.healthcheck.configuration.ConfigureTestGroups;

/**
 * 
 * A factory class for creating implementations of TestRegistry. Currently 
 * supported are the ConfigurationBasedTestRegistry and the 
 * DiscoveryBasedTestRegistry.
 * 
 * Use the getTestRegistry method for creating a TestRegistry.
 *
 */
public class TestRegistryFactory {

	/**
	 * The kinds of test registries that this factory can produce
	 *
	 */
	public static enum TestRegistryType {
		ConfigurationBased, DiscoveryBased
	}
	
	protected final ConfigureTestGroups configurationUserParameters;
	
	public TestRegistryFactory(ConfigureTestGroups configurationUserParameters) {
		
		this.configurationUserParameters = configurationUserParameters;
	}

	/**
	 * 
	 * @param testRegistryType: An element of the enum TestRegistryType
	 * @return An implementation of TestRegistry
	 * @throws TestRegistryCreationException
	 * 
	 * Use for creating different kinds of configuration objects, use
	 * the enum TestRegistryType for specifying which kind you want.
	 * 
	 */
	public TestRegistry getTestRegistry(TestRegistryType testRegistryType) throws TestRegistryCreationException {

		TestRegistry testRegistry = null;
		
		if (testRegistryType == TestRegistryType.ConfigurationBased) {
			testRegistry = createConfigurationBasedTestRegistry();
		}
		if (testRegistryType == TestRegistryType.DiscoveryBased) {
			testRegistry = createDiscoveryBasedTestRegistry();
		}
		
		return testRegistry;
	}
	
	/**
	 * @return An instance of a ConfigurationBasedTestRegistry
	 * @throws TestRegistryCreationException 
	 * 
	 * Creates a ConfigurationBasedTestRegistry, the configurationUserParameters
	 * must be set for this or a TestRegistryCreationException will be thrown. 
	 * 
	 */
	protected ConfigurationBasedTestRegistry createConfigurationBasedTestRegistry() throws TestRegistryCreationException {
		
		ConfigurationBasedTestRegistry testRegistry = null;
		ConfigureTestGroups   configurationUserParameters  = this.configurationUserParameters;
		
		if (configurationUserParameters == null) {
			throw new TestRegistryCreationException("configurationUserParameters attribute is null!");
		}
		
		try {
			testRegistry = new ConfigurationBasedTestRegistry(configurationUserParameters);

		} catch (InstantiationException   e) { throw new TestRegistryCreationException(e.getMessage());
		} catch (IllegalAccessException   e) { throw new TestRegistryCreationException(e.getMessage());
		} catch (UnknownTestTypeException e) { throw new TestRegistryCreationException(e.getMessage());
		} catch (ClassNotFoundException   e) { throw new TestRegistryCreationException(
				"\n--------------------------------------------------\n"
				+ "The following class could not be found:\n"
				+ e.getMessage() + "\n\n"
				+ "The current classpath is:\n"
				+ PrintClasspath()
				+ "\n--------------------------------------------------\n"
			);
		}
		return testRegistry;
	}
	/**
	 * @return An instance of a ConfigurationBasedTestRegistry
	 * 
	 */
	protected DiscoveryBasedTestRegistry createDiscoveryBasedTestRegistry() {
		return new DiscoveryBasedTestRegistry();
	}
	
	/**
	 * @return A string with the classpath
	 * 
	 * Copied the relevant code from
	 * http://www.java-tips.org/java-se-tips/java.lang/how-to-print-classpath.html
	 * 
	 */
	protected String PrintClasspath() {
		//Get the System Classloader
        ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();
        StringBuffer buf = new StringBuffer(); 

        //Get the URLs
        URL[] urls = ((URLClassLoader)sysClassLoader).getURLs();

        for(int i=0; i< urls.length; i++) {
        	buf.append(urls[i].getFile());
        	buf.append("\n");
        }
        return buf.toString();
	}
}
