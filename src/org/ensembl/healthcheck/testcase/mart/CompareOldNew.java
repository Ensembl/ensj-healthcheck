/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

/*
 * 
 * $Log$ Revision 1.1.2.3 2004/03/17 17:31:53 gp1
 * Changes to nearly every file in the project; many are stylistic only, but
 * many remove potential errors (e.g. tab characters in SQL) or improve
 * performance (removal of use of on-demand imports) Revision 1.1.2.2
 * 2004/03/02 14:35:01 gp1 Updated for new API; now extends
 * OrderedDatabaseTestCase so getting the databases int the right order is more
 * straightforward.
 * 
 * Revision 1.1.2.1 2004/03/01 09:42:08 gp1 Moved into mart subdirectory. Some
 * tests renamed
 * 
 * Revision 1.3.2.1 2004/02/23 14:26:57 gp1 No longer depends on SchemaInfo etc
 * 
 * Revision 1.3 2004/01/12 11:19:50 gp1 Updated relevant dates (Copyright
 * notices etc) to 2004.
 * 
 * Revision 1.2 2003/11/19 09:13:45 dkeefe added checks for sudden increases or
 * decreases in distinct counts
 * 
 * Revision 1.1 2003/11/18 17:01:12 dkeefe Compare the _meta_table_info for old
 * and new mart. So far just looks for missing columns in new
 * 
 *  
 */

package org.ensembl.healthcheck.testcase.mart;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.OrderedDatabaseTestCase;

/**
 * Compares the contents of two marts and reports significant changes
 */
public class CompareOldNew extends OrderedDatabaseTestCase {

    /**
     * Creates a new instance of MartCompareOldNewTestCase
     */
    public CompareOldNew() {

        addToGroup("post_ensmartbuild");
        setDescription("Compares the _meta_table_info for two marts and reports big differences");

    }

    /**
     * Compare the _meta_table_info for two marts and report big differences.
     * 
     * @param databases
     *          The databases to check, in order old->new
     * @return true if the test passes.
     */
    public boolean run(DatabaseRegistryEntry[] databases) {

        boolean result = true;

        int i;
        Connection con;

        // first check we got acceptable input from user
        // check we have 2 and only 2 databases
        if (databases.length != 2) {
            result = false;
            logger.severe("Incorrect number of marts specified");
            return result;
        }

        DatabaseRegistryEntry mart1 = databases[0];
        DatabaseRegistryEntry mart2 = databases[1];
        System.out.println("Using " + mart1.getName() + " as mart one and " + mart2.getName() + " as mart 2");

        String query = "select table_column, column_non_null_value_count,column_distinct_non_null_value_count from _meta_table_info ";

        try {

            Connection con1 = mart1.getConnection();
            ResultSet rs1 = con1.createStatement().executeQuery(query);

            // put results for new mart in a hash
            Connection con2 = mart2.getConnection();
            ResultSet rs2 = con2.createStatement().executeQuery(query);
            Hashtable counts = new Hashtable();
            while (rs2.next()) {
                String key = rs2.getString(1);
                Integer nonNull = new Integer(rs2.getInt(2));
                Integer distinct = new Integer(rs2.getInt(3));
                counts.put(key, distinct);
                //System.out.print(key+" "+distinct+"\n");
            }

            while (rs1.next()) {
                String key = rs1.getString(1);
                Integer nonNull = new Integer(rs1.getInt(2));
                Integer distinct = new Integer(rs1.getInt(3));
                if (counts.containsKey(key)) {
                    // does the new mart contain this column
                    // if so compare the values
                    Integer newCount = (Integer) counts.get(key);
                    if (newCount.intValue() > distinct.intValue() * 2) {
                        ReportManager.info(this, con2, "SUDDEN INCREASE: " + key + " " + mart1.getName() + " "
                                + distinct + " " + mart2.getName() + " " + newCount);
                    }

                    if (newCount.intValue() < distinct.intValue() / 2) {
                        ReportManager.info(this, con2, "SUDDEN DECREASE: " + key + " " + mart1.getName() + " "
                                + distinct + " " + mart2.getName() + " " + newCount);
                    }

                    // and remove the entry from the hash table
                    counts.remove(key);
                } else {
                    // report the missing column as a problem
                    ReportManager.problem(this, con2, "MISSING COLUMN: " + key + " not in " + mart2.getName());
                    result = false;
                }

                // look at what is left in the hash table - ie new stuff in new
                // mart

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;

    } // run

} // CompareOldNew
