/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2021] EMBL-European Bioinformatics Institute
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

package org.ensembl.healthcheck.test;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author glenn
 * 
 *         To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Generation - Code and Comments
 */
public class DatabaseRegistryEntryTest {

	@Test
	public void testSetSpeciesAndTypeFromName() {

		// all of these should resolve
		String[] names = { "homo_sapiens_core_20_34", "human_core_20" };

		for (int i = 0; i < names.length; i++) {
			DatabaseRegistryEntry dbre = new DatabaseRegistryEntry(null, names[i], null, null);
			Assert.assertTrue(!dbre.getSpecies().equals(DatabaseRegistryEntry.UNKNOWN));
			Assert.assertTrue(dbre.getType() != DatabaseType.UNKNOWN);
		}
	}

	// -----------------------------------------------------------------

}
