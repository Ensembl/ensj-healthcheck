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


package org.ensembl.healthcheck.testcase.variation;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key relationships between core and variation database.
 */

public class VFCoordinates extends MultiDatabaseTestCase {

	/**
	 * Create an ForeignKeyCoreId that applies to a specific set of databases.
	 */
	public VFCoordinates() {

		setDescription("Check for possible wrong coordinates in Vf table, due to wrong length or outside range seq_region.");
		// setHintLongRunning(true);
		setTeamResponsible(Team.VARIATION);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbr
	 *          Registry containing the databases to check, in order core->variation
	 * @return true if same transcripts and seq_regions in core and variation are the same.
	 * 
	 */
	public boolean run(DatabaseRegistry dbr) {

		boolean allResult = true;

		DatabaseRegistryEntry[] variationDBs = dbr.getAll(DatabaseType.VARIATION);

		for (int i = 0; i < variationDBs.length; i++) {

			boolean result = true;
			DatabaseRegistryEntry dbrvar = variationDBs[i];
			String variationName = dbrvar.getName();

			// the database registry parameter dbr only contains the databases matching the regular expression passed on the command line
			// so create a database registry containing all the core databases and find the one we want
			List<String> coreRegexps = new ArrayList<String>();
			coreRegexps.add(".*_core_.*");

			DatabaseRegistry allDBR = new DatabaseRegistry(coreRegexps, null, null, false);

			String coreName = variationName.replaceAll("variation", "core");
			DatabaseRegistryEntry dbrcore = allDBR.getByExactName(coreName);
			if (dbrcore == null) {
				logger.severe("Incorrect core database " + coreName + " for " + variationName);
				return false;
			}

			Connection con = dbrvar.getConnection();

			System.out.println("Using " + coreName + " as core database and " + variationName + " as variation database");

			int mc = DBUtils.getRowCount(
					con,
					"SELECT COUNT(*) FROM "
							+ variationName
							+ ".variation_feature vf LEFT JOIN "
							+ variationName
							+ ".failed_variation f ON vf.variation_id = f.variation_id WHERE f.variation_id IS NULL AND length(vf.allele_string) = 3 and vf.seq_region_start<> vf.seq_region_end and vf.allele_string NOT LIKE '%-%'");

			if (mc > 0) {
				ReportManager.problem(this, con, "Wrong allele length !! (allele_string <> coordinates length) for " + mc + " entries in " + variationName);
				result = false;
			}

			mc = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM " + coreName + ".seq_region s, " + variationName + ".variation_feature vf WHERE vf.seq_region_id = s.seq_region_id AND vf.seq_region_end > s.length");
			if (mc > 0) {
				ReportManager.problem(this, con, "Variation Features outside range in " + variationName);
				result = false;
			}
			mc = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM " + variationName + ".variation_feature vf WHERE vf.seq_region_start = 1 AND vf.seq_region_end > 1");
			if (mc > 0) {
				ReportManager.problem(this, con, "Variation Features with coordinates = 1 " + variationName);
				result = false;
			}
			// Check that no VFs are on the negative strand, unless they have map_weight > 1 and/or are located on non-reference
			// seq_regions or correspond to CNV probes
			String vfId = DBUtils.getRowColumnValue(con, "SELECT vf.variation_feature_id FROM " + variationName
					+ ".variation_feature vf WHERE vf.seq_region_strand = -1 AND vf.map_weight = 1 AND vf.allele_string NOT LIKE 'CNV_PROBE' AND NOT EXISTS (SELECT * FROM " + coreName
					+ ".seq_region_attrib sra JOIN " + coreName + ".attrib_type at USING (attrib_type_id) WHERE sra.seq_region_id = vf.seq_region_id AND at.code = 'non_ref') LIMIT 1");
			if (vfId.length() > 0) {
				ReportManager.problem(this, con, "Variation Features on the negative strand (e.g. variation_feature_id = " + vfId + ") in " + variationName);
				result = false;
			}
			// Check that no VFs are duplicated
			mc = DBUtils.getRowCount(con, "SELECT COUNT(DISTINCT vf1.variation_id) FROM " + variationName + ".variation_feature vf1 JOIN " + variationName
					+ ".variation_feature vf2 USING (variation_id,seq_region_id,seq_region_start,seq_region_end,seq_region_strand) WHERE vf1.variation_feature_id < vf2.variation_feature_id");
			if (mc > 0) {
				ReportManager.problem(this, con, "There are duplicated Variation Features for " + String.valueOf(mc) + " variations in " + variationName);
				result = false;
			}
			if (result) {
				ReportManager.correct(this, con, "VFCoordinates test run successfully");
			}
			allResult = (allResult && result);

		}
		return allResult;

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
