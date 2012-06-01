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

import java.io.EOFException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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
     * @throws ClassNotFoundException 
     * @throws SQLException 
     */
    public static Connection getConnection(String driverClassName, String databaseURL, String user, String password) throws SQLException {

        Connection con = null;

        if (pool.containsKey(databaseURL)) {

            logger.finest("Got connection to " + databaseURL + " from pool");
            con = getConnectionFromPool(driverClassName, databaseURL, user, password);

        } else {

        	try {
        		con = getConnectionByClassloader(driverClassName, databaseURL, user, password);
        		
        	} catch(AbstractMethodError e) {
        		logger.finest("Got connection to " + databaseURL + " from pool");
        	}
        }
        
        return con;
    }
    
    protected static boolean isValidConnection(Connection con) {
    	
		try {
			
			Statement stmt = con.createStatement();
			stmt.executeQuery("select NOW();");
			
		} catch (Exception e) {
			
			// Trying to catch java.io.EOFException here, but java doesn't
			// allow this. The compile claims this exception never gets thrown
			// from the statements in the try block, but stack traces indicate
			// otherwise.
			//
			return false;
		}
		
    	return true;
    }
    
    public static Connection getConnectionFromPool(String driverClassName, String databaseURL, String user, String password) throws SQLException {
    	
        Connection con = (Connection) pool.get(databaseURL);
        
        boolean connectionIsValid;
        
        try {
        	
        	// Currently throws a java.lang.AbstractMethodError, but maybe
        	// someday this will work.
        	//
        	connectionIsValid = con.isValid(5000);
        	
        } catch(java.lang.AbstractMethodError e) {
        	
        	logger.info("Connection object doesn't implement \"isValid call, using manual implementation.\"");
        	connectionIsValid = isValidConnection(con);
        }
        
        if (connectionIsValid) {
        	
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
