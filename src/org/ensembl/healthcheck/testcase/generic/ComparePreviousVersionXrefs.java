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
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Compare the xrefs in the current database with those from the equivalent database on the secondary server.
 */

public class ComparePreviousVersionXrefs extends SingleDatabaseTestCase {

    /**
     * Create a new XrefTypes testcase.
     */
    public ComparePreviousVersionXrefs() {

        addToGroup("release");
        addToGroup("core_xrefs");
        setDescription("Compare the xrefs in the current database with those from the equivalent database on the secondary server");

    }

    /**
     * Run the test.
     * 
     * @param dbre The database to use.
     * @return true if the test pased.
     * 
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

        DatabaseRegistryEntry sec = getEquivalentFromSecondaryServer(dbre);

        logger.finest("Equivalent database on secondary server is " + sec.getName());

        Map currentXrefCounts = getXrefCounts(dbre);
        Map secondaryXrefCounts = getXrefCounts(sec);

        // compare each of the secondary (previous release, probably) with current
        Set externalDBs = secondaryXrefCounts.keySet();
        Iterator it = externalDBs.iterator();
        while (it.hasNext()) {
            String externalDB = (String) it.next();
            int secondaryCount = ((Integer) (secondaryXrefCounts.get(externalDB))).intValue();

            // check it exists at all
            if (currentXrefCounts.containsKey(externalDB)) {

                int currentCount = ((Integer) (currentXrefCounts.get(externalDB))).intValue();
                if (currentCount < secondaryCount) { // TODO - some sort of threshold?
                    ReportManager.problem(this, dbre.getConnection(), sec.getName() + " contains " + secondaryCount + " xrefs of type " + externalDB
                            + " but " + dbre.getName() + " has none");
                    result = false;
                }

            } else {
                ReportManager.problem(this, dbre.getConnection(), sec.getName() + " contains " + secondaryCount + " xrefs of type " + externalDB
                        + " but " + dbre.getName() + " has none");
                result = false;
            }
        }
        return result;

    } // run

    // ----------------------------------------------------------------------

    private Map getXrefCounts(DatabaseRegistryEntry dbre) {

        Map result = new HashMap();

        try {

            Statement stmt = dbre.getConnection().createStatement();

            logger.finest("Getting xref counts for " + dbre.getName());

            ResultSet rs = stmt.executeQuery("SELECT DISTINCT(e.db_name) AS db_name, COUNT(*) AS count"
                    + " FROM external_db e, xref x WHERE e.external_db_id=x.external_db_id GROUP BY e.db_name");

            while (rs != null && rs.next()) {
                result.put(rs.getString("db_name"), new Integer(rs.getInt("count")));
                //System.out.println("# " + rs.getString("db_name") + " " + rs.getInt("count"));
            }

            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    // ----------------------------------------------------------------------

} // XrefTypes
