/**
 * File: EgMetaTest.java
 * Created by: dstaines
 * Created on: May 26, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.SqlTemplate;
import org.ensembl.healthcheck.util.TestCaseUtils;

/**
 * Test for whether EnsemblGenomes species are correctly named
 * 
 * @author dstaines
 * 
 */
public class AliasAndNaming extends AbstractEgCoreTestCase {

	private final static String META_QUERY = "select meta_value from meta where meta_key=? and species_id=?";
	private static final Pattern VALID_SQL_NAME = Pattern
			.compile("^[0-9A-Za-z_ ]+$");

	private static final Pattern INVALID_SQL_NAME2 = Pattern.compile("__+");

	private static final String COMPARA_NAME = "species.compara_name";
	private static final String DB_NAME = "species.db_name";
	private static final String ALIAS = "species.alias";

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		SqlTemplate template = getTemplate(dbre);
		boolean passes = true;
		for (int speciesId : dbre.getSpeciesIds()) {
			passes &= checkNames(dbre, template, speciesId);
		}
		return passes;
	}

	private boolean checkNames(DatabaseRegistryEntry dbre,
			SqlTemplate template, int speciesId) {
		boolean passes = true;

		List<String> aliases = template.queryForDefaultObjectList(META_QUERY,
				String.class, ALIAS, speciesId);
		String binomialName = null;
		if (dbre.isMultiSpecies()) {
			binomialName = TestCaseUtils.getBinomialNameMulti(template, speciesId);
			List<String> dbNames = template.queryForDefaultObjectList(
					META_QUERY, String.class, DB_NAME, speciesId);
			if (dbNames.size() != 1) {
				passes = false;
				ReportManager.problem(this, dbre.getConnection(),
						"There should be exactly one " + ALIAS
								+ " meta value for species " + speciesId);
			}
			if (!dbNames.contains(binomialName) && !aliases.contains(binomialName)) {
				passes = false;
				ReportManager.problem(this, dbre.getConnection(),
						"There should be one " + ALIAS
								+ " or "+DB_NAME+" meta value that matches name "
								+ binomialName + " for species " + speciesId);
			}
		} else {
			binomialName = TestCaseUtils.getBinomialName(template, speciesId);
			if (!aliases.contains(binomialName)) {
				passes = false;
				ReportManager.problem(this, dbre.getConnection(), "No " + ALIAS
						+ " meta value found that matches name " + binomialName
						+ " for species " + speciesId);
				ReportManager
						.problem(
								this,
								dbre.getConnection(),
								"INSERT INTO "
										+ dbre.getName()
										+ ".meta(species_id,meta_key,meta_value) VALUES("
										+ speciesId + ",'species.alias','"
										+ binomialName + "');");
			}
		}

		if (dbre.isMultiSpecies()
				|| !TestCaseUtils.isValidBinomial(binomialName)) {

			String comparaName = template.queryForDefaultObject(META_QUERY,
					String.class, COMPARA_NAME, speciesId);
			if (StringUtils.isEmpty(comparaName)) {
				passes = false;
				ReportManager.problem(this, dbre.getConnection(),
						"Meta value for " + COMPARA_NAME
								+ " is not set for species " + speciesId);
			} else if (!VALID_SQL_NAME.matcher(comparaName).matches()) {
				passes = false;
				ReportManager.problem(this, dbre.getConnection(), "Meta value "
						+ comparaName + " for key  " + COMPARA_NAME
						+ " does not match the required value for species "
						+ speciesId);
			} else if (INVALID_SQL_NAME2.matcher(comparaName).matches()) {
				passes = false;
				ReportManager.problem(this, dbre.getConnection(), "Meta value "
						+ comparaName + " for key  " + COMPARA_NAME
						+ " does not match the required value for species "
						+ speciesId);
			}
			if (passes) {
				// 2. is there an alias set to this value
				if (!aliases.contains(comparaName)) {
					passes = false;
					ReportManager.problem(this, dbre.getConnection(), "No "
							+ ALIAS
							+ " meta value found that matches compara name "
							+ comparaName + " for species " + speciesId);
					ReportManager
							.problem(
									this,
									dbre.getConnection(),
									"INSERT INTO "
											+ dbre.getName()
											+ ".meta(species_id,meta_key,meta_value) VALUES("
											+ speciesId + ",'species.alias','"
											+ comparaName + "');");
				}
			}
		}
		return passes;
	}

}
