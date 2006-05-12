/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.generic;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Species;

/**
 * Check that all the transcripts in the human CCDS set are present.
 */
public class HumanCCDS extends BaseCCDS {

	/**
	 * Creates a new instance of HumanCCDS.
	 */
	public HumanCCDS() {

		addToGroup("release");
		
		setDescription("Check that all the transcripts in the human CCDS set are present.");

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

		// return true for any non-human databases
		if (dbre.getSpecies() != Species.HOMO_SAPIENS) {
			
			logger.warning("Human CCDS healthcheck returning default of true for " + dbre.getSpecies() + " database type " + dbre.getType().toString());
			return true;
			
		}
		
		return doRun(dbre);
		
	} // run
	
} // HumanCCDS
