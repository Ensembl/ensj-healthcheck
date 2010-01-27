/**
 * File: EgMetaTest.java
 * Created by: dstaines
 * Created on: May 26, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import java.util.regex.Pattern;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * Check to see if sql_names are valid
 * @author dstaines
 * 
 */
public class SpeciesSqlName extends AbstractEgCoreTestCase {

	private final static String META_QUERY = "select meta_value from meta where meta_key=?";

	private final static String[] META_KEYS = { "species.sql_name" };

	private static final Pattern VALID_SQL_NAME = Pattern
			.compile("^[A-z0-9_]+$");

	private static final Pattern INVALID_SQL_NAME2 = Pattern
	.compile("__+");

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;
		SqlTemplate template = getTemplate(dbre);
		for (String key : META_KEYS) {
			for (String speciesName : template.queryForDefaultObjectList(
					META_QUERY, String.class, key)) {
				if (!VALID_SQL_NAME.matcher(speciesName).matches()) {
					passes = false;
					ReportManager.problem(this, dbre.getConnection(),
							"Meta value " + speciesName + " for key  " + key
									+ " does not match the required value");
				} else if (INVALID_SQL_NAME2.matcher(speciesName).matches()) {
					passes = false;
					ReportManager.problem(this, dbre.getConnection(),
							"Meta value " + speciesName + " for key  " + key
									+ " does not match the required value");					
				}
			}
		}
		return passes;
	}

}
