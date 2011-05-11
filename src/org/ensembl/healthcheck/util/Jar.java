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
			FileNameFilter.mapClassFilesToClassName(
				FileNameFilter.filterForClassFiles(
					FileNameFilter.noInnerClassFiles(
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
