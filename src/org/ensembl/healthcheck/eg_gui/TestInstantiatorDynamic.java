package org.ensembl.healthcheck.eg_gui;

import java.util.HashMap;
import java.util.Map;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.TestInstantiator;

/**
 * <p>
 * 	An extension of the TestInstantiator which can deal with dynamic groups
 * of tests. A dynamic group of tests is a group of tests that has been built
 * during the execution of the program. There is no class file for it. Hence 
 * it can't be loaded from the classpath by the classic TestInstantiator.
 * </p>
 * 
 * <p>
 * 	This class can hold names of dynamic groups and their objects. It can 
 * behave as a drop in replacement for the TestInstantiator.
 * </p>
 * 
 * @author michael
 *
 */
public class TestInstantiatorDynamic extends TestInstantiator {
	
	private final Map<String,GroupOfTests> nameToDynamicClassMap;
	
	public TestInstantiatorDynamic(String... packageToScan) {
		
		super(packageToScan);		
		nameToDynamicClassMap = new HashMap<String,GroupOfTests>();
	}
	
	public void addDynamicGroups(String name, GroupOfTests DynamicGroupOfTests) {
		
		nameToDynamicClassMap.put(name, DynamicGroupOfTests);
	}
	
	/**
	 * <p>
	 * 	Check whether a group is dynamic.
	 * </p>
	 * <p>
	 * 	Dynamic groups don't have their own class. When dealing with classes 
	 * of groups of tests this has to be considered.
	 * </p>
	 * 
	 * @param name
	 * @return
	 */
	public boolean isDynamic(String name) {
		
		return nameToDynamicClassMap.containsKey(name);
	}
	
	public <T> T instanceByName(String testName) {
		
		if (nameToDynamicClassMap.containsKey(testName)) {			
			return (T) nameToDynamicClassMap.get(testName);
		}		
		return (T) super.instanceByName(testName);
	}
	
	public <T> T instanceByName(String testName, Class<T> expectedType) {
		
		if (GroupOfTests.class.isAssignableFrom(expectedType)) {
			
			return (T) instanceByName(testName);
		}
		return (T) super.instanceByName(testName);
	}
}
