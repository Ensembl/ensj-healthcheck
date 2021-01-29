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

import java.sql.SQLException;
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
public class MultiDbStableId extends MultiDatabaseTestCase {

	public MultiDbStableId() {
		super();
		addAppliesToType(DatabaseType.CORE);
		setTeamResponsible(Team.ENSEMBL_GENOMES);
	}

	private final static String STABLE_ID = "select o.stable_id from %obj% o";
	private final static String[] OBJ_TYPES = { "gene", "transcript",
			"translation", "exon" };
	private final int MAX_REPORTS = 10;

	@Override
	public boolean run(DatabaseRegistry dbr) throws SQLException {
		boolean result = true;
		for (String objectType : OBJ_TYPES) {
			String query = STABLE_ID.replaceAll("%obj%", objectType);
			Map<String, Collection<String>> stableIds = CollectionUtils
					.createHashMap();
			for (DatabaseRegistryEntry coreDb : dbr.getAll(DatabaseType.CORE)) {
				System.out.println("TYPE:"+coreDb.getType()+" from "+coreDb.getName());
				if (!coreDb.getName().contains(
						DatabaseType.OTHERFEATURES.getName())) {
					ReportManager.info(this, coreDb.getConnection(),
							"Checking " + objectType + " stable ID for "
									+ coreDb.getName());
					ConnectionBasedSqlTemplateImpl template = new ConnectionBasedSqlTemplateImpl(
							coreDb.getConnection());
					int checked = 0;
					int dups = 0;
					for (String stableId : template.queryForDefaultObjectList(
							query, String.class)) {
						checked++;
						Collection<String> dbs = stableIds.get(stableId);
						if (dbs == null) {
							dbs = CollectionUtils.createArrayList(1);
							stableIds.put(stableId, dbs);
						} else {
							dups++;
							if (dups <= MAX_REPORTS) {
								ReportManager
										.problem(
												this,
												coreDb.getConnection(),
												"The "
														+ objectType
														+ " stable ID "
														+ stableId
														+ " has been found in the following core databases :"
														+ StringUtils.join(dbs,
																", "));
								if (dups == MAX_REPORTS) {
									ReportManager
											.problem(
													this,
													coreDb.getConnection(),
													MAX_REPORTS
															+ " duplications have been found for  "
															+ objectType
															+ " stable ID in this database - no more will be reported");
								}
							}
							result = false;
						}
						dbs.add(coreDb.getName());
					}
					ReportManager.info(this, coreDb.getConnection(), "Checked "
							+ checked + " " + objectType + " stable IDs for "
							+ coreDb.getName() + ": found " + dups
							+ " duplicates");
                                        coreDb.getConnection().close();
				}
			}
		}
		return result;
	}

}
