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
