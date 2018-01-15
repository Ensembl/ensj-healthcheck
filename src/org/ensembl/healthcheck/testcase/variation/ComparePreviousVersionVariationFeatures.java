/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2017] EMBL-European Bioinformatics Institute
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
package org.ensembl.healthcheck.testcase.variation;

import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionBase;
import org.ensembl.healthcheck.Species;

/**
 * Compare the number of variation features between the current database and the database on the secondary server.
 */

public class ComparePreviousVersionVariationFeatures extends ComparePreviousVersionBase {

	/**
	 * Create a new testcase.
	 */
	public ComparePreviousVersionVariationFeatures() {

		setDescription("Compare the number of variation features in the current database with those from the equivalent database on the secondary server");
		setTeamResponsible(Team.VARIATION);

	}

	// ------------------------------------------------------------------------
	
	public boolean run(DatabaseRegistryEntry dbre) {

		// Additional check to avoid having thousands of HC "Problem" when comparing 2 differents assemblies (with different contigs/scaffolds).
		if (sameAssemblyNumber(dbre)) {
			return super.run(dbre);
		} else {
			return true;
		}

	} // run
		
	// ------------------------------------------------------------------------
	
	protected Map getCounts(DatabaseRegistryEntry dbre) {
		
	    //	return getCountsBySQL(dbre, "SELECT sr.name, COUNT(*) FROM seq_region sr JOIN variation_feature vf ON (sr.seq_region_id = vf.seq_region_id) GROUP BY sr.name");

		String vardbname  = dbre.getName();
		String coredbname = vardbname.replace("variation", "core");

           return getCountsBySQL(dbre, "SELECT src.name, COUNT(*) FROM " + coredbname + ".seq_region src, " + coredbname + ".coord_system cs, variation_feature vf where cs.rank = 1 and cs.coord_system_id = src.coord_system_id and src.seq_region_id = vf.seq_region_id GROUP BY src.name");

	}

	// ------------------------------------------------------------------------

	protected String entityDescription() {

		return "number of variation features on seq_region";

	}

	// ------------------------------------------------------------------------

	protected double threshold() {

		return 1;

	}

        // ------------------------------------------------------------------------

        protected double minimum() {

                return 0;

        }

	// ------------------------------------------------------------------------
	
	protected boolean sameAssemblyNumber(DatabaseRegistryEntry dbre) {
		
		// Primary database
		String variationName = dbre.getName();
		String[] parts = variationName.split("_");
		String assembly_prim = parts[4];
		
		// Secondary database
		DatabaseRegistryEntry sec = getEquivalentFromSecondaryServer(dbre);
		if (sec == null) {
			logger.warning("Can't get equivalent database for " + dbre.getName());
			return true;
		}
		
		String variationName2 = sec.getName();
		String[] parts2 = variationName2.split("_");
		String assembly_sec = parts2[4];
		
		
		// Compare the last number in the database names (e.g. homo_sapiens_variation_70_37 => "37" & homo_sapiens_variation_69_37 => "37")
		// Corresponds to the assembly number
		if (assembly_sec.equals(assembly_prim)) {
			return true;
		} else {
			return false;
		}
	}
	
	// ------------------------------------------------------------------------

} // ComparePreviousVersionVariationFeatures
