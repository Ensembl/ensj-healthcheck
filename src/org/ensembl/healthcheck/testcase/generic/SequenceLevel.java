/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2017] EMBL-European Bioinformatics Institute
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.SqlTemplate;
import org.ensembl.healthcheck.util.DBUtils;


/**
 * Check for DNA that is not stored on the sequence-level coordinate system.
 */

public class SequenceLevel extends SingleDatabaseTestCase {

	/**
	 * Constructor.
	 */
	public SequenceLevel() {

		setDescription("Check for DNA that is not stored on the sequence-level coordinate system.");
		setTeamResponsible(Team.GENEBUILD);
	}

        /**
         * Data is only tested in core database, as the tables are in sync
         */
        public void types() {

                removeAppliesToType(DatabaseType.OTHERFEATURES);
                removeAppliesToType(DatabaseType.RNASEQ);
                removeAppliesToType(DatabaseType.CDNA);

        }

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if the test passed.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

                result &= checkVersion(dbre);

		Connection con = dbre.getConnection();
		String sql = "SELECT cs.name, COUNT(1) FROM coord_system cs, seq_region s, dna d WHERE d.seq_region_id = s.seq_region_id AND cs.coord_system_id =s.coord_system_id AND attrib NOT LIKE '%sequence_level%' GROUP BY cs.coord_system_id";

		try {

			Statement stmt = con.createStatement();

			ResultSet rs = stmt.executeQuery(sql);

			if (rs == null) {

				ReportManager.correct(this, con, "All DNA is attached to a sequence-level co-ordinate system.");

			} else {

				while (rs.next()) {

					String coordSystem = rs.getString(1);
					int rows = rs.getInt(2);

					ReportManager.problem(this, con, String.format("Coordinate system %s has %d seq regions containing sequence, but it does not have the sequence_level attribute", coordSystem, rows));
					result = false;

				}

			}

		} catch (SQLException e) {
			System.err.println("Error executing: " + sql);
			e.printStackTrace();
		}

		return result;

	} // run

  private boolean checkVersion(DatabaseRegistryEntry dbre) {

    SqlTemplate t = DBUtils.getSqlTemplate(dbre);
    boolean result = true;
    String sql = "SELECT count(*) FROM coord_system WHERE name = 'contig' AND version is not NULL";
    int rows = t.queryForDefaultObject(sql, Integer.class);
    if (rows > 0) {
      result = false;
      ReportManager.problem(this, dbre.getConnection(), "Contig version is not null"); 
    }
    return result;
  }

} // SequenceLevel
