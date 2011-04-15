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
 * Compare the xref synonyms in the current database with those from the equivalent
 * database on the secondary server.
 */

public class ComparePreviousVersionSynonyms extends ComparePreviousVersionBase {

	/**
	 * Constructor.
	 */
	public ComparePreviousVersionSynonyms() {

		addToGroup("release");
		addToGroup("core_xrefs");
		setDescription("Compare the xref synonyms in the current database with those from the equivalent database on the secondary server");
                setTeamResponsible("Core and GeneBuilders");
                 
	}

    /**
	 * This test Does not apply to sanger_vega dbs
	 */
	public void types() {
		removeAppliesToType(DatabaseType.SANGER_VEGA);
	}

	// ----------------------------------------------------------------------

	protected Map<String, Integer> getCounts(DatabaseRegistryEntry dbre) {

		String sql = "SELECT e.db_name, count(*) FROM external_db e, external_synonym es, xref x, object_xref ox WHERE x.xref_id=ox.xref_id AND e.external_db_id=x.external_db_id AND x.xref_id=es.xref_id GROUP BY e.db_name";

		return getCountsBySQL(dbre, sql);

	} // ------------------------------------------------------------------------

	protected String entityDescription() {

		return "synonyms for xrefs of type";

	}

	// ------------------------------------------------------------------------

	protected double threshold() {

		return 0.78;

	}

	// ----------------------------------------------------------------------

} // ComparePreviousVersionSynonyms

