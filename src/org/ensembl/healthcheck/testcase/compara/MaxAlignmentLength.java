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

import java.util.HashMap;
import java.util.Iterator;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.Repair;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.Utils;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key
 * relationships.
 */

public class MaxAlignmentLength extends SingleDatabaseTestCase implements Repair {

    private HashMap MetaEntriesToAdd = new HashMap();
    private HashMap MetaEntriesToRemove = new HashMap();
    private HashMap MetaEntriesToUpdate = new HashMap();

    /**
     * Create an ForeignKeyMethodLinkId that applies to a specific set of databases.
     */
    public MaxAlignmentLength() {

        addToGroup("compara_db_constraints");
        setDescription("Tests that proper max_alignment_length have been defined.");

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
        int globalMaxAlignmentLength = 0;

        Connection con = dbre.getConnection();

        // Check whether tables are empty or not
        if (!tableHasRows(con, "meta") || !tableHasRows(con, "genomic_align")) {
            ReportManager.problem(this, con, "NO ENTRIES in meta or in genomic_align table");
            return false;
        }

        // Calculate current max_alignment_length by method_link_species_set
        String sql = new String("SELECT CONCAT('max_align_', method_link_species_set_id)," +
            " MAX(dnafrag_end - dnafrag_start) FROM genomic_align" +
            " GROUP BY method_link_species_set_id");
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            // Add 2 to the dnafrag_end - dnafrag_start in order to get length + 1.
            // Adding this at this point is probably faster than asking MySQL to add 2
            // to every single row...
            while (rs.next()) {
                MetaEntriesToAdd.put(rs.getString(1), new Integer(rs.getInt(2) + 2).toString());
                if (rs.getInt(2) > globalMaxAlignmentLength) {
                    globalMaxAlignmentLength = rs.getInt(2) + 2;
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }

        // Get values currently stored in the meta table
        sql = new String("SELECT meta_key, meta_value, count(*)" +
            " FROM meta WHERE meta_key LIKE \"max\\_align\\_%\" GROUP BY meta_key");
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                if (rs.getInt(3) != 1) {
                    MetaEntriesToRemove.put(rs.getString(1), rs.getString(2));
                } else if (MetaEntriesToAdd.containsKey(rs.getString(1))) {
                    if (!MetaEntriesToAdd.get(rs.getString(1)).equals(rs.getString(2))) {
                        MetaEntriesToUpdate.put(rs.getString(1),
                            MetaEntriesToAdd.get(rs.getString(1)));
                    }
                    MetaEntriesToAdd.remove(rs.getString(1));
                } else {
                    MetaEntriesToRemove.put(rs.getString(1), rs.getString(2));
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }

        // Get current global value from the meta table (used for backwards compatibility)
        sql = new String("SELECT meta_key, meta_value" +
            " FROM meta WHERE meta_key = \"max_alignment_length\"");
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.first()) {
                if (rs.getInt(2) != globalMaxAlignmentLength) {
                    MetaEntriesToUpdate.put(new String("max_alignment_length"),
                        new Integer(globalMaxAlignmentLength).toString());
                }
            } else {
                MetaEntriesToAdd.put(new String("max_alignment_length"),
                    new Integer(globalMaxAlignmentLength).toString());
            }
            rs.close();
            stmt.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }

        Iterator it = MetaEntriesToRemove.keySet().iterator();
        while (it.hasNext()) {
            Object next = it.next();
            ReportManager.problem(this, con, "Remove from meta: " + next +
                " -- " + MetaEntriesToRemove.get(next));
            result = false;
        }
        it = MetaEntriesToAdd.keySet().iterator();
        while (it.hasNext()) {
            Object next = it.next();
            ReportManager.problem(this, con, "Add in meta: " + next +
                " -- " + MetaEntriesToAdd.get(next));
            result = false;
        }
        it = MetaEntriesToUpdate.keySet().iterator();
        while (it.hasNext()) {
            Object next = it.next();
            ReportManager.problem(this, con, "Update in meta: " + next +
                " -- " + MetaEntriesToUpdate.get(next));
            result = false;
        }

        return result;

    }

    // ------------------------------------------
    // Implementation of Repair interface.

    /**
     * Update, insert and delete entries in the meta table in order to match
     * max. alignment lengths found in the genomic_align table.
     * 
     * @param dbre
     *            The database to use.
     */
    public void repair(DatabaseRegistryEntry dbre) {

        if (MetaEntriesToAdd.isEmpty() && MetaEntriesToUpdate.isEmpty() &&
                MetaEntriesToRemove.isEmpty()) {
            System.out.println("No repair needed.");
            return;
        }

        System.out.print("Repairing <meta> table... ");
        Connection con = dbre.getConnection();
        try {
            Statement stmt = con.createStatement();

            // Start by removing entries as a duplicated entry will be both deleted and then inserted
            Iterator it = MetaEntriesToRemove.keySet().iterator();
            while (it.hasNext()) {
                Object next = it.next();
                String sql = new String("DELETE FROM meta WHERE meta_key = \"" + next + "\";");
                int numRows = stmt.executeUpdate(sql);
                if (numRows != 1) {
                    ReportManager.problem(this, con, "WARNING: " + numRows
                        + " rows DELETED for meta_key \"" + next
                        + "\" while repairing meta");
                }
            }
            it = MetaEntriesToAdd.keySet().iterator();
            while (it.hasNext()) {
                Object next = it.next();
                String sql = new String("INSERT INTO meta VALUES (NULL, \"" + next + "\", "
                    + MetaEntriesToAdd.get(next) + ");");
                int numRows = stmt.executeUpdate(sql);
                if (numRows != 1) {
                    ReportManager.problem(this, con, "WARNING: " + numRows
                        + " rows INSERTED for meta_key \"" + next
                        + "\" while repairing meta");
                }
            }
            it = MetaEntriesToUpdate.keySet().iterator();
            while (it.hasNext()) {
                Object next = it.next();
                String sql = new String("UPDATE meta SET meta_value = "
                    + MetaEntriesToUpdate.get(next) + " WHERE meta_key = \"" + next + "\";");
                int numRows = stmt.executeUpdate(sql);
                if (numRows != 1) {
                    ReportManager.problem(this, con, "WARNING: " + numRows
                        + " rows UPDATED for meta_key \"" + next
                        + "\" while repairing meta");
                }
            }
            stmt.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }

        System.out.println(" ok.");

    }

    /**
     * Show MySQL statements needed to repair meta table
     * 
     * @param dbre
     *            The database to use.
     */
    public void show(DatabaseRegistryEntry dbre) {

        if (MetaEntriesToAdd.isEmpty() && MetaEntriesToUpdate.isEmpty() &&
                MetaEntriesToRemove.isEmpty()) {
            System.out.println("No repair needed.");
            return;
        }

        System.out.println("MySQL statements needed to repair meta table:");

        Iterator it = MetaEntriesToRemove.keySet().iterator();
        while (it.hasNext()) {
            Object next = it.next();
            System.out.println("  DELETE FROM meta WHERE meta_key = \"" + next + "\";");
        }
        it = MetaEntriesToAdd.keySet().iterator();
        while (it.hasNext()) {
            Object next = it.next();
            System.out.println("  INSERT INTO meta VALUES (NULL, \"" + next + "\", "
                + MetaEntriesToAdd.get(next) + ");");
        }
        it = MetaEntriesToUpdate.keySet().iterator();
        while (it.hasNext()) {
            Object next = it.next();
            System.out.println("  UPDATE meta SET meta_value = "
                + MetaEntriesToUpdate.get(next) + " WHERE meta_key = \"" + next + "\";");
        }

    }

    // -------------------------------------------------------------------------

} // ForeignKeyMethodLinkId
