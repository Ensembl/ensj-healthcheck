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

import org.ensembl.healthcheck.*;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.Utils;

/**
 * Check that features exist for expected analysis types.
 */
public class FeatureAnalysis extends SingleDatabaseTestCase {

    String[] proteinFeatureAnalyses = { "prints", "pfscan", "scanprosite", "signalp", "seg", "ncoils", "pfam", "tmhmm"};

    /**
     * Creates a new instance of FeatureAnalysis
     */
    public FeatureAnalysis() {

        addToGroup("post_genebuild");
        addToGroup("release");
        setDescription("Check that features exist for the expected analyses.");

    }

    public void types() {

        //	  this testcase only applies to CORE and VEGA
        removeAppliesToType(DatabaseType.EST);
        removeAppliesToType(DatabaseType.ESTGENE);

    }

    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

        // --------------------------------
        // Check protein_feature
        result &= checkFeatureAnalyses(dbre, "protein_feature", proteinFeatureAnalyses);

        // -------------------------------
        // TODO - other feature tables

        // -------------------------------

        return result;

    } // run

    // -----------------------------------------------------------------

    private boolean checkFeatureAnalyses(DatabaseRegistryEntry dbre, String table, String[] analyses) {

        logger.fine("Checking analyses for " + table + " in " + dbre.getName());

        boolean result = true;

        for (int i = 0; i < analyses.length; i++) {

            String sql = "SELECT COUNT(*) FROM " + table + ", analysis a WHERE LCASE(a.logic_name)='" + analyses[i]
                    + "' AND a.analysis_id=" + table + ".analysis_id";
            Connection con = dbre.getConnection();
            int rows = getRowCount(con, sql);
            if (rows == 0) {
                ReportManager.problem(this, con, table + " has no features for analysis " + analyses[i]);
            } else {
                ReportManager.correct(this, con, table + " has features for analysis " + analyses[i]);
            }
        }

        return result;

    }
    // -----------------------------------------------------------------

} // FeatureAnalysis
