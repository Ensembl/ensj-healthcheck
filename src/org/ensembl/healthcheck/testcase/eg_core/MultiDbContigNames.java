/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2020] EMBL-European Bioinformatics Institute
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

package org.ensembl.healthcheck.testcase.eg_core;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;
import org.ensembl.healthcheck.util.CollectionUtils;
import org.ensembl.healthcheck.util.ConnectionBasedSqlTemplateImpl;

/**
 * EG specific test that does not use species to
 * 
 * @author dstaines
 * 
 */
public class MultiDbContigNames extends MultiDatabaseTestCase {

	public MultiDbContigNames() {
		super();
		setTeamResponsible(Team.ENSEMBL_GENOMES);
		addAppliesToType(DatabaseType.CORE);
	}

	private final static String SR_NAMES = "select s.name from seq_region s "
			+ "join coord_system c using (coord_system_id) "
			+ "where c.attrib like '%sequence_level%' and c.species_id=?";

	@Override
	public boolean run(DatabaseRegistry dbr) {
		boolean result = true;
		Map<String, Collection<String>> names = CollectionUtils.createHashMap();
		for (DatabaseRegistryEntry coreDb : dbr.getAll(DatabaseType.CORE)) {
			if (coreDb.getName().contains("_core_")) {
				ReportManager.info(this, coreDb.getConnection(), "Checking "
						+ coreDb.getName());
				ConnectionBasedSqlTemplateImpl template = new ConnectionBasedSqlTemplateImpl(
						coreDb.getConnection());
				for (int speciesId : coreDb.getSpeciesIds()) {
					for (String name : template.queryForDefaultObjectList(
							SR_NAMES, String.class, speciesId)) {
						Collection<String> dbs = names.get(name);
						if (dbs == null) {
							dbs = CollectionUtils.createArrayList(1);
							names.put(name, dbs);
							dbs.add(coreDb.getName() + ":" + speciesId);
						} else {
							dbs.add(coreDb.getName() + ":" + speciesId);
							ReportManager
									.problem(
											this,
											coreDb.getConnection(),
											"The sequence region name "
													+ name
													+ " has been found in the following core databases :"
													+ StringUtils.join(dbs,
															", "));
							result = false;
						}
					}
				}
			}
		}
		return result;
	}

}
