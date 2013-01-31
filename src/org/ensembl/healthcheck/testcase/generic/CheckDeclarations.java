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
import org.ensembl.healthcheck.util.SqlTemplate;
import org.ensembl.healthcheck.util.Utils;


/**
 * Checks that changes between releases (assembly, repeatmasking or gene set) have been declared. 
 */


public class CheckDeclarations extends SingleDatabaseTestCase {

        public CheckDeclarations() {

                addToGroup("post_genebuild");
                addToGroup("release");
                addToGroup("compara-ancestral");
                addToGroup("pre-compara-handover");
                addToGroup("post-compara-handover");

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

    String sql = "CHECKSUM table gene";
    int currentGenes = DBUtils.getRowCount(con, sql);
    int previousGenes = DBUtils.getRowCount(previousCon, sql);

    if (currentGenes != previousGenes) {
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





