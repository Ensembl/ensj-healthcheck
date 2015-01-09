/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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


package org.ensembl.healthcheck.testcase.compara;

import java.lang.Integer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.compara.AbstractComparaTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key
 * relationships.
 */

public class ForeignKeyMethodLinkSpeciesSetId extends AbstractComparaTestCase {

    /**
     * Create an ForeignKeyMethodLinkSpeciesSetId that applies to a specific set of databases.
     */
    public ForeignKeyMethodLinkSpeciesSetId() {

        addToGroup("compara_genomic");
        addToGroup("compara_homology");
        setDescription("Check for broken foreign-key relationships in ensembl_compara databases.");
        setTeamResponsible(Team.COMPARA);

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

        if (tableHasRows(con, "method_link_species_set")) {

            /* Check method_link_species_set <-> species_set */
            result &= checkForOrphans(con, "method_link_species_set", "species_set_id", "species_set", "species_set_id");
            result &= checkForOrphansWithConstraint(con, "species_set", "species_set_id", "method_link_species_set", "species_set_id", "species_set_id not in (SELECT distinct species_set_id from species_set_tag)");

			/* Check that species_set_tag refers to existing species sets */
            result &= checkForOrphans(con, "species_set_tag", "species_set_id", "species_set", "species_set_id");

            /* Check uniqueness of species_set entries */
            int numOfDuplicatedSpeciesSets = DBUtils.getRowCount(con,
                "SELECT gdbs, count(*) num, GROUP_CONCAT(species_set_id) species_set_ids FROM ("+
                "SELECT species_set_id, GROUP_CONCAT(genome_db_id) gdbs FROM species_set GROUP by species_set_id) t1 GROUP BY gdbs HAVING COUNT(*)>1");
            if (numOfDuplicatedSpeciesSets > 0) {
                ReportManager.problem(this, con, "FAILED species_set table contains " + numOfDuplicatedSpeciesSets + " duplicated entries");
                ReportManager.problem(this, con, "USEFUL SQL: SELECT gdbs, count(*) num, GROUP_CONCAT(species_set_id) species_set_ids FROM ("+
                    "SELECT species_set_id, GROUP_CONCAT(genome_db_id) gdbs FROM species_set GROUP by species_set_id) t1 GROUP BY gdbs HAVING COUNT(*)>1");
                result = false;
	    }

			if (!isMasterDB(dbre.getConnection())) {
				/* Check method_link_species_set <-> species_tree_root */
				result &= checkForOrphans(con, "species_tree_root", "method_link_species_set_id", "method_link_species_set", "method_link_species_set_id");
			}

            /* Check number of MLSS with no source */
            int numOfUnsetSources = DBUtils.getRowCount(con, "SELECT count(*) FROM method_link_species_set WHERE source = 'NULL' OR source IS NULL");
            if (numOfUnsetSources > 0) {
                ReportManager.problem(this, con, "FAILED method_link_species_set table contains " + numOfUnsetSources + " with no source");
                result = false;
            }

            /* Check number of MLSS with no name */
            int numOfUnsetNames = DBUtils.getRowCount(con, "SELECT count(*) FROM method_link_species_set WHERE name = 'NULL' OR name IS NULL");
            if (numOfUnsetNames > 0) {
                ReportManager.problem(this, con, "FAILED method_link_species_set table contains " + numOfUnsetNames + " with no name");
                result = false;
            }

            /* Check the genomes in the species_set linked to the MLSS table */
            int numOfGenomesInTheDatabase = DBUtils.getRowCount(con, "SELECT count(*) FROM genome_db WHERE taxon_id > 0");
            Pattern unaryPattern = Pattern.compile("^([A-Z].[a-z]{3}) ");
            Pattern binaryPattern = Pattern.compile("^([A-Z].[a-z]{3})-([A-Z].[a-z]{3})");
            Pattern multiPattern = Pattern.compile("([0-9]+)");
            Pattern lastzpatchPattern = Pattern.compile("lastz-patch");
            /* Query returns the MLLS.name, the number of genomes and their name ("H.sap" format) */
            String sql = "SELECT method_link_species_set.name, count(*),"+
                " GROUP_CONCAT( CONCAT( UPPER(substr(genome_db.name, 1, 1)), '.', SUBSTR(SUBSTRING_INDEX(genome_db.name, '_', -1),1,3) ) ), "+
                " species_set_id, "+
                " method_link_species_set_id "+
                " FROM method_link_species_set JOIN species_set USING (species_set_id)"+
                " JOIN genome_db USING (genome_db_id) GROUP BY method_link_species_set_id";
            try {
              Statement stmt = con.createStatement();
              ResultSet rs = stmt.executeQuery(sql);
              if (rs != null) {
                while (rs.next()) {
                  String name = rs.getString(1);
                  int num = rs.getInt(2);
                  String genomes = rs.getString(3);
                  String ss_id = rs.getString(4);
                  String mlss_id = rs.getString(5);
                  Matcher unaryMatcher = unaryPattern.matcher(name);
                  Matcher binaryMatcher = binaryPattern.matcher(name);
                  Matcher multiMatcher = multiPattern.matcher(name);
                  Matcher lastzpatchMatcher = lastzpatchPattern.matcher(name);
                  if (unaryMatcher.find()) {
                    if (num != 1) {
                      ReportManager.problem(this, con, "FAILED species_set(" + ss_id + ") for \"" + name + "\"(" + mlss_id + ") links to " + num + " genomes instead of 1");
                      result = false;
                    }
                    if (!genomes.equals(unaryMatcher.group(1))) {
                      ReportManager.problem(this, con, "FAILED species_set(" + ss_id + ") for \"" + name + "\"(" + mlss_id + ") links to " + genomes);
                    }
                  } else if (binaryMatcher.find()) {
                    if (num != 2 && binaryMatcher.group(1) != binaryMatcher.group(2) && !lastzpatchMatcher.find()) {
                      ReportManager.problem(this, con, "FAILED species_set(" + ss_id + ") for \"" + name + "\"(" + mlss_id + ") links to " + num + " genomes instead of 2");
                      result = false;
                    }
                    if (!genomes.equals(binaryMatcher.group(1)+ "," + binaryMatcher.group(2)) && !genomes.equals(binaryMatcher.group(2) + "," + binaryMatcher.group(1))) {
                            if (binaryMatcher.group(1).equals (binaryMatcher.group(2)) && genomes.equals(binaryMatcher.group(1))) {
                            } else {
                            ReportManager.problem(this, con, "Yes, we felt here... FAILED species_set(" + ss_id + ") for \"" + name + "\"(" + mlss_id + ") links to " + genomes + " instead of " + binaryMatcher.group(1) + "," + binaryMatcher.group(2));
                            }
                    }
                  } else if (multiMatcher.find()) {
                    if (num != Integer.valueOf(multiMatcher.group()).intValue()) {
                      ReportManager.problem(this, con, "FAILED species_set(" + ss_id + ") for \"" + name + "\"(" + mlss_id + ") links to " + num + " genomes instead of " + multiMatcher.group());
                      result = false;
                    }
                  } else if (num != numOfGenomesInTheDatabase && !isMasterDB(dbre.getConnection())) {
                      ReportManager.problem(this, con, "FAILED species_set(" + ss_id + ") for \"" + name + "\"(" + mlss_id + ") links to " + num + " genomes instead of " + numOfGenomesInTheDatabase);
                  }
                }
              }

            } catch (Exception e) {
              e.printStackTrace();
            }


        } else {

            ReportManager.correct(this, con, "NO ENTRIES in method_link_species_set table, so nothing to test IGNORED");
        }

        return result;

    }

} // ForeignKeyMethodLinkSpeciesSetId
