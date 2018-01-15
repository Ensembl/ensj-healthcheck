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


package org.ensembl.healthcheck.testcase.compara;

import java.sql.Connection;

import java.util.HashSet;
import java.util.Set;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.compara.AbstractComparaTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.CollectionUtils;

/**
 * An EnsEMBL Healthcheck test case that checks that the sizes of the
 * Compara tables are similar to the previous release
 */

public class CheckTableSizes extends AbstractComparaTestCase {

	public CheckTableSizes() {
		setDescription("Checks the size of the Compara tables");
		setTeamResponsible(Team.COMPARA);
	}


	/**
	 * Define what tables are to be checked.
	 */
	private Set<String> getTablesToCheck(final DatabaseRegistryEntry dbre) {

		// get the full list of tables
		Set<String> tables = CollectionUtils.createLinkedHashSet(DBUtils.getTableNames(dbre.getConnection()));

		// remove views since we don't care if they're empty
		Set<String> views = CollectionUtils.createLinkedHashSet(DBUtils.getViews(dbre.getConnection()).toArray(new String[]{}));
		tables.removeAll(views);

		// Tables we don't want to check
		tables.remove( "meta" );

		return tables;
	}


	/**
	 * Kick-off a comparison between the current database and all the
	 * previous ones
	 */
	public boolean run(final DatabaseRegistryEntry dbre) {

		DatabaseRegistryEntry lastReleaseDbre = getLastComparaReleaseDbre(dbre);

		if (lastReleaseDbre == null) {
			ReportManager.problem( this, dbre.getConnection(),
					"Cannot find the compara database in the secondary server. This check expects to find a previous version of the compara database for checking that all the *named* species_sets are still present in the current database.");
			return false;
		}

		boolean result = true;
		result &= compareTableSizes(dbre, lastReleaseDbre);
		return result;

	} // run

	/**
	 * Reports the tables that are now missing or empty, the new
	 * tables, and the tables with a signficantly different size.
	 * NB: Thresholds are -5% and +10%
	 */
	public boolean compareTableSizes(final DatabaseRegistryEntry primaryComparaDbre, final DatabaseRegistryEntry secondaryComparaDbre) {

		Set<String> tables1 = getTablesToCheck(primaryComparaDbre);
		Set<String> tables2 = getTablesToCheck(secondaryComparaDbre);

		Connection con1 = primaryComparaDbre.getConnection();
		Connection con2 = secondaryComparaDbre.getConnection();

		boolean result = true;

		for (String table : tables2) {
			int count2 = DBUtils.countRowsInTable(con2, table);
			if (tables1.contains(table)) {
				int count1 = DBUtils.countRowsInTable(con1, table);
				if (count1 == 0 && count2 > 0) {
					result = false;
					ReportManager.problem( this, con1, String.format("Table %s is now empty but had %,d rows in %s.", table, count2, DBUtils.getShortDatabaseName(con2)));
				} else if (count1 > 0 && count2 == 0) {
					result = false;
					ReportManager.problem( this, con1, String.format("Table %s has %,d rows but was empty in %s.", table, count1, DBUtils.getShortDatabaseName(con2)));
				} else if (count1 == count2 && count1 > 0) {
					result = false;
					ReportManager.problem( this, con1, String.format("Table %s has exactly the same number of rows (%,d) as in %s. Is it expected ?", table, count2, DBUtils.getShortDatabaseName(con2)));
				} else if (count1 < (int) (.95 * count2) || count2 > (int) (1.10 * count2)) {
					result = false;
					ReportManager.problem( this, con1, String.format("Table %s had %,d rows in %s, but now has %,d rows (%+,d = %+.2f %%)", table, count2, DBUtils.getShortDatabaseName(con2), count1, count1-count2, (100.*(count1-count2))/count2));
				}
			} else {
				result = false;
				ReportManager.problem( this, con1, String.format("Table %s was in %s (and had %,d rows) but is now missing.", table, DBUtils.getShortDatabaseName(con2), count2));
			}
		}

		for (String table : tables1) {
			if (! tables2.contains(table)) {
				result = false;
				int count1 = DBUtils.countRowsInTable(con1, table);
				if (count1 == 0) {
					ReportManager.problem( this, con1, String.format("Table %s is new (comparing to %s) but empty.", table, DBUtils.getShortDatabaseName(con2)));
				} else {
					ReportManager.problem( this, con1, String.format("Table %s is new (comparing to %s) and has %,d rows.", table, DBUtils.getShortDatabaseName(con2), count1));
				}
			}
		}

		return result;
	}

} // CheckEmptyTables
