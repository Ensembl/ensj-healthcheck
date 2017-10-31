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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Look for entries in the exon_transcript table that are duplicates apart from
 * the rank.
 */

public class ExonRank extends SingleDatabaseTestCase {

    /**
     * Create a new EonRank testcase.
     */
    public ExonRank() {

        setDescription("Look for entries in the exon_transcript table that are "
            + "duplicates apart from the rank.");
        setTeamResponsible(Team.GENEBUILD);
    }

    /**
     * Only run on core databases.
     */
    public void types() {

        removeAppliesToType(DatabaseType.CDNA);

    }

    /**
     * Run the test.
     * 
     * @param dbre
     *          The database to use.
     * @return true if the test passed.
     * 
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

        Connection con = dbre.getConnection();

        int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) "
            + "FROM exon_transcript et1, exon_transcript et2 "
            + "WHERE et1.exon_id=et2.exon_id "
            + "AND et1.transcript_id=et2.transcript_id "
            + "AND et1.rank != et2.rank");

        if (rows > 0) {

            ReportManager.problem(this, con, rows + " rows in exon_transcript"
                + "specify the same exon more than once in a transcript with a "
                + "different rank");
            result = false;

        } else {

            ReportManager.correct(this, con, "All ranks OK");

        }

        return result;

    } // run

    // ----------------------------------------------------------------------

} // ExonRank

