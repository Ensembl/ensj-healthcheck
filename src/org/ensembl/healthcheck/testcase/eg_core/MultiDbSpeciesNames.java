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
			"species.alias", "species.db_name" };

	private final static String NAME_HOLDER = "name";
	private final static String KEY_HOLDER = "metaKey";
	private final static String FIX_SQL = "update meta name, meta ass set "
			+ "name.meta_value=concat(name.meta_value,'_',ass.meta_value) "
			+ "where name.species_id=ass.species_id and ass.meta_key='assembly.name' "
			+ "and name.meta_value='$" + NAME_HOLDER
			+ "$' and name.meta_key='$" + KEY_HOLDER + "$'";
	private final static String DELETE_SQL = "delete from meta name where name.meta_value='$"
			+ NAME_HOLDER + "$' and name.meta_key='$" + KEY_HOLDER + "$'";

	@Override
	public boolean run(DatabaseRegistry dbr) {
		boolean result = true;
		for (String metaKey : META_KEYS) {
			Map<String, Collection<String>> names = CollectionUtils
					.createHashMap();
			for (DatabaseRegistryEntry coreDb : dbr.getAll(DatabaseType.CORE)) {
				ReportManager.info(this, coreDb.getConnection(), "Checking "
						+ metaKey + " stable ID for " + coreDb.getName());
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
												+ StringUtils.join(dbs, ", "));
						ReportManager.info(this,
										coreDb.getConnection(), "SQL to fix: "+TemplateBuilder.template(FIX_SQL, NAME_HOLDER,name, KEY_HOLDER,metaKey));
						ReportManager.info(this,
								coreDb.getConnection(), "SQL to remove: "+TemplateBuilder.template(DELETE_SQL, NAME_HOLDER,name, KEY_HOLDER,metaKey));
						result = false;
					}
					dbs.add(coreDb.getName());
				}
				ReportManager.info(this, coreDb.getConnection(),
						"Checked " + checked + " " + metaKey + " names for "
								+ coreDb.getName() + ": found " + dups
								+ " duplicates");
			}
		}
		return result;
	}

}
