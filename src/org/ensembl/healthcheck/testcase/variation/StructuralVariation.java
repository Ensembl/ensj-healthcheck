/*
 * Copyright (C) 2004 EBI, GRL
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

package org.ensembl.healthcheck.testcase.variation;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.ReportManager;

/**
 * Check that the structural variations do not contain anomalities
 */
public class StructuralVariation extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of StructuralVariation
	 */
	public StructuralVariation() {

		addToGroup("variation");
		addToGroup("variation-release");

		setDescription("Checks that the structural variation tables make sense");

	}

	// ---------------------------------------------------------------------

	/**
	 * Check that the structural variation tables make sense.
	 * 
	 * @param dbre
	 *          The database to check.
	 * @return true if the test passed.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

	    Connection con = dbre.getConnection();

	    boolean result = true;
	    result = (checkCountIsZero(con,"structural_variation","(seq_region_start < bound_start OR seq_region_end > bound_end)") && result);
	    result = (checkCountIsZero(con,"structural_variation","seq_region_start = seq_region_end") && result);
	    
	    if (!result) {
		ReportManager.problem(this, con, "NOTE: In total, structural_variation contains " + String.valueOf(countRowsInTable(con,"structural_variation")) + " entries");
	    }
	    return result;

	} // run

	// -----------------------------------------------------------------


   /**
     * This only applies to variation databases.
     */
     public void types() {

	 removeAppliesToType(DatabaseType.OTHERFEATURES);
	 removeAppliesToType(DatabaseType.CDNA);
	 removeAppliesToType(DatabaseType.CORE);
	 removeAppliesToType(DatabaseType.VEGA);

     }

} // StructuralVariation
