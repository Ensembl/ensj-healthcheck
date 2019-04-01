/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
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
 * Compare the xrefs in the current database with those from the equivalent database on the secondary server.
 */

public class ComparePreviousVersionAssociatedXrefGroups extends ComparePreviousVersionBase {

	/**
	 * Create a new XrefTypes testcase.
	 */
	public ComparePreviousVersionAssociatedXrefGroups() {

		setDescription("Compare the associated xrefs in the current database with those from the equivalent database on the secondary server");
		setTeamResponsible(Team.CORE);
		setSecondTeamResponsible(Team.GENEBUILD);

	}

	/**
	 * This test Does not apply to sangervega dbs
	 */
	public void types() {
		removeAppliesToType(DatabaseType.SANGER_VEGA);
	}

	// ----------------------------------------------------------------------

	protected Map<String, Integer> getCounts(DatabaseRegistryEntry dbre) {
		String sql = "SELECT"
				     + "  CONCAT_WS( '_',"
				     + "    IF( gene.stable_id IS NULL,"
				     + "      IF( transcript.stable_id IS NULL,"
				     + "        translation.stable_id,"
				     + "        transcript.stable_id"
				     + "      ),"
				     + "      gene.stable_id"
				     + "    ),"
				     + "    x1.dbprimary_acc,"
				     + "    xs.dbprimary_acc"
				     + "  ) AS obj_stable_id,"
				     + "  COUNT("
				     + "    DISTINCT associated_xref.associated_group_id"
				     + "  ) AS count "
				     + "FROM"
				     + "  object_xref"
				     + "  JOIN xref x1 ON (object_xref.xref_id=x1.xref_id)"
				     + "  JOIN external_db ON (x1.external_db_id=external_db.external_db_id)"
				     + "  JOIN associated_xref ON (object_xref.object_xref_id=associated_xref.object_xref_id)"
				     + "  JOIN xref xs ON (associated_xref.source_xref_id=xs.xref_id)"
				     + "  LEFT JOIN gene ON ("
				     + "    object_xref.ensembl_id=gene.gene_id AND"
				     + "    object_xref.ensembl_object_type='Gene')"
				     + "  LEFT JOIN transcript ON ("
				     + "    object_xref.ensembl_id=transcript.transcript_id AND"
				     + "    object_xref.ensembl_object_type='Transcript')"
				     + "  LEFT JOIN translation ON ("
				     + "    object_xref.ensembl_id=translation.translation_id AND"
				     + "    object_xref.ensembl_object_type='Translation') "
				     + "GROUP BY"
				     + "  object_xref.ensembl_id,"
				     + "  object_xref.ensembl_object_type,"
				     + "  x1.dbprimary_acc,"
				     + "  xs.dbprimary_acc;";
		System.out.println(dbre.getDatabaseServer() + " " + dbre.getSchemaVersion());
		System.out.println(sql);
		return getCountsBySQL(dbre, sql);

	} // ------------------------------------------------------------------------

	protected String entityDescription() {

		return "xrefs of type";

	}

	// ------------------------------------------------------------------------

	protected double threshold() {

		return 0.00;

	}

        // ------------------------------------------------------------------------

        protected double minimum() {

                return 100;

        }

	// ----------------------------------------------------------------------

} // ComparePreviousVersionAssociatedXrefs

