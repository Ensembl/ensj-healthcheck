/*
 Copyright (C) 2010 EBI, GRL
 
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
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that only genes have projected xrefs.
 */

public class ProjectedXrefGenes extends SingleDatabaseTestCase {

    /**
     * Constructor.
     */
    public ProjectedXrefGenes() {

        addToGroup("post_genebuild");
        addToGroup("release");
        setDescription("Check that only genes have projected xrefs");
        setTeamResponsible("Core");
      
    }

    /**
     * Run the test for each database.
     * @param dbre The database to check.
     * @return Result.
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

        Connection con = dbre.getConnection();
    			
    			String sql = "SELECT COUNT(*) FROM external_db e, xref x, object_xref ox, transcript t WHERE e.external_db_id=x.external_db_id AND x.xref_id=ox.xref_id AND ox.ensembl_object_type='Transcript' AND ox.ensembl_id=t.transcript_id AND x.info_type='PROJECTION'";
    			
    			int rows = getRowCount(con, sql);
    			
    			if (rows > 0) {
    				
    				ReportManager.problem(this, con, "There are " + rows + " projected xrefs linked to transcripts however only genes and translations should have them.");
    				result = false;
    				
    			} else {
    				
    				ReportManager.correct(this, con, "No projected xrefs associated with transcripts");
    				
    			}

        return result;
    }

} // ProjectedXrefGenes
