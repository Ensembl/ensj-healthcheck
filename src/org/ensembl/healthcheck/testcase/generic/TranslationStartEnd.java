/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2017] EMBL-European Bioinformatics Institute
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

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that if the start and end of translation is on the same exon, that
 * start < end. Also check that translation ends aren't beyond exon ends.
 */
public class TranslationStartEnd extends SingleDatabaseTestCase {

    private static final String SEQ_REGION_SQL = "select distinct seq_region_id from seq_region_attrib "
            + "join attrib_type using (attrib_type_id) " + "where code='toplevel' and seq_region_id not in "
            + "(select seq_region_id from seq_region_attrib join attrib_type using (attrib_type_id) where code='circular_seq')";

    /**
     * Creates a new instance of CheckTranslationStartEnd
     */
    public TranslationStartEnd() {

        setDescription(
                "Check that if the start and end of translation is on the same exon, that start < end. Also check that translation ends aren't beyond exon ends.");
        setTeamResponsible(Team.GENEBUILD);
    }

    /**
     * This only applies to core and Vega databases.
     */
    public void types() {

        removeAppliesToType(DatabaseType.OTHERFEATURES);
        removeAppliesToType(DatabaseType.RNASEQ);

    }

    /**
     * Find any matching databases that have start > end.
     * 
     * @param dbre
     *            The database to use.
     * @return Result.
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

        Connection con = dbre.getConnection();

        String clause = " and seq_region_id in ("
                + StringUtils.join(DBUtils.getColumnValuesList(con, SEQ_REGION_SQL), ',') + ")";

        // check start < end
        int rows = DBUtils.getRowCount(con,
                "SELECT COUNT(translation_id) FROM translation JOIN transcript USING (transcript_id) WHERE start_exon_id = end_exon_id AND seq_start > seq_end"
                        + clause);
        if (rows > 0) {
            result = false;
            ReportManager.problem(this, con, rows + " translations have start > end");
        }
        
        // check start < end
        rows = DBUtils.getRowCount(con,
                "SELECT COUNT(translation_id) FROM translation JOIN transcript USING (transcript_id) WHERE start_exon_id = end_exon_id AND (seq_end-seq_start)<2"
                        + clause);
        if (rows > 0) {
            result = false;
            ReportManager.problem(this, con, rows + " translations are less than 1 codon long");
        }

        // check no translations overrun their exons
        rows = DBUtils.getRowCount(con,
                "SELECT COUNT(*) FROM translation t, exon e WHERE t.end_exon_id=e.exon_id AND cast(e.seq_region_end as signed int)-cast(e.seq_region_start as signed int)+1 < t.seq_end"
                        + clause);
        if (rows > 0) {
            result = false;
            ReportManager.problem(this, con, rows + " translations end beyond the end of their exons");
        }
        rows = DBUtils.getRowCount(con,
                "SELECT COUNT(*) FROM translation t, exon e WHERE t.start_exon_id=e.exon_id AND cast(e.seq_region_end as signed int)-cast(e.seq_region_start as signed int)+1 < t.seq_start"
                        + clause);
        if (rows > 0) {
            result = false;
            ReportManager.problem(this, con, rows + " translations end beyond the end of their exons");
        }

        // check the start and end exon have a correct phase
        rows = DBUtils.getRowCount(con,
                "SELECT COUNT(*) FROM translation t, exon e WHERE t.start_exon_id=e.exon_id AND start_exon_id <> end_exon_id and end_phase = -1"
                        + clause);
        if (rows > 0) {
            result = false;
            ReportManager.problem(this, con, rows + " translations have start exon with a -1 end phase");
        }

        rows = DBUtils.getRowCount(con,
                "SELECT COUNT(*) FROM translation t, exon e WHERE t.end_exon_id=e.exon_id AND start_exon_id <> end_exon_id and phase = -1"
                        + clause);
        if (rows > 0) {
            result = false;
            ReportManager.problem(this, con, rows + " translations have end exon with -1 phase");
        }

        return result;

    } // run

} // TranslationStartEnd
