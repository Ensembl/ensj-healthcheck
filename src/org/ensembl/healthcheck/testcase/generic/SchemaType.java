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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.Utils;

/**
 * Check that the schema_type meta key is present and correct.
 */
public class SchemaType extends SingleDatabaseTestCase {

	String[] allowedKeys = { "core", "compara", "funcgen", "variation" };

	/**
	 * Creates a new instance of SchemaType
	 */
	public SchemaType() {

		setDescription("Check that the schema_type meta key is present and correct.");
		setPriority(Priority.AMBER);
		setFix("Set schema_type meta key.");
		setTeamResponsible(Team.GENEBUILD);

	}

	public void types() {

		addAppliesToType(DatabaseType.VARIATION);
		addAppliesToType(DatabaseType.FUNCGEN);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database registry containing all the specified databases.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		// check that the key is present
		String key = DBUtils.getRowColumnValue(con, "SELECT meta_value FROM meta WHERE meta_key='schema_type'");
		if (key == null || key.length() == 0) {
			ReportManager.problem(this, con, "No entry in meta table for schema_type");
			return false;
		}

		// check that its value is one of the allowed ones
		if (!Utils.stringInArray(key, allowedKeys, false)) {
			ReportManager.problem(this, con, "schema_type value " + key + " in meta table is not one of the allowed values (" + Utils.arrayToString(allowedKeys, ",") + ")");
			return false;
		}

		// check that the value matches the type of the database
		if (!typeMatches(dbre.getType(), key)) {
			ReportManager.problem(this, con, "schema_type value " + key + " in meta table does not match database type (" + dbre.getType().getName() + ")");
			result = false;
		}

		return result;

	} // run

	/**
	 * Check if the key in the meta table matches the database type. Note that all generic (CDNA, core, otherfeatures) dbs
	 * should have schema_type of "core"
	 */
	private boolean typeMatches(DatabaseType type, String key) {

		boolean result = false;

		if (type.isGeneric() && key.equals("core")) {
			result = true;
		} else if (type.getName().equals(key)) {
			result = true;
		}

		return result;
	}

} // SchemaType

