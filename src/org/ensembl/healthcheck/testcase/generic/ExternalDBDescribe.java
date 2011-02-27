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

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;

/**
 * EnsEMBL Healthcheck test case that ensures that the results of the SQL query <code>DESCRIBE external_db</code> are the same for a
 * set of databases.
 */

public class ExternalDBDescribe extends MultiDatabaseTestCase {

	private DatabaseType[] types = { DatabaseType.CORE, DatabaseType.VEGA };

	/**
	 * Create a new ExternalDBDescribe test case.
	 */
	public ExternalDBDescribe() {

		addToGroup("release");
		addToGroup("core_xrefs");
		setDescription("Check that the external_db table is the same in all databases.");

	}

	/**
	 * Check that the external_db tables are the same for each matched database.
	 * 
	 * @param dbr
	 *          The database registry containing all the specified databases.
	 * @return Result.
	 */
	public boolean run(DatabaseRegistry dbr) {

		boolean result = true;

		for (int i = 0; i < types.length; i++) {
			// ignore db_release column as this is allowed to be different between
			// species
			result &= checkSameSQLResult("SELECT external_db_id, db_name, status, dbprimary_acc_linkable, priority, db_display_name, type FROM external_db ORDER BY external_db_id", dbr.getAll(types[i]),
					false);
		}

		return result;

	} // run

} // ExternalDBDescribe
