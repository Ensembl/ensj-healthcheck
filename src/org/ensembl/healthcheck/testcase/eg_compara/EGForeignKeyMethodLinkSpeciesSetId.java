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

package org.ensembl.healthcheck.testcase.eg_compara;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;
import org.ensembl.healthcheck.util.AbstractStringMapRowMapper;
import org.ensembl.healthcheck.util.CollectionUtils;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.DefaultMapRowMapper;
import org.ensembl.healthcheck.util.MapRowMapper;

public class EGForeignKeyMethodLinkSpeciesSetId extends
		AbstractTemplatedTestCase {

	public EGForeignKeyMethodLinkSpeciesSetId() {
		setTeamResponsible(Team.ENSEMBL_GENOMES);
		appliesToType(DatabaseType.COMPARA);
		setDescription("Checks the consistency of MLSS foreign keys");
	}

	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean result = true;
		result &= assertNoEmptyNames(dbre);
		result &= assertNoSource(dbre);
		result &= assertMethodLinkSpeciesSetCounts(dbre);
		if (DBUtils.getShortDatabaseName(dbre.getConnection()).contains(System.getProperty("compara_master.database"))) {
			return result;
		}
		result &= assertMlssIdForeignKeysAndRanges(dbre);
		result &= assertMlssGeneTreeRootOrphans(dbre);
		result &= assertGeneTreeRootOrphans(dbre);
		result &= assertMlssGenomicAlignOrphans(dbre);
		result &= assertGenomicAlignOrphans(dbre);
		return result;
	}

	/**
	 * Check for MLSS which lack a name
	 */
	protected boolean assertNoEmptyNames(DatabaseRegistryEntry dbre) {
		boolean result = true;
		Connection con = dbre.getConnection();
		int numOfUnsetNames = DBUtils
				.getRowCount(
						con,
						"SELECT count(*) FROM method_link_species_set WHERE name = 'NULL' OR name IS NULL");
		if (numOfUnsetNames > 0) {
			ReportManager.problem(this, con,
					"FAILED method_link_species_set table contains "
							+ numOfUnsetNames + " with no name");
			result = false;
		}
		return result;
	}

	/**
	 * Check for MLSS which lack a source
	 */
	protected boolean assertNoSource(DatabaseRegistryEntry dbre) {
		boolean result = true;
		Connection con = dbre.getConnection();
		int numOfUnsetSources = DBUtils
				.getRowCount(
						con,
						"SELECT count(*) FROM method_link_species_set WHERE source = 'NULL' OR source IS NULL");
		if (numOfUnsetSources > 0) {
			ReportManager.problem(this, con,
					"FAILED method_link_species_set table contains "
							+ numOfUnsetSources + " with no source");
			result = false;
		}
		return result;
	}

	/**
	 * Loops through all known method link types from
	 * {@link #getMethodLinkTypeToTable()} and uses
	 * {@link #getMethodLinkTypeRange()} to assert that all link method link
	 * species set identifiers have the correct method_link_id range
	 */
	protected boolean assertMlssIdForeignKeysAndRanges(
			DatabaseRegistryEntry dbre) {
		boolean result = true;

		Connection con = dbre.getConnection();
		Map<String, String> typeToTable = getMethodLinkTypeToTable();
		Map<String, List<Integer>> typeToRanges = getMethodLinkTypeRange();

		for (Map.Entry<String, String> entry : typeToTable.entrySet()) {
			String type = entry.getKey();
			String table = entry.getValue();
			List<Integer> ranges = typeToRanges.get(type);
			Integer lower = ranges.get(0);
			Integer upper = ranges.get(1);

			result &= checkForOrphansWithConstraint(con,
					"method_link_species_set", "method_link_species_set_id",
					table, "method_link_species_set_id", "method_link_id >= "
							+ lower + " and method_link_id < " + upper);
			result &= checkForOrphans(con, table, "method_link_species_set_id",
					"method_link_species_set", "method_link_species_set_id");
		}

		return result;
	}

	/**
	 * Check for the number of MLSS unlinked to a protein tree and those protein
	 * tree members unlinked to a MLSS
	 */
	protected boolean assertMlssGeneTreeRootOrphans(
			DatabaseRegistryEntry dbre) {
		return checkForOrphansWithConstraint(
				dbre.getConnection(),
				"method_link_species_set",
				"method_link_species_set_id",
				"gene_tree_root",
				"method_link_species_set_id",
				"method_link_id IN (SELECT method_link_id FROM method_link WHERE class LIKE 'ProteinTree.%')");
	}

	protected boolean assertGeneTreeRootOrphans(DatabaseRegistryEntry dbre) {
		return checkForOrphans(dbre.getConnection(), "gene_tree_root",
				"method_link_species_set_id", "method_link_species_set",
				"method_link_species_set_id");
	}

	protected boolean assertMlssGenomicAlignOrphans(
			DatabaseRegistryEntry dbre) {
		return checkForOrphansWithConstraint(
				dbre.getConnection(),
				"method_link_species_set",
				"method_link_species_set_id",
				"genomic_align_block",
				"method_link_species_set_id",
				"method_link_id BETWEEN 1 AND 99 AND method_link_id != 11");
	}

	protected boolean assertGenomicAlignOrphans(DatabaseRegistryEntry dbre) {
		return checkForOrphans(dbre.getConnection(), "genomic_align_block",
				"method_link_species_set_id", "method_link_species_set",
				"method_link_species_set_id");
	}

	// Hashed out because we do not do this kind of analysis yet
	// protected boolean assertNCTreeMethodLinkSpeciesSet(
	// DatabaseRegistryEntry dbre) {
	// return checkForOrphansWithConstraint(
	// dbre.getConnection(),
	// "method_link_species_set",
	// "method_link_species_set_id",
	// "nc_tree_member",
	// "method_link_species_set_id",
	// "method_link_id IN (SELECT method_link_id FROM method_link WHERE class LIKE 'NCTree.%')");
	// }

	/**
	 * loops through all method link species sets where an expected count is
	 * known from {@link #getMethodLinkTypeToExpectedCounts()} and asserts that
	 * the number of species in the method link species set is equal to one of
	 * those values
	 */
	protected boolean assertMethodLinkSpeciesSetCounts(
			DatabaseRegistryEntry dbre) {
		boolean result = true;

		Map<String, List<Long>> methodLinkToMlssId = getMethodLinkTypeToMlssId(dbre);
		Map<Long, Integer> mlssIdToCount = getMlssIdCount(dbre);
		Map<String, List<Integer>> methodLinkTypeExpectedCounts = getMethodLinkTypeToExpectedCounts();

		for (Map.Entry<String, List<Long>> methodLink : methodLinkToMlssId
				.entrySet()) {
			String methodLinkType = methodLink.getKey();
			if (!methodLinkTypeExpectedCounts.containsKey(methodLinkType))
				continue;

			for (Long methodLinkSpeciesSetId : methodLink.getValue()) {
				Integer count = mlssIdToCount.get(methodLinkSpeciesSetId);
				if (count != null) {
					boolean countOkay = false;
					List<Integer> expectedCounts = methodLinkTypeExpectedCounts
							.get(methodLinkType);
					for (int expected : expectedCounts) {
						if (count == expected) {
							countOkay = true;
							break;
						}
					}

					if (!countOkay) {
						result = false;
						String expecteds = StringUtils
								.join(expectedCounts, ',');
						ReportManager.problem(this, dbre.getConnection(),
								"MLSS ID " + methodLinkSpeciesSetId
										+ " of type " + methodLinkType
										+ " count was " + count
										+ ". We expected [" + expecteds + "]");
					}
				} else {
					ReportManager.problem(this, dbre.getConnection(),
							"No count found for MLSS ID "
									+ methodLinkSpeciesSetId + " of type "
									+ methodLinkType);
				}
			}
		}

		return result;
	}

	protected Map<Long, Integer> getMlssIdCount(DatabaseRegistryEntry dbre) {
		return getTemplate(dbre)
				.queryForMap(
						"select mlss.method_link_species_set_id, count(*) from method_link_species_set mlss join species_set ss using (species_set_id) group by mlss.method_link_species_set_id",
						new DefaultMapRowMapper<Long, Integer>(Long.class,
								Integer.class));
	}

	protected Map<String, List<Long>> getMethodLinkTypeToMlssId(
			DatabaseRegistryEntry dbre) {
		MapRowMapper<String, List<Long>> mapper = new AbstractStringMapRowMapper<List<Long>>() {
			@Override
			public List<Long> mapRow(ResultSet resultSet, int position)
					throws SQLException {
				List<Long> longs = CollectionUtils.createArrayList();
				existingObject(longs, resultSet, position);
				return longs;
			}

			@Override
			public void existingObject(List<Long> currentValue,
					ResultSet resultSet, int position) throws SQLException {
				currentValue.add(resultSet.getLong(2));
			}
		};
		return getTemplate(dbre)
				.queryForMap(
						"select ml.type, mlss.method_link_species_set_id from method_link ml join method_link_species_set mlss using (method_link_id)",
						mapper);
	}

	protected Map<String, List<Integer>> getMethodLinkTypeToExpectedCounts() {
		Map<String, List<Integer>> output = CollectionUtils.createHashMap();
		List<Integer> pairwise = Arrays.asList(2);
		output.put("ENSEMBL_ORTHOLOGUES", pairwise);
		output.put("ENSEMBL_PARALOGUES", Arrays.asList(1, 2));
		output.put("BLASTZ_NET", pairwise);
		output.put("LASTZ_NET", pairwise);
		output.put("TRANSLATED_BLAT_NET", pairwise);
		return output;
	}

	protected Map<String, List<Integer>> getMethodLinkTypeRange() {
		Map<String, List<Integer>> output = CollectionUtils.createHashMap();
		output.put("ENSEMBL_ORTHOLOGUES", Arrays.asList(201, 202));
		output.put("ENSEMBL_PARALOGUES", Arrays.asList(202, 300));
		output.put("SYNTENY", Arrays.asList(101, 200));
		output.put("FAMILY", Arrays.asList(301, 400));
		return output;
	}

	protected Map<String, String> getMethodLinkTypeToTable() {
		Map<String, String> output = CollectionUtils.createHashMap();
		output.put("ENSEMBL_ORTHOLOGUES", "homology");
		output.put("ENSEMBL_PARALOGUES", "homology");
		output.put("SYNTENY", "synteny_region");
		output.put("FAMILY", "family");
		return output;
	}
}
