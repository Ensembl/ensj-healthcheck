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

package org.ensembl.healthcheck.testcase.compara;

import java.sql.Connection;
import java.util.ArrayList;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key
 * relationships.
 */

public class CheckSynteny extends SingleDatabaseTestCase {

	/**
	 * Create an CheckSynteny that applies to a specific set of databases.
	 */
	public CheckSynteny() {

		addToGroup("compara_genomic");
		setDescription("Check for missing syntenies in the compara database.");
		setTeamResponsible(Team.COMPARA);

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

		if (!tableHasRows(con, "synteny_region")) {
			ReportManager.problem(this, con,
					"NO ENTRIES in the synteny_region table");
		} else if (!tableHasRows(con, "dnafrag_region")) {
			ReportManager.problem(this, con,
					"NO ENTRIES in the dnafrag_region table");
		} else if (!tableHasRows(con, "dnafrag")) {
			ReportManager.problem(this, con, "NO ENTRIES in the dnafrag table");
		} else {
			String[] method_link_species_set_ids = get_all_method_link_species_set_ids(con);
			for (int i = 0; i < method_link_species_set_ids.length; i++) {
				boolean this_result = check_this_synteny(con,
						method_link_species_set_ids[i]);
				if (!this_result) {
					result = false;
				}
			}
		}

		return result;

	}

	private String[] get_all_method_link_species_set_ids(Connection con) {
		ArrayList method_link_species_set_ids = new ArrayList();

		String[] method_link_ids = DBUtils
				.getColumnValues(
						con,
						"SELECT method_link_id FROM method_link WHERE class LIKE 'SyntenyRegion%' OR type = 'SYNTENY'");
		for (int i = 0; i < method_link_ids.length; i++) {
			String[] these_method_link_ids = DBUtils
					.getColumnValues(
							con,
							"SELECT method_link_species_set_id FROM method_link_species_set WHERE method_link_id = "
									+ method_link_ids[i]);
			for (int j = 0; j < these_method_link_ids.length; j++) {
				method_link_species_set_ids.add(these_method_link_ids[j]);
			}
		}

		return (String[]) method_link_species_set_ids
				.toArray(new String[method_link_species_set_ids.size()]);
	}

	private boolean check_this_synteny(Connection con,
			String method_link_species_set_id) {
		boolean result = true;

		String[] genome_db_ids = DBUtils
				.getColumnValues(
						con,
						"SELECT genome_db_id FROM method_link_species_set LEFT JOIN species_set"
								+ " USING (species_set_id) WHERE method_link_species_set_id = "
								+ method_link_species_set_id);
		String[] alignment_mlss_id = DBUtils
				.getColumnValues(
						con,
						"SELECT mlss2.method_link_species_set_id FROM method_link_species_set mlss1,"
								+ " method_link_species_set mlss2, method_link ml WHERE mlss1.method_link_species_set_id = "
								+ method_link_species_set_id
								+ " AND mlss1.species_set_id = mlss2.species_set_id"
								+ " AND mlss2.method_link_id = ml.method_link_id AND ml.class like 'GenomicAlignBlock%'");
		for (int i = 0; i < genome_db_ids.length; i++) {
			String genome_db_name = DBUtils.getRowColumnValue(con,
					"SELECT name FROM genome_db " + " WHERE genome_db_id = "
							+ genome_db_ids[i]);
			String[] these_dnafrag_ids = DBUtils.getColumnValues(con,
					"SELECT dnafrag_id FROM dnafrag WHERE genome_db_id = "
							+ genome_db_ids[i]
							+ " AND coord_system_name = 'chromosome'"
							+ " AND name NOT LIKE '%\\_%'"
							+ " AND name NOT LIKE '%Un%'"
							+ " AND name NOT IN ('MT') AND length > 1000000");
			for (int j = 0; j < these_dnafrag_ids.length; j++) {
				int count = DBUtils
						.getRowCountFast(
								con,
								"SELECT count(*) FROM synteny_region "
										+ " LEFT JOIN dnafrag_region USING (synteny_region_id) WHERE"
										+ " method_link_species_set_id = "
										+ method_link_species_set_id
										+ " AND dnafrag_id = "
										+ these_dnafrag_ids[j]);
				if (count == 0) {
					int aln_count = 0;
					String aln_name = "";
					String aln_mlss_id = "";
					for (int k = 0; k < alignment_mlss_id.length; k++) {
						String[] aln_result = DBUtils
								.getRowValues(
										con,
										"SELECT dnafrag.name, count(*) FROM"
												+ " genomic_align ga1 LEFT JOIN genomic_align ga2 USING (genomic_align_block_id)"
												+ " LEFT JOIN dnafrag ON (ga2.dnafrag_id = dnafrag.dnafrag_id) WHERE"
												+ " ga1.dnafrag_id = "
												+ these_dnafrag_ids[j]
												+ " AND dnafrag.coord_system_name = 'chromosome'"
												+ " AND ga1.method_link_species_set_id = "
												+ alignment_mlss_id[k]
												+ " AND ga1.dnafrag_id <> ga2.dnafrag_id GROUP BY ga2.dnafrag_id "
												+ " ORDER BY count(*) DESC LIMIT 1");

						if (aln_result.length > 0
								&& Integer.valueOf(aln_result[1]).intValue() > aln_count) {
							aln_count = Integer.valueOf(aln_result[1])
									.intValue();
							aln_name = aln_result[0];
							aln_mlss_id = alignment_mlss_id[k];
						}
					}
					if (aln_count > 1000) {
						String dnafrag_name = DBUtils.getRowColumnValue(con,
								"SELECT name FROM dnafrag "
										+ " WHERE dnafrag_id = "
										+ these_dnafrag_ids[j]);
						String dnafrag_length = DBUtils.getRowColumnValue(con,
								"SELECT length FROM dnafrag "
										+ " WHERE dnafrag_id = "
										+ these_dnafrag_ids[j]);
						ReportManager.problem(this, con, aln_count
								+ " alignments to " + genome_db_name + " chr."
								+ dnafrag_name + " and no syntenies for MLSS "
								+ method_link_species_set_id);
						result = false;
					}
				}
			}
		}

		return result;
	}

} // CheckHomology
