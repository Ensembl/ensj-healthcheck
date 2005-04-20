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
 * Check dnafrag table against core databases.
 */

public class CheckTopLevelDnaFrag extends MultiDatabaseTestCase {

    /**
     * Create a new instance of MetaCrossSpecies
     */
    public CheckTopLevelDnaFrag() {

        addToGroup("compara_external_foreign_keys");
        setDescription("Check that every dnafrag corresponds to a top_level seq_region in the core DB and vice versa.");

    }
    
    /**
     * Check that every dnafrag corresponds to a top_level seq_region in the core DB
     * and vice versa.
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
          if ((i != comparaIndex) &&
              (allDBs[i].getType().toString().equalsIgnoreCase("core"))) {
            Species s = allDBs[i].getSpecies();
            DatabaseRegistryEntry[] speciesDBs = dbr.getAll(s);
            logger.finest("Got " + speciesDBs.length + " databases for " + s.toString());
            String name = s.toString().replace('_', ' ');
            Connection speciesCon = allDBs[i].getConnection();
            speciesCons.put(name.toLowerCase(), speciesCon);
          }
        }
        Vector comparaSpecies = new Vector();
        String sql = "SELECT DISTINCT genome_db.name FROM dnafrag LEFT JOIN genome_db USING (genome_db_id)";
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
            String sql1 = "SELECT dnafrag.coord_system_name, dnafrag.name" +
                " FROM dnafrag LEFT JOIN genome_db USING (genome_db_id)" +
                " WHERE genome_db.name = \"" + name + "\"";
            String sql2 = "SELECT coord_system.name, seq_region.name" +
                " FROM seq_region, coord_system, seq_region_attrib, attrib_type" +
                " WHERE seq_region.coord_system_id = coord_system.coord_system_id " +
                " AND seq_region.seq_region_id = seq_region_attrib.seq_region_id " +
                " AND seq_region_attrib.attrib_type_id = attrib_type.attrib_type_id " +
                " AND attrib_type.code = \"toplevel\"";
            result &= compareQueries(con, sql1, (Connection) speciesCons.get(name), sql2);
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
          " -d .+_core_.+ CheckTopLevelDnaFrag");
    }
    
} // CheckTopLevelDnaFrag
