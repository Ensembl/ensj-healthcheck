/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2018] EMBL-European Bioinformatics Institute
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


package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.util.ConnectionBasedSqlTemplateImpl;
import org.ensembl.healthcheck.util.MapRowMapper;
import org.ensembl.healthcheck.util.SqlTemplate;
import org.ensembl.healthcheck.util.StringListMapRowMapper;

/**
 * Check that all xrefs from a certain source (e.g. HGNC, EntrezGene) are consistently assigned to the same Ensembl object type.
 */
public class XrefLevels extends MultiDatabaseTestCase {

	/**
	 * Creates a new instance of XrefLevels
	 */
	public XrefLevels() {

		setDescription("Check that all xrefs from a certain source (e.g. HGNC, EntrezGene) are consistently assigned to the same Ensembl object type across all species");
		setPriority(Priority.AMBER);
		setEffect("Causes BioMart to require specific workarounds for each case.");
		setFix("Manually fix affected xrefs.");
		setTeamResponsible(Team.CORE);

	}

	/**
	 * This only applies to core and Vega databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.RNASEQ);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbr
	 *          The database registry containing all the specified databases.
	 */
	public boolean run(DatabaseRegistry dbr) {

		boolean result = true;

		// easier to do this in SQL than Java
		// Create an in-memory SQL database via h2 driver
		
		try {
			Class.forName("org.h2.Driver");               //      memory:tablename
			Connection tempDB = DriverManager.getConnection("jdbc:h2:mem:XrefLevels"); 
			
			createTempTable(tempDB);

			// master list of species, sources and objects
			DatabaseRegistryEntry masterDBRE = null;
			PreparedStatement masterPrep = null;
			
			String masterTable = "healthcheck_xref";
			
			DatabaseRegistryEntry[] dbres = dbr.getAll(DatabaseType.CORE);
			
			if (dbres.length == 0) {
				return true;
			}
			
			for (DatabaseRegistryEntry dbre : dbres) {
				
				if (masterDBRE == null) {
					masterDBRE = dbre;
					masterPrep = tempDB.prepareStatement("INSERT INTO " + masterTable + " (species, source, object, database) VALUES (?,?,?,?)");
				}
				
				// fill with the list of sources and object types from each species
				logger.fine("Adding sources and objects for " + dbre.getName());
				
				Statement stmt = dbre.getConnection().createStatement();
				
				// can't do this in one query as the databases being written to and read from might be on separate servers
				ResultSet rs = stmt
						.executeQuery("SELECT e.db_name, ox.ensembl_object_type FROM external_db e, xref x, object_xref ox WHERE e.external_db_id=x.external_db_id AND x.xref_id=ox.xref_id GROUP BY e.db_name, ox.ensembl_object_type");
				
				while (rs.next()) {
                                        String species = dbre.getSpecies().toString();
                                        if (species == null || species.equalsIgnoreCase("unknown")) {
                                                species = dbre.getAlias();
                                        }

					masterPrep.setString(1, species);
					masterPrep.setString(2, rs.getString("db_name"));
					masterPrep.setString(3, rs.getString("ensembl_object_type"));
                                        masterPrep.setString(4, dbre.getName());
					masterPrep.execute();
				}
				
				stmt.close();
								
			}
			
			PreparedStatement sourcePrep = tempDB.prepareStatement("select distinct source from "+masterTable);
			ResultSet sources = sourcePrep.executeQuery();
			
			while (sources.next()) {
				String source = sources.getString("source");
				String query = "select object,species from "+ masterTable +" where source = ?";
                                String queryDb = "select species,database from "+ masterTable +" where source = ?";
				
				MapRowMapper<String,List<String>> mapper = new StringListMapRowMapper();
				SqlTemplate template = new ConnectionBasedSqlTemplateImpl(tempDB);
				Map<String,List<String>> map = template.queryForMap(query, mapper, source);
                                Map<String,List<String>> mapDb = template.queryForMap(queryDb, mapper, source);
				
				if (map.size() != 1) {
					// more than one list in the map implies there are at least two object types referenced
					// figure out which species are different
					String message = "Source:"+source+", types differ between species. ";
										
					int smallest = 1000;
					String smallestType = "";
					for (Map.Entry<String, List<String>> entry: map.entrySet()) {
						List<String> species = entry.getValue();
						if (species.size() < smallest) {
							smallest = species.size();
							smallestType = entry.getKey();
						}
						message = message.concat(entry.getKey() + " has " + species.size() + " species. ");
					}
					
					List<String> minoritySpecies = map.get(smallestType);
					
					message = message.concat("Problem species are:"+ StringUtils.join(minoritySpecies,","));

                                        for (String species: minoritySpecies) {
                                                List<String> minorityDatabases = mapDb.get(species);
                                                message = message.concat(". In problem databases:"+ StringUtils.join(minorityDatabases,","));
                                        }
					
					ReportManager.problem(this, "", message);
					result = false;
				}
				
			}
			
			dropTempTable(tempDB);
			
			tempDB.close();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block for making h2 database connection.
			// Somebody do this properly!
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}


		return result;

	} // run

	// -----------------------------------------------------------------------

	private void createTempTable(Connection conn) {

		// making a temporary table in memory rather than affecting production DB
		try {
			Statement stmt = conn.createStatement();
			stmt.execute("CREATE TABLE healthcheck_xref (species VARCHAR(255), source VARCHAR(255), object VARCHAR(255), database VARCHAR(255))");
			logger.fine("Created table healthcheck_xref in temporary H2 DB");
		} catch (SQLException se) {
			se.printStackTrace();
		}

	}

	// -----------------------------------------------------------------------

	private void dropTempTable(Connection conn) {

		try {
			Statement stmt = conn.createStatement();
			stmt.execute("DROP TABLE IF EXISTS healthcheck_xref");
			logger.fine("Dropped table healthcheck_xref in temporary DB");
		} catch (SQLException se) {
			se.printStackTrace();
		}

	}
	// -----------------------------------------------------------------------

} // XrefLevels

