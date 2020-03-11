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

package org.ensembl.healthcheck.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class Jar {
	/**
	 * Returns a list of all files in a jar file.
	 * 
	 * @param jarFileName
	 * @return List<String>
	 */
	public static List<String> findAllFilesInJar(String jarFileName) {
		
		List<String> allFiles = new ArrayList<String>(); 
		
		try {
			JarFile jarFile = new JarFile(jarFileName);

			for (Enumeration en = jarFile.entries(); en.hasMoreElements();) {

				// Returns the files in the jar file. Directories are 
				// separated as usual with slashes. 
				//
				JarEntry currentJarFileEntry = (JarEntry) en.nextElement();
				allFiles.add(currentJarFileEntry.getName());
			}
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
		return allFiles;
	}
	
	public static List<String> findAllClassesInJar(String jarFile) {
		List<String> classesInJar =
			ClassFileFilter.mapClassFilesToClassName(
				ClassFileFilter.filterForClassFiles(
					ClassFileFilter.noInnerClassFiles(
						Jar.findAllFilesInJar(
								jarFile
						)
					)
				)
			)
		;
		return classesInJar;
	}
}
