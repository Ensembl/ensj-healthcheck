/*
	Copyright (C) 2003 EBI, GRL

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

package org.ensembl.healthcheck.testcase;

import java.sql.Connection;

import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.TestResult;
import org.ensembl.healthcheck.util.DatabaseConnectionIterator;

/**
 * Checks that the archive tables are up to date.
 */
public class CheckArchiveTestCase extends EnsTestCase {

	public CheckArchiveTestCase() {
		addToGroup("check_archive");
		setDescription("Checks the archive tables are up to date.");
	}

	public TestResult run() {

		boolean result = true;

		DatabaseConnectionIterator it = getDatabaseConnectionIterator();

		System.out.println("Warning - some tests are commented out for speed");

		while (it.hasNext()) {

			Connection con = (Connection)it.next();

			System.out.println("Checking tables exist and are populated");
			result = checkTablesExistAndPopulated(con) && result;
			System.out.println("Checking for null strings");
			result = checkNoNullStrings(con) && result;
			System.out.println("Checking archive integrity");
			result = checkArchiveIntegrity(con) && result;
			System.out.println("Checking DB name format in mapping_session");
			result = checkDBNameFormat(con) && result;
			System.out.println("Checking mapping_session chaining");
			result = checkMappingSessionChaining(con) && result;
			System.out.println("Checking mapping_session/stable_id_event keys");
			result = checkMappingSessionStableIDKeys(con) && result;
			System.out.println("Checking that ALL/LATEST mapping session has most entries");
			result = checkAllLatest(con) && result;

			System.out.println("Checking deleted genes");
			result = checkDeletedInGeneArchive(con, "gene", "G", 355) && result;
			System.out.println("Checking deleted transcripts");
			result = checkDeletedInGeneArchive(con, "transcript", "T", 355) && result;
			//System.out.println("Checking deleted translations");
			//result = checkDeletedInGeneArchive(con, "translation", "P", 355) && result;
			//System.out.println("Checking changed translations");
			//result = checkChangedInGeneArchive(con, "translation", "P", 355) && result;
			System.out.println("Checking changed transcript");
			result = checkChangedInGeneArchive(con, "transcript", "T", 355) && result;
			System.out.println("Checking changed genes");
			result = checkChangedInGeneArchive(con, "gene", "G", 355) && result;
			//System.out.println("Checking deleted translations in peptide archive");
			//result = checkDeletedTranslationsInPeptideArchive(con, 355) && result;
			//System.out.println("Checking deleted translations in peptide archive");
			//result = checkChangedTranslationsInPeptideArchive(con, 355) && result;
			//System.out.println("Checking translations from peptide archive in gene archive");
			//result = checkTranslationsFromPeptideArchiveInGeneArchive(con) && result;
			//System.out.println("Checking no current translations in peptide archive");
			//result = checkNoCurrentTranslationsInPeptideArchive(con) && result;
			System.out.println("Checking gene propagation IDs are current");
			result = checkPropagationIDsAreCurrent(con, "gene", "G");
			System.out.println("Checking transcript propagation IDs are current");
			result = checkPropagationIDsAreCurrent(con, "transcript", "T");
			//System.out.println("Checking translation propagation IDs are current");
			//result = checkPropagationIDsAreCurrent(con, "translation", "P");

		}

		return new TestResult(getShortTestName(), result);
	}

	/**
	 * Checks tables exist and have >0 rows.
	 * @param con
	 * @return
	 */
	private boolean checkTablesExistAndPopulated(Connection con) {
		String tables[] = new String[] { "stable_id_event", "mapping_session", "gene_archive", "peptide_archive" };

		boolean result = true;

		for (int i = 0; i < tables.length; i++) {
			String table = tables[i];
			boolean exists = checkTableExists(con, table);
			if (exists) {
				if (countRowsInTable(con, table) == 0) {
					ReportManager.problem(this, con, "Empty table:" + table);
					result = false;
				}
			} else {
				ReportManager.problem(this, con, "Missing table:" + table);
				result = false;
			}
		}

		return result;
	}

	private boolean checkDeletedTranslationsInPeptideArchive(Connection con, long minMappingSessionID) {
		boolean result = true;

		String sql =
			"SELECT CONCAT(old_stable_id, \".\", old_version) "
				+ "FROM stable_id_event LEFT JOIN peptide_archive "
				+ "                     ON old_stable_id=translation_stable_id "
				+ "WHERE "
				+ "     mapping_session_id >= "
				+ minMappingSessionID
				+ " "
				+ "     AND old_stable_id like \"%P%\" "
				+ "     AND new_stable_id is NULL "
				+ "     AND translation_stable_id is NULL;";

		String[] rows = getColumnValues(con, sql);
		if (rows.length > 0) {
			StringBuffer msg = new StringBuffer();
			msg.append("Deleted translation missing from peptide_archive");
			for (int i = 0; i < rows.length && rows.length < 10; i++) {
				msg.append(rows[i]).append("\n");
			}

			ReportManager.problem(this, con, msg.toString());
			result = false;
		}

		return result;
	}

	private boolean checkChangedTranslationsInPeptideArchive(Connection con, long minMappingSessionID) {

		boolean result = true;

		String sql =
			"SELECT CONCAT(old_stable_id, \".\", old_version) "
				+ "FROM stable_id_event LEFT JOIN peptide_archive "
				+ "                     ON old_stable_id=translation_stable_id "
				+ "WHERE "
				+ "     mapping_session_id >= "
				+ minMappingSessionID
				+ " "
				+ "     AND old_stable_id like \"%P%\" "
				+ "     AND new_stable_id=old_stable_id "
				+ "     AND old_version!=new_version "
				+ "     AND translation_stable_id is NULL;";

		String[] rows = getColumnValues(con, sql);
		if (rows.length > 0) {
			StringBuffer msg = new StringBuffer();
			msg.append("Updated Translations missing from peptide_archive.");
			for (int i = 0; i < rows.length && rows.length < 10; i++) {
				msg.append(rows[i]).append("\n");
			}

			ReportManager.problem(this, con, msg.toString());
			result = false;
		}

		return result;
	}

	private boolean checkTranslationsFromPeptideArchiveInGeneArchive(Connection con) {

		boolean result = true;

		String sql =
			"SELECT CONCAT( pa.translation_stable_id, \".\", pa.translation_version) "
				+ " FROM  peptide_archive pa LEFT JOIN gene_archive ga "
				+ "                          ON  ga.translation_stable_id=pa.translation_stable_id "
				+ "                              AND	 ga.translation_version=pa.translation_version "
				+ " WHERE ga.translation_stable_id is NULL;";

		String[] rows = getColumnValues(con, sql);
		if (rows.length > 0) {
			StringBuffer msg = new StringBuffer();
			for (int i = 0; i < rows.length && rows.length < 10; i++) {
				msg.append(rows[i]).append("\n");
			}

			ReportManager.problem(this, con, msg.toString());
			result = false;
		}

		return result;
	}

	private boolean checkNoCurrentTranslationsInPeptideArchive(Connection con) {

		boolean result = true;

		String sql =
			"SELECT CONCAT(ts.stable_id , \".\",ts.version)"
				+ " FROM translation_stable_id ts, peptide_archive pa "
				+ " WHERE ts.stable_id=pa.translation_stable_id "
				+ "       AND ts.version= pa.translation_version;";
		String[] rows = getColumnValues(con, sql);
		if (rows.length > 0) {
			StringBuffer msg = new StringBuffer();
			for (int i = 0; i < rows.length && rows.length < 10; i++) {
				msg.append(rows[i]).append("\n");
			}

			ReportManager.problem(this, con, msg.toString());
			result = false;
		}

		return result;
	}

	/** this quite a slow query, about 1min to hopefulyy return nothing. */
	private boolean checkPropagationIDsAreCurrent(Connection con, String type, String filter) {
		// select * from stable_id_event sie left join gene_stable_id gsi on sie.new_stable_id = gsi.stable_id where mapping_session_id = 348 and sie.new_stable_id like "ENSG%" and gsi.stable_id is null													String type, String filter) {

		boolean result = true;

		String sql =
			"SELECT sie.new_stable_id "
				+ "FROM stable_id_event sie "
				+ "         LEFT_JOIN "
				+ type
				+ "_stable_id tsi "
				+ "         ON sie.new_stable_id = tsi.stable_id "
				+ "         mapping_session ms "
				+ " WHERE ms.old_database_name=\"ALL\" "
				+ "       AND sie.mapping_session_id = ms.mapping_session_id "
				+ "       AND sie.new_stable_id like \"%"
				+ filter
				+ "%\" "
				+ "       AND tsi.stable_id is NULL";

		return result;
	}

	/**
	 * Checks that all the changed _type_s are included in the gene_archive. A change has occured if the
	 * version is different.
	 * @param con connection on which to execute queries
	 * @param type type of item deleted
	 * @param filter substring to use use to filter relevant stableIDs, 
	 * will be used in SQL as "%FILTER%".
	 * @param minMappingSessionID mapping_session_id for first mapping session
	 * which contains arechive data. This is needed because no archive data exists
	 * for previous release.
	 * @return whether the test succeeded.
	 */
	private boolean checkChangedInGeneArchive(Connection con, String type, String filter, long minMappingSessionID) {
		boolean result = true;

		String sql =
			"SELECT CONCAT(old_stable_id, \".\", old_version) "
				+ "FROM stable_id_event sie LEFT JOIN gene_archive ga "
				+ "                     ON old_stable_id="
				+ type
				+ "_stable_id "
				+ "WHERE "
				+ "     sie.mapping_session_id >= "
				+ minMappingSessionID
				+ " "
				+ "     AND sie.mapping_session_id=ga.mapping_session_id "
				+ "     AND old_stable_id like \"%"
				+ filter
				+ "%\" "
				+ "     AND gene_stable_id is NULL "
				+ "     AND new_stable_id=old_stable_id and old_version!=new_version;";

		String[] rows = getColumnValues(con, sql);
		if (rows.length > 0) {
			StringBuffer msg = new StringBuffer();
			msg.append(rows.length + " deleted " + type + "s not in gene_archive ");
			for (int i = 0; i < rows.length && rows.length < 10; i++) {
				msg.append(rows[i]).append("\n");
			}

			ReportManager.problem(this, con, msg.toString());
			result = false;
		}

		return result;

	}

	/**
	 * Checks that all the deleted _type_s are included in the gene_archive.
	 * @param con connection on which to execute queries
	 * @param type type of item deleted
	 * @param filter substring to use use to filter relevant stableIDs, 
	 * will be used in SQL as "%FILTER%".
	 * @param minMappingSessionID mapping_session_id for first mapping session
	 * which contains arechive data. This is needed because no archive data exists
	 * for previous release.
	 * @return whether the test succeeded.
	 */
	private boolean checkDeletedInGeneArchive(Connection con, String type, String filter, long minMappingSessionID) {

		boolean result = true;

		String sql =
			"SELECT CONCAT(old_stable_id, \".\", old_version) "
				+ "FROM stable_id_event sie LEFT JOIN gene_archive ga "
				+ "                     ON old_stable_id="
				+ type
				+ "_stable_id "
				+ "WHERE "
				+ "   sie.mapping_session_id >= "
				+ minMappingSessionID
				+ " "
				+ "   AND sie.mapping_session_id=ga.mapping_session_id "
				+ "   AND old_stable_id like \"%"
				+ filter
				+ "%\""
				+ "   AND new_stable_id is NULL "
				+ "   AND "
				+ type
				+ "_stable_id is NULL;";
		String[] rows = getColumnValues(con, sql);
		if (rows.length > 0) {
			StringBuffer msg = new StringBuffer();
			msg.append(rows.length + " deleted " + type + "s not in gene_archive ");
			for (int i = 0; i < rows.length && rows.length < 10; i++) {
				msg.append(rows[i]).append("\n");
			}

			ReportManager.problem(this, con, msg.toString());
			result = false;
		}

		return result;
	}

	/**
	 * @param con
	 * @return
	 */
	private boolean checkArchiveIntegrity(Connection con) {
		// TODO Auto-generated method stub
		return true;
	}

	/**
	 * Check no "NULL" or "null" strings in stable_id_event.new_stable_id or stable_id_event.oldable_id.
	 * @param con
	 * @return
	 */
	private boolean checkNoNullStrings(Connection con) {

		boolean result = true;

		int rows = getRowCount(con, "select count(*) from stable_id_event sie where	new_stable_id='NULL'");
		if (rows > 0) {
			ReportManager.problem(
				this,
				con,
				rows + " rows in stable_id_event.new_stable_id contains \"NULL\" string instead of NULL value.");
			result = false;
		}

		rows = getRowCount(con, "select count(*) from stable_id_event sie where	new_stable_id='null'");
		if (rows > 0) {
			ReportManager.problem(
				this,
				con,
				rows + " rows in stable_id_event.new_stable_id contains \"null\" string instead of NULL value.");
			result = false;
		}

		rows = getRowCount(con, "select count(*) from stable_id_event sie where	old_stable_id='NULL'");
		if (rows > 0) {
			ReportManager.problem(
				this,
				con,
				rows + " rows in stable_id_event.old_stable_id contains \"NULL\" string instead of NULL value.");
			result = false;
		}

		rows = getRowCount(con, "select count(*) from stable_id_event sie where	old_stable_id='null'");
		if (rows > 0) {
			ReportManager.problem(
				this,
				con,
				rows + " rows in stable_id_event.old_stable_id contains \"null\" string instead of NULL value.");
			result = false;
		}

		// todo: add this auto-fix code?

		//		#update stable_id_event set old_stable_id=NULL where
		//		#old_stable_id="NULL"

		//		#update stable_id_event set new_stable_id=NULL where
		//		#new_stable_id="NULL"	

		return result;
	}

	// -----------------------------------------------------------------
	/**
	 * Check that the old_db_name and new_db_name columns "chain" together.
	 */
	private boolean checkMappingSessionChaining(Connection con) {

		boolean result = true;

		String[] oldNames = getColumnValues(con, "SELECT old_db_name FROM mapping_session WHERE old_db_name <> 'ALL' ORDER BY created");
		String[] newNames = getColumnValues(con, "SELECT new_db_name FROM mapping_session WHERE new_db_name <> 'LATEST' ORDER BY created");

		for (int i = 1; i < oldNames.length; i++) {
			if (!(oldNames[i].equalsIgnoreCase(newNames[i - 1]))) {
				ReportManager.problem(this, con, "Old/new names " + oldNames[i] + " " + newNames[i - 1] + " do not chain properly");
				result = false;
			}
		}

		ReportManager.correct(this, con, "Old/new db name chaining in mapping_session seems OK");

		return result;

	}

	// -----------------------------------------------------------------
	/**
	 * Check that all mapping_sessions have entries in stable_id_event and vice-versa.
	 */
	private boolean checkMappingSessionStableIDKeys(Connection con) {

		boolean result = true;

		int orphans = countOrphans(con, "mapping_session", "mapping_session_id", "stable_id_event", "mapping_session_id", false);
		if (orphans > 0) {
			ReportManager.problem(this, con, orphans + " dangling references between mapping_session and stable_id_event tables");
			result = false;
		} else {
			ReportManager.correct(this, con, "All mapping_session/stable_id_event keys are OK");
		}

		return result;

	}

	// -----------------------------------------------------------------
	/** 
	 * Check that the ALL/LATEST mapping session has more entries than 
	 * the others
	 */
	private boolean checkAllLatest(Connection con) {

		boolean result = true;

		// Following query should give one result - the ALL/LATEST one - if all is well
		String sql =
			"SELECT ms.old_db_name, ms.new_db_name, count(*) AS entries "
				+ "FROM stable_id_event sie, mapping_session ms "
				+ "WHERE sie.mapping_session_id=ms.mapping_session_id "
				+ "GROUP BY ms.mapping_session_id ORDER BY entries DESC LIMIT 1";

		String oldDBName = getRowColumnValue(con, sql);
		if (!(oldDBName.equalsIgnoreCase("ALL"))) {
			ReportManager.problem(this, con, "ALL/LATEST mapping session does not seem to have the most stable_id_event entries");
			result = false;
		} else {
			ReportManager.correct(this, con, "ALL/LATEST mapping session seems to have the most stable_id_event entries");
		}

		return result;

	}

	// -----------------------------------------------------------------
	/**
	 * Check format of old/new DB names in mapping_session.
	 */
	private boolean checkDBNameFormat(Connection con) {

		boolean result = true;
		String dbNameRegexp = CORE_DB_REGEXP;

		String[] sql =
			{
				"SELECT old_db_name from mapping_session WHERE old_db_name <> 'ALL'",
				"SELECT new_db_name from mapping_session WHERE new_db_name <> 'LATEST'" };

		for (int i = 0; i < sql.length; i++) {

			String[] names = getColumnValues(con, sql[i]);
			for (int j = 0; j < names.length; j++) {
				if (!(names[j].matches(dbNameRegexp))) {
					ReportManager.problem(
						this,
						con,
						"Database name " + names[j] + " in mapping_session does not appear to be in the correct format");
					result = false;
				}
			}

		}

		if (result == true) {
			ReportManager.correct(this, con, "All database names in mapping_session appear to be in the correct format");
		}

		return result;
	}

	// -----------------------------------------------------------------
	/**
	 * Runs this TestCase via org.ensembl.healthcheck.TextTestRunner. Uses
	 * default if no parameters provided.
	 * @param args command line parameters to TextTestRunner.
	 */
	public static void main(String[] args) {
		if (args.length == 0)
			args = "-d danio_rerio_core_15_2 -config database.properties.ecs2dforward check_archive".split(" ");
		org.ensembl.healthcheck.TextTestRunner.main(args);
	}

}
