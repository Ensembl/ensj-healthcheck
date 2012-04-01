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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * An EnsEMBL Healthcheck test case that looks for entries in the GenomeDB table with the same name and assembly_default set to true for more than one.
 */


public class DuplicateGenomeDb extends SingleDatabaseTestCase {

    public DuplicateGenomeDb() {

        addToGroup("compara_homology"); 
        setDescription("Searches for species where assembly_default has been set to true more than once for the same name. This seems to happen when the contents of the GenomeDB table is copied from the master to the pan compara database. ");
        setTeamResponsible(Team.ENSEMBL_GENOMES);

    }

    /**
     * Run the test.
     * 
     * @param dbre
     *          The database to use.
     * @return true if the test pased.
     *  
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

        Connection con = dbre.getConnection();

        String sql = "SELECT "
	        + "	genome_db_id, "
	        + "	a.name, "
	        + "	assembly, "
	        + "	assembly_default "
	        + "FROM ( "
	        + "	SELECT NAME, count(*) AS cnt "
	        + "	FROM genome_db "
	        + "	WHERE assembly_default = 1 "
	        + "	GROUP BY NAME "
	        + "	HAVING cnt > 1 "
	        + ") a JOIN genome_db g ON (g.name = a.name) ORDER BY NAME; ";


        String[] genomeDbIds = DBUtils.getColumnValues(con, sql);

        if (genomeDbIds.length > 0) {
            result = false;
            ReportManager.problem(this, con,
                    "Genome dbs were found with the same name with assembly_default set to true:");

    		try {
    			Statement stmt = con.createStatement();
    			ResultSet rs = stmt.executeQuery(sql);

    				while (rs.next()) {
    					ReportManager.problem(this, con, 
    					"  genome_db_id: "      + rs.getString("genome_db_id")
    					+ " a.name: "           + rs.getString("a.name")
    					+ " assembly: "         + rs.getString("assembly")
    					+ " assembly_default: " + rs.getString("assembly_default")
                        );
    				}

    			rs.close();
    			stmt.close();
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
            
        } else {
            ReportManager.correct(this, con, "PASSED test! :-D");
        }

        return result;
    }

} // DuplicateGenomeDb
