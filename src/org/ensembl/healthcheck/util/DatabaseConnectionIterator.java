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
 * <p>Title: DatabaseConnectionIterator.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Organisation: EMBL</p>
 * <p>Created on March 12, 2003, 3:25 PM</p>
 * @author Glenn Proctor <glenn@ebi.ac.uk>
 * @version $Revision$
 */


package org.ensembl.healthcheck.util;

import java.util.*;
import java.util.logging.*;
import java.sql.*;

public class DatabaseConnectionIterator implements Iterator {
  
  private int databaseIndex;
  private String driverClassName, baseURL, user, password;
  private String[] databaseNames;
  
  private static Logger logger = Logger.getLogger("HealthCheckLogger");

  // -------------------------------------------------------------------------
  
  /** Creates a new instance of DatabaseConnectionIterator */
  public DatabaseConnectionIterator(String driverClassName, String baseURL, String user, String password, String[] databaseNames) {
  
    this.driverClassName = driverClassName;
    this.baseURL = baseURL;
    this.user = user;
    this.password = password;
    this.databaseNames = databaseNames;
    
    databaseIndex = 0;
    
  }
  
  // -------------------------------------------------------------------------
  
  public boolean hasNext() {
    
    return (databaseIndex < databaseNames.length);

  } // hasNext
  
  // -------------------------------------------------------------------------
  
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
  
  public String getCurrentDatabaseName() {
    
    return databaseNames[databaseIndex-1];
    
  } // getCurrentDatabaseName
  // -------------------------------------------------------------------------
  
  public void remove() {
     // not implemented
  }
  
  // -------------------------------------------------------------------------
  
} // DatabaseConnectionIterator
