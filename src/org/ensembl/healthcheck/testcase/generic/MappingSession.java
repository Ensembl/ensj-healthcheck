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
package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * @author glenn
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class MappingSession extends SingleDatabaseTestCase {

    /** 
     * Create a new MappingSession healthcheck.
     */
    public MappingSession() {
        
        addToGroup("id_mapping");
        addToGroup("release");
        setDescription("Checks the mapping session and stable ID tables.");
    
    }

    /**
     * This only really applies to core databases
     */
    public void types() {

        removeAppliesToType(DatabaseType.EST);
        removeAppliesToType(DatabaseType.ESTGENE);
        removeAppliesToType(DatabaseType.VEGA);

    }

    /**
     * Run the test - check the ID mapping-related tables.
     * @param dbre The database to check.
     * @return true if the test passes.
     */
    public boolean run(final DatabaseRegistryEntry dbre) {

        boolean result = true;

        // there are several species where ID mapping is not done
        Species s = dbre.getSpecies();
        if (s != Species.CAENORHABDITIS_ELEGANS && s != Species.DROSOPHILA_MELANOGASTER && s != Species.FUGU_RUBRIPES) {

            Connection con = dbre.getConnection();

            logger.info("Checking tables exist and are populated");
            result = checkTablesExistAndPopulated(dbre) && result;
            logger.info("Checking for null strings");
            result = checkNoNullStrings(con) && result;
            logger.info("Checking DB name format in mapping_session");
            result = checkDBNameFormat(con) && result;
            logger.info("Checking mapping_session chaining");
            result = checkMappingSessionChaining(con) && result;
            logger.info("Checking mapping_session/stable_id_event keys");
            result = checkMappingSessionStableIDKeys(con) && result;
            logger.info("Checking that ALL/LATEST mapping session has most entries");
            result = checkAllLatest(con) && result;

        }

        return result;

    } // run

    // -----------------------------------------------------------------
    /**
     * Check that the ALL/LATEST mapping session has more entries than the
     * others
     */
    private boolean checkAllLatest(final Connection con) {

        boolean result = true;

        // Following query should give one result - the ALL/LATEST one - if all
        // is well
        String sql = "SELECT ms.old_db_name, ms.new_db_name, count(*) AS entries "
                + "FROM stable_id_event sie, mapping_session ms "
                + "WHERE sie.mapping_session_id=ms.mapping_session_id "
                + "GROUP BY ms.mapping_session_id ORDER BY entries DESC LIMIT 1";

        String oldDBName = getRowColumnValue(con, sql);
        if (!(oldDBName.equalsIgnoreCase("ALL"))) {
            ReportManager.problem(this, con,
                    "ALL/LATEST mapping session does not seem to have the most stable_id_event entries");
            result = false;
        } else {
            ReportManager.correct(this, con,
                    "ALL/LATEST mapping session seems to have the most stable_id_event entries");
        }

        return result;

    }

    // -----------------------------------------------------------------
    /**
     * Check format of old/new DB names in mapping_session.
     */
    private boolean checkDBNameFormat(final Connection con) {

        boolean result = true;
        String dbNameRegexp = "[A-Za-z]+_[A-Za-z]+_(core|est|estgene|vega)_\\d+_\\d+[A-Za-z]?.*";

        String[] sql = {"SELECT old_db_name from mapping_session WHERE old_db_name <> 'ALL'",
                "SELECT new_db_name from mapping_session WHERE new_db_name <> 'LATEST'"};

        for (int i = 0; i < sql.length; i++) {

            String[] names = getColumnValues(con, sql[i]);
            for (int j = 0; j < names.length; j++) {
                if (!(names[j].matches(dbNameRegexp))) {
                    ReportManager.problem(this, con, "Database name " + names[j]
                            + " in mapping_session does not appear to be in the correct format");
                    result = false;
                }
            }

        }

        if (result) {
            ReportManager
                    .correct(this, con, "All database names in mapping_session appear to be in the correct format");
        }

        return result;

    }

    // -----------------------------------------------------------------

    /**
     * Checks tables exist and have >0 rows.
     * 
     * @param con
     * @return True when all ID mapping-related tables exist and have > 0 rows.
     *  
     */
    private boolean checkTablesExistAndPopulated(final DatabaseRegistryEntry dbre) {

        String[] tables = new String[] {"stable_id_event", "mapping_session", "gene_archive", "peptide_archive"};

        boolean result = true;

        Connection con = dbre.getConnection();

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

    // -----------------------------------------------------------------
    /**
     * Check no "NULL" or "null" strings in stable_id_event.new_stable_id or
     * stable_id_event.oldable_id.
     * 
     * @param con
     * @return
     */
    private boolean checkNoNullStrings(final Connection con) {

        boolean result = true;

        int rows = getRowCount(con, "select count(*) from stable_id_event sie where new_stable_id='NULL'");
        if (rows > 0) {
            ReportManager.problem(this, con, rows
                    + " rows in stable_id_event.new_stable_id contains \"NULL\" string instead of NULL value.");
            result = false;
        }

        rows = getRowCount(con, "select count(*) from stable_id_event sie where new_stable_id='null'");
        if (rows > 0) {
            ReportManager.problem(this, con, rows
                    + " rows in stable_id_event.new_stable_id contains \"null\" string instead of NULL value.");
            result = false;
        }

        rows = getRowCount(con, "select count(*) from stable_id_event sie where old_stable_id='NULL'");
        if (rows > 0) {
            ReportManager.problem(this, con, rows
                    + " rows in stable_id_event.old_stable_id contains \"NULL\" string instead of NULL value.");
            result = false;
        }

        rows = getRowCount(con, "select count(*) from stable_id_event sie where old_stable_id='null'");
        if (rows > 0) {
            ReportManager.problem(this, con, rows
                    + " rows in stable_id_event.old_stable_id contains \"null\" string instead of NULL value.");
            result = false;
        }

        return result;
    }

    // -----------------------------------------------------------------
    /**
     * Check that the old_db_name and new_db_name columns "chain" together.
     */
    private boolean checkMappingSessionChaining(final Connection con) {

        boolean result = true;

        String[] oldNames = getColumnValues(con,
                "SELECT old_db_name FROM mapping_session WHERE old_db_name <> 'ALL' ORDER BY created");
        String[] newNames = getColumnValues(con,
                "SELECT new_db_name FROM mapping_session WHERE new_db_name <> 'LATEST' ORDER BY created");

        for (int i = 1; i < oldNames.length; i++) {
            if (!(oldNames[i].equalsIgnoreCase(newNames[i - 1]))) {
                ReportManager.problem(this, con, "Old/new names " + oldNames[i] + " " + newNames[i - 1]
                        + " do not chain properly");
                result = false;
            }
        }

        if (result) {
            ReportManager.correct(this, con, "Old/new db name chaining in mapping_session seems OK");
        }

        return result;

    }

    // -----------------------------------------------------------------
    /**
     * Check that all mapping_sessions have entries in stable_id_event and
     * vice-versa.
     */
    private boolean checkMappingSessionStableIDKeys(final Connection con) {

        boolean result = true;

        int orphans = countOrphans(con, "mapping_session", "mapping_session_id", "stable_id_event",
                "mapping_session_id", false);
        if (orphans > 0) {
            ReportManager.problem(this, con, orphans
                    + " dangling references between mapping_session and stable_id_event tables");
            result = false;
        } else {
            ReportManager.correct(this, con, "All mapping_session/stable_id_event keys are OK");
        }

        return result;

    }

    // -----------------------------------------------------------------

} // MappingSession
