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

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Vector;
import java.sql.SQLException;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key
 * relationships.
 */

public class CheckSpeciesSetTag extends MultiDatabaseTestCase {

    /**
     * Create an ForeignKeyMethodLinkSpeciesSetId that applies to a specific set of databases.
     */
    public CheckSpeciesSetTag() {

        addToGroup("compara_homology");
        setDescription("Check the content of the species_set_tag table");
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
    public boolean run(DatabaseRegistry dbr) {

        boolean result = true;

        // Get compara DB connection
        DatabaseRegistryEntry[] allComparaDBs = dbr.getAll(DatabaseType.COMPARA);
        if (allComparaDBs.length == 0) {
          result = false;
          ReportManager.problem(this, "", "Cannot find compara database");
          usage();
          return false;
        }

        Map speciesDbrs = getSpeciesDatabaseMap(dbr, true);

        // For each compara connection...
        for (int i = 0; i < allComparaDBs.length; i++) {
            // ... check the entries with a taxon id
            result &= checkSpeciesSetByTaxon(allComparaDBs[i]);
            // ... check the entriy for low-coverage genomes
            result &= checkLowCoverageSpecies(allComparaDBs[i], speciesDbrs);
        }

        return result;
    }



    public boolean checkLowCoverageSpecies(DatabaseRegistryEntry comparaDbre, Map speciesDbrs) {

        boolean result = true;
        Connection con = comparaDbre.getConnection();

        // Get list of species (assembly_default) in compara
        Vector comparaSpecies = new Vector();
        Vector comparaGenomeDBids = new Vector();
        String sql2 = "SELECT DISTINCT genome_db.genome_db_id, genome_db.name FROM genome_db WHERE assembly_default = 1"
            + " AND name <> 'Ancestral sequences' ORDER BY genome_db.genome_db_id";
        try {
          Statement stmt = con.createStatement();
          ResultSet rs = stmt.executeQuery(sql2);
          while (rs.next()) {
            comparaSpecies.add(Species.resolveAlias(rs.getString(2).toLowerCase().replace(' ', '_')));
            comparaGenomeDBids.add(rs.getString(1));
          }
          rs.close();
          stmt.close();
        } catch (Exception e) {
          e.printStackTrace();
        }

        // Find which of these species are low-coverage by looking into the meta table
        // I don't know if this will work for multi-species DBs, but hopefully these don't have low-cov assemblies
        boolean allSpeciesFound = true;
        Vector lowCoverageSpecies = new Vector();
        Vector lowCoverageGenomeDdIds = new Vector();
        for (int i = 0; i < comparaSpecies.size(); i++) {
            Species species = (Species) comparaSpecies.get(i);
            DatabaseRegistryEntry[] speciesDbr = (DatabaseRegistryEntry[]) speciesDbrs.get(species);
            if (speciesDbr != null) {
                Connection speciesCon = speciesDbr[0].getConnection();
                String coverageDepth = getRowColumnValue(speciesCon,
                    "SELECT meta_value FROM meta WHERE meta_key = \"assembly.coverage_depth\"");
                
                if (coverageDepth.equals("low")) {
                    lowCoverageSpecies.add(species);
                    lowCoverageGenomeDdIds.add(comparaGenomeDBids.get(i));
                }
            } else {
                ReportManager.problem(this, con, "No connection for " + species.toString());
                allSpeciesFound = false;
            }
        }
        if (!allSpeciesFound) {
            ReportManager.problem(this, con, "Cannot find all the species");
            usage();
        }

        // If there are low-coverage species, check the species_set_tag entry
        if (lowCoverageSpecies.size() > 0) {
            // Check that the low-coverage entry exists in the species_set_tag table
            String speciesSetId1 = getRowColumnValue(con,
                "SELECT species_set_id FROM species_set_tag WHERE tag = 'name' AND value = 'low-coverage'");
            if (speciesSetId1.equals("")) {
                ReportManager.problem(this, con, "There is no species_set_tag entry for low-coverage genomes");
                result = false;
            }

            // Check the species_set_id for the set of low-coverage assemblies
            String sql = "" + lowCoverageGenomeDdIds.get(0);
            for (int i = 1; i < lowCoverageGenomeDdIds.size(); i++) {
              sql += "," + lowCoverageGenomeDdIds.get(i);
            }
            String speciesSetId2 = getRowColumnValue(con,
                "SELECT species_set_id, GROUP_CONCAT(genome_db_id ORDER BY genome_db_id) gdbs" +
                " FROM species_set GROUP BY species_set_id HAVING gdbs = \"" + sql + "\"");
            if (speciesSetId2.equals("")) {
                ReportManager.problem(this, con, "Wrong set of low-coverage (" + speciesSetId1
                    + ") genome_db_ids. It must be: " + sql);
                result = false;
            }

            // Check that both are the same
            if (!speciesSetId1.equals("") && !speciesSetId2.equals("") && !speciesSetId1.equals(speciesSetId2)) {
                ReportManager.problem(this, con, "The species_set_id for low-coverage should be " +
                    speciesSetId2 + " and not " + speciesSetId1);
                result = false;
            }
        }
        return result;
    }
    
    


    public boolean checkSpeciesSetByTaxon(DatabaseRegistryEntry dbre) {

        boolean result = true;

        Connection con = dbre.getConnection();

        if (tableHasRows(con, "species_set_tag")) {

            // Find all the entries with a taxon_id tag
            String sql_tag = new String("SELECT species_set_id, value FROM species_set_tag WHERE tag = 'taxon_id'");

            try {
                Statement stmt_tag = con.createStatement();
                ResultSet rs_tag = stmt_tag.executeQuery(sql_tag);
                while (rs_tag.next()) {
                    // Check that all the genome_db_ids for that taxon are included
                    // 1. genome_db_ids from ncbi_taxa_node + genome_db tables
                    String sql_taxon = new String("SELECT GROUP_CONCAT(genome_db_id ORDER BY genome_db_id)" +
                        " FROM ncbi_taxa_node nod1" +
                        " LEFT JOIN ncbi_taxa_node nod2 ON (nod1.left_index < nod2.left_index and nod1.right_index > nod2.left_index)" +
                        " LEFT JOIN genome_db ON (nod2.taxon_id = genome_db.taxon_id)" +
                        " WHERE nod1.taxon_id = '" + rs_tag.getInt(2) + "'" +
                        " AND genome_db_id IS NOT NULL AND genome_db.assembly_default = 1");
                    // 2. genome_db_ids from the species_set table
                    String sql_sset = new String("SELECT GROUP_CONCAT(genome_db_id ORDER BY genome_db_id)" +
                        " FROM species_set WHERE species_set_id = " + rs_tag.getInt(1));
                    Statement stmt_taxon = con.createStatement();
                    ResultSet rs_taxon = stmt_taxon.executeQuery(sql_taxon);
                    Statement stmt_sset = con.createStatement();
                    ResultSet rs_sset = stmt_sset.executeQuery(sql_sset);

                    // Check that 1 and 2 are the same
                    if (rs_taxon.next() && rs_sset.next()) {
                        if (!rs_taxon.getString(1).equals(rs_sset.getString(1))) {
                            ReportManager.problem(this, con, "Species set " + rs_tag.getInt(1) + " has not the right set of genome_db_ids: " + rs_taxon.getString(1));
                            result = false;
                        }
                    }
                    rs_taxon.close();
                    stmt_taxon.close();
                    rs_sset.close();
                    stmt_sset.close();
                }
                rs_tag.close();
                stmt_tag.close();
            } catch (SQLException se) {
                se.printStackTrace();
                result = false;
            }
        } else {
            ReportManager.problem(this, con, "species_set_tag table is empty. There will be no colouring in the gene tree view");
            result = false;
        }


        return result;

    }

    private void usage() {

      ReportManager.problem(this, "USAGE", "run-healthcheck.sh -d ensembl_compara_.+ " + 
          " -d2 .+_core_.+ SpeciesSetTag");
    }

} // CheckSpeciesSetTag

