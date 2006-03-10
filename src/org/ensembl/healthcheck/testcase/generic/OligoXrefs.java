/*
 Copyright (C) 2003 EBI, GRL

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
package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check Oligometrix xrefs: - that each chromosome has at least 1 Oligo xref
 */
public class OligoXrefs extends SingleDatabaseTestCase {

    // if a database has more than this number of seq_regions in the chromosome coordinate system, it's ignored
    private static final int MAX_CHROMOSOMES = 50;

    /**
     * Creates a new instance of OligoXrefs
     */
    public OligoXrefs() {

        addToGroup("post_genebuild");
        addToGroup("release");
        addToGroup("core_xrefs");
        setDescription("Check oligo xrefs");
        setHintLongRunning(true);

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

        Connection con = dbre.getConnection();

        // Check if there are any Oligo features - if so there should be Oligo Xrefs
        if (getRowCount(con, "SELECT COUNT(*) FROM oligo_array") > 0) {

            // Get a list of chromosomes, then check the number of Oligo xrefs associated with each one
            // Note that this can't be done with a GROUP BY/HAVING clause as that would miss any chromosomes that had zero xrefs
            String sql = "SELECT DISTINCT(sr.name) AS chromosome FROM seq_region sr, coord_system cs "
                    + "WHERE sr.coord_system_id=cs.coord_system_id AND cs.name='chromosome' AND sr.name NOT LIKE '%\\_%'";

            String[] chrNames = getColumnValues(con, "SELECT s.name FROM seq_region s, coord_system c WHERE c.coord_system_id=s.coord_system_id AND c.name='chromosome'");

            if (chrNames.length > MAX_CHROMOSOMES) {

                ReportManager.problem(this, con, "Database has more than " + MAX_CHROMOSOMES + " seq_regions in 'chromosome' coordinate system (actually " + chrNames.length + ") - test skipped");
                result = false;
                
            } else {
                for (int i = 0; i < chrNames.length; i++) {

                    logger.fine("Counting Oligo xrefs associated with chromosome " + chrNames[i]);

                    sql = "SELECT DISTINCT(sr.name) AS chromosome, COUNT(x.xref_id) AS count "
                            + "FROM xref x, external_db e, object_xref ox, translation tl, transcript ts, gene g, seq_region sr, coord_system cs "
                            + "WHERE e.db_name LIKE \'AFFY%\' AND x.external_db_id=e.external_db_id "
                            + "AND x.xref_id=ox.xref_id AND ox.ensembl_id=tl.translation_id "
                            + "AND tl.transcript_id=ts.transcript_id AND ts.gene_id=g.gene_id "
                            + "AND g.seq_region_id=sr.seq_region_id AND sr.coord_system_id=cs.coord_system_id "
                            + "AND cs.name=\'chromosome\' AND sr.name='" + chrNames[i] + "\' GROUP BY chromosome";

                    int count = 0;
                    try {
                        ResultSet rs = con.createStatement().executeQuery(sql);
                        if (rs.next()) {
			    count = rs.getInt("count"); 
			}
                        rs.close();
                    } catch (SQLException se) {
                        se.printStackTrace();
                    }

                    if (count == 0) {
                        ReportManager.problem(this, con, "Chromosome " + chrNames[i] + " has no associated Oligo xrefs.");
                        result = false;
                    } else if (count < 0) {
                        logger.warning("Could not get count for chromosome " + chrNames[i]);
                    } else {
                        ReportManager.correct(this, con, "Chromosome " + chrNames[i] + " has " + count + " associated Oligo xrefs.");
                    }
                }
            }

        } else {
	    
	    logger.info(DBUtils.getShortDatabaseName(con) + " has no Oligo features, not checking for Oligo xrefs");

	}

        return result;

    } // run

} // OligoXrefs

