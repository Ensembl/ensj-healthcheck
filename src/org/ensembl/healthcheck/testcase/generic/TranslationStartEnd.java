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
 * Check that if the start and end of translation is on the same exon, that
 * start < end.
 */
public class TranslationStartEnd extends SingleDatabaseTestCase {

    /**
     * Creates a new instance of CheckTranslationStartEnd
     */
    public TranslationStartEnd() {
        addToGroup("post_genebuild");
        addToGroup("release");
        setDescription("Check that if the start and end of translation is on the same exon, that start < end.");
    }

    /**
     * This only applies to core and Vega databases.
     */
    public void types() {

        removeAppliesToType(DatabaseType.EST);

    }
    
    /**
     * Find any matching databases that have start > end.
     * @param dbre
     *          The database to use.
     * @return Result.
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

        Connection con = dbre.getConnection();
        int rows = getRowCount(con,
                "select count(translation_id) from translation where start_exon_id = end_exon_id and seq_start > seq_end");
        if (rows > 0) {
            result = false;
            //logger.warning(rows + " translations in " +
            // DBUtils.getShortDatabaseName(con) +
            // " have start > end");
            ReportManager.problem(this, con, rows + " translations have start > end");
        } else {
            ReportManager.correct(this, con, "No translations have start > end");
        }

        return result;

    } // run

} // TranslationStartEnd
