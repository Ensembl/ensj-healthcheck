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

/**
 * <p>Title: TestReadingClasspath.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Organisation: EMBL</p>
 * <p>Created on March 18, 2003, 9:53 AM</p>
 * @author Glenn Proctor <glenn@ebi.ac.uk>
 * @version $Revision$
 */


package org.ensembl.healthcheck.test;

import org.ensembl.healthcheck.util.*;

public class TestReadingClasspath {
  
  public static void main(String[] args) {
    
    String[] allPaths = Utils.splitClassPath(System.getProperty("java.class.path"), ":");
    System.out.println("\nSystem classpath: ");
    System.out.println(System.getProperty("java.class.path"));
    
    String[] localPaths = Utils.grepPaths(allPaths, "healthcheck");
    
    System.out.println("\nLocal paths:");
    Utils.printArray(localPaths);
    
    String[] jarFiles = Utils.grepPaths(localPaths, ".jar");
    System.out.println("\nJar files and contents:");
    if (jarFiles.length > 0) {
      for (int i = 0; i < jarFiles.length; i++) {
	System.out.println(jarFiles[i]);
	Utils.printJarFileContents(jarFiles[i]);
      }
    } else {
      System.out.println("No jar files found in classpath.");
    }
    
    System.out.println("\n");
    
  } // main
  
  // -------------------------------------------------------------------------
  
} // TestReadingClasspath
