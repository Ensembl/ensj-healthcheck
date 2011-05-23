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

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
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

		addToGroup("release");
		addToGroup("pre-compara-handover");
		addToGroup("post-compara-handover");
		
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
	 * @param dbr
	 *          The database registry containing all the specified databases.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		// check that the key is present
		String key = getRowColumnValue(con, "SELECT meta_value FROM meta WHERE meta_key='schema_type'");
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
	 * Check if the key in the meta table matches the database type. Note that all generic (CDNA, core, otherfeatures, vega) dbs
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

