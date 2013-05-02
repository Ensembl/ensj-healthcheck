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
package org.ensembl.healthcheck.testcase.variation;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionBase;

/**
 * Compare the number of phenotype features between the current database and the database on the secondary server.
 */

public class ComparePreviousVersionPhenotypeFeatures extends ComparePreviousVersionBase {

	/**
	 * Create a new testcase.
	 */
	public ComparePreviousVersionPhenotypeFeatures() {

		addToGroup("variation");
		addToGroup("variation-release");

		setDescription("Compare the number of phenotype features in the current database with those from the equivalent database on the secondary server");
		setTeamResponsible(Team.VARIATION);

	}

	protected Map getCounts(DatabaseRegistryEntry dbre) {

		Map<String, Integer> counts = new HashMap<String, Integer>();
		Connection con = dbre.getConnection();

		try {
			// Count phenotype features by source
			counts.putAll(getCountsBySQL(dbre, "SELECT s.name, COUNT(*) FROM phenotype_feature pf JOIN source s ON (s.source_id = pf.source_id) GROUP BY s.name"));
			// Count total number of variations
			counts.putAll(getCountsBySQL(dbre, "SELECT 'all sources', COUNT(*) FROM phenotype_feature"));
		} catch (Exception e) {
			ReportManager.problem(this, con, "HealthCheck caused an exception: " + e.getMessage());
		}
		
		return counts;
	}

	// ------------------------------------------------------------------------

	protected String entityDescription() {

		return "number of phenotype features from";

	}

	// ------------------------------------------------------------------------

	protected double threshold() {

		return 1;

	}

	// ------------------------------------------------------------------------

} // ComparePreviousVersionPhenotypeFeatures
