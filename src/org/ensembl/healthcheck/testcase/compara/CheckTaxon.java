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

import java.util.HashMap;
import java.util.Map;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Vector;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;
import org.ensembl.healthcheck.Species;


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
     * @return true if the all the dnafrags are top_level seq_regions in their corresponding
     *    core database.
     */
    public boolean run(DatabaseRegistry dbr) {

        boolean result = true;

        DatabaseRegistryEntry[] allDBs = dbr.getAll();
        DatabaseRegistryEntry comparaDB;
        int comparaIndex = -1;
        Connection con;
        for (int i = 0; i < allDBs.length; i++) {
          if (allDBs[i].getType().toString().equalsIgnoreCase("compara")) {
              comparaIndex = i;
          }
        }
        if (comparaIndex == -1) {
          result = false;
          ReportManager.problem(this, "", "Cannot find compara database");
          usage();
          return false;
        } else {
          comparaDB = allDBs[comparaIndex];
          con = comparaDB.getConnection();
        }
        Map speciesCons = new HashMap();
        for (int i = 0; i < allDBs.length; i++) {
          if (i != comparaIndex) {
            Species s = allDBs[i].getSpecies();
            DatabaseRegistryEntry[] speciesDBs = dbr.getAll(s);
            logger.finest("Got " + speciesDBs.length + " databases for " + s.toString());
            String name = s.toString().replace('_', ' ');
            Connection speciesCon = allDBs[i].getConnection();
            speciesCons.put(name.toLowerCase(), speciesCon);
          }
        }
        Vector comparaSpecies = new Vector();
        String sql = "SELECT DISTINCT genome_db.name FROM genome_db WHERE assembly_default = 1";
        try {
          Statement stmt = con.createStatement();
          ResultSet rs = stmt.executeQuery(sql);
          while (rs.next()) {
            comparaSpecies.add(rs.getString(1).toLowerCase());
          }
          rs.close();
          stmt.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
        
        boolean allSpeciesFound = true;
        for (int i = 0; i < comparaSpecies.size(); i++) {
          String name = (String) comparaSpecies.get(i);
          if (speciesCons.get(name) != null) {
            String sql1, sql2;
            /* Get taxon_id */
            String taxon_id = getRowColumnValue((Connection) speciesCons.get(name),
                "SELECT meta_value FROM meta WHERE meta_key = \"species.taxonomy_id\"");
            
            /* Check genus */
            sql1 = "SELECT \"genus\", genus " +
                " FROM taxon where taxon_id = " + taxon_id;
            sql2 = "SELECT \"genus\", meta_value FROM meta" +
                " WHERE meta_key = \"species.classification\" ORDER BY meta_id LIMIT 1, 1";
            result &= compareQueries(con, sql1, (Connection) speciesCons.get(name), sql2);
            
            /* Check species */
            sql1 = "SELECT \"species\", species " +
                " FROM taxon where taxon_id = " + taxon_id;
            sql2 = "SELECT \"species\", meta_value FROM meta" +
                " WHERE meta_key = \"species.classification\" ORDER BY meta_id LIMIT 0, 1";
            result &= compareQueries(con, sql1, (Connection) speciesCons.get(name), sql2);
            
            /* Check common_name */
            sql1 = "SELECT \"common_name\", common_name " +
                " FROM taxon where taxon_id = " + taxon_id;
            sql2 = "SELECT \"common_name\", meta_value FROM meta" +
                " WHERE meta_key = \"species.common_name\"";
            /* Do not take the result into account at the moment:
                    discrepancies are known and will be removed at some point
            result &= compareQueries(con, sql1, (Connection) speciesCons.get(name), sql2);*/
            compareQueries(con, sql1, (Connection) speciesCons.get(name), sql2);
            
            /* Check classification */
            int depth = getRowCount((Connection) speciesCons.get(name),
                "SELECT count(*) FROM meta WHERE meta_key = \"species.classification\"");
            sql1 = "SELECT \"classification\", classification " +
                " FROM taxon where taxon_id = " + taxon_id;
            /* It will be much better to run this using GROUP_CONCAT() but our MySQL server does not support it yet */
            sql2 = "SELECT \"classification\", \"";
            String[] values = getColumnValues((Connection) speciesCons.get(name),
                    "SELECT meta_value FROM meta WHERE meta_key = \"species.classification\"" +
                    " ORDER BY meta_id");
            sql2 += values[0];
            for (int a = 1; a < values.length; a++) {
              sql2 += " " + values[a];
            }
            sql2 += "\"";
            compareQueries(con, sql1, (Connection) speciesCons.get(name), sql2);
          } else {
            ReportManager.problem(this, con, "No connection for " + comparaSpecies.get(i));
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
          " -d .+_core_.+ CheckTaxon");
    }
    
} // CheckTopLevelDnaFrag
