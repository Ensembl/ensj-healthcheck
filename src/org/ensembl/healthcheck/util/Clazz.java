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

package org.ensembl.healthcheck.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.EnsTestCase;

/**
 * @author michael
 *
 *	<p>
 *		Set of static convenience methods to deal with common tasks involving
 *		classes.
 *	</p>
 */
public class Clazz {
	
	private static Logger logger = Logger.getLogger("HealthCheckLogger");
	
	/**
	 * @param jarFile
	 * @return String
	 * 
	 * <p>
	 * 	Creates a list as a text string with all testgroups found in the
	 * 	jar file passed as the parameter. For every testgroup all testcases
	 * 	belonging to it are listed as well.
	 * </p>
	 * 
	 */
	public static String testGroupOverviewAsString(String jarFile) {
		
		List<String> classesInJar = Jar.findAllClassesInJar(jarFile);
		
		List<GroupOfTests> testGroupList = Clazz.instantiateListOfTestGroups(
			ClassFileFilter.filterForTrueSubclassesOf(
					classesInJar,
					GroupOfTests.class
			)
		);		
		StringBuffer result = new StringBuffer(); 
		
		for (GroupOfTests testGroup : testGroupList) {
			
			result.append(testGroup.getName() + "\n");
			
			Iterator<GroupOfTests> i = testGroupList.iterator();
			
			while (i.hasNext()) {
				
				GroupOfTests testCase = i.next();
				result.append("\t" + testCase.getName() + "\n");
			}
			result.append("\n");
		}
		return result.toString();
	}
	
	/**
	 * 
	 * Create a List<Class<T>> of classes from a List<String> of class names.
	 * 
	 * @param <T>
	 * @param listOfClassNames
	 * @return list of classes
	 */
	@SuppressWarnings("unchecked")
  public static <T> List<Class<T>> classloadListOfClasses(List<String> listOfClassNames) {

		List<Class<T>> listOfClasses = new ArrayList<Class<T>>();		
		
		for (String currentClassName : listOfClassNames) {
			
			Class<T> newClass = null;
			
			try {
				
				newClass = (Class<T>) Class.forName(currentClassName);
				
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
			
			listOfClasses.add(newClass);
		}
		return listOfClasses;
	}
	
	/**
	 * The method takes a list of class names. It instantiates them and 
	 * returns a list of objects. The generic type T specifies the type of
	 * class in the list.
	 * 
	 */
	@SuppressWarnings("unchecked")
  public static <T> List<T> instantiateGenericListOfClasses(List<String> listOfClassNames) {

		List<T> listOfTestGroups = new ArrayList<T>(); 
		
		for (String currentClassName : listOfClassNames) {
			
			try {
				Class<T> newClass = (Class<T>) Class.forName(currentClassName);
				
				T testGroup = newClass.newInstance();
				listOfTestGroups.add(testGroup);
				
			} catch (IllegalAccessException e) {
				// EG: Catch and log reflection warnings
				logger.warning("Exception when instantiating " + currentClassName + "\n" + e.getMessage());
			} catch (InstantiationException e) {
				logger.warning("Exception when instantiating " + currentClassName + "\n" + e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return listOfTestGroups;
	}
	
	/**
	 * The input is a list of class names of groups of tests. This method
	 * loads every class in the list, instantiates it and returns a list of
	 * objects.
	 * 
	 */
	public static List<GroupOfTests> instantiateListOfTestGroups(List<String> listOfClassNames) {		
		return instantiateGenericListOfClasses(listOfClassNames);
	}		

	/**
	 * The input is a list of class names of groups of tests. This method
	 * loads every class in the list, instantiates it and returns a list of
	 * objects.
	 * 
	 */
	public static List<EnsTestCase> instantiateListOfTests(List<String> listOfClassNames) {		
		return instantiateGenericListOfClasses(listOfClassNames);
	}		
}
