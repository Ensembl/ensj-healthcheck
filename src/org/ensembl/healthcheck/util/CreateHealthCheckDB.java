/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2020] EMBL-European Bioinformatics Institute
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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.io.*;
import org.ensembl.healthcheck.ConfigurableTestRunner;
import org.ensembl.healthcheck.Debug;
import org.ensembl.healthcheck.configuration.ConfigureHealthcheckDatabase;

/**
 * Class used to create an empty database with the healthcheck schema. 
 * Location and name of the database to be created is passed as a 
 * configuration object to the constructor.
 *
 */
public class CreateHealthCheckDB {
	
	static final Logger log = Logger.getLogger(CreateHealthCheckDB.class.getCanonicalName());
	
	private final ConfigureHealthcheckDatabase conf;
	
	public CreateHealthCheckDB(ConfigureHealthcheckDatabase conf) {
		this.conf = conf;
		try {
			Class.forName(conf.getOutputDriver());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Could not load database driver: " + conf.getOutputDriver(), e);
		}
	}
	
	public String getDbUrl() {
		
		String dbUrl = "jdbc:mysql://"
			+ conf.getOutputHost()
			+ ":"
			+ conf.getOutputPort()
			+ "/";
		return dbUrl;
	}

	public Set<String> getDatabasesOnServer() {
		
		Connection con = connectToDb(getDbUrl());

		Set databasesOnServer = new HashSet<String>();
		
		try {
	        
	        DatabaseMetaData metadata = con.getMetaData();
	        ResultSet catalogs = metadata.getCatalogs();
	        ResultSetMetaData rsmd = catalogs.getMetaData();
	        
	        int cols = rsmd.getColumnCount();
	        while(catalogs.next()) {
	           for (int i = 1; i <= cols; i++) {
	              //System.out.println(catalogs.getString(i));
	        	   databasesOnServer.add(catalogs.getString(i));
	           }
	        }
		} catch(SQLException e) {
			throw new RuntimeException("Could not get databases on Server! " + conf, e);
		}

        return databasesOnServer;
	}
	
	public Connection connectToDb(String dbUrl) {
		
		Connection con;
		try {
			con = DriverManager.getConnection(
					dbUrl, 
					conf.getOutputUser(), 
					conf.getOutputPassword()
			);
		} catch (SQLException e) {
			
			throw new RuntimeException("Could not connect to database! " + conf.toString(), e);
		}
		return con;
	}
	
	/**
	 * Checks, if a database exists with the name passed as an argument.
	 * 
	 * @param nameOfDatabase
	 * @return true if exists
	 * 
	 */
	public boolean databaseExists(String nameOfDatabase) {
		
		Set<String> databasesOnServer = getDatabasesOnServer();
		return databasesOnServer.contains(nameOfDatabase);
	}
	
	public void dropDatabase(Connection con, String dbName) {
		
		try {
			Statement stmt = con.createStatement();
			log.info("Dropping database " + dbName);        	  
			stmt.execute("drop   database if exists " + dbName + ";");
		} catch (SQLException e) {
			
			throw new RuntimeException("Error dropping database " + dbName, e);
		}
	}
	
	public void run() {
		
		try {
			String dbUrl = getDbUrl(); 
			
			log.info("Connecting to " + dbUrl + " as " + conf.getOutputUser());
			
			Connection con = connectToDb(dbUrl);
			
          Statement stmt = con.createStatement();
          
          dropDatabase(con, conf.getOutputDatabase());
    	  //log.info("Dropping database " + conf.getOutputDatabase());        	  
          //stmt.execute("drop   database if exists " + conf.getOutputDatabase() + ";");
          
          log.info("Creating database " + conf.getOutputDatabase());	          
          stmt.execute("create database "           + conf.getOutputDatabase() + ";");
          
          stmt.execute("use "                       + conf.getOutputDatabase() + ";");

          log.info("Running " + conf.getOutputSchemafile() + " to create schema.");
          
          ScriptRunner s = new ScriptRunner(con, true, true);
          
          Reader r;
          
          try {
        	  // Try to find the file on the filesystem
        	  r = new FileReader(conf.getOutputSchemafile());
          } catch (FileNotFoundException e) {
        	  
        	  try {
	        	  // if that does not work, try to find the file on the classpath.
	        	  r = new BufferedReader(
	        			  new InputStreamReader(
	        					  getClass().getClassLoader().getResourceAsStream(
	        							 conf.getOutputSchemafile()
	        					  )
	        			  )
	        	  );
        	  }

        	  // If the file can't be found, the previous statement will throw
        	  // a NullPointerException. This is because getResourceAsStream 
        	  // will return null and the InputStreamReader will then throw 
        	  // the exception.
        	  //
        	  catch (NullPointerException n) {
        		  
        		  throw new FileNotFoundException("Couldn't find "
        				+ conf.getOutputSchemafile()
        				+ " from the current location and in the classpath which is:\n\n"
        				+ Debug.classpathToString()
        		  );  
        	  }
          }
          s.runScript(r);
		}
		catch (SQLException e)           { throw new RuntimeException(e); } 
		catch (FileNotFoundException e)  { throw new RuntimeException(e); }
		catch (IOException e)            { throw new RuntimeException(e); }
	}
}
