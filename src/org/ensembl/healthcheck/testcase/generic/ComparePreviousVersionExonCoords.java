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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Compare the transcript stable IDs and exon coordinates between 2 releases. Note this is not comparing counts so doesn't extend
 * ComparePreviousVersionBase. Note this reads 2 complete exon sets into memory and so needs quite a bit of memory allocated.
 * Suggest -Xmx1700m
 */

public class ComparePreviousVersionExonCoords extends SingleDatabaseTestCase {

	/**
	 * Create a new testcase.
	 */
	public ComparePreviousVersionExonCoords() {

		addToGroup("release");
		addToGroup("pre-compara-handover");
		addToGroup("post-compara-handover");
		
		setDescription("Compare the transcript stable IDs and exon coordinates for each exon across releases to ensure that protein sequences are the same.");
		setEffect("Causes problems for Compara if proteins are not identical");
		setTeamResponsible(Team.CORE);
		setSecondTeamResponsible(Team.GENEBUILD);

	}

	/**
	 * This test Does not apply to sanger_vega dbs
	 */
	public void types() {
		removeAppliesToType(DatabaseType.SANGER_VEGA);
    removeAppliesToType(DatabaseType.RNASEQ);
	}

	// ----------------------------------------------------------------------

	public boolean run(DatabaseRegistryEntry current) {

		boolean result = true;

		if (System.getProperty("ignore.previous.checks") != null) {
			logger.finest("ignore.previous.checks is set in database.properties, skipping this test");
			return true;
		}

		Connection currentCon = current.getConnection();

		// skip databases where there's no previous one (e.g. new species)
		DatabaseRegistryEntry previous = getEquivalentFromSecondaryServer(current);
		if (previous == null) {
			ReportManager.correct(this, currentCon, "Can't identify previous database - new species?");
			return true;
		}
		Connection previousCon = previous.getConnection();

		// and those where the genebuild version has changed - expect exon coords to change then
		// if we can't get the genebuild version (due to a non-standard database name for example, check anyway)
		int currentVersion = current.getNumericGeneBuildVersion();
		int previousVersion = previous.getNumericGeneBuildVersion();

		if (currentVersion > 0 && previousVersion > 0 && currentVersion != previousVersion) {
			ReportManager.correct(this, currentCon, "Genebuild version has changed since " + previous.getName() + ", skipping");
			return true;
		}

		// and those where the meta key genebuild.last_geneset_update has changed
		if (!DBUtils.getMetaValue(currentCon, "genebuild.last_geneset_update").equals(DBUtils.getMetaValue(previousCon, "genebuild.last_geneset_update"))) {
			ReportManager.correct(this, currentCon, "Meta entry genebuild.last_geneset_update has changed since " + previous.getName() + ", skipping");
			return true;
		}

		// build hashes of transcript stable id:exon start:exon end for both databases
		logger.finest("Building hash of current exon coords");
		Map<String, String> currentHash = buildHash(currentCon);
		logger.finest("Building hash of previous exon coords");
		Map<String, String> previousHash = buildHash(previousCon);

		// compare and store any differences
		logger.finest("Comparing ...");

		List<String> inNewNotOld = new ArrayList<String>();

		for (String currentKey : currentHash.keySet()) {

			// if it's not in the old one, make a note
			if (!previousHash.containsKey(currentKey)) {

				inNewNotOld.add(currentKey);

			} else { // otherwise we're no longer interested, remove from both

				previousHash.remove(currentKey);

			}

		}

		// now previousHash will only contain keys that were in the old but not in the new
		List<String> inOldNotNew = new ArrayList<String>(previousHash.keySet());

    if (inNewNotOld.size() > 0 && inOldNotNew.size() == 0 ) {

            ReportManager
                             .problem(this, currentCon, inNewNotOld.size() + " exons in " + current.getName() + " are not in " + previous.getName());
            result = false;

    }

    if (inNewNotOld.size() == 0 && inOldNotNew.size() > 0 ) {

            ReportManager
                             .problem(this, currentCon, inOldNotNew.size() + " exons in " + previous.getName() + " are not in " + current.getName());
            result = false;

    }

    if (inNewNotOld.size() > 0 && inOldNotNew.size() > 0 ) {

             ReportManager
                             .problem(this, currentCon, inOldNotNew.size() + " exons in " + previous.getName() + " have coordinates that are different from those in the same transcript in " + current.getName());
             result = false;

    }


		if (inOldNotNew.size() == 0 && inNewNotOld.size() == 0) {

			ReportManager.correct(this, currentCon, "All exons identical between databases");

		}

		return result;

	}

	// ----------------------------------------------------------------------

	private Map<String, String> buildHash(Connection con) {

		Map<String, String> hash = new HashMap<String, String>();

		String sql = "SELECT CONCAT(t.stable_id, ':', e.seq_region_start, ':', e.seq_region_end) FROM transcript t, exon_transcript et, exon e WHERE t.transcript_id=et.transcript_id AND et.exon_id=e.exon_id AND t.biotype='protein_coding'";

		try {

			Statement stmt = con.createStatement();

			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {

				hash.put(rs.getString(1), "1");

			}

			rs.close();

			stmt.close();

		} catch (SQLException e) {

			System.err.println("Error executing " + sql);
			e.printStackTrace();

		}

		return hash;

	}
	// ----------------------------------------------------------------------

} // ComparePreviousVersionExonCoords

