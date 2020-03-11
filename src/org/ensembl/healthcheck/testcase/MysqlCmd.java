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

package org.ensembl.healthcheck.testcase;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.ensembl.healthcheck.DatabaseRegistryEntry;

/**
 * <p>
 * 	Abstract class providing methods for subclasses used to assess what the 
 * data looks like.
 * </p>
 * 
 * <p>
 * 	A subclass only has to set the sql and the description attribute.
 * </p>
 * 
 * <p>
 * 	The mysql client is the run to execute the query and the table returned
 * will appear in the report.
 * </p>
 * 
 * @author mnuhn
 *
 */
public abstract class MysqlCmd extends AbstractShellBasedTestCase {

	/**
	 * This must be set by the subclass to the sql that will be executed.
	 */
	protected String sql;

	/**
	 * The command to call the mysql binary
	 */
	protected String mysqlCmd = "mysql";
	
	@Override
	protected String createCommandLine(DatabaseRegistryEntry dbre) {
		
		String cmdLine = 
			mysqlCmd
			+ " --host="     + dbre.getDatabaseServer().getHost().trim()
			+ " --port="     + dbre.getDatabaseServer().getPort().trim()
			+ " --user="     + dbre.getDatabaseServer().getUser().trim()
			+ " --password=" + dbre.getDatabaseServer().getPass().trim()
			+ " " + dbre.getName()
			+ " -t"
			+ " -e"
			+ " \"" + sql + "\""
		;

		return cmdLine;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public String getMysqlCmd() {
		return mysqlCmd;
	}

	public void setMysqlCmd(String mysqlCmd) {
		this.mysqlCmd = mysqlCmd;
	}
}
