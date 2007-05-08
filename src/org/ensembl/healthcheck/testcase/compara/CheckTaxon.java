/*
 * Copyright (C) 2003 EBI, GRL
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

package org.ensembl.healthcheck.testcase.compara;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.util.Vector;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;


/**
 * Check compara taxon table against core meta ones.
 */

public class CheckTaxon extends MultiDatabaseTestCase {

    /**
     * Create a new instance of MetaCrossSpecies
     */
    public CheckTaxon() {

        addToGroup("compara_external_foreign_keys");
        setDescription("Check that the attributes of the taxon table (genus, species," +
            " common_name and classification) correspond to the meta data in the core DB and vice versa.");

    }
    
    /**
     * Check that the attributes of the taxon table (genus, species, common_name and
     * classification) correspond to the meta data in the core DB and vice versa.
     * NB: A warning message is displayed if some dnafrags cannot be checked because
     * there is not any connection to the corresponding core database.
     * 
     * @param dbr
     *          The database registry containing all the specified databases.
     * @return true if the all the taxa in compara.taxon table which have a counterpart in
     *    the compara.genome_db table match the corresponding core databases.
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

        for (int i = 0; i < allComparaDBs.length; i++) {
            result &= checkTaxon(allComparaDBs[i], speciesDbrs);
        }
        return result;
    }


    /**
     * Check that the attributes of the taxon table (genus, species, common_name and
     * classification) correspond to the meta data in the core DB and vice versa.
     * NB: A warning message is displayed if some dnafrags cannot be checked because
     * there is not any connection to the corresponding core database.
     * 
     * @param comparaDbre
     *          The database registry entry for Compara DB
     * @param Map
     *          HashMap of DatabaseRegistryEntry[], one key/value pair for each Species.
     * @return true if the all the taxa in compara.taxon table which have a counterpart in
     *    the compara.genome_db table match the corresponding core databases.
     */
    public boolean checkTaxon(DatabaseRegistryEntry comparaDbre, Map speciesDbrs) {

        boolean result = true;
        Connection comparaCon = comparaDbre.getConnection();

        // Get list of species in compara
        Vector comparaSpecies = new Vector();
        String sql = "SELECT DISTINCT genome_db.name FROM genome_db WHERE assembly_default = 1";
        try {
          Statement stmt = comparaCon.createStatement();
          ResultSet rs = stmt.executeQuery(sql);
          while (rs.next()) {
            comparaSpecies.add(Species.resolveAlias(rs.getString(1).toLowerCase().replace(' ', '_')));
          }
          rs.close();
          stmt.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
        
        boolean allSpeciesFound = true;
        for (int i = 0; i < comparaSpecies.size(); i++) {
          Species species = (Species) comparaSpecies.get(i);
          DatabaseRegistryEntry[] speciesDbr = (DatabaseRegistryEntry[]) speciesDbrs.get(species);
          if (speciesDbr != null) {
            Connection speciesCon = speciesDbr[0].getConnection();
            String sql1, sql2;
            /* Get taxon_id */
            String taxon_id = getRowColumnValue(speciesCon,
                "SELECT meta_value FROM meta WHERE meta_key = \"species.taxonomy_id\"");
            
            /* Check name */
            sql1 = "SELECT \"name\", name " +
                " FROM ncbi_taxa_name WHERE name_class = \"scientific name\" AND taxon_id = " + taxon_id;
            sql2 = "SELECT \"name\", GROUP_CONCAT(meta_value ORDER BY meta_id DESC SEPARATOR \" \") " +
                " FROM (SELECT meta_id, meta_key, meta_value FROM meta " +
                " WHERE meta_key = \"species.classification\" ORDER BY meta_id LIMIT 2) AS name " +
                " GROUP BY meta_key";
            result &= compareQueries(comparaCon, sql1, speciesCon, sql2);
            
            /* Check common_name */
            sql1 = "SELECT \"common_name\", name " +
                " FROM ncbi_taxa_name WHERE name_class = \"genbank common name\" AND taxon_id = " + taxon_id;
            sql2 = "SELECT \"common_name\", meta_value FROM meta" +
                " WHERE meta_key = \"species.common_name\" and meta_value != \"\"";
            result &= compareQueries(comparaCon, sql1, speciesCon, sql2);
            
            /* Check classification */
            /* This check is quite complex as the axonomy is stored in very different ways in compara
               and core DBs. In compara, the tree structure is stored in the ncbi_taxa_node table
               while the names are in the ncbi_taxa_name table. In the core DB, the taxonomy is
               stored in the meta table as values of the key "species.classification" and they
               should be sorted by meta_id. In the core DB, only the abbreviated lineage is
               described which means that we have to ignore ncbi_taxa_node with the
               genbank_hidden_flag set. On top of that, we want to compare the classification
               in one single SQL. Therefore, we are getting the results recursivelly and
               then execute a dumb SQL query with result itself */
            String comparaClassification = new String("");
            String values1[] = getRowValues(comparaCon,
                "SELECT rank, parent_id, genbank_hidden_flag FROM ncbi_taxa_node WHERE taxon_id = " + taxon_id);
            if (values1.length == 0) {
              /* if no rows are fetched, this taxon is missing from compara DB */
              ReportManager.problem(this, comparaCon, "No taxon for " + species.toString());
            } else {
              String this_taxon_id = values1[1];
              while (!this_taxon_id.equals("0")) {
                values1 = getRowValues(comparaCon,
                    "SELECT rank, parent_id, genbank_hidden_flag FROM ncbi_taxa_node WHERE taxon_id = " + this_taxon_id);
                if (values1[2].equals("0") && !values1[1].equals("0") && !values1[0].equals("subgenus") && !values1[0].equals("subspecies")) {
                  comparaClassification += " " + getRowColumnValue(comparaCon,
                      "SELECT name FROM ncbi_taxa_name " +
                      "WHERE name_class = \"scientific name\" AND taxon_id = " + this_taxon_id);
                }
                this_taxon_id = values1[1];
              }
              sql1 = "SELECT \"classification\", \"" + comparaClassification + "\"";
              /* It will be much better to run this using GROUP_CONCAT() but our MySQL server does not support it yet */
              sql2 = "SELECT \"classification\", \"";
              String[] values2 = getColumnValues(speciesCon,
                  "SELECT meta_value FROM meta WHERE meta_key = \"species.classification\"" +
                  " ORDER BY meta_id");
              /* Skip first value as it is part of the species name and not the lineage */
              for (int a = 1; a < values2.length; a++) {
                sql2 += " " + values2[a];
              }
              sql2 += "\"";
              result &= compareQueries(comparaCon, sql1, speciesCon, sql2);
            }
          } else {
            ReportManager.problem(this, comparaCon, "No connection for " + species.toString());
            allSpeciesFound = false;
          }
        }
        if (!allSpeciesFound) {
          usage();
        }

        return result;
    }
    
    /**
     * Prints the usage through the ReportManager
     * 
     * @param
     *          The database registry containing all the specified databases.
     * @return true if the all the dnafrags are top_level seq_regions in their corresponding
     *    core database.
     */
    private void usage() {

      ReportManager.problem(this, "USAGE", "run-healthcheck.sh -d ensembl_compara_.+ " + 
          " -d2 .+_core_.+ CheckTaxon");
    }
    
} // CheckTopLevelDnaFrag
