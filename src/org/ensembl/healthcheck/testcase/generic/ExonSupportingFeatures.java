/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2020] EMBL-European Bioinformatics Institute
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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that exons overlap their supporting features.
 */

public class ExonSupportingFeatures extends SingleDatabaseTestCase {

        /**
         * Create a new TranscriptSupportingFeatures testcase.
         */
        public ExonSupportingFeatures() {

                setDescription("Check that exons overlap with their protein supporting features.");
                setPriority(Priority.AMBER);
                setTeamResponsible(Team.GENEBUILD);

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

                // only run for core databases
                if (dbre.getType() != DatabaseType.CORE) {
                    return result;
                }

                Connection con = dbre.getConnection();

                String sql = "SELECT COUNT(*) FROM exon e, supporting_feature sf, protein_align_feature paf " +
                 "WHERE paf.protein_align_feature_id = sf.feature_id AND sf.exon_id = e.exon_id AND sf.feature_type = 'protein_align_feature' " +
                 "AND paf.seq_region_start > e.seq_region_end";

                int rows = DBUtils.getRowCount(con, sql);

                if (rows > 0) {
                        ReportManager.problem(this, con, rows + " exons have protein align features which do not overlap\nUseful SQL: " + sql);
                        result = false;
                } else {
                        ReportManager.correct(this, con, "All exons overlap their protein supporting features");
                }

                return result;

         } // run

} // ExonSupportingFeatures
