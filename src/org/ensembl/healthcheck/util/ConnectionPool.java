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

import java.sql.*;
import java.util.*;
import java.util.logging.*;

/**
 * Implements connection pooling.
 */
public class ConnectionPool {
  
  /** The logger to use for this class */
  protected static Logger logger = Logger.getLogger("HealthCheckLogger");
  
  // store connections; key = database URL (as String), Connection object
  private static Map pool = new HashMap();
  
  /**
   * Get a connection from the pool. If a connection to this database already exists in the
   * pool, it is returned. If not, it is created and added to the pool.
   * @return A new connection, or one re-used from the pool.
   * @param driverClassName The class of the JDBC driver.
   * @param databaseURL The URL of the database to connect to.
   * @param user The username to connect to the database with.
   * @param password The password for username.
   */
  public static Connection getConnection(String driverClassName, String databaseURL, String user, String password) {
    
    Connection con = null;
    
    if (pool.containsKey(databaseURL)) {
      
      logger.finest("Got connection to " + databaseURL + " from pool");
      con = (Connection)pool.get(databaseURL);
      
    } else {
      
      // create a connection and add it to the pool
      try {
        
        Class.forName(driverClassName);
        con = DriverManager.getConnection(databaseURL, user, password);
        pool.put(databaseURL, con);
        logger.finest("Added connection to " + databaseURL + " to pool");
        
      } catch (Exception e) {
        
        e.printStackTrace();
        
      }
    }
    
    return con;
    
  } // getConnection
  
  // -------------------------------------------------------------------------
  /**
   * Close all the connections in the pool.
   */
  public static void closeAll() {
    
    Set keys = pool.keySet();
    Iterator it = keys.iterator();
    
    while (it.hasNext()) {
      try {
        
        Connection con = (Connection)pool.get(it.next());
        con.close();
        
      } catch (Exception e) {
        
        e.printStackTrace();
        
      }
    }
    
  } // closeAll
  
} // ConnectionPool
