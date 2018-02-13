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
 * Copyright (C) 2011 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.variation;

import static org.ensembl.healthcheck.util.CollectionUtils.createLinkedHashSet;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.generic.AbstractCompareSchema;

/**
 * Extension of the {@link AbstractCompareSchema} class which brings schema
 * comparison to variation schemas. This also contains logic for the enforcement
 * of the existence of tables and logic to optionally ignore certain missing
 * tables. These are indicated by the methods
 * 
 * @{link {@link #requiredTables()} and @{link {@link #notRequiredTables()}.
 */
public class CompareVariationSchema extends AbstractCompareSchema {

	private Map<String, Set<String>> nr;
	private Map<String, Set<String>> r;

	@Override
	public void types() {
		addAppliesToType(DatabaseType.VARIATION);
	}

	@Override
	protected void addResponsible() {
		setTeamResponsible(Team.VARIATION);
	}

	/**
	 * We want to continue check the schema even if we know we are missing tables.
	 */
	@Override
	protected boolean skipCheckingIfTablesAreUnequal() {
		return false;
	}

	/**
	 * All tests are applicable
	 */
	@Override
	protected void addTestTypes() {
		Set<TestTypes> types = EnumSet.allOf(TestTypes.class);
		getTestTypes().addAll(types);
	}

	@Override
	protected String getDefinitionFileKey() {
		return "variation_schema.file";
	}

	@Override
	protected String getMasterSchemaKey() {
		return "master.variation_schema";
	}

	/**
	 * These are tables which could be defined in the master schema but can be
	 * missing from the target schema
	 */
	protected Map<String, Set<String>> notRequiredTables() {
		if (nr == null) {
			nr = new HashMap<String, Set<String>>();
			// Uncomment to bring in a table which applies to all species
			// nr.put(String.UNKNOWN, createLinkedHashSet(""));
			nr.put(DatabaseRegistryEntry.HOMO_SAPIENS, createLinkedHashSet("tmp_sample_genotype_single_bp"));
			nr.put(DatabaseRegistryEntry.PAN_TROGLODYTES, createLinkedHashSet("tmp_sample_genotype_single_bp"));
		}
		return nr;
	}

	/**
	 * Set of tables which MUST be in the target schema. One would assume that they
	 * could be missing from the master schema
	 */
	protected Map<String, Set<String>> requiredTables() {
		if (r == null) {
			r = new HashMap<String, Set<String>>();
			r.put(DatabaseRegistryEntry.UNKNOWN, createLinkedHashSet("subsnp_map"));
			// r.put(Species.MUS_MUSCULUS, createLinkedHashSet("strain_gtype_poly"));
			// r.put(Species.RATTUS_NORVEGICUS, createLinkedHashSet("strain_gtype_poly"));
		}
		return r;
	}

	/**
	 * Set of tables which may be in the target schema to aid Mart build. They will
	 * be missing from the master schema.
	 */
	protected Map<String, Set<String>> martTables() {
		if (r == null) {
			r = new HashMap<String, Set<String>>();
			r.put(DatabaseRegistryEntry.UNKNOWN, createLinkedHashSet("MTMP_transcript_variation"));
		}
		return r;
	}

	/**
	 * Override of the test method which makes sure we only test those tables which
	 * are required by the schema. If a table appears in the list specified by
	 * {@link #notRequiredTables()}.
	 */
	@Override
	protected boolean compareTable(Connection master, DatabaseRegistryEntry targetDbre, String table)
			throws SQLException {
		String species = targetDbre.getSpecies();
		Set<String> notRequired = getSets(notRequiredTables(), species);
		Set<String> required = getSets(requiredTables(), species);
		Set<String> martTables = getSets(martTables(), species);
		if (notRequired.contains(table) || required.contains(table) || martTables.contains(table)) {
			return true;
		}
		return super.compareTable(master, targetDbre, table);
	}

	/**
	 * Re-implementation of the compare tables in schema method used to detect if
	 * two schemas are equal to each other. This is due to variation specific logic
	 * which means that schemas can differ but this is still valid.
	 * 
	 * The logic is the same as variation's original version
	 * 
	 * @param master
	 *            The master connection
	 * @param targetDbre
	 *            The registry entry for the given target schema
	 * @param ignoreBackupTables
	 *            Ignored option
	 * @param directionFlag
	 *            Ignored option
	 */
	@Override
	public boolean compareTableEquality(Connection master, DatabaseRegistryEntry targetDbre, boolean ignoreBackupTables,
			int directionFlag) {
		boolean result = true;

		Connection target = targetDbre.getConnection();
		String targetName = getDbNameForMsg(target);
		String masterName = getDbNameForMsg(master);

		Set<String> targetTables = createLinkedHashSet(getTableNames(target));
		Set<String> masterTables = createLinkedHashSet(getTableNames(master));

		String species = targetDbre.getSpecies();

		Set<String> notRequired = getSets(notRequiredTables(), species);
		Set<String> required = getSets(requiredTables(), species);

		// Check that tables that are in the master are in the target & skip those
		// in the notRequired list
		for (String masterTable : masterTables) {
			if (notRequired.contains(masterTable)) {
				String msg = String.format(
						"Table `%s` is in the list of 'notRequiredTables()' exceptions for %s. Skipping", masterTable,
						species);
				ReportManager.info(this, target, msg);
				continue;
			}
			if (!targetTables.contains(masterTable)) {
				String msg = String.format("Table `%s` exists in `%s` but not in `%s`", masterTable, masterName,
						targetName);
				ReportManager.problem(this, target, msg);
				result = false;
			}
		}

		// Check that tables in the target are in the master & it was a required table
		for (String targetTable : targetTables) {
			if (!masterTables.contains(targetTable) && !required.contains(targetTable)) {
				String msg = String.format("Table `%s` exists in `%s` but not in `%s`", targetTable, targetName,
						masterName);
				ReportManager.problem(this, target, msg);
				result = false;
			}
		}

		// Finally check for the required tables
		for (String table : required) {
			if (!targetTables.contains(table)) {
				String msg = String.format("Table `%s` does not exist in `%s`", table, targetName);
				ReportManager.problem(this, target, msg);
				result = false;
			}
		}

		return result;
	}

	/**
	 * Returns a set which represents the tables which apply to all species (those
	 * who are UNKNOWN) and to the given species.
	 */
	private Set<String> getSets(Map<String, Set<String>> input, String species) {
		Set<String> output = createLinkedHashSet();
		if (input.containsKey(DatabaseRegistryEntry.UNKNOWN)) {
			output.addAll(input.get(DatabaseRegistryEntry.UNKNOWN));
		}
		if (input.containsKey(species)) {
			output.addAll(input.get(species));
		}
		return output;
	}

}
