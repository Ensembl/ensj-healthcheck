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

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.util.Set;
import java.util.Map;
import java.util.logging.Logger;



import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.SqlTemplate;
import org.ensembl.healthcheck.util.Utils;
import org.ensembl.healthcheck.util.RowMapper;
import org.ensembl.healthcheck.util.DefaultMapRowMapper;
import org.ensembl.healthcheck.util.DefaultObjectRowMapper;




/**
 * Check that feature co-ords make sense.
 */
public class FeatureCoords extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of CheckFeatureCoordsTestCase
	 */
	public FeatureCoords() {

		addToGroup("post_genebuild");
		addToGroup("release");
		addToGroup("pre-compara-handover");
		addToGroup("post-compara-handover");

		setDescription("Check that feature co-ords make sense.");
		setHintLongRunning(true);
		setTeamResponsible(Team.GENEBUILD);
	}

	/**
	 * Iterate over each affected database and perform various checks.
	 * 
	 * @param dbre
	 *            The database to check.
	 * @return True if the test passed.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		String[] featureTables = getCoreFeatureTables();
                Connection con = dbre.getConnection();

		for (int tableIndex = 0; tableIndex < featureTables.length; tableIndex++) {

			String tableName = featureTables[tableIndex];
                        result &= checkStart(dbre, tableName);
                        result &= checkStartEnd(dbre, tableName);
                        result &= checkLength(dbre, tableName);

		} // foreach table

		return result;

	} // run


        protected boolean checkLength(DatabaseRegistryEntry dbre, String tableName) {
                SqlTemplate t = DBUtils.getSqlTemplate(dbre);
                boolean result = true;
                if (tableName.equals("repeat_feature")) {
                        return true;
                }
                DefaultMapRowMapper<Integer, Integer> mapper = new DefaultMapRowMapper<Integer, Integer>(Integer.class, Integer.class);
                String featureSQL = "SELECT seq_region_id, max(seq_region_start) from " + tableName + " group by seq_region_id ";
                String lengthSQL = "SELECT length from seq_region where seq_region_id = ?";
                String nameSQL = "SELECT name from seq_region where seq_region_id = ?";
                Map<Integer, Integer> featureResults = t.queryForMap(featureSQL, mapper);

                for (Map.Entry<Integer, Integer> entry : featureResults.entrySet()) {
                        Integer max = entry.getValue();
                        Integer region = entry.getKey();
                        Integer length = t.queryForDefaultObject(lengthSQL, Integer.class, region);
                        String name = t.queryForDefaultObject(nameSQL, String.class, region);

                        if (max > length) {
                                ReportManager.problem(this, dbre.getConnection(), "Some features in " + tableName + " start on position " + max + " when region " + name + " is only " + length + " long");
                                result = false;
                        }
                }
                return result;

        }


        protected boolean checkStart(DatabaseRegistryEntry dbre, String tableName) {
                String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE seq_region_start < 1";
                int rows = DBUtils.getRowCount(dbre.getConnection(), sql);
                if (rows > 0) {
                        ReportManager.problem(this, dbre.getConnection(), rows + " rows in " + tableName + " have seq_region_start < 1");
                        return false;
                 } else {
                        return true;
                 }

        }
	/**
	 * Subroutine to carry out a check on whether the start is after the end.
	 * This is to allow EG to skip this check for circular molecules
	 * 
	 * @param tableName
	 * @param con
	 * @return
	 */
	protected boolean checkStartEnd(DatabaseRegistryEntry dbre, String tableName) {
		String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE seq_region_start > seq_region_end";
		int rows = DBUtils.getRowCount(dbre.getConnection(), sql);;
		if (rows > 0) {
			ReportManager.problem(this, dbre.getConnection(), rows + " rows in " + tableName + " have seq_region_start > seq_region_end");
			return false;
		} else {
                        return true;
		}
	}

} // FeatureCoords
