/*
 Copyright (C) 2004 EBI, GRL
 
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

package org.ensembl.healthcheck.testcase.compara;

import java.lang.Integer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key
 * relationships.
 */

public class ForeignKeyMethodLinkSpeciesSetId extends SingleDatabaseTestCase {

    /**
     * Create an ForeignKeyMethodLinkSpeciesSetId that applies to a specific set of databases.
     */
    public ForeignKeyMethodLinkSpeciesSetId() {

        addToGroup("compara_genomic");
        addToGroup("compara_homology");
        setDescription("Check for broken foreign-key relationships in ensembl_compara databases.");
        setTeamResponsible("compara");

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
            result &= checkForOrphans(con,
                "method_link_species_set", "species_set_id",
                "species_set", "species_set_id");
            result &= checkForOrphansWithConstraint(con, "species_set", "species_set_id",
                "method_link_species_set", "species_set_id",
                "species_set_id not in (SELECT distinct species_set_id from species_set_tag)");

            /* Check uniqueness of species_set entries */
            int numOfDuplicatedSpeciesSets = getRowCount(con,
                "SELECT gdbs, count(*) num, GROUP_CONCAT(species_set_id) species_set_ids FROM ("+
                "SELECT species_set_id, GROUP_CONCAT(genome_db_id) gdbs FROM species_set GROUP by species_set_id) t1 GROUP BY gdbs HAVING COUNT(*)>1");
            if (numOfDuplicatedSpeciesSets > 0) {
                ReportManager.problem(this, con, "FAILED species_set table contains " + numOfDuplicatedSpeciesSets + " duplicated entries");
                ReportManager.problem(this, con, "USEFUL SQL: SELECT gdbs, count(*) num, GROUP_CONCAT(species_set_id) species_set_ids FROM ("+
                    "SELECT species_set_id, GROUP_CONCAT(genome_db_id) gdbs FROM species_set GROUP by species_set_id) t1 GROUP BY gdbs HAVING COUNT(*)>1");
                result = false;
            }

            /* Check method_link_species_set <-> synteny_region */
            /* All method_link for syntenies must have an internal ID between 101 and 199 */
            result &= checkForOrphansWithConstraint(con,
                "method_link_species_set", "method_link_species_set_id",
                "synteny_region", "method_link_species_set_id",
                "method_link_id >= 101 and method_link_id < 200");
            result &= checkForOrphans(con,
                "synteny_region", "method_link_species_set_id",
                "method_link_species_set", "method_link_species_set_id");

            /* Check method_link_species_set <-> homology */
            /* All method_link for homologies must have an internal ID between 201 and 299 */
            result &= checkForOrphansWithConstraint(con,
                "method_link_species_set", "method_link_species_set_id",
                "homology", "method_link_species_set_id",
                "method_link_id >= 201 and method_link_id < 300");
            result &= checkForOrphans(con,
                "homology", "method_link_species_set_id",
                "method_link_species_set", "method_link_species_set_id");

            /* Check method_link_species_set <-> family */
            /* All method_link for families must have an internal ID between 301 and 399 */
            result &= checkForOrphansWithConstraint(con,
                "method_link_species_set", "method_link_species_set_id",
                "family", "method_link_species_set_id",
                "method_link_id >= 301 and method_link_id < 400");
            result &= checkForOrphans(con,
                "family", "method_link_species_set_id",
                "method_link_species_set", "method_link_species_set_id");

            /* Check method_link_species_set <-> protein_tree_member */
            result &= checkForOrphansWithConstraint(con, "method_link_species_set", "method_link_species_set_id", "protein_tree_member", "method_link_species_set_id", "method_link_id IN (SELECT method_link_id FROM method_link WHERE class LIKE 'ProteinTree.%')");
            result &= checkForOrphansWithConstraint(con, "method_link_species_set", "method_link_species_set_id", "nc_tree_member", "method_link_species_set_id", "method_link_id IN (SELECT method_link_id FROM method_link WHERE class LIKE 'NCTree.%')");
            result &= checkForOrphans(con, "protein_tree_member", "method_link_species_set_id", "method_link_species_set", "method_link_species_set_id");

            /* Check number of MLSS with no source */
            int numOfUnsetSources = getRowCount(con, "SELECT count(*) FROM method_link_species_set WHERE source = 'NULL' OR source IS NULL");
            if (numOfUnsetSources > 0) {
                ReportManager.problem(this, con, "FAILED method_link_species_set table contains " + numOfUnsetSources + " with no source");
                result = false;
            }

            /* Check number of MLSS with no name */
            int numOfUnsetNames = getRowCount(con, "SELECT count(*) FROM method_link_species_set WHERE name = 'NULL' OR name IS NULL");
            if (numOfUnsetNames > 0) {
                ReportManager.problem(this, con, "FAILED method_link_species_set table contains " + numOfUnsetNames + " with no name");
                result = false;
            }

            /* Check the genomes in the species_set linked to the MLSS table */
            int numOfGenomesInTheDatabase = getRowCount(con, "SELECT count(*) FROM genome_db WHERE taxon_id > 0");
            Pattern unaryPattern = Pattern.compile("^([A-Z].[a-z]{3}) ");
            Pattern binaryPattern = Pattern.compile("^([A-Z].[a-z]{3})-([A-Z].[a-z]{3})");
            Pattern multiPattern = Pattern.compile("([0-9]+)");
            /* Query returns the MLLS.name, the number of genomes and their name ("H.sap" format) */
            String sql = "SELECT method_link_species_set.name, count(*),"+
                " GROUP_CONCAT( CONCAT( UPPER(substr(genome_db.name, 1, 1)), '.', SUBSTR(SUBSTRING_INDEX(genome_db.name, '_', -1),1,3) ) )"+
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
                  Matcher unaryMatcher = unaryPattern.matcher(name);
                  Matcher binaryMatcher = binaryPattern.matcher(name);
                  Matcher multiMatcher = multiPattern.matcher(name);
                  if (unaryMatcher.find()) {
                    if (num != 1) {
                      ReportManager.problem(this, con, "FAILED species_set for \"" + name + "\" links to " + num + " genomes instead of 1");
                      result = false;
                    }
                    if (!genomes.equals(unaryMatcher.group(1))) {
                      ReportManager.problem(this, con, "FAILED species_set for \"" + name + "\" links to " + genomes);
                    }
                  } else if (binaryMatcher.find()) {
                    if (num != 2) {
                      ReportManager.problem(this, con, "FAILED species_set for \"" + name + "\" links to " + num + " genomes instead of 2");
                      result = false;
                    }
                    if (!genomes.equals(binaryMatcher.group(1)+ "," + binaryMatcher.group(2)) && !genomes.equals(binaryMatcher.group(2) + "," + binaryMatcher.group(1))) {
                      ReportManager.problem(this, con, "FAILED species_set for \"" + name + "\" links to " + genomes);
                    }
                  } else if (multiMatcher.find()) {
                    if (num != Integer.valueOf(multiMatcher.group()).intValue()) {
                      ReportManager.problem(this, con, "FAILED species_set for \"" + name + "\" links to " + num + " genomes instead of " + multiMatcher.group());
                      result = false;
                    }
                  } else if (num != numOfGenomesInTheDatabase) {
                      ReportManager.problem(this, con, "FAILED species_set for \"" + name + "\" links to " + num + " genomes instead of " + numOfGenomesInTheDatabase);
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
