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

package org.ensembl;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * Collection of static methods for finding classes in a package.
 *
 */
public class PackageScan {
	
	/**
	 * Retrieve classes that belong to the specified package from directories and jars on the classpath. Does not include
	 * subpackages - use {@link #getClassesForPackage(String, boolean)} for this
	 * behaviour. note that this will not support zip files.
	 *
	 * @author dstaines (fixed behaviour for subclasses)
	 * @author Thanos.Panousis from
	 *         http://forums.sun.com/thread.jspa?messageID=10115467#10115467
	 * @param pckgname
	 *            name of package
	 * @return list of matching classes
	 * @throws ClassNotFoundException
	 */
	public static List<Class<?>> getClassesForPackage(String pckgname)
	throws ClassNotFoundException {
		return getClassesForPackage(pckgname, false);
	}

	/**
	 * Retrieve classes that belong to the specified package. note that this will not support zip files.
	 *
	 * @author dstaines (fixed behaviour for subclasses)
	 * @author Thanos.Panousis from
	 *         http://forums.sun.com/thread.jspa?messageID=10115467#10115467
	 * @param pckgname
	 *            name of package
	 * @param sub
	 *            if true, include subpackages
	 * @return list of matching classes
	 * @throws ClassNotFoundException
	 */
	public static List<Class<?>> getClassesForPackage(String pckgname,
			boolean sub) throws ClassNotFoundException {
		// This will hold a list of directories matching the pckgname.
		// There may be more than one if a package is split over multiple
		// jars/paths
		List<Class<?>> classes = new ArrayList<Class<?>>();
		List<File> directories = new ArrayList<File>();
		Pattern classPattern = null;
		if (sub) {
			classPattern = Pattern.compile("^" + pckgname.replace('.', '/')
					+ "[/A-z0-9_]+\\.class");
		} else {
			classPattern = Pattern.compile("^" + pckgname.replace('.', '/')
					+ "/[A-z0-9_]+\\.class");
		}
		try {
			ClassLoader cld = Thread.currentThread().getContextClassLoader();
			if (cld == null) {
				throw new ClassNotFoundException("Can't get class loader.");
			}
			// Ask for all resources for the path
			Enumeration<URL> resources = cld.getResources(pckgname.replace('.',
			'/'));
			while (resources.hasMoreElements()) {
				URL res = resources.nextElement();
				if (res.getProtocol().equalsIgnoreCase("jar")) {
					JarURLConnection conn = (JarURLConnection) res
					.openConnection();
					JarFile jar = conn.getJarFile();
					for (JarEntry e : Collections.list(jar.entries())) {
						if (classPattern.matcher(e.getName()).matches()) {
							String className = e.getName().replace("/", ".")
							.substring(0, e.getName().length() - 6);
							classes.add(Class.forName(className));
						}
					}
				} else
					directories.add(new File(URLDecoder.decode(res.getPath(),
					"UTF-8")));
			}
		} catch (NullPointerException x) {
			throw new ClassNotFoundException(pckgname
					+ " does not appear to be "
					+ "a valid package (Null pointer exception)");
		} catch (UnsupportedEncodingException encex) {
			throw new ClassNotFoundException(pckgname
					+ " does not appear to be "
					+ "a valid package (Unsupported encoding)");
		} catch (IOException ioex) {
			throw new ClassNotFoundException(
					"IOException was thrown when trying "
					+ "to get all resources for " + pckgname);
		}

		// For every directory identified capture all the .class files
		for (File directory : directories) {
			classes.addAll(getClassesForDirectory(directory, pckgname, sub));
		}
		return classes;
	}

	protected static List<Class<?>> getClassesForDirectory(File directory,
			String pckgname, boolean sub) throws ClassNotFoundException {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		if (directory.exists() && directory.isDirectory()) {
			for (File f : directory.listFiles()) {
				if (f.isDirectory() && sub) {
					classes.addAll(getClassesForDirectory(f, pckgname + "."
							+ f.getName(), sub));
				} else {
					String name = f.getName();
					if (name.matches("^[A-z0-9-]+\\.class$")) {
						// removes the .class extension
						try {
							String clazzName = pckgname + '.'
							+ name.substring(0, name.length() - 6);
							classes.add(Class.forName(clazzName));
						} catch (ClassNotFoundException e) {
							throw new ClassNotFoundException(pckgname + " ("
									+ f.getPath()
									+ ") does not appear to be a valid package");
						}
					}
				}
			}
		}
		return classes;
	}
	
}
