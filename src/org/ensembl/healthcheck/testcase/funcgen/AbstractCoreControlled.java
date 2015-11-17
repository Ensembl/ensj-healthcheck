package org.ensembl.healthcheck.testcase.funcgen;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * @author mnuhn
 * 
 * Abstract class providing method "getCoreDb". This can be used by an 
 * inheriting class to test, if the funcgen data is in sync with the data in 
 * the core database.
 *
 */
public abstract class AbstractCoreControlled extends SingleDatabaseTestCase {
	
	public AbstractCoreControlled() {
		setTeamResponsible(Team.FUNCGEN);
		setDescription("");
	}
	
	protected String fetchSpeciesNameFromDb(DatabaseRegistryEntry dbre) throws SQLException {
		Statement stmt = dbre.getConnection().createStatement();
		ResultSet rs = stmt.executeQuery("select meta_value from meta where meta_key = \"species.production_name\"");
		rs.next();
		String speciesName = rs.getString("meta_value");
		return speciesName;
	}
	
	protected String fetchSchemaBuild(DatabaseRegistryEntry dbre)  throws SQLException {
		Statement stmt = dbre.getConnection().createStatement();
		ResultSet rs = stmt.executeQuery("select schema_build from coord_system where is_current=true");
		rs.next();
		String speciesName = rs.getString("schema_build");
		return speciesName;
	}
	
	/**
	 * Takes a DatabaseRegistryEntry of a funcgen database and returns the 
	 * name of the core database it belongs to. 
	 */
	protected String getCoreDbName(DatabaseRegistryEntry dbre) throws SQLException {
		
		String speciesProductionName = fetchSpeciesNameFromDb(dbre);
		String schemaBuild = fetchSchemaBuild(dbre);
		
		String dbreSchemaBuild = dbre.getSchemaVersion() + "_" + dbre.getGeneBuildVersion();
		
		if (!dbreSchemaBuild.equals(schemaBuild)) {
			logger.warning("Schema build from database ("+schemaBuild+") is not the same as from the database registry ("+dbreSchemaBuild+").");
		} else {
			//logger.info("Schema build from database ("+schemaBuild+") and database registry ("+dbreSchemaBuild+") agree.");
		}
		
		String dbreSpecies = dbre.getSpecies().toString();
		if (!dbreSpecies.equals(speciesProductionName)) {
			logger.warning("Species name from database ("+speciesProductionName+") is not the same as from the database registry ("+dbreSpecies+"). This can happen, if the name of your database is non-standard.");
		}
		
		String coreDbName = speciesProductionName + "_core_" + dbre.getSchemaVersion() + "_" + dbre.getGeneBuildVersion();
		return coreDbName;
	}
	
	/**
	 * Takes a DatabaseRegistryEntry of a funcgen database and returns the 
	 * DatabaseRegistryEntry of the core database it belongs to. 
	 */
	protected DatabaseRegistryEntry getCoreDb(DatabaseRegistryEntry dbre){
		
		String coreDbName;
		try {
			coreDbName = getCoreDbName(dbre);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		DatabaseRegistryEntry coreDbre = getDatabaseRegistryEntryByPattern(coreDbName);
		if (coreDbre==null) {
			ReportManager.problem(this, dbre.getConnection(), "Can't find core database " + coreDbName + "!");
		}
		return coreDbre;
	}
}
