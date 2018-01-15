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
 * Copyright (C) 2004 EBI, GRL
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
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ensembl.healthcheck.AssemblyNameInfo;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.RowMapper;
import org.ensembl.healthcheck.util.SqlTemplate;
import org.ensembl.healthcheck.util.Utils;


/**
 * Checks that changes between releases (assembly, repeatmasking or gene set) have been declared. 
 */


public class CheckDeclarations extends SingleDatabaseTestCase {

        public CheckDeclarations() {

                setTeamResponsible(Team.GENEBUILD);
                setSecondTeamResponsible(Team.RELEASE_COORDINATOR);
                setDescription("Check that all changes have been correctly declared");
        }

        /**
         * This test applies only to core dbs
         */
        public void types() {
                removeAppliesToType(DatabaseType.SANGER_VEGA);
                removeAppliesToType(DatabaseType.VEGA);
                removeAppliesToType(DatabaseType.CDNA);
                removeAppliesToType(DatabaseType.OTHERFEATURES);
                removeAppliesToType(DatabaseType.RNASEQ);
        }


        /**
         * Checks that changes in database have been declared.
         * 
         * @param dbre
         *          The database to check.
         * @return True if the test passed.
         */

        public boolean run(final DatabaseRegistryEntry dbre) {

                boolean result = true;

                Connection con = dbre.getConnection();
                DatabaseRegistryEntry sec = getEquivalentFromSecondaryServer(dbre);

                if (sec == null) {
                        boolean newSpecies = checkDeclaration(dbre, "assembly");
                        if (!newSpecies) {
                                ReportManager.problem(this, con, "New database " + dbre.getName() + " has not been declared");
                                return false;
                        } else {
                                return true;
                        }
                }

                Connection previousCon = sec.getConnection();

                result &= checkAssembly(dbre, sec);

                result &= checkRepeats(dbre, sec);

                result &= checkGenes(dbre, sec);

                return result;
        }

  private boolean checkAssembly(DatabaseRegistryEntry dbre, DatabaseRegistryEntry sec) {

    boolean result = true;

    Connection con = dbre.getConnection();
    Connection previousCon = sec.getConnection();

    String sql = "CHECKSUM table assembly";
    int currentAssembly = DBUtils.getRowCount(con, sql);
    int previousAssembly = DBUtils.getRowCount(previousCon, sql);

    if (previousAssembly != currentAssembly) {
      boolean declared = checkDeclaration(dbre, "assembly");
      if (!declared) {
        ReportManager.problem(this, con, "Assembly has changed but has not been declared");
        result = false;
      }
    }

    return result;
  }

  private boolean checkRepeats(DatabaseRegistryEntry dbre, DatabaseRegistryEntry sec) {

    boolean result = true;

    Connection con = dbre.getConnection();
    Connection previousCon = sec.getConnection();

    String sql = "CHECKSUM table repeat_feature";
    int currentRepeats = DBUtils.getRowCount(con, sql);
    int previousRepeats = DBUtils.getRowCount(previousCon, sql);

    if (currentRepeats != previousRepeats) {
      boolean declared = checkDeclaration(dbre, "repeat_masking");
      if (!declared) {
        ReportManager.problem(this, con, "Repeats have changed but have not been declared");
        result = false;
      }
    }

    return result;
  }

  private boolean checkGenes(DatabaseRegistryEntry dbre, DatabaseRegistryEntry sec) {

    boolean result = true;

    Connection con = dbre.getConnection();
    Connection previousCon = sec.getConnection();
    SqlTemplate t = getSqlTemplate(dbre);
    RowMapper<Set<Object>> rowMapper = new RowMapper<Set<Object>>(){
      public Set<Object> mapRow(ResultSet rs, int position) throws SQLException {
        Set<Object> set = new HashSet<Object>();
        for (int i=1; i <= 10; i++) {
          set.add(rs.getObject(i));
        }
        return set;
      }
    };

    String sql = "SELECT biotype, analysis_id, seq_region_id, seq_region_start, seq_region_end, seq_region_end, seq_region_strand, stable_id, is_current, version FROM gene" ;
    Set<Set<Object>> currentGenes = t.queryForSet(sql, rowMapper);
    Set<Set<Object>> previousGenes = getSqlTemplate(sec).queryForSet(sql, rowMapper);

    if (! currentGenes.equals(previousGenes)) {
      boolean declared = checkDeclaration(dbre, "gene_set");
      if (!declared) {
        ReportManager.problem(this, con, "Genes have changed but have not been declared");
        result = false;
      }
    }

    return result;
  }

  private boolean checkDeclaration(DatabaseRegistryEntry dbre, String change) {

    boolean result = true;

    Connection con = dbre.getConnection();

    String release = DBUtils.getMetaValue(con, "schema_version");
    DatabaseRegistryEntry prod = getProductionDatabase();
    String sql = "SELECT count(*) FROM db_list dl, db d WHERE dl.db_id = d.db_id and db_type = 'core' and is_current = 1 AND full_db_name = '" + dbre.getName() + "' AND species_id IN (SELECT species_id FROM changelog c, changelog_species cs WHERE c.changelog_id = cs.changelog_id AND release_id = " + release + " AND status not in ('cancelled', 'postponed') AND " + change + " = 'Y')";
    int rows = DBUtils.getRowCount(prod.getConnection(), sql);
    if (rows == 0) {
      result = false;
    }

    return result;
  }


}





