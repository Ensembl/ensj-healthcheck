/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2018] EMBL-European Bioinformatics Institute
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

import java.sql.Connection;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * Checks that seq regions which should have refseq annotation do so
 */

public class SeqRegionsTopLevelRefSeq extends SingleDatabaseTestCase {

	/**
	 * Create a new SeqRegionsTopLevel testcase.
	 */
	public SeqRegionsTopLevelRefSeq() {

		setDescription("Check that all seq_regions comprising genes are marked as toplevel in seq_region_attrib, and that there is at least one toplevel seq_region. Also check that all toplevel seq regions are marked as such, and no seq regions that are marked as toplevel are not toplevel. Will check as well if the toplevel seqregions have information in the assembly table");
		setTeamResponsible(Team.GENEBUILD);
	}

	/**
	 * Data is only tested in core database, as the tables are in sync
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.RNASEQ);
		removeAppliesToType(DatabaseType.CDNA);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *            The database to use.
	 * @return true if the test passed.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		String assemblyAccession = DBUtils.getMetaValue(con,
				"assembly.accession");

		if (!StringUtils.isEmpty(assemblyAccession)
				&& assemblyAccession.contains("GCA")) {

			int topLevelAttribTypeID = getAttribTypeID(con, "toplevel");
			if (topLevelAttribTypeID == -1) {
				return false;
			}

			int karyotypeAttribTypeID = getAttribTypeID(con, "karyotype_rank");
			if (karyotypeAttribTypeID == -1) {
				return false;
			}

			Set<String> synsRefSeqGenomic = getSyns(dbre, "RefSeq_genomic");
			Set<String> topLevelRegions = getRegions(dbre, topLevelAttribTypeID);
			result &= checkSynonyms(dbre, synsRefSeqGenomic, topLevelRegions,
					"RefSeq_genomic", "toplevel");
			Set<String> synsINSDC = getSyns(dbre, "INSDC");
			Set<String> karyotypeRegions = getRegions(dbre,
					karyotypeAttribTypeID);
			result &= checkSynonyms(dbre, synsINSDC, karyotypeRegions, "INSDC",
					"chromosome");
			Set<String> patchRegions = getPatches(dbre);
			result &= checkSynonyms(dbre, synsRefSeqGenomic, patchRegions,
					"RefSeq_genomic", "patch");
		}

		return result;

	} // run

	// --------------------------------------------------------------------------

	private int getAttribTypeID(Connection con, String attrib) {

		String val = DBUtils.getRowColumnValue(con,
				"SELECT attrib_type_id FROM attrib_type WHERE code='" + attrib
						+ "'");
		if (val == null || val.equals("")) {
			ReportManager.problem(this, con,
					"Can't find a seq_region attrib_type with code '" + attrib
							+ "', exiting");
			return -1;
		}
		int attribTypeID = Integer.parseInt(val);

		logger.info("attrib_type_id for '" + attrib + "': " + attribTypeID);

		return attribTypeID;

	}

	// --------------------------------------------------------------------------

	private <T extends CharSequence> boolean checkSynonyms(
			DatabaseRegistryEntry dbre, Collection<T> syns,
			Collection<T> regions, String dbName, String type) {

		boolean result = true;
		
		Connection con = dbre.getConnection();

		Set<T> missing = new HashSet<T>(regions);
		missing.removeAll(syns);

		if (!missing.isEmpty()) {
			ReportManager.problem(this, con, missing.size() + " " + type
					+ " region(s) do not have a " + dbName + " synonym");
			result = false;
		} else {
			ReportManager.correct(this, con, "All " + type
					+ " regions have a synonym for " + dbName);
		}

		return result;
	}

	private Set<String> getSyns(DatabaseRegistryEntry dbre, String dbName) {
		SqlTemplate t = DBUtils.getSqlTemplate(dbre);
		String sql = "SELECT DISTINCT s.name FROM seq_region s, seq_region_synonym ss, external_db e WHERE s.seq_region_id = ss.seq_region_id AND ss.external_db_id = e.external_db_id AND e.db_name = ?";
		List<String> results = t.queryForDefaultObjectList(sql, String.class,
				dbName);
		return new HashSet<String>(results);
	}

	private Set<String> getRegions(DatabaseRegistryEntry dbre, int attribTypeID) {
		SqlTemplate t = DBUtils.getSqlTemplate(dbre);
		String sql = "SELECT DISTINCT s.name FROM seq_region s, seq_region_attrib sa WHERE s.seq_region_id = sa.seq_region_id AND s.name NOT LIKE 'CHR_%' AND s.name NOT LIKE 'LRG%' AND s.name NOT LIKE 'MT' AND attrib_type_id = ? AND s.seq_region_id NOT IN (SELECT s.seq_region_id FROM seq_region s, attrib_type at, seq_region_attrib sa WHERE s.seq_region_id = sa.seq_region_id AND sa.attrib_type_id = at.attrib_type_id AND code = 'codon_table') ";
		List<String> results = t.queryForDefaultObjectList(sql, String.class,
				attribTypeID);
		return new HashSet<String>(results);
	}

	private Set<String> getPatches(DatabaseRegistryEntry dbre) {
		SqlTemplate t = DBUtils.getSqlTemplate(dbre);
		String sql = "SELECT DISTINCT s1.name FROM seq_region s1, seq_region s2, coord_system cs1, coord_system cs2 WHERE s2.name = concat('CHR_', s1.name) AND s1.coord_system_id = cs1.coord_system_id AND cs1.name = 'scaffold' AND s2.coord_system_id = cs2.coord_system_id AND cs2.name = 'chromosome'";
		List<String> results = t.queryForDefaultObjectList(sql, String.class);
		return new HashSet<String>(results);
	}

} // SeqRegionsTopLevel
