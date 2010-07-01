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

/**
 * Compare the RepeatTypes in the current database with those from the equivalent
 * database on the secondary server.
 */

public class ComparePreviousVersionRepeatTypes extends ComparePreviousVersionBase {

	/**
	 * Create a new XrefTypes testcase.
	 */
	public ComparePreviousVersionRepeatTypes() {

		addToGroup("post_genebuild");
		addToGroup("release");
		setDescription("Compare the types of repeat features in the current database with those from the equivalent database on the secondary server");

	}

    /**
	 * This test Does not apply to sanger_vega dbs
	 */
	public void types() {
		removeAppliesToType(DatabaseType.SANGER_VEGA);
	}

	// ----------------------------------------------------------------------

	protected Map getCounts(DatabaseRegistryEntry dbre) {

		return getCountsBySQL(dbre, "SELECT DISTINCT(rc.repeat_type), COUNT(*) FROM repeat_feature rf, repeat_consensus rc WHERE rf.repeat_consensus_id=rc.repeat_consensus_id GROUP BY rc.repeat_type");

	}

	// ------------------------------------------------------------------------

	protected String entityDescription() {

		return "Repeats of repeat consensus type";

	}

	// ------------------------------------------------------------------------

	protected double threshold() {

		return 0.75;

	}

	// ------------------------------------------------------------------------

} // ComparePreviousVersionRepeatTypes
