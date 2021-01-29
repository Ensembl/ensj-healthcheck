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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;
import org.ensembl.healthcheck.util.CollectionUtils;
import org.ensembl.healthcheck.util.ConnectionBasedSqlTemplateImpl;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.MapRowMapper;

/**
 * EG specific test that tries to find species that have disappeared since last
 * time
 * 
 * @author dstaines
 * 
 */
public class MultiDbCompareNames extends MultiDatabaseTestCase {

	public MultiDbCompareNames() {
		super();
		setTeamResponsible(Team.ENSEMBL_GENOMES);
		addAppliesToType(DatabaseType.CORE);
	}

	private final static String NAMES_ACCESSIONS = "select m1.meta_value,m2.meta_value "
			+ "from meta m1 join meta m2 using (species_id) where "
			+ "m1.meta_key='species.production_name' AND m2.meta_key='assembly.name'";

	@Override
	public boolean run(DatabaseRegistry dbr) {
		boolean result = true;

		DatabaseRegistry secondaryDBR = DBUtils.getSecondaryDatabaseRegistry();

		Map<String, String> newNames = getNames(dbr.getAll(DatabaseType.CORE));
		Map<String, String> oldNames = getNames(secondaryDBR
				.getAll(DatabaseType.CORE));

		for (Entry<String, String> e : oldNames.entrySet()) {
			String name = e.getKey();
			String ass = e.getValue();
			if (!newNames.containsKey(name)) {
				// direct name match not found
				// try some different ones instead...
				result = false;
				ReportManager.problem(this, "unknown", "Existing species "
						+ name + " not found");
				// try some other variations
				String assName = name + "_" + ass.toLowerCase();
				if (newNames.containsKey(assName)) {
					ReportManager.info(this, "", "Name may have changed from "
							+ name + " to " + assName);
				}
				if (name.contains("_" + ass.toLowerCase())) {
					String asslessName = name.replaceAll(
							"_" + ass.toLowerCase(), "");
					if (newNames.containsKey(asslessName)) {
						ReportManager.info(this, "",
								"Name may have changed from " + name + " to "
										+ asslessName);
					}
				}
			}
		}

		return result;
	}

	private Map<String, String> getNames(DatabaseRegistryEntry[] newDbs) {
		Map<String, String> names = CollectionUtils.createHashMap();
		for (DatabaseRegistryEntry coreDb : newDbs) {
			if (coreDb.getName().contains("_core_")) {
				ReportManager.info(this, coreDb.getConnection(), "Checking "
						+ coreDb.getName());
				ConnectionBasedSqlTemplateImpl template = new ConnectionBasedSqlTemplateImpl(
						coreDb.getConnection());
				names.putAll(template.queryForMap(NAMES_ACCESSIONS,
						new MapRowMapper<String, String>() {

							@Override
							public String mapRow(ResultSet resultSet,
									int position) throws SQLException {
								return resultSet.getString(2);
							}

							@Override
							public Map<String, String> getMap() {
								return CollectionUtils.createHashMap();
							}

							@Override
							public String getKey(ResultSet resultSet)
									throws SQLException {
								return resultSet.getString(1);
							}

							@Override
							public void existingObject(String currentValue,
									ResultSet resultSet, int position)
									throws SQLException {
								throw new SQLException("Duplicate name "
										+ currentValue + " found");
							}
						}));
			}
		}
		return names;
	}

}
