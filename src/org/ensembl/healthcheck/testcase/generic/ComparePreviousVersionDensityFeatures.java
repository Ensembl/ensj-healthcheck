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

import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;

/**
 * Compare the density features in the current database with those from the equivalent database on the secondary server.
 */

public class ComparePreviousVersionDensityFeatures extends ComparePreviousVersionBase {

	/**
	 * Create a new Density Feature Types.
	 */
	public ComparePreviousVersionDensityFeatures() {

		addToGroup("release");
		addToGroup("post-compara-handover");

		setDescription("Compare the density features in the current database with those from the equivalent database on the secondary server");
		setTeamResponsible(Team.RELEASE_COORDINATOR);

	}

	public void types() {
		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.ESTGENE);
		removeAppliesToType(DatabaseType.EST);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.VEGA);
		removeAppliesToType(DatabaseType.RNASEQ);
	}

	// ----------------------------------------------------------------------

	protected Map<String, Integer> getCounts(DatabaseRegistryEntry dbre) {

		String sql = "SELECT logic_name, count(1) AS count FROM density_feature JOIN density_type"
			+ " USING(density_type_id) JOIN analysis USING(analysis_id) GROUP BY logic_name";
		// System.out.println(sql);
		return getCountsBySQL(dbre, sql);

	} // ------------------------------------------------------------------------

	protected String entityDescription() {

		return "density features of type";

	}

	// ------------------------------------------------------------------------

	protected double threshold() {

		return 0.78;

	}

	// ----------------------------------------------------------------------

} // ComparePreviousVersionDensityFeatures

