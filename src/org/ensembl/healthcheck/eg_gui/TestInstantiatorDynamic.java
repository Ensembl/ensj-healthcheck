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
	 * @return true if group is dynamic
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
