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


package org.ensembl.healthcheck.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements connection pooling.
 */
public final class ConnectionPool {

    /** The logger to use for this class */
    private static Logger logger = Logger.getLogger("HealthCheckLogger");

    // store connections; key = database URL (as String), Connection object
    private static Map<String, Connection> pool = new HashMap<String, Connection>();

    // hide constructor to stop people instantiating this
    private ConnectionPool() { }
    
    /**
     * Get a connection from the pool. If a connection to this database already
     * exists in the pool, it is returned. If not, it is created and added to
     * the pool.
     * 
     * @return A new connection, or one re-used from the pool.
     * @param driverClassName
     *          The class of the JDBC driver.
     * @param databaseURL
     *          The URL of the database to connect to.
     * @param user
     *          The username to connect to the database with.
     * @param password
     *          The password for username.
     * @throws SQLException 
     */
    public static Connection getConnection(String driverClassName, String databaseURL, String user, String password) throws SQLException {

        Connection con = null;

        if (pool.containsKey(databaseURL)) {

		logger.finest("Got connection to " + databaseURL + " from pool");
		con = getConnectionFromPool(driverClassName, databaseURL, user, password);
		// mnuhn: Turn off connection pooling, see if this fixes the problems we are having.
		//
		//con = getConnectionByClassloader(driverClassName, databaseURL, user, password);

        } else {

        	con = getConnectionByClassloader(driverClassName, databaseURL, user, password);
        }
        
        return con;
    }

  public static boolean isValidConnection(Connection con) {

    String url;
    boolean valid = true;

    try {
      url = con.getMetaData().getURL();
    }
    catch (SQLException e1) {
      url = "(Could not get url of connection)";
    }

    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery("select 5;");
      rs.next();
      int i = rs.getInt(1);
      if (i != 5) {
        throw new RuntimeException("Got unexpected value (" + i + ") when "
            + "testing connection to " + url + ". Expected value was 5");
      }
    }
    catch (Exception e) {
      String message;
      if (e instanceof java.net.SocketException) {
        message = "Connection threw a SocketException, so the "
            + "connection probably timed out";
      }
      else {
        if (e instanceof java.sql.SQLException) {
          // The original exception thrown is an EOFException. It is
          // wrapped by a SQLException, so that is what we get here.
          message = "Connection threw a SQLException";
        }
        else {
          message = "Exception thrown was not a SocketException and not a SQLException!"
                  + "something else is going wrong";
        }
      }
      logger.fine("Connection is not valid");
      logger.log(Level.FINE, message, e);
      valid = false;
    }
    finally {
      DBUtils.closeQuietly(rs);
      DBUtils.closeQuietly(stmt);
    }
		
	  if(valid) 
	    logger.fine("Connection is valid");
		  
  	return valid;
  }
 
    public static Connection getConnectionFromPool(String driverClassName, String databaseURL, String user, String password) throws SQLException {
    	
        Connection con = (Connection) pool.get(databaseURL);
        
        boolean connectionIsValid;

        if (con.isClosed()) {
        	con = getConnectionByClassloader(driverClassName, databaseURL, user, password);
        	return con;
        }

        try {
        	
        	// Currently throws a java.lang.AbstractMethodError, but maybe
        	// someday this will work.
        	//
        	connectionIsValid = con.isValid(5000);
        	
        } catch(java.lang.AbstractMethodError e) {
        	
        	logger.finest("Connection object does not implement \"isValid()\" call. Using manual implementation");
        	connectionIsValid = isValidConnection(con);
        }
        if (!connectionIsValid) {
        	
        	logger.warning("Connection in pool was invalid. Creating again from scratch.");
        	con = getConnectionByClassloader(driverClassName, databaseURL, user, password); 
        }
        return con;
    }
    
    public static Connection getConnectionByClassloader(String driverClassName, String databaseURL, String user, String password) throws SQLException {

    	Connection con = null;
    	
        // create a connection and add it to the pool
        try {

            Class.forName(driverClassName);

        } catch (ClassNotFoundException e) {

            logger.severe("Can't load class " + driverClassName);
            throw new RuntimeException(e);

        }
       	con = DriverManager.getConnection(databaseURL, user, password);
        pool.put(databaseURL, con);
        logger.finest("Added connection to " + databaseURL + " to pool");
    	
    	return con;
    }

    // -------------------------------------------------------------------------
    /**
     * Close all the connections in the pool.
     */
    public static void closeAll() {

        Set<String> keys = pool.keySet();
        Iterator<String> it = keys.iterator();

        while (it.hasNext()) {
            try {

                Connection con = (Connection) pool.get(it.next());
                con.close();

            } catch (Exception e) {

                e.printStackTrace();

            }
        }

    } // closeAll

} // ConnectionPool
