/*
  Copyright (C) 2003 EBI, GRL
 
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.
 
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNUsql
  Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.ensembl.healthcheck;

import java.util.*;
import java.util.logging.*;

/**
 * Subclass of TestRunner that lists all tests.
 */
public class ListAllTests extends TestRunner {
  
  String groupToList = "";
  
  /**
   * Creates a new instance of ListAllTests
   */
  public ListAllTests() {
  }
  
  // -------------------------------------------------------------------------
  /**
   * Command-line run method.
   * @param args The command-line arguments.
   */
  public static void main(String[] args) {
    
    ListAllTests lat = new ListAllTests();
    
    logger.setLevel(Level.OFF);
    
    lat.parseCommandLine(args);
    
    lat.readPropertiesFile();
    
    lat.listAllTests();
    
  } // main
  
  // -------------------------------------------------------------------------
  
  
  private void parseCommandLine(String[] args) {
    
    if (args.length == 0) {
      
      groupToList = "all";
      
    } else {
      
      for (int i=0; i < args.length; i++) {
        
        if (args[i].equals("-h")) {
          
          printUsage();
          System.exit(0);
          
        } else {
          
          groupToList = args[i];
          
        }
      }
      
    }
    
  } // parseCommandLine
  // -------------------------------------------------------------------------
  
  private void printUsage() {
    
    System.out.println("\nUsage: ListTests group1 ...\n");
    System.out.println("Options:");
    System.out.println("  -h         This message.");
    System.out.println("  group1     List tests that are members of group1.");
    System.out.println("");
    System.out.println("If no groups are specified, the group 'all', which contains all tests, is listed.");
    
  } // printUsage
  
  // -------------------------------------------------------------------------
  
  private void listAllTests() {
    
    System.out.println("Tests in group " + groupToList + ":");
    List tests = findAllTests();
    
    Iterator it = tests.iterator();
    while (it.hasNext()) {
      EnsTestCase test = (EnsTestCase)it.next();
      if (test.inGroup(groupToList)) {
        System.out.println(test.getShortTestName());
      }
      
    }
    
  } // listAllTests
  
  // -------------------------------------------------------------------------
  
} // ListAllTests
