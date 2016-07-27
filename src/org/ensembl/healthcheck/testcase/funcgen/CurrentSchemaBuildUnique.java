package org.ensembl.healthcheck.testcase.funcgen;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

public class CurrentSchemaBuildUnique extends SingleDatabaseTestCase {

	protected Connection con;
	
	@Override
	public boolean run(DatabaseRegistryEntry dbre) {
		
		boolean passes = true;
		
		con = dbre.getConnection();
		int countCurrentCoordSystems;
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("select group_concat(distinct schema_build) current_schema_build, count(distinct schema_build) as c from coord_system where is_current=true");
			rs.next();
			countCurrentCoordSystems = rs.getInt("c");
			if (countCurrentCoordSystems>1) {
				
				String current_schema_build = rs.getString("current_schema_build");
				
				ReportManager.problem(this, con, "There is more than one schema_build flagged as current.");
				ReportManager.problem(this, con, "The following schema builds are marked as current: " + current_schema_build);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		passes = countCurrentCoordSystems==1;
		if (!passes) {
			ReportManager.problem(this, con, "Useful SQL: select * from coord_system where is_current=true");
		}
		return passes;
	}
}
