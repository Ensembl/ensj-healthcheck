/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.generic;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;
import org.ensembl.healthcheck.testcase.Priority;

/**
 * Check that all xrefs from a certain source (e.g. HGNC, EntrezGene) are consistently assigned to the same Ensembl object type.
 */
public class XrefLevels extends MultiDatabaseTestCase {

	/**
	 * Creates a new instance of XrefLevels
	 */
	public XrefLevels() {

		addToGroup("release");
		addToGroup("core_xrefs");
		addToGroup("post-compara-handover");
		
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

		// create a temporary table in the first database we come to to hold the
		// master list of species, sources and objects
		DatabaseRegistryEntry masterDBRE = null;
		PreparedStatement masterPrep = null;

		String masterTable = "";

		DatabaseRegistryEntry[] dbres = dbr.getAll();

		if (dbres.length == 0) {
			return true;
		}

		for (DatabaseRegistryEntry dbre : dbres) {

			if (masterDBRE == null) {

				createTempTable(dbre);
				masterDBRE = dbre;
				masterTable = "healthcheck_xref";

				try {

					masterPrep = masterDBRE.getConnection().prepareStatement("INSERT INTO " + masterTable + " (species, source, object) VALUES (?,?,?)");

				} catch (SQLException se) {
					se.printStackTrace();
				}

			}

			// fill with the list of sources and object types from each species
			logger.fine("Adding sources and objects for " + dbre.getName());

			try {

				Statement stmt = dbre.getConnection().createStatement();

				// can't do this in one query as the databases being written to and read from might be on separate servers
				ResultSet rs = stmt
						.executeQuery("SELECT e.db_name, ox.ensembl_object_type FROM external_db e, xref x, object_xref ox WHERE e.external_db_id=x.external_db_id AND x.xref_id=ox.xref_id GROUP BY e.db_name, ox.ensembl_object_type");

				while (rs.next()) {
					masterPrep.setString(1, dbre.getSpecies().toString());
					masterPrep.setString(2, rs.getString("db_name"));
					masterPrep.setString(3, rs.getString("ensembl_object_type"));
					masterPrep.execute();
				}

				stmt.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}

		}

		// find problems - loop over results grouped by source and object
		// find situations where the source is the same but the species and object
		// are different
		try {

			ResultSet rs = masterPrep.executeQuery("SELECT * FROM " + masterTable + " GROUP BY source, object");

			String source = "";
			String object = "";
			String species = "";

			String lastSource = "";
			String lastObject = "";
			String lastSpecies = "";

			while (rs.next()) {

				source = rs.getString("source");
				object = rs.getString("object");
				species = rs.getString("species");

				if (!source.equals("")) { // first time around is a special case

					if (source.equals(lastSource) && !species.equals(lastSpecies) && !object.equals(lastObject)) {
						result = false;
						ReportManager.problem(this, "", source + " is on " + object + " in some species (e.g. " + species + ") and " + lastObject + " in others (e.g." + lastSpecies + ")");
					} else {
						ReportManager.correct(this, "", source + " only on " + object + " in all species.");
					}
				}
				lastSource = source;
				lastObject = object;
				lastSpecies = species;

			}
		} catch (SQLException se) {
			se.printStackTrace();
		}

		dropTempTable(masterDBRE);

		return result;

	} // run

	// -----------------------------------------------------------------------

	private void createTempTable(DatabaseRegistryEntry dbre) {

		// can't use a "real" temporary table here since connection pooling can lead to connections being closed
		try {
			Statement stmt = dbre.getConnection().createStatement();
			stmt.execute("DROP TABLE IF EXISTS healthcheck_xref");
			stmt.execute("CREATE TABLE healthcheck_xref (species VARCHAR(255), source VARCHAR(255), object VARCHAR(255))");
			logger.fine("Created table healthcheck_xref in " + dbre.getName());
		} catch (SQLException se) {
			se.printStackTrace();
		}

	}

	// -----------------------------------------------------------------------

	private void dropTempTable(DatabaseRegistryEntry dbre) {

		try {
			Statement stmt = dbre.getConnection().createStatement();
			stmt.execute("DROP TABLE IF EXISTS healthcheck_xref");
			logger.fine("Dropped table healthcheck_xref in " + dbre.getName());
		} catch (SQLException se) {
			se.printStackTrace();
		}

	}
	// -----------------------------------------------------------------------

} // XrefLevels

