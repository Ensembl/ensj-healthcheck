/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Checks that the archive tables are up to date.
 */

public class Archive extends SingleDatabaseTestCase {

    private static final int MIN_MAPPING_SESSION_ID = 355;
    
    /**
     * Create a new Archvive test case.
     */
    public Archive() {

        addToGroup("id_mapping");
        addToGroup("release");
        setDescription("Checks the archive tables are up to date.");
        setHintLongRunning(true);

    }

    /**
     * This only applies to core and Vega databases.
     */
    public void types() {

        removeAppliesToType(DatabaseType.EST);
        removeAppliesToType(DatabaseType.ESTGENE);

    }
    
    /**
     * Run the test.
     * 
     * @param dbre
     *          The database to use.
     * @return true if the test passed.
     */
    public boolean run(final DatabaseRegistryEntry dbre) {

        boolean result = true;

        Connection con = dbre.getConnection();

        logger.info("Checking deleted genes");
        result &= checkDeletedInGeneArchive(con, "gene", "G", MIN_MAPPING_SESSION_ID);
        logger.info("Checking deleted transcripts");
        result &= checkDeletedInGeneArchive(con, "transcript", "T", MIN_MAPPING_SESSION_ID);
        logger.info("Checking deleted translations");
        result &= checkDeletedInGeneArchive(con, "translation", "P", MIN_MAPPING_SESSION_ID);
        logger.info("Checking changed translations");
        result &= checkChangedInGeneArchive(con, "translation", "P", MIN_MAPPING_SESSION_ID);
        logger.info("Checking changed transcript");
        result &= checkChangedInGeneArchive(con, "transcript", "T", MIN_MAPPING_SESSION_ID);
        logger.info("Checking changed genes");
        result &= checkChangedInGeneArchive(con, "gene", "G", MIN_MAPPING_SESSION_ID);
        logger.info("Checking deleted translations in peptide archive");
        result &= checkDeletedTranslationsInPeptideArchive(con, MIN_MAPPING_SESSION_ID);
        logger.info("Checking translations from peptide archive in gene archive");
        result &= checkTranslationsFromPeptideArchiveInGeneArchive(con);
        logger.info("Checking no current translations in peptide archive");
        result &= checkNoCurrentTranslationsInPeptideArchive(con);
        logger.info("Checking gene propagation IDs are current");
        result &= checkPropagationIDsAreCurrent(con, "gene", "G");
        logger.info("Checking transcript propagation IDs are current");
        result &= checkPropagationIDsAreCurrent(con, "transcript", "T");
        logger.info("Checking translation propagation IDs are current");
        result &= checkPropagationIDsAreCurrent(con, "translation", "P");

        return result;
    }

    private boolean checkDeletedTranslationsInPeptideArchive(final Connection con, final long minMappingSessionID) {
        boolean result = true;

        String sql = "SELECT CONCAT(old_stable_id, \".\", old_version) "
                + "FROM stable_id_event LEFT JOIN peptide_archive "
                + "                     ON old_stable_id=translation_stable_id " + "WHERE "
                + "     mapping_session_id >= " + minMappingSessionID + " " + "     AND old_stable_id like \"%P%\" "
                + "     AND new_stable_id is NULL " + "     AND translation_stable_id is NULL;";

        String[] rows = getColumnValues(con, sql);
        if (rows.length > 0) {
            ReportManager.problem(this, con, rows + " deleted translations missing from peptide_archive");
            result = false;
        }

        return result;
    }

    private boolean checkChangedTranslationsInPeptideArchive(final Connection con, final long minMappingSessionID) {

        boolean result = true;

        String sql = "SELECT CONCAT(old_stable_id, \".\", old_version) "
                + "FROM stable_id_event LEFT JOIN peptide_archive "
                + "                     ON old_stable_id=translation_stable_id " + "WHERE "
                + "     mapping_session_id >= " + minMappingSessionID + " " + "     AND old_stable_id like \"%P%\" "
                + "     AND new_stable_id=old_stable_id " + "     AND old_version!=new_version "
                + "     AND translation_stable_id is NULL;";

        String[] rows = getColumnValues(con, sql);
        if (rows.length > 0) {
            ReportManager.problem(this, con, rows + " updated translations missing from peptide_archive");
            result = false;
        }

        return result;
    }

    private boolean checkTranslationsFromPeptideArchiveInGeneArchive(final Connection con) {

        boolean result = true;

        String sql = "SELECT CONCAT( pa.translation_stable_id, \".\", pa.translation_version) "
                + " FROM peptide_archive pa LEFT JOIN gene_archive ga "
                + " ON ga.translation_stable_id=pa.translation_stable_id "
                + " AND ga.translation_version=pa.translation_version " + " WHERE ga.translation_stable_id is NULL;";
        String[] rows = getColumnValues(con, sql);
        if (rows.length > 0) {
            ReportManager.problem(this, con, rows.length + " translations from peptide archive not in gene archive");
            for (int i = 0; i < rows.length && i < 10; i++) {
                System.out.println(rows[i]);
            }
            result = false;
        }

        return result;
    }

    private boolean checkNoCurrentTranslationsInPeptideArchive(final Connection con) {

        boolean result = true;

        String sql = "SELECT CONCAT(ts.stable_id , \".\",ts.version)"
                + " FROM translation_stable_id ts, peptide_archive pa "
                + " WHERE ts.stable_id=pa.translation_stable_id " + "       AND ts.version= pa.translation_version;";
        String[] rows = getColumnValues(con, sql);
        if (rows.length > 0) {
            ReportManager.problem(this, con, rows.length + " current translations in peptide archive");
            result = false;
        }

        return result;
    }

    /** this quite a slow query, about 1min to hopefulyy return nothing. */
    private boolean checkPropagationIDsAreCurrent(final Connection con, final String type, final String filter) {
        // select * from stable_id_event sie left join gene_stable_id gsi on
        // sie.new_stable_id = gsi.stable_id where mapping_session_id = 348 and
        // sie.new_stable_id like "ENSG%" and gsi.stable_id is null String
        // type, String
        // filter) {

        boolean result = true;

        String sql = "SELECT sie.new_stable_id " + "FROM stable_id_event sie " + "         LEFT_JOIN " + type
                + "_stable_id tsi " + "         ON sie.new_stable_id = tsi.stable_id " + "         mapping_session ms "
                + " WHERE ms.old_database_name=\"ALL\" " + "       AND sie.mapping_session_id = ms.mapping_session_id "
                + "       AND sie.new_stable_id like \"%" + filter + "%\" " + "       AND tsi.stable_id is NULL";

        // TODO errrm

        return result;
    }

    /**
     * Checks that all the changed _type_s are included in the gene_archive. A
     * change has occured if the version is different.
     * 
     * @param con
     *          connection on which to execute queries
     * @param type
     *          type of item deleted
     * @param filter
     *          substring to use use to filter relevant stableIDs, will be used
     *          in SQL as "%FILTER%".
     * @param minMappingSessionID
     *          mapping_session_id for first mapping session which contains
     *          arechive data. This is needed because no archive data exists
     *          for previous release.
     * @return whether the test succeeded.
     */
    private boolean checkChangedInGeneArchive(final Connection con, final String type, final String filter, final long minMappingSessionID) {
        boolean result = true;

        String sql = "SELECT CONCAT(old_stable_id, \".\", old_version) "
                + "FROM stable_id_event sie LEFT JOIN gene_archive ga " + "                     ON old_stable_id="
                + type + "_stable_id " + "WHERE " + "     sie.mapping_session_id >= " + minMappingSessionID + " "
                + "     AND sie.mapping_session_id=ga.mapping_session_id " + "     AND old_stable_id like \"%" + filter
                + "%\" " + "     AND gene_stable_id is NULL "
                + "     AND new_stable_id=old_stable_id and old_version!=new_version;";

        String[] rows = getColumnValues(con, sql);
        if (rows.length > 0) {
            ReportManager.problem(this, con, rows.length + " deleted " + type + "s not in gene_archive ");
            result = false;
        }

        return result;

    }

    /**
     * Checks that all the deleted _type_s are included in the gene_archive.
     * 
     * @param con
     *          connection on which to execute queries
     * @param type
     *          type of item deleted
     * @param filter
     *          substring to use use to filter relevant stableIDs, will be used
     *          in SQL as "%FILTER%".
     * @param minMappingSessionID
     *          mapping_session_id for first mapping session which contains
     *          arechive data. This is needed because no archive data exists
     *          for previous release.
     * @return whether the test succeeded.
     */
    private boolean checkDeletedInGeneArchive(final Connection con, final String type, final String filter, final long minMappingSessionID) {

        boolean result = true;

        String sql = "SELECT CONCAT(old_stable_id, \".\", old_version) "
                + "FROM stable_id_event sie LEFT JOIN gene_archive ga " + "                     ON old_stable_id="
                + type + "_stable_id " + "WHERE " + "   sie.mapping_session_id >= " + minMappingSessionID + " "
                + "   AND sie.mapping_session_id=ga.mapping_session_id " + "   AND old_stable_id like \"%" + filter
                + "%\"" + "   AND new_stable_id is NULL " + "   AND " + type + "_stable_id is NULL;";
        String[] rows = getColumnValues(con, sql);
        if (rows.length > 0) {
            ReportManager.problem(this, con, rows.length + " deleted " + type + "s not in gene_archive ");
            result = false;
        }

        return result;
    }

    // -----------------------------------------------------------------

}
