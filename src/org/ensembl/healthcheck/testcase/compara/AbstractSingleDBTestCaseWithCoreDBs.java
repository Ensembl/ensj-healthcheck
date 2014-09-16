/*
 * Copyright [1999-2014] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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


package org.ensembl.healthcheck.testcase.compara;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;


public abstract class AbstractSingleDBTestCaseWithCoreDBs extends SingleDatabaseTestCase {

	/**
	 * Get a Map associating each species with its core database
	 *
	 * @return A map (key:Species, value:DatabaseRegistryEntry).
	 */
	public final Map<Species, DatabaseRegistryEntry> getSpeciesCoreDbMap(final DatabaseRegistry dbr) {

		HashMap<Species, DatabaseRegistryEntry> speciesCoreMap = new HashMap();

		for (DatabaseRegistryEntry entry : dbr.getAllEntries()) {
			// We need to check the database name because some _cdna_
			// databases have the DatabaseType.CORE type
			if (entry.getType().equals(DatabaseType.CORE) && entry.getName().contains("_core_")) {
				speciesCoreMap.put(entry.getSpecies(), entry);
			}
		}

		return speciesCoreMap;

	} // getSpeciesCoreDbMap

} // AbstractSingleDBTestCaseWithCoreDBs
