/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * AltAllele
 * 
 * @author lairdm
 */
package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Test to ensure altallele group members all map back to the same
 * chromosome
 * 
 * @author lairdm
 * 
 */

public class AltAllele extends SingleDatabaseTestCase {

    private static final int MAX_WARNINGS = 10;

    private final static String ALLELE_SQL = "SELECT aa.alt_allele_group_id, GROUP_CONCAT( DISTINCT CONCAT(IFNULL(ae.exc_seq_region_id, -1),',', g.seq_region_id, ',', IFNULL(ae.exc_type, 'NULL')) SEPARATOR ';') AS seq_regions FROM alt_allele_group aag LEFT JOIN alt_allele aa ON aa.alt_allele_group_id = aag.alt_allele_group_id LEFT JOIN gene g ON aa.gene_id = g.gene_id LEFT JOIN seq_region sr ON g.seq_region_id = sr.seq_region_id LEFT JOIN assembly_exception ae ON g.seq_region_id = ae.seq_region_id GROUP BY alt_allele_group_id";

    public AltAllele() {

	appliesToType(DatabaseType.CORE);
	setDescription("Test to ensure AltAllele group members all map back to the same chromosome");

	setTeamResponsible(Team.GENEBUILD);
	
    }

    /**
     * This test only applies to the core database.
     */
    public void types() {

	removeAppliesToType(DatabaseType.OTHERFEATURES);
	removeAppliesToType(DatabaseType.CDNA);
	removeAppliesToType(DatabaseType.SANGER_VEGA);
	removeAppliesToType(DatabaseType.RNASEQ);
	removeAppliesToType(DatabaseType.VEGA);

    }

    /**
     * Check if AltAllele group members map to different chromosomes.
     * 
     * @param dbre
     *          The database to check.
     * @return True if the test passes.
     */
    public boolean run(DatabaseRegistryEntry dbre) {

	boolean result = true;

	Connection con = dbre.getConnection();
	try {
	    Statement stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
	    stmt.setFetchSize(1000);
	    ResultSet rs = stmt.executeQuery(ALLELE_SQL);

	    // Loop through Alt Allele groups
	    while(rs.next()) {
		// Values for this row
		int alt_allele_group_id = rs.getInt("alt_allele_group_id");
		String seq_regions = rs.getString("seq_regions");

		// Store the seq_region_id for this Alt Allele group as
		// we loop through group members
		int seq_region_id = 0;

		// For each Alt Allel in the group
		for (String seq_region_pair : seq_regions.split(";")) {
		    int curr_seq_region_id = 0;

		    // We have the exception seq_region_id and the seq_region in our pair,
		    // for an allele that's mapped to the main sequence we'll have
		    // -1 in the exception column, and the seq_region_id in the second
		    // value. For alleles mapped to alternative splices, the base seq_region_id
		    // will be in the first value coming from the exception table.

		    // Break our AltAllele group member in to components, and cast the
		    // integer ones appropriately.
		    String[] seq_region_pieces = seq_region_pair.split(",");
		    int exc_seq_region_id = Integer.parseInt( seq_region_pieces[0] );
		    int base_seq_region_id = Integer.parseInt( seq_region_pieces[1] );

		    // If the seq_region_id from the exception table is -1 (NULL) or if
		    // the exception type is PAR, we want to use the seq_region_id from
		    // the base slice. Otherwise we'll use the seq_region_id from the
		    // exception table. Thibaut says blame him for the extra checking
		    // logic ;)
		    if( exc_seq_region_id == -1 || seq_region_pieces[2].equals("PAR") ) {
			curr_seq_region_id = base_seq_region_id;
		    } else {
			curr_seq_region_id = exc_seq_region_id;
		    }

		    // Did we even find a seq_region_id in this member of the group?
		    if(curr_seq_region_id > 0) {
			// First iteration, remember what our base seq_region_id should be
			if(seq_region_id == 0) {
			    seq_region_id = curr_seq_region_id;

			// Does this member's seq_region_id not match the first member
			// of the group? Error!
			} else if(seq_region_id != curr_seq_region_id) {
			    ReportManager.problem(this, con, "Has non-matching seq_regions for AltAllele group " + alt_allele_group_id + ".");
			    result = false;
			    break;
			}
		    }
		} // end: for (String seq_region_pair : seq_regions.split(';'))

		// What happens if we didn't find any seq_region_id, that's a different error!
		if(seq_region_id == 0) {
		    ReportManager.problem(this, con,"Has no seq_region_id associated with AltAllele group " + alt_allele_group_id + ".");
		    result = false;
		}
	    } // end: while(rs.next())

	} catch (Exception e) {
	    result = false;
	    e.printStackTrace();
	}

	if (result) {
	    ReportManager.correct(this, con, "No mis-matched AltAllele groups.");
	}

	return result;

    }

} //AltAllele
