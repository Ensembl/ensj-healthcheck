/*
 Copyright (C) 2004 EBI, GRL
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.ensembl.healthcheck.testcase.variation;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key
 * relationships between core and variation database.
 */

public class VFCoordinates extends MultiDatabaseTestCase {

	/**
	 * Create an ForeignKeyCoreId that applies to a specific set of databases.
	 */
	public VFCoordinates() {

		addToGroup("variation");
		addToGroup("variation-release");
		setDescription("Check for possible wrong coordinates in Vf table, due to wrong length or outside range seq_region.");
		setHintLongRunning(true);

	}

	/**
	 * Run the test.
	 * 
	 * @param databases
	 *          The databases to check, in order core->variation
	 * @return true if same transcripts and seq_regions in core and variation are
	 *         the same.
	 * 
	 */
	public boolean run(DatabaseRegistry dbr) {

		boolean result = true;

		DatabaseRegistryEntry[] variationDBs = dbr.getAll(DatabaseType.VARIATION);

		for (int i = 0; i < variationDBs.length; i++) {

			DatabaseRegistryEntry dbrvar = variationDBs[i];
			Species species = dbrvar.getSpecies();
			String variationName = dbrvar.getName();
			String coreName = variationName.replaceAll("variation", "core");
			DatabaseRegistryEntry dbrcore = new DatabaseRegistryEntry(coreName, species, DatabaseType.CORE, true);
			if (dbrcore == null) {
				result = false;
				logger.severe("Incorrect core database " + coreName + " for " + variationName);
				return result;
			}

			Connection con = dbrvar.getConnection();

			System.out.println("Using " + coreName + " as core database and " + variationName + " as variation database");

			int mc = getRowCount(
					con,
					"SELECT COUNT(*) FROM "
							+ variationName
							+ ".variation_feature WHERE length(allele_string) = 3 and seq_region_start<> seq_region_end and allele_string NOT LIKE '%-%'");

			if (mc > 0) {
				ReportManager.problem(this, con, "Wrong allele length !! (allele_string <> coordinates length) for " + variationName);
				result = false;
			}

			mc = getRowCount(con, "SELECT COUNT(*) FROM " + coreName + ".seq_region s, " + variationName
					+ ".variation_feature vf WHERE vf.seq_region_id = s.seq_region_id AND vf.seq_region_end > s.length");
			if (mc > 0) {
				ReportManager.problem(this, con, "Variation Features outside range in " + variationName);
				result = false;
			}
			mc = getRowCount(con, "SELECT COUNT(*) FROM " + variationName
					+ ".variation_feature vf WHERE vf.seq_region_start = 1 AND vf.seq_region_end > 1");
			if (mc > 0) {
				ReportManager.problem(this, con, "Variation Features with coordinates = 1 " + variationName);
				result = false;
			}
			
		}
		return result;

	}

	/**
	 * This only applies to variation databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.CORE);
		removeAppliesToType(DatabaseType.VEGA);

	}

} // VFCoordinates
