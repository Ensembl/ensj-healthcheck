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

import java.util.*;
import java.util.logging.*;
import java.sql.*;

import org.ensembl.healthcheck.*;

/**
 * Implentation of the Iterator interface that facilitates getting database
 * connections to a number of databases, given their names.
 */

public class DatabaseConnectionIterator implements Iterator {
  
  private int databaseIndex;
  private String driverClassName, baseURL, user, password;
  private String[] databaseNames;
  
  private static Logger logger = Logger.getLogger("HealthCheckLogger");

  // -------------------------------------------------------------------------
  
  /** 
   * Creates a new instance of DatabaseConnectionIterator 
   * @param driverClassName The class name of the database driver to load.
   * @param baseURL The base URL to use (does not include database name), e.g. jdbc:mysql://127.0.0.1:5000/
   * @param user Username.
   * @param password Password for user, if any
   * @param databaseNames An array of names of databases to iterate over.
   */
  public DatabaseConnectionIterator(String driverClassName, String baseURL, String user, String password, String[] databaseNames) {
  
    this.driverClassName = driverClassName;
    this.baseURL = baseURL;
    this.user = user;
    this.password = password;
    this.databaseNames = databaseNames;
    
    databaseIndex = 0;
    
  }
  
  // -------------------------------------------------------------------------
  /**
   * Check if there are any more connections to return.
   * @return true If there are any more connections after the current one.
   */
  public boolean hasNext() {
    
    return (databaseIndex < databaseNames.length);

  } // hasNext
  
  // -------------------------------------------------------------------------
  /**
   * Return the next Connection (as an Object) if there is one.
   * @return The next object.
   * @throws NoSuchElementException if there is no next object - use hasNext() to check this.
   */
  public Object next() throws NoSuchElementException {
    
    Connection con;
    
    if (hasNext()) {
      
      String databaseURL = baseURL + databaseNames[databaseIndex];
      logger.fine("Trying to open connection to " + databaseURL);
      con = DBUtils.openConnection(driverClassName, databaseURL, user, password);
      
      databaseIndex++;
    
    } else {
     
      throw new NoSuchElementException();
      
    }
    return con;
    
  } // next
  
  // -------------------------------------------------------------------------
  /**
   * Return the name of the database name of the current object.
   * @return The name.
   */
  public String getCurrentDatabaseName() {
    
    return databaseNames[databaseIndex-1];
    
  } // getCurrentDatabaseName

  // -------------------------------------------------------------------------
  /**
   * Remove the current connection from the list. Currently not implemented.
   */
  public void remove() {
     
     throw new NotImplementedException("remove() method is not implemented for " + this.getClass().getName());
     
  }
  
  // -------------------------------------------------------------------------
  
} // DatabaseConnectionIterator
