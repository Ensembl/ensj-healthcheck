/*
  Copyright (C) 2003 EBI, GRL
 
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

import java.io.*;
import java.util.*;
import java.util.jar.*;

/**
 * General utilities (not database-related).
 * For database-related utilities, see {@link DBUtils DBUtils}.
 */

public class Utils {
  
  // -------------------------------------------------------------------------
  /**
   * Read a properties file.
   * @param propertiesFileName The name of the properties file to use.
   * @return The Properties hashtable.
   */
  public static Properties readPropertiesFile(String propertiesFileName) {
    
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
   * @param l The List to be printed.
   */
  public static void printList(List l) {
    
    Iterator it = l.iterator();
    while (it.hasNext()) {
      System.out.println((String)it.next());
    }
    
  } // printList
  
  // -------------------------------------------------------------------------
  /** Concatenate a list of Strings into a single String.
   * @param list The Strings to list.
   * @param delim The delimiter to use.
   * @return A String containing the elements of list separated by delim. No trailing delimiter.
   */
  public static String listToString(List list, String delim) {
    
    StringBuffer buf = new StringBuffer();
    Iterator it = list.iterator();
    while (it.hasNext()) {
      buf.append((String)it.next());
      if (it.hasNext()) {
        buf.append(delim);
      }
    }
    
    return buf.toString();
    
  }
  
  // -------------------------------------------------------------------------
  /**
   * Print the keys in a HashMap.
   * @param m The map to use.
   */
  public static void printKeys(Map m) {
    
    Set s = m.keySet();
    Iterator it = s.iterator();
    while (it.hasNext()) {
      System.out.println((String)it.next());
    }
    
  } // printKeys
  
  // -------------------------------------------------------------------------
  /**
   * Print an array of Strings, one per line.
   * @param a The array to be printed.
   */
  public static void printArray(String[] a) {
    
    for (int i = 0; i < a.length; i++) {
      System.out.println(a[i]);
    }
    
  } // printArray
  
  // -------------------------------------------------------------------------
  /** Print an Enumeration, one String per line.
   * @param e The enumeration to be printed.
   */
  public static void printEnumeration(Enumeration e) {
    
    while (e.hasMoreElements()) {
      System.out.println(e.nextElement());
    }
    
  } // printEnumeration
  
  // -------------------------------------------------------------------------
  /**
   * Split a classpath-like string into a list of constituent paths.
   * @param classPath The String to split.
   * @param delim FileSystem classpath delimiter.
   * @return An array containing one string per path, in the order they appear in classPath.
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
   * @param paths The List to search.
   * @param pattern The pattern to look for.
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
   * @param path The path to the jar file.
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
   * @param str The string to truncate.
   * @param size The maximum number of characters.
   * @param useEllipsis If true, add "..." to the truncated string to show it's been truncated.
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
   * @return The padded String.
   * @param size The desired length of the final, padded string.
   * @param str The String to add the padding to.
   * @param pad The String to pad with.
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
  
} // Utils