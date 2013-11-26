/*
 * Copyright [1999-2013] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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
import java.util.ArrayList;
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that all species that should have projected xrefs do in fact have them.
 */
public class ProjectedXrefs extends MultiDatabaseTestCase {

	private Species[] projectedDisplayXrefSpecies = { Species.CANIS_FAMILIARIS, Species.BOS_TAURUS, Species.PAN_TROGLODYTES, Species.MACACA_MULATTA, Species.GALLUS_GALLUS, Species.XENOPUS_TROPICALIS,
			Species.MONODELPHIS_DOMESTICA, Species.RATTUS_NORVEGICUS };

	private Species[] projectedGOTermSpecies = { Species.HOMO_SAPIENS, Species.MUS_MUSCULUS, Species.RATTUS_NORVEGICUS, Species.CANIS_FAMILIARIS, Species.BOS_TAURUS, Species.GALLUS_GALLUS };

	/**
	 * Creates a new instance of ProjectedXrefs.
	 */
	public ProjectedXrefs() {

		addToGroup("core_xrefs");
		addToGroup("post-compara-handover");
		
		setDescription("Check that all species that should have projected xrefs do in fact have them.");
		setTeamResponsible(Team.CORE);

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

				int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM gene g, xref x WHERE g.display_xref_id=x.xref_id AND x.info_type='PROJECTION'");

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

				int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM xref x, external_db e WHERE e.external_db_id=x.external_db_id AND e.db_name='GO' AND x.info_type='PROJECTION'");

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
