package org.ensembl.healthcheck.testcase.funcgen;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.ensembl.CoreDbNotFoundException;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseRegistryEntry.DatabaseInfo;
import org.ensembl.healthcheck.MissingMetaKeyException;
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
public abstract class AbstractCoreDatabaseUsingTestCase extends SingleDatabaseTestCase {
	
	public AbstractCoreDatabaseUsingTestCase() {
		setTeamResponsible(Team.FUNCGEN);
		setDescription("");
	}

	protected String getMetaValue(Connection connection, String metaKey) throws MissingMetaKeyException {
		
		String sql = "select meta_value from meta where meta_key = \"" + metaKey + "\"";
		String metaValue = "";
		try {
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			boolean is_valid_row = rs.next();
			
			if (! is_valid_row) {
				throw new MissingMetaKeyException("species.production_name has not been set " + connection.getCatalog() + "!");
			}
			
			metaValue = rs.getString("meta_value");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return metaValue;
	}

	protected String getProductionName(Connection connection) throws MissingMetaKeyException { 
		return getMetaValue(connection, "species.production_name");
	}

	protected String getAssembly(Connection connection) throws MissingMetaKeyException {
		return getMetaValue(connection, "assembly.default");
	}

	/**
	 * Takes a DatabaseRegistryEntry of a funcgen database and returns the 
	 * name of the core database it belongs to. 
	 */
	protected String getCoreDbName(DatabaseRegistryEntry dbre) throws MissingMetaKeyException {
		
		String speciesProductionName = getProductionName(dbre.getConnection());
		
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
	 * @throws CoreDbNotFoundException 
	 */
	protected DatabaseRegistryEntry getCoreDb(DatabaseRegistryEntry dbre) throws MissingMetaKeyException, CoreDbNotFoundException{
		
		String coreDbName = getCoreDbName(dbre);
		DatabaseRegistryEntry coreDbre = getDatabaseRegistryEntryByPattern(coreDbName);
		if (coreDbre == null) {
			String errorMessage = "Can't find core database " + coreDbName + "!";
			ReportManager.problem(this, dbre.getConnection(), errorMessage);
			throw new CoreDbNotFoundException(errorMessage);
		}
		return coreDbre;
	}
}
