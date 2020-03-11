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

package org.ensembl.healthcheck;

import java.net.URL;
import java.net.URLClassLoader;

public class Debug {

	public static String classpathToString() {
		//Get the System Classloader
        ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();

        // The code below only work with the interface provided by
        // sun.misc.Launcher$AppClassLoader. OpenJDK since v9 returns a
        // jdk.internal.loader.ClassLoaders$AppClassLoader
        // Instead we can print the current CLASSPATH. It's not exactly the
        // same format, but close enough when debugging
        if (sysClassLoader.getClass().getName().startsWith("jdk.internal.loader")) {
            return System.getProperty("java.class.path").replace(":", "\n");
        }

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
