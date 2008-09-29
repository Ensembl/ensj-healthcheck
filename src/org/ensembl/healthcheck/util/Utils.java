/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarFile;

/**
 * General utilities (not database-related). For database-related utilities, see
 * {@link DBUtils DBUtils}.
 */

public final class Utils {

	// hide constuctor to prevent instantiation
	private Utils() {

	}

	/**
	 * Read the <code>database.properties</code> file into the System properties
	 * so that it can be overridden with -D.
	 * 
	 * @param propertiesFileName
	 *          The properties file to read.
	 * @param skipBuildDatabaseURLs
	 *        Don't automatically build database URLs. Should generally be false.         
	 */
	public static void readPropertiesFileIntoSystem(final String propertiesFileName, final boolean skipBuildDatabaseURLs) {

		String propsFile;

		// Prepend home directory if not absolute path
		if (propertiesFileName.indexOf(File.separator) == -1) {
			propsFile = System.getProperty("user.dir") + File.separator + propertiesFileName;
		} else {
			propsFile = propertiesFileName;
		}

		Properties dbProps = Utils.readSimplePropertiesFile(propsFile);
		Enumeration e = dbProps.propertyNames();
		String name, value;
		while (e.hasMoreElements()) {
			name = (String) e.nextElement();
			value = dbProps.getProperty(name);
			// add to System
			System.setProperty(name, value);

		}

		// check that properties that need to be set are set
		String[] requiredProps = { "port", "host", "user" };
		for (int i = 0; i < requiredProps.length; i++) {
			if (System.getProperty(requiredProps[i]) == null) {
				System.err.println("WARNING: " + requiredProps[i] + " is not set in " + propertiesFileName
						+ " - cannot connect to database");
				System.exit(1);
			}
		}

		if (!skipBuildDatabaseURLs) {
			
			buildDatabaseURLs();
			
		}
		
	} // readPropertiesFile
	

	/**
	 * Build database URLs from system properties.
	 * 
	 * @param propertiesFileName
	 *          The properties file to read.
	 */
	public static void buildDatabaseURLs() {
		
		// check if a databaseURL property has been specified; if so, use it
		// if not, build the databaseURL property from host, port etc

		String databaseURL = System.getProperty("databaseURL");

		if (databaseURL == null || databaseURL.equals("")) {

			// build it
			databaseURL = "jdbc:mysql://";

			if (System.getProperty("host") != null) {
				databaseURL += System.getProperty("host");
			}

			if (System.getProperty("port") != null) {
				databaseURL += ":" + System.getProperty("port");
			}

			databaseURL += "/";
			System.setProperty("databaseURL", databaseURL);

		} else {

			// validate database URL - if it doesn't start with jdbc: this can
			// cause confusion
			String prefix = databaseURL.substring(0, 5);
			if (!prefix.equalsIgnoreCase("jdbc:")) {
				System.err
						.println("WARNING - databaseURL property should start with jdbc: but it does not seem to. Check this if you experience problems loading the database driver");
			}
		}

		// similarly for secondary database URL

		String secondaryDatabaseURL = System.getProperty("secondary.databaseURL");

		if (secondaryDatabaseURL == null || secondaryDatabaseURL.equals("")) {

			// build it
			secondaryDatabaseURL = "jdbc:mysql://";

			if (System.getProperty("secondary.host") != null) {
				secondaryDatabaseURL += System.getProperty("secondary.host");
			}

			if (System.getProperty("secondary.port") != null) {
				secondaryDatabaseURL += ":" + System.getProperty("secondary.port");
			}

			secondaryDatabaseURL += "/";
			System.setProperty("secondary.databaseURL", secondaryDatabaseURL);

		} else {

			// validate secondary database URL - if it doesn't start with jdbc: this
			// can
			// cause confusion
			String prefix = secondaryDatabaseURL.substring(0, 5);
			if (!prefix.equalsIgnoreCase("jdbc:")) {
				System.err
						.println("WARNING - secondary.databaseURL property should start with jdbc: but it does not seem to. Check this if you experience problems loading the database driver");
			}
		}

		// ... and for output database URL

		String outputDatabaseURL = System.getProperty("output.databaseURL");

		if (outputDatabaseURL == null || outputDatabaseURL.equals("")) {

			// build it
			outputDatabaseURL = "jdbc:mysql://";

			if (System.getProperty("output.host") != null) {
				outputDatabaseURL += System.getProperty("output.host");
			}

			if (System.getProperty("output.port") != null) {
				outputDatabaseURL += ":" + System.getProperty("output.port");
			}

			outputDatabaseURL += "/";
			System.setProperty("output.databaseURL", outputDatabaseURL);

		} else {

			// validate output database URL - if it doesn't start with jdbc: this
			// can
			// cause confusion
			String prefix = outputDatabaseURL.substring(0, 5);
			if (!prefix.equalsIgnoreCase("jdbc:")) {
				System.err
						.println("WARNING - output.databaseURL property should start with jdbc: but it does not seem to. Check this if you experience problems loading the database driver");
			}
		}

		
	}
	// -------------------------------------------------------------------------
	/**
	 * Read a properties file.
	 * 
	 * @param propertiesFileName
	 *          The name of the properties file to use.
	 * @return The Properties hashtable.
	 */
	public static Properties readSimplePropertiesFile(String propertiesFileName) {

		Properties props = new Properties();

		try {

			FileInputStream in = new FileInputStream(propertiesFileName);
			props.load(in);
			in.close();

		} catch (Exception e) {

			e.printStackTrace();
			System.exit(1);

		}

		return props;

	} // readPropertiesFile

	// -------------------------------------------------------------------------
	/**
	 * Print a list of Strings, one per line.
	 * 
	 * @param l
	 *          The List to be printed.
	 */
	public static void printList(List l) {

		Iterator it = l.iterator();
		while (it.hasNext()) {
			System.out.println((String) it.next());
		}

	} // printList

	// -------------------------------------------------------------------------
	/**
	 * Concatenate a list of Strings into a single String.
	 * 
	 * @param list
	 *          The Strings to list.
	 * @param delim
	 *          The delimiter to use.
	 * @return A String containing the elements of list separated by delim. No
	 *         trailing delimiter.
	 */
	public static String listToString(List list, String delim) {

		StringBuffer buf = new StringBuffer();
		Iterator it = list.iterator();
		while (it.hasNext()) {
			buf.append((String) it.next());
			if (it.hasNext()) {
				buf.append(delim);
			}
		}

		return buf.toString();

	}

	// -------------------------------------------------------------------------
	/**
	 * Concatenate an array of Strings into a single String.
	 * 
	 * @param a
	 *          The Strings to list.
	 * @param delim
	 *          The delimiter to use.
	 * @return A String containing the elements of a separated by delim. No
	 *         trailing delimiter.
	 */
	public static String arrayToString(String[] a, String delim) {

		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < a.length; i++) {
			buf.append(a[i]);
			if (i + 1 < a.length) {
				buf.append(delim);
			}
		}

		return buf.toString();

	}

	// -------------------------------------------------------------------------
	/**
	 * Print the keys in a HashMap.
	 * 
	 * @param m
	 *          The map to use.
	 */
	public static void printKeys(Map m) {

		Set s = m.keySet();
		Iterator it = s.iterator();
		while (it.hasNext()) {
			System.out.println((String) it.next());
		}

	} // printKeys

	// -------------------------------------------------------------------------
	/**
	 * Print an array of Strings, one per line.
	 * 
	 * @param a
	 *          The array to be printed.
	 */
	public static void printArray(String[] a) {

		for (int i = 0; i < a.length; i++) {
			System.out.println(a[i]);
		}

	} // printArray

	// -------------------------------------------------------------------------
	/**
	 * Print an Enumeration, one String per line.
	 * 
	 * @param e
	 *          The enumeration to be printed.
	 */
	public static void printEnumeration(Enumeration e) {

		while (e.hasMoreElements()) {
			System.out.println(e.nextElement());
		}

	} // printEnumeration

	// -------------------------------------------------------------------------
	/**
	 * Split a classpath-like string into a list of constituent paths.
	 * 
	 * @param classPath
	 *          The String to split.
	 * @param delim
	 *          FileSystem classpath delimiter.
	 * @return An array containing one string per path, in the order they appear
	 *         in classPath.
	 */
	public static String[] splitClassPath(String classPath, String delim) {

		StringTokenizer tok = new StringTokenizer(classPath, delim);
		String[] paths = new String[tok.countTokens()];

		int i = 0;
		while (tok.hasMoreElements()) {
			paths[i++] = tok.nextToken();
		}

		return paths;

	} // splitClassPath

	// -------------------------------------------------------------------------
	/**
	 * Search an array of strings for those that contain a pattern.
	 * 
	 * @param paths
	 *          The List to search.
	 * @param pattern
	 *          The pattern to look for.
	 * @return The matching paths, in the order that they were in the input array.
	 */
	public static String[] grepPaths(String[] paths, String pattern) {

		int count = 0;
		for (int i = 0; i < paths.length; i++) {
			if (paths[i].indexOf(pattern) > -1) {
				count++;
			}
		}

		String[] greppedPaths = new String[count];
		int j = 0;
		for (int i = 0; i < paths.length; i++) {
			if (paths[i].indexOf(pattern) > -1) {
				greppedPaths[j++] = paths[i];
			}
		}

		return greppedPaths;

	} // grepPaths

	// -------------------------------------------------------------------------
	/**
	 * Print the contents of a jar file.
	 * 
	 * @param path
	 *          The path to the jar file.
	 */
	public static void printJarFileContents(String path) {

		try {

			JarFile f = new JarFile(path);
			printEnumeration(f.entries());

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

	} // printJarFileContents

	// -------------------------------------------------------------------------
	/**
	 * Truncate a string to a certain number of characters.
	 * 
	 * @param str
	 *          The string to truncate.
	 * @param size
	 *          The maximum number of characters.
	 * @param useEllipsis
	 *          If true, add "..." to the truncated string to show it's been
	 *          truncated.
	 * @return The truncated String, with ellipsis if specified.
	 */
	public static String truncate(String str, int size, boolean useEllipsis) {

		String result = str;

		if (str != null && str.length() > size) {

			result = str.substring(0, size);

			if (useEllipsis) {
				result += "...";
			}
		}

		return result;

	} // truncate

	// -------------------------------------------------------------------------
	/**
	 * Pad (on the right) a string with a certain number of characters.
	 * 
	 * @return The padded String.
	 * @param size
	 *          The desired length of the final, padded string.
	 * @param str
	 *          The String to add the padding to.
	 * @param pad
	 *          The String to pad with.
	 */
	public static String pad(String str, String pad, int size) {

		StringBuffer result = new StringBuffer(str);

		int startSize = str.length();
		for (int i = startSize; i < size; i++) {
			result.append(pad);
		}

		return result.toString();

	} // pad

	// -------------------------------------------------------------------------
	/**
	 * Read a text file.
	 * 
	 * @param name
	 *          The name of the file to read.
	 * @return An array of Strings representing the lines of the file.
	 */
	public static String[] readTextFile(String name) {

		List lines = new ArrayList();

		BufferedReader br = null;

		try {

			br = new BufferedReader(new FileReader(name));

		} catch (FileNotFoundException fe) {

			System.err.println("Cannot find " + name);
			fe.printStackTrace();

		}

		String line;

		try {

			while ((line = br.readLine()) != null) {

				lines.add(line);

			}

			br.close();

		} catch (Exception e) {

			e.printStackTrace();

		}

		return (String[]) lines.toArray(new String[lines.size()]);

	} // readTextFile

	// -------------------------------------------------------------------------
	/**
	 * Check if a String is in an array of Strings. The whole array is searched
	 * (until a match is found); this is quite slow but does not require the array
	 * to be sorted in any way beforehand.
	 * 
	 * @param str
	 *          The String to search for.
	 * @param a
	 *          The array to search through.
	 * @param caseSensitive
	 *          If true, case sensitive searching is done.
	 * @return true if str is in a.
	 */
	public static boolean stringInArray(String str, String[] a, boolean caseSensitive) {

		boolean result = false;

		for (int i = 0; i < a.length; i++) {

			if (caseSensitive) {
				if (a[i].equals(str)) {
					result = true;
					break;
				}
			} else {
				if (a[i].equalsIgnoreCase(str)) {
					result = true;
					break;
				}
			}

		}

		return result;

	}

	// -------------------------------------------------------------------------
	/**
	 * Check if an object is in an array. The whole array is searched (until a
	 * match is found); this is quite slow but does not require the array to be
	 * sorted in any way beforehand.
	 * 
	 * @param o
	 *          The Object to search for.
	 * @param a
	 *          The array to search through.
	 * @return true if o is in a.
	 */
	public static boolean objectInArray(Object o, Object[] a) {

		for (int i = 0; i < a.length; i++) {

			if (a[i].equals(o)) {
				return true;
			}

		}

		return false;

	}

	// -----------------------------------------------------------------
	/**
	 * Return an array containing all of the subdirectories of a given directory.
	 * 
	 * @param parentDir
	 *          The directory to look in.
	 * @return All the subdirectories (if any) in parentDir.
	 */
	public static String[] getSubDirs(String parentDir) {

		List dirs = new ArrayList();

		File parentDirFile = new File(parentDir);

		String[] filesAndDirs = parentDirFile.list();

		if (filesAndDirs != null) {

			for (int i = 0; i < filesAndDirs.length; i++) {
				File f = new File(parentDir + File.separator + filesAndDirs[i]);
				if (f.isDirectory()) {
					dirs.add(filesAndDirs[i]);
				}

			}

		}

		return (String[]) (dirs.toArray(new String[dirs.size()]));

	}

	// -----------------------------------------------------------------
	/**
	 * Remove the objects from one array that are present in another.
	 * 
	 * @param source
	 *          The array to be filtered.
	 * @param remove
	 *          An array of objects to be removed from source.
	 * @return A new array containing all objects that are in source minus any
	 *         that are in remove.
	 */
	public static Object[] filterArray(Object[] source, Object[] remove) {

		List result = new ArrayList();

		for (int i = 0; i < source.length; i++) {

			if (!objectInArray(source[i], remove)) {
				result.add(source[i]);
			}
		}

		return result.toArray(new Object[result.size()]);

	}

	// ---------------------------------------------------------------------
	/**
	 * Format a time as hours, minutes and seconds.
	 * 
	 * @param time
	 *          The time in ms, e.g. from System.currentTimeMillis()
	 * @return The time formatted as e.g. 4 hours 2 min 3s. Hours is largest unit
	 *         currently supported.
	 */
	public static String formatTimeString(long time) {

		String s = "";

		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(time);

		// TODO years etc
		// Calendar.HOUR starts from 1
		if (cal.get(Calendar.HOUR_OF_DAY) > 1) {
			s += (cal.get(Calendar.HOUR_OF_DAY) - 1) + " hours ";
		}
		if (cal.get(Calendar.MINUTE) > 0) {
			s += cal.get(Calendar.MINUTE) + " min ";
		}
		if (cal.get(Calendar.SECOND) > 0) {
			s += cal.get(Calendar.SECOND) + "s ";
		}

		if (time < 1000) {
			s = time + "ms";
		}

		return s;

	}

	// -------------------------------------------------------------------------
	/**
	 * Delete a file.
	 * 
	 * @param file
	 *          The file to delete.
	 */
	public static void deleteFile(String file) {

		File f = new File(file);
		f.delete();

	}

	// -------------------------------------------------------------------------
	/**
	 * Write/append a string to a file.
	 * 
	 * @param file
	 *          The file to write to.
	 * @param str
	 *          The string to write.
	 * @param append
	 *          If true, append the string to the file if it already exists.
	 * @param newLine
	 *          If true, add a new line character after writing
	 */
	public static void writeStringToFile(String file, String str, boolean append, boolean newLine) {

		try {

			FileWriter fw = new FileWriter(file, append);
			fw.write(str);
			if (newLine) {
				fw.write("\n");
			}
			fw.close();

		} catch (IOException ioe) {

			ioe.printStackTrace();

		}

	}

	// -------------------------------------------------------------------------
	/**
	 * Convert a list of Longs to an array of longs.
	 */
	public static long[] listToArrayLong(List list) {

		long[] array = new long[list.size()];

		Iterator it = list.iterator();
		int i = 0;
		while (it.hasNext()) {

			array[i++] = ((Long) it.next()).longValue();

		}

		return array;

	}

	// -------------------------------------------------------------------------
	/**
	 * Convert the first character of a string to upper case. Ignore the rest of
	 * the string.
	 */
	public static String ucFirst(String str) {

		String first = str.substring(0, 1).toUpperCase();

		String last = str.substring(1);

		return first + last;

	}

	public static String truncateDatabaseName(String db) {

		if (db.length() <= 27) {
			return db;
		}

		// change genus to single letter
		int underscoreIndex = db.indexOf("_");
		String genusLetter = db.substring(0, 1);

		String result = genusLetter + "_" + db.substring(underscoreIndex + 1);

		// if length is *still* > 27, change long database type to an abbreviated
		// version
		if (result.length() > 27) {
			result = result.replaceAll("otherfeatures", "other...");
		}

		return result;

	}

	// ---------------------------------------------------------------------

	public static String truncateTestName(String test) {

		return (test.length() <= 27) ? test : test.substring(0, 28);

	}

	// ---------------------------------------------------------------------

	public static String[] removeStringFromArray(String[] tables, String table) {

		String[] result = new String[tables.length - 1];
		int j = 0;
		for (int i = 0; i < tables.length; i++) {
			if (!tables[i].equalsIgnoreCase(table)) {
				if (j < result.length) {
					result[j++] = tables[i];
				} 
			}
		}

		return result;

	}

	// -------------------------------------------------------------------------

} // Utils
