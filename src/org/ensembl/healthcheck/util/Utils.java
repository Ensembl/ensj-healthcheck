/*
 Copyright (C) 2004 EBI, GRL
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.ensembl.healthcheck.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * General utilities (not database-related). For database-related utilities,
 * see {@link DBUtils DBUtils}.
 */

public final class Utils {

    private static Logger logger = Logger.getLogger("HealthCheckLogger");

    // hide constuctor to prevent instantiation
    private Utils() {
    };

    /**
     * Read the <code>database.properties</code> file into the System
     * properties so that it can be overridden with -D.
     * 
     * @param propertiesFileName
     *          The properties file to read.
     */
    public static void readPropertiesFileIntoSystem(final String propertiesFileName) {

        String propsFile = System.getProperty("user.dir") + System.getProperty("file.separator") + propertiesFileName;
        Properties dbProps = Utils.readSimplePropertiesFile(propsFile);
        Enumeration e = dbProps.propertyNames();
        String name, value;
        while (e.hasMoreElements()) {
            name = (String) e.nextElement();
            value = dbProps.getProperty(name);
            // add to System
            System.setProperty(name, value);

        }

        // check if a databaseURL property has been specified; if so, use it
        // if not, build the databaseURL property from host, port etc

        String databaseURL = System.getProperty("databaseURL");

        if (databaseURL == null || databaseURL.equals("")) {

            // build it
            databaseURL = "jdbc:mysql://";

            if (System.getProperty("host") != null) {
                databaseURL += System.getProperty("host");
            } else {
                System.err.println("Error: host not specified in " + propertiesFileName);
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

    } // readPropertiesFile

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
            if (i < a.length) {
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
     * @return An array containing one string per path, in the order they
     *         appear in classPath.
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
     * @return The matching paths, in the order that they were in the input
     *         array.
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

        if (str.length() > size) {

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
     * (until a match is found); this is quite slow but does not require the
     * array to be sorted in any way beforehand.
     * 
     * @param str
     *          The String to search for.
     * @param a
     *          The array to search through.
     * @param caseSensitive
     *          If true, case sensitive searching is done.
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
     * Return an array containing all of the subdirectories of a given
     * directory.
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

    // -------------------------------------------------------------------------

} // Utils
