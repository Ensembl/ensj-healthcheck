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

package org.ensembl.healthcheck;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.ensembl.healthcheck.util.DBUtils;

/**
 * Class to represent a physical database server.
 * 
 * @author glennproctor
 * 
 */
public class DatabaseServer {

	String driver;

	String databaseURL;

	String host;

	String port;

	String user;

	String pass;
	
	protected boolean connectedSuccessfully;

	public boolean isConnectedSuccessfully() {
		return connectedSuccessfully;
	}

	public void setConnectedSuccessfully(boolean connectedSuccessfully) {
		this.connectedSuccessfully = connectedSuccessfully;
	}

	Connection connection; // connection to this server, not a specific named database - use getDatabaseConnection for that

	private static Logger logger = Logger.getLogger("HealthCheckLogger");

	public DatabaseServer(String host, String port, String user, String pass, String driver) {

		this.driver = driver;

		this.host = host;

		this.port = port;

		this.user = user;

		this.pass = pass;

		this.databaseURL = buildDatabaseURL();

		try {
		
			this.connection = DBUtils.openConnection(driver, databaseURL, user, pass);
			connectedSuccessfully = true;
		
		} catch (SQLException e) {
			
			logger.warning(
				"Couldn't connect to database"
				+ " " + host 
				+ " " + port
				+ " " + user
			);
			connectedSuccessfully = false;
			
		}

	}

	// -------------------------------------------------------------------------

	public Connection getDatabaseConnection(String databaseName) throws SQLException {

		return DBUtils.openConnection(driver, databaseURL + databaseName, user, pass);

	}

	// -------------------------------------------------------------------------

	public Connection getServerConnection() throws SQLException {

		return DBUtils.openConnection(driver, databaseURL, user, pass);

	}

	// -------------------------------------------------------------------------

	private String buildDatabaseURL() {

		databaseURL = "jdbc:mysql://" + host + ":" + port + "/";

		logger.fine("Database URL: " + databaseURL);

		return databaseURL;

	}

	// -------------------------------------------------------------------------

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getDatabaseURL() {
		return databaseURL;
	}

	public void setDatabaseURL(String databaseURL) {
		this.databaseURL = databaseURL;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String toString() {

		String driverStr = driver.contains("mysql") ? "MySQL" : "Unknown";

		return driverStr + " database on " + host + ":" + port + " as " + user;

	}
}
