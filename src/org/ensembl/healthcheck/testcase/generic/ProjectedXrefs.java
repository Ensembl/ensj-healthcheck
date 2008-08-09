/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;

/**
 * Check that all species that should have projected xrefs do in fact have them.
 */
public class ProjectedXrefs extends MultiDatabaseTestCase {

	private Species[] projectedDisplayXrefSpecies = { Species.CANIS_FAMILIARIS, Species.BOS_TAURUS, Species.PAN_TROGLODYTES,
			Species.MACACA_MULATTA, Species.GALLUS_GALLUS, Species.XENOPUS_TROPICALIS, Species.MONODELPHIS_DOMESTICA,
			Species.RATTUS_NORVEGICUS };

	private Species[] projectedGOTermSpecies = { Species.HOMO_SAPIENS, Species.MUS_MUSCULUS, Species.RATTUS_NORVEGICUS,
			Species.CANIS_FAMILIARIS, Species.BOS_TAURUS, Species.GALLUS_GALLUS };

	/**
	 * Creates a new instance of ProjectedXrefs.
	 */
	public ProjectedXrefs() {

		addToGroup("release");
		addToGroup("core_xrefs");

		setDescription("Check that all species that should have projected xrefs do in fact have them.");

	}

	public void types() {

		List types = new ArrayList();
		types.add(DatabaseType.CORE);

		setAppliesToTypes(types);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if the test passed.
	 * 
	 */
	public boolean run(DatabaseRegistry dbr) {

		boolean result = true;

		// check display xrefs
		for (int i = 0; i < projectedDisplayXrefSpecies.length; i++) {

			DatabaseRegistryEntry[] dbres = dbr.getAll(DatabaseType.CORE, projectedDisplayXrefSpecies[i]);

			for (int j = 0; j < dbres.length; j++) {

				Connection con = dbres[j].getConnection();

				String species = projectedDisplayXrefSpecies[i].toString();

				int rows = getRowCount(con,
						"SELECT COUNT(*) FROM gene g, xref x WHERE g.display_xref_id=x.xref_id AND x.info_type='PROJECTION'");

				if (rows == 0) {

					ReportManager.problem(this, con, "No genes in " + species + " have projected display_xrefs");
					result = false;

				} else {

					ReportManager.correct(this, con, rows + " genes in " + species + " have projected display_xrefs");

				}

			}

		}

		// ----------------------------

		// check GO terms
		for (int k = 0; k < projectedGOTermSpecies.length; k++) {

			DatabaseRegistryEntry[] dbres = dbr.getAll(DatabaseType.CORE, projectedGOTermSpecies[k]);

			for (int j = 0; j < dbres.length; j++) {

				Connection con = dbres[j].getConnection();

				String species = projectedGOTermSpecies[k].toString();

				int rows = getRowCount(
						con,
						"SELECT COUNT(*) FROM xref x, external_db e WHERE e.external_db_id=x.external_db_id AND e.db_name='GO' AND x.info_type='PROJECTION'");

				if (rows == 0) {

					ReportManager.problem(this, con, "No projected GO terms in " + species);
					result = false;

				} else {

					ReportManager.correct(this, con, rows + " projected GO terms in " + species);

				}

			}

		}

		return result;

	} // run

} // ProjectedXrefs
