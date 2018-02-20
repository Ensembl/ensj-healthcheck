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

/*
 * Copyright (C) 2003 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that all xrefs for a particular external_db map to one and only one ensembl object type.
 */

public class XrefTypes extends SingleDatabaseTestCase {

	/**
	 * Create a new XrefTypes testcase.
	 */
	public XrefTypes() {

		setDescription("Check that all xrefs only map to one ensembl object type.");
		setTeamResponsible(Team.CORE);
                setSecondTeamResponsible(Team.GENEBUILD);
	}

        /**
         * This only applies to core databases.
         */
        public void types() {

			setAppliesToType(DatabaseType.CORE);

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

		Connection con = dbre.getConnection();

		try {

			Statement stmt = con.createStatement();

			// Query returns all external_db_id-object type relations
			// execute it and loop over each row checking for > 1 consecutive row with same ID

			ResultSet rs = stmt
					.executeQuery("SELECT  x.external_db_id, ox.ensembl_object_type, COUNT(*), e.db_name FROM object_xref ox, external_db e, xref x LEFT JOIN transcript t ON t.display_xref_id = x.xref_id WHERE x.xref_id = ox.xref_id AND e.external_db_id = x.external_db_id AND isnull(transcript_id) GROUP BY x.external_db_id, ox.ensembl_object_type");

			try {
				long previousID = -1;
				String previousType = "";
	
				while (rs != null && rs.next()) {
	
					long externalDBID = rs.getLong(1);
					String objectType = rs.getString(2);
					// int count = rs.getInt(3);
					String externalDBName = rs.getString(4);
	
					if (externalDBID == previousID) {
	
						ReportManager.problem(this, con, "External DB with ID " + externalDBID + " (" + externalDBName + ") is associated with " + objectType + " as well as " + previousType);
						result = false;
	
					}
	
					previousType = objectType;
					previousID = externalDBID;
	
				} // while rs
			}
			finally {
				DBUtils.closeQuietly(rs);
			}

			stmt.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (result) {

			ReportManager.correct(this, con, "All external dbs are only associated with one Ensembl object type");

		}

		return result;

	} // run

	// ----------------------------------------------------------------------

} // XrefTypes
