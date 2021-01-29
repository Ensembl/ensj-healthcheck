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
import org.ensembl.healthcheck.util.TemplateBuilder;

/**
 * EG specific test that does not use species to
 * 
 * @author dstaines
 * 
 */
public class MultiDbSpeciesNames extends MultiDatabaseTestCase {

	public MultiDbSpeciesNames() {
		super();
		setTeamResponsible(Team.ENSEMBL_GENOMES);
		addAppliesToType(DatabaseType.CORE);
	}

	private final static String STABLE_ID = "select meta_value from meta where meta_key=?";
	private final static String[] META_KEYS = { "species.production_name",
			"species.db_name", "species.display_name", "species.alias" };

	private final static String NAME_HOLDER = "name";
	private final static String KEY_HOLDER = "metaKey";
	private final static String DB_HOLDER = "dbName";
	private final static String FIX_PROD_SQL = "update $"
			+ DB_HOLDER
			+ "$.meta name, $"
			+ DB_HOLDER
			+ "$.meta ass set "
			+ "name.meta_value=concat(name.meta_value,'_',ass.meta_value) "
			+ "where name.species_id=ass.species_id and ass.meta_key='assembly.name' "
			+ "and name.meta_value='$" + NAME_HOLDER + "$'";

	private final static String FIX_DISPLAY_SQL = "update $"
			+ DB_HOLDER
			+ "$.meta name, $"
			+ DB_HOLDER
			+ "$.meta ass set "
			+ "name.meta_value=concat(name.meta_value,' (',ass.meta_value,')') "
			+ "where name.species_id=ass.species_id and ass.meta_key='assembly.name' "
			+ "and name.meta_value='$" + NAME_HOLDER + "$'";

	private final static String DELETE_SQL = "delete name.* from meta name where name.meta_value='$"
			+ NAME_HOLDER + "$' and name.meta_key='$" + KEY_HOLDER + "$'";

	@Override
	public boolean run(DatabaseRegistry dbr) {
		boolean result = true;
		for (String metaKey : META_KEYS) {
			Map<String, Collection<String>> names = CollectionUtils
					.createHashMap();
			for (DatabaseRegistryEntry coreDb : dbr.getAll(DatabaseType.CORE)) {
				if (coreDb.getName().contains("_core_")) {
					ReportManager.info(
							this,
							coreDb.getConnection(),
							"Checking " + metaKey + " stable ID for "
									+ coreDb.getName());
					ConnectionBasedSqlTemplateImpl template = new ConnectionBasedSqlTemplateImpl(
							coreDb.getConnection());
					int checked = 0;
					int dups = 0;
					for (String name : template.queryForDefaultObjectList(
							STABLE_ID, String.class, metaKey)) {
						checked++;
						Collection<String> dbs = names.get(name);
						if (dbs == null) {
							dbs = CollectionUtils.createArrayList(1);
							names.put(name, dbs);
						} else {
							dups++;
							ReportManager
									.problem(
											this,
											coreDb.getConnection(),
											"The name "
													+ name
													+ " (meta key "
													+ metaKey
													+ ") has been found in the following core databases :"
													+ StringUtils.join(dbs,
															", "));
							if (metaKey.equals("species.alias")) {
								ReportManager.problem(
										this,
										coreDb.getConnection(),
										"SQL to remove: "
												+ TemplateBuilder.template(
														DELETE_SQL,
														NAME_HOLDER, name,
														KEY_HOLDER, metaKey,
														DB_HOLDER,
														coreDb.getName()));
							} else if (metaKey.equals("species.display_name")) {
								ReportManager.problem(
										this,
										coreDb.getConnection(),
										"SQL to fix: "
												+ TemplateBuilder.template(
														FIX_DISPLAY_SQL,
														NAME_HOLDER, name,
														DB_HOLDER,
														coreDb.getName()));
							} else {
								ReportManager.problem(
										this,
										coreDb.getConnection(),
										"SQL to fix: "
												+ TemplateBuilder.template(
														FIX_PROD_SQL,
														NAME_HOLDER, name,
														DB_HOLDER,
														coreDb.getName()));
							}
							result = false;
						}
						dbs.add(coreDb.getName());
					}
					ReportManager.info(this, coreDb.getConnection(),
							"Checked " + checked + " " + metaKey
									+ " names for " + coreDb.getName()
									+ ": found " + dups + " duplicates");
				}
			}
		}
		return result;
	}

}
