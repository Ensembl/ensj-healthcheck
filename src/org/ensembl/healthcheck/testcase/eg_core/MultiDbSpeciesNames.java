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
public class MultiDbSpeciesNames extends MultiDatabaseTestCase {
	
	public MultiDbSpeciesNames() {
		super();
		setTeamResponsible(Team.ENSEMBL_GENOMES);
		addAppliesToType(DatabaseType.CORE);
	}

	private final static String STABLE_ID = "select meta_value from meta where meta_key=?";
	private final static String[] META_KEYS = { "species.production_name", "species.alias", "species.db_name" };

	@Override
	public boolean run(DatabaseRegistry dbr) {
		boolean result = true;
		for (String metaKey : META_KEYS) {
			Map<String, Collection<String>> names  = CollectionUtils
			.createHashMap();
			for (DatabaseRegistryEntry coreDb : dbr.getAll(DatabaseType.CORE)) {
				ReportManager.info(this, coreDb.getConnection(), "Checking "+ metaKey
						+ " stable ID for "+ coreDb.getName());
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
												+ metaKey
												+ " has been found in the following core databases :"
												+ StringUtils.join(dbs, ", "));
						result = false;
					}
					dbs.add(coreDb.getName());
				}
				ReportManager.info(this, coreDb.getConnection(), "Checked "+checked+" "+ metaKey
						+ " names for "+ coreDb.getName()+": found "+dups+" duplicates");
			}
		}
		return result;
	}

}
