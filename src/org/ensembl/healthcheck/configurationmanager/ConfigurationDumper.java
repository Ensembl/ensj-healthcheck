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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import uk.co.flamingpenguin.jewel.cli.OptionNotPresentException;

/**
 * Class for dumping the parameters in configuration objects.
 *
 */
public class ConfigurationDumper<T> extends ConfigurationProcessor<T> {

	/**
	 * 
	 * This method will iterate over the getMethods defined in T and call
	 * them on the configurationObject. The results are summarised in a table
	 * which is returned as a string.
	 * 
	 * @param configurationObject
	 * @return A string summarising the contents of the configuration object.
	 * 
	 */
	public String dump(T configurationObject) {
		
		Class<T> configurationClass = (Class<T>) configurationObject.getClass();
		List<Method> methods = getGetMethods(configurationClass);

		StringBuffer out = new StringBuffer();
		for (Method method : methods) {

			out.append(String.format(
					"%1$-20s"
					, 
					method.getName().substring(3)
			) + " :   ");
			try {
				out.append(
					  "\""
					+ method.invoke(configurationObject, (Object[]) null).toString()
					+ "\""
				);
			} catch (OptionNotPresentException e) {
				// This should be thrown for every options that has not 
				// been set,
				//
				out.append("- not set -");
			} catch (InvocationTargetException e) {
				// But instead this is. How odd.
				//
				out.append("- not set -");
			} catch (IllegalArgumentException e) { throw new RuntimeException(e); }
			  catch (IllegalAccessException   e) { throw new RuntimeException(e); }

			  out.append("\n");
		}		
		return out.toString();
	}
}
