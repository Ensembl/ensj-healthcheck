/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
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


package org.ensembl.healthcheck.testcase.compara;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.google.gson.Gson;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.SqlUncheckedException;

/**
 * An EnsEMBL Healthcheck test case that checks the validity of JSON
 * objects
 */

public class CheckJSONObjects extends SingleDatabaseTestCase {

	private static final Gson gson = new Gson();

	public CheckJSONObjects() {
		setDescription("Check that all the JSON objects of the ensembl_compara database are valid.");
		setTeamResponsible(Team.COMPARA);
	}

	public boolean run(DatabaseRegistryEntry dbre) {

		Connection con = dbre.getConnection();

		if (!tableHasRows(con, "gene_tree_object_store")) {
			ReportManager.correct(this, con, "NO ENTRIES in gene_tree_object_store table, so nothing to test IGNORED");
			return true;
		}

		boolean result = true;
		result &= checkAllJSONs(con);
		return result;
	}

    public boolean checkAllJSONs(Connection con) {

		boolean result = true;
        Statement stmt = null;
        ResultSet rs = null;
		String sql = "SELECT root_id, data_label, UNCOMPRESS(compressed_data) FROM gene_tree_object_store";
        try {
            stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
            rs = stmt.executeQuery(sql);

			while (result && rs.next()) {
				String json = rs.getString(3);
				if (!isJSONValid(json)) {
					ReportManager.problem(this, con, "FAILED JSON not valid for root_id=" + rs.getString(1) + " / data_label=" + rs.getString(2));
					result = false;
				}
			}
            rs.close();
            stmt.close();
        } catch (Exception e) {
            throw new SqlUncheckedException("Could not execute query", e);
        } finally {
            DBUtils.closeQuietly(rs);
            DBUtils.closeQuietly(stmt);
        }

        return result;

    } // checkAllJSONs 

	public static boolean isJSONValid(String jsonInString) {
		try {
			gson.fromJson(jsonInString, Object.class);
			return true;
		} catch(com.google.gson.JsonSyntaxException ex) { 
			return false;
		}
	}

} // CheckJSONObjects
