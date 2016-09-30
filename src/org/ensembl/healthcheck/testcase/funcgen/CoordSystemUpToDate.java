package org.ensembl.healthcheck.testcase.funcgen;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;

/**
 * @author mnuhn
 * 
 * Checks, whether the coord system that is set to current in the funcgen 
 * database is the default version in the corresponding core database.
 *
 */
public class CoordSystemUpToDate extends AbstractCoreDatabaseUsingTestCase {
	
	protected List<CoordSystemEntry> fetchCurrentCoreCoordSystem(DatabaseRegistryEntry dbre) throws SQLException {
		Statement stmt = dbre.getConnection().createStatement();
		ResultSet rs = stmt.executeQuery("select coord_system_id, name, version from coord_system where attrib like \"default_version%\" order by coord_system_id");
		List<CoordSystemEntry> coordSystemEntryList = new ArrayList<CoordSystemEntry>();
		while (rs.next()) {
			
			CoordSystemEntry coordSystemEntry = new CoordSystemEntry();
			
			coordSystemEntry.coord_system_id = rs.getInt("coord_system_id");
			coordSystemEntry.name = rs.getString("name");
			coordSystemEntry.version = rs.getString("version");
			
			coordSystemEntryList.add(coordSystemEntry);
		}
		return coordSystemEntryList;
	}
	
	protected List<CoordSystemEntry> fetchCurrentFuncgenCoordSystem(DatabaseRegistryEntry dbre) throws SQLException {
		Statement stmt = dbre.getConnection().createStatement();
		ResultSet rs = stmt.executeQuery("select coord_system_id, core_coord_system_id, name, version from coord_system where is_current=true order by core_coord_system_id");
		List<CoordSystemEntry> coordSystemEntryList = new ArrayList<CoordSystemEntry>();
		while (rs.next()) {
			
			CoordSystemEntry coordSystemEntry = new CoordSystemEntry();
			
			coordSystemEntry.coord_system_id = rs.getInt("coord_system_id");
			coordSystemEntry.core_coord_system_id = rs.getInt("core_coord_system_id");
			coordSystemEntry.name = rs.getString("name");
			coordSystemEntry.version = rs.getString("version");
			
			coordSystemEntryList.add(coordSystemEntry);
		}
		return coordSystemEntryList;
	}
	
	@Override
	public boolean run(DatabaseRegistryEntry dbre) {
		
		DatabaseRegistryEntry coreDbre = getCoreDb(dbre);
		List<CoordSystemEntry> coordSystemEntryListFuncgen;
		List<CoordSystemEntry> coordSystemEntryListCore;
		
		try {
			coordSystemEntryListCore = fetchCurrentCoreCoordSystem(coreDbre);
			coordSystemEntryListFuncgen = fetchCurrentFuncgenCoordSystem(dbre);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
		if (coordSystemEntryListFuncgen.size()!=coordSystemEntryListCore.size()) {
			String msg = "Coord system sizes don't agree.";
			logger.severe(msg);
			ReportManager.problem(this, dbre.getConnection(), msg);
			return false;
		}
		
		for (int i=0; i<coordSystemEntryListFuncgen.size(); i++) {
			CoordSystemEntry coordSystemEntryFuncgen = coordSystemEntryListFuncgen.get(i);
			CoordSystemEntry coordSystemEntryCore    = coordSystemEntryListCore.get(i);
			
			if (coordSystemEntryFuncgen.core_coord_system_id != coordSystemEntryCore.coord_system_id) {
				String msg = "Coord system ids don't agree: Funcgen: \"" + coordSystemEntryFuncgen.core_coord_system_id + "\" Core: \"" + coordSystemEntryCore.coord_system_id + "\"";
				logger.severe(msg);
				ReportManager.problem(this, dbre.getConnection(), msg);
				return false;
			}
			
			if (!coordSystemEntryFuncgen.name.equals(coordSystemEntryCore.name)) {
				String msg = "Coord system names don't agree: Funcgen: \"" + coordSystemEntryFuncgen.name + "\" Core: \"" + coordSystemEntryCore.name + "\"";
				logger.severe(msg);
				ReportManager.problem(this, dbre.getConnection(), msg);
				return false;
			}
			
			// Treat nulls and blanks equally here. There is another healthcheck
			// to flag this problem.
			//
			if (coordSystemEntryCore.version == null) {
				coordSystemEntryCore.version = "";
			}
			if (coordSystemEntryFuncgen.version == null) {
				coordSystemEntryFuncgen.version = "";
			}
			
			if (!coordSystemEntryFuncgen.version.equals(coordSystemEntryCore.version)) {
				String msg = 
					"Coord system versions don't agree for coord system " + coordSystemEntryFuncgen.name + ": Funcgen: \"" + coordSystemEntryFuncgen.version + "\" Core: \"" + coordSystemEntryCore.version + "\"\n"
					+ "Fix for funcgen database: "
					+ "update coord_system set name=\""+coordSystemEntryCore.version+"\" where coord_system_id=" + coordSystemEntryFuncgen.coord_system_id + ";";
				logger.severe(msg);
				ReportManager.problem(this, dbre.getConnection(), msg);
				return false;
			}
		}
		return true;
	}
}

class CoordSystemEntry {
	public int coord_system_id;
	public int core_coord_system_id;
	public String name;
	public String version;
	
}
