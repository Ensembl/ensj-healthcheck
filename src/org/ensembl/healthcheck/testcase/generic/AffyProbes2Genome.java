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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.TextTestRunner;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check mappings from Affymetrix probes to genome.
 * 
 * Even though we *don't* provide Affymetrix data for all species the healthcheck follows the convention of failing if the data is missing.
 */
public class AffyProbes2Genome extends SingleDatabaseTestCase {

    /**
     * Runs test against a few databases on the server specified in database.properties.
     * 
     * @param args ignored.
     */
    public static void main(String[] args) {
        TextTestRunner.main(new String[] { "-d", "homo_sapiens_core_3.*", "-d", "pan_troglodytes_core_3.*", "AffyProbes2Genome" });
    }

    /**
     * Creates a new instance of FeatureAnalysis
     */
    public AffyProbes2Genome() {

        addToGroup("post_genebuild");
        addToGroup("release");

    }

    /**
     * This test only applies to core databases.
     */
    public void types() {

        removeAppliesToType(DatabaseType.EST);
        removeAppliesToType(DatabaseType.CDNA);
        removeAppliesToType(DatabaseType.VEGA);

    }

    /**
     * Run the test.
     * 
     * @param dbre The database to use.
     * @return true if the test pased.
     * 
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        Connection con = dbre.getConnection();

        if (testAffyTablesPopulated(dbre)) {
            return testProbsetSizesSet(con) && testAffyArraysInExternalDB(con) && testAffyFeatureInMetaCoord(con);
        } else {
            return false;
        }

    }

    private boolean testAffyArraysInExternalDB(Connection con) {

        boolean result = true;

        // We have to do some guessing and pattern matching to find the
        // external database corresponding to this AffyArray because the
        // names used in external_db.db_name do not quite match affy_array.name.

        // 1 - get set of external_db.db_names
        String[] xdbNames = getColumnValues(con, "SELECT db_name FROM external_db");
        Set xdbNamesSet = new HashSet();
        for (int i = 0; i < xdbNames.length; i++)
            xdbNamesSet.add(xdbNames[i].toLowerCase());

        // 2 - check to see if every affy_array.name is in the set of
        // external_db.db_names.
        String[] affyArrayNames = getColumnValues(con, "SELECT name FROM affy_array");
        for (int i = 0; i < affyArrayNames.length; i++) {

            String name = affyArrayNames[i];
            Set possibleExternalDBNames = new HashSet();
            possibleExternalDBNames.add(name.toLowerCase());
            possibleExternalDBNames.add(name.toLowerCase().replace('-', '_'));
            possibleExternalDBNames.add(("affy_" + name).toLowerCase().replace('-', '_'));
            possibleExternalDBNames.add(("afyy_" + name).toLowerCase().replace('-', '_'));

            possibleExternalDBNames.retainAll(xdbNamesSet);

            if (possibleExternalDBNames.size() == 0) {
                ReportManager.problem(this, con, "AffyArray (affy_array.name) " + name + " has no corresponding entry in external_db");
                result = false;
            }

        }

        return result;
    }

    /**
     * Checks that all affy_* tables are populated.
     * 
     * If at least one is not then the test fails.
     * 
     * @param con
     * @return true if all affy_* tables have rows, otherwise false.
     */

    private boolean testAffyTablesPopulated(DatabaseRegistryEntry dbre) {

        List emptyTables = new ArrayList();

        String[] tables = { "affy_array", "affy_probe", "affy_feature" };

        Species species = dbre.getSpecies();
        Connection con = dbre.getConnection();

        if (species == Species.HOMO_SAPIENS || species == Species.MUS_MUSCULUS || species == Species.RATTUS_NORVEGICUS
                || species == Species.GALLUS_GALLUS || species == Species.DANIO_RERIO) {

            for (int i = 0; i < tables.length; i++)
                if (Integer.parseInt(getRowColumnValue(con, "SELECT count(*) from " + tables[i])) == 0)
                    emptyTables.add(tables[i]);

        }
        if (emptyTables.size() == 0)
            return true;
        else {
            ReportManager.problem(this, con, "Empty table(s): " + emptyTables);
            return false;
        }

    }

    private boolean testProbsetSizesSet(Connection con) {

        boolean result = true;

        try {
            String sql = "SELECT name, probe_setsize FROM affy_array";
            for (ResultSet rs = con.createStatement().executeQuery(sql); rs.next();) {
                int probesetSize = rs.getInt("probe_setsize");
                if (probesetSize < 1) {
                    ReportManager.problem(this, con, "affy_array.probeset_size not set for " + rs.getString("name"));
                    result = false;
                }
            }
        } catch (SQLException e) {
            result = false;
            e.printStackTrace();
        }

        return result;
    }
    

    private boolean testAffyFeatureInMetaCoord(Connection con) {

      boolean result = true;

      String sql = "select * from meta_coord where table_name='affy_feature'";
      if (getRowCount(con, sql) == 0) {
        ReportManager.problem(this, con, "no entry for affy_feature in meta_coord table. ");
        result = false;
      }
      
      return result;
  }

    
}
