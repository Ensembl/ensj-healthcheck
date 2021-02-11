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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ensembl.healthcheck.configurationmanager.AbstractAliasAwareConfigurationBacking.configurationDataType;

import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * 
 * Provides methods for classes that operate on objects implementing the
 * configuration interface. These objects are the objects proxying the
 * configuration interface and the ConfigurationDumper.
 * 
 */
public class ConfigurationProcessor<T> {

	/**
	 * @param m
	 *            method of type java.lang.reflect.Method
	 * @return true, if the method expects a java.util.List of Strings, false
	 *         otherwise.
	 */
	boolean listOfStringsExpected(Method m) {

		boolean listOfStringsExpected = false;

		Type t = m.getGenericReturnType();

		if (t instanceof ParameterizedType) {

			Type rawType = ((ParameterizedType) t).getRawType();
			Type actualType = ((ParameterizedType) t).getActualTypeArguments()[0];

			listOfStringsExpected = rawType.equals(java.util.List.class)
					&& actualType.equals(java.lang.String.class);

		}
		return listOfStringsExpected;
	}

	/**
	 * @param m
	 *            - A method
	 * @return True, if the method name begins with get, false otherwise
	 */
	boolean isGetMethod(Method m) {
		return isGetMethod(m.getName());
	}

	boolean isGetMethod(String m) {
		return m.startsWith("get");
	}

	/**
	 * @param m
	 *            - A method
	 * @return True, if the method name begins with is, false otherwise
	 */
	boolean isIsMethod(Method m) {
		return isIsMethod(m.getName());
	}

	boolean isIsMethod(String m) {
		return m.startsWith("is");
	}

	/**
	 * @param m
	 * @return
	 * 
	 *         returns the name of a variable requested by a method.
	 * 
	 *         getHost will return host getDatabase will return database etc.
	 * 
	 */
	String getVariableRequested(Method m) {
		return getVariableRequested(m.getName());
	}

	String getVariableRequested(String methodName) {

		if (isGetMethod(methodName)) {
			return methodName.substring(3).toLowerCase();
		}

		if (isIsMethod(methodName)) {
			return methodName.substring(2).toLowerCase();
		}

		throw new IllegalArgumentException("Method " + methodName
				+ " is an unknown type of method!");
	}

	/**
	 * <p>
	 * Creates a map that maps the name of a variable, as it may be requested in
	 * a get method like configuration.get*someVariable* to all its possible
	 * aliases. Aliases for variables can be specified in the configuration
	 * interface as long- or short name annotations like these:
	 * </p>
	 * 
	 * <pre>
	 * &#064;Option(longName = &quot;output.databases&quot;)
	 * List&lt;String&gt; getOutputDatabases();
	 * </pre>
	 * 
	 * or
	 * 
	 * <pre>
	 * &#064;Option(shortName = &quot;c&quot;)
	 * List&lt;File&gt; getConf();
	 * </pre>
	 * 
	 */
	public Map<String, Set<String>> createVarName2AllAliases(
			Class<T> configurationClass) {

		Map<String, Set<String>> varName2AllAliases = new HashMap<String, Set<String>>();

		List<Method> getMethods = getGetMethods(configurationClass);

		for (Method getMethod : getMethods) {

			varName2AllAliases.put(getVariableRequested(getMethod),
					getAliases(getMethod));
		}
		return varName2AllAliases;
	}

	public Map<String, String> createAlias2CanonicalVarName(
			Class<T> configurationClass) {

		Map<String, String> alias2MethodName = new HashMap<String, String>();

		List<Method> getMethods = getGetMethods(configurationClass);

		for (Method getMethod : getMethods) {

			String methodName = getVariableRequested(getMethod);

			for (String possibleName : getAliases(getMethod)) {

				alias2MethodName.put(possibleName, methodName);
			}
		}
		return alias2MethodName;
	}

	public Map<String, configurationDataType> createVarName2DataType(
			Class<T> configurationClass) {

		Map<String, configurationDataType> varName2DataType = new HashMap<String, configurationDataType>();

		List<Method> getMethods = getGetMethods(configurationClass);

		for (Method getMethod : getMethods) {

			configurationDataType returnTypeExpected = null;

			if (getMethod.getReturnType().equals(String.class)) {
				returnTypeExpected = configurationDataType.String;
			}
			if (getMethod.getReturnType().equals(java.util.List.class)) {
				returnTypeExpected = configurationDataType.List_Of_Strings;
			}

			if (returnTypeExpected == null) {

				throw new RuntimeException("Unknown return type "
						+ getMethod.getReturnType().getName() + " for "
						+ getMethod.getName() + "!");
			}

			varName2DataType.put(getVariableRequested(getMethod),
					returnTypeExpected);
		}
		return varName2DataType;
	}

	/**
	 * <p>
	 * This method takes a getMethod from the configuration interface as a
	 * parameter and returns the names (aliases) by which this parameter could
	 * be requested for as a set of strings.
	 * </p>
	 * 
	 * <p>
	 * The aliases are taken from the annotations in the configuration
	 * interface. The names are in the "shortName" and the "longName"
	 * Annotations.
	 * </p>
	 * 
	 * @param getMethod
	 *            - A method
	 * @return A list of names of configuration variables that can be returned
	 * 
	 */
	Set<String> getAliases(Method getMethod) {

		Set<String> possibleRequestedNames = new HashSet<String>();
		String variableRequested = getVariableRequested(getMethod);
		possibleRequestedNames.add(variableRequested);
		Annotation[] annotations = getMethod.getAnnotations();

		for (Annotation annotation : annotations) {
			if (annotation instanceof uk.co.flamingpenguin.jewel.cli.Option) {
				Option o = (uk.co.flamingpenguin.jewel.cli.Option) annotation;
				for (String sn : o.shortName()) {
					if (sn.length() > 0) {
						possibleRequestedNames.add(sn);
					}
				}
				if (o.longName().length() > 0) {
					possibleRequestedNames.add(o.longName());
				}
			}
		}
		return possibleRequestedNames;
	}

	/**
	 * @param configurationClass
	 * @return A Map that maps a the name of a parameter from the get* or is*
	 *         methods of a configuration class to a set of Strings under which
	 *         this parameter may be known in the properties file.
	 * 
	 *         For example the getOutputDatabase method is for the parameter
	 *         outputDatabase which may be represented in the properties file as
	 *         "outputDatabase" or "output.database". The alternatives can be
	 *         specified as an Option annotation in the configuration interface
	 *         like this
	 * 
	 * @Option(longName="output.databases") List<String> getOutputDatabases();
	 * 
	 *                                      or this:
	 * 
	 * @Option(shortName="c") List<File> getConf();
	 * 
	 */
	public Map<String, Set<String>> createParameterAliasesMap(
			Class<T> configurationClass) {

		List<Method> getMethods = getGetMethods(configurationClass);

		Map<String, Set<String>> parameterAliasesMap = new HashMap<String, Set<String>>();

		for (Method getMethod : getMethods) {

			parameterAliasesMap.put(getVariableRequested(getMethod),
					getAliases(getMethod));
		}

		return parameterAliasesMap;
	}

	/**
	 * @param configurationClass
	 * @return A list of get methods. The get methods returned are the ones
	 *         meant for accessing configuration parameters.
	 */
	public List<Method> getGetMethods(Class<T> configurationClass) {

		Method[] methods = configurationClass.getMethods();

		// These get methods are not about configuration parameters.
		Set<String> specialGetMethodNames = new HashSet<String>();

		// Java methods that start with get, but have nothing to do with
		// accessing parameters.
		//
		specialGetMethodNames.add("getInvocationHandler");
		specialGetMethodNames.add("getProxyClass");
		specialGetMethodNames.add("getClass");
		//
		// Special method, when called and delegated to jewelcli causes it to
		// autogenerate a help message. It doese not provide access to
		// parameters so should be ignored when dealing with them.
		//
		specialGetMethodNames.add("getHelp");

		List<Method> methodList = new ArrayList<Method>();

		for (Method method : methods) {

			String methodName = method.getName();

			boolean isGetMethodForConfiguration = methodName.startsWith("get")
					&& !(specialGetMethodNames.contains(methodName));

			if (isGetMethodForConfiguration) {
				methodList.add(method);
			}
		}
		return methodList;
	}
}
