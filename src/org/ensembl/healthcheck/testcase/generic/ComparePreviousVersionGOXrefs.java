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

/**
 * Compare the go xrefs in the current database with those from the equivalent
 * database on the secondary server, the species projected from in previous
 * release should appear in current as well.
 */

public class ComparePreviousVersionGOXrefs extends ComparePreviousVersionBase {

	/**
	 * Create a new XrefTypes testcase.
	 */
	public ComparePreviousVersionGOXrefs() {

		addToGroup("release");
		addToGroup("core_xrefs");
		setDescription("Compare the GO xrefs in the current database with those from the equivalent database on the secondary server");

	}

	// ----------------------------------------------------------------------

	protected Map getCounts(DatabaseRegistryEntry dbre) {

		String sql = "SELECT substring_index(substring_index(info_text,' ',2),' ',-1) as species, count(*)" + " FROM xref x"
				+ " WHERE info_type = 'projection' "
				+ " GROUP BY species";
		//System.out.println(sql);
		return getCountsBySQL(dbre, sql);

	} // ------------------------------------------------------------------------

	protected String entityDescription() {

		return "go xrefs by specie";

	}

	// ------------------------------------------------------------------------

	protected double threshold() {

		return 0.1;

	}

	

	// ----------------------------------------------------------------------

} // ComparePreviousVersionGOXrefs
