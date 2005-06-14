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

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that there are no translations for pseudogenes.
 */

public class Pseudogene extends SingleDatabaseTestCase {

    /**
     * Check the assembly_exception table.
     */
    public Pseudogene() {
        addToGroup("post_genebuild");
        addToGroup("release");
        setDescription("Check that there are no translations for pseudogenes");
    }

    /**
     * Check the data in the assembly_exception table. Note referential
     * integrity checks are done in CoreForeignKeys.
     * 
     * @param dbre
     *          The database to use.
     * @return Result.
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

        Connection con = dbre.getConnection();

        String qry = 
           "select translation.* from gene,transcript,translation " + 
           "where gene.biotype like '%pseudogene%'" +
            " and transcript.gene_id=gene.gene_id " +
            " and translation.transcript_id=transcript.transcript_id";

        int rows = getRowCount(con,qry); 
        if (rows > 0) {
            result = false;
            ReportManager.problem(this, con, "Translation table contains " + rows
                    + " rows for pseudogene types - should contain none");
        }


        if (result) {
            ReportManager.correct(this, con, "No pseudogenes have translations");
        }

        return result;

    }

    /**
     * This applies to 'core and 'vega' core schema databases
     */
    public void types() {

        removeAppliesToType(DatabaseType.EST);
        removeAppliesToType(DatabaseType.ESTGENE);

    }


} // Pseudogene
