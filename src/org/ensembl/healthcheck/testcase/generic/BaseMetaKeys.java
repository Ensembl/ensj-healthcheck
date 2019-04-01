package org.ensembl.healthcheck.testcase.generic;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.ensembl.healthcheck.util.Pair;


import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.MapRowMapper;
import org.ensembl.healthcheck.util.SqlTemplate;
import org.ensembl.healthcheck.util.TestCaseUtils;


/**
 * Base class for checking on the contents of the meta tables to see for each
 * species ID if:
 * <ul>
 * <li>all mandatory keys are present</li>
 * <li>if keys have the correct cardinality</li>
 * <li>if any keys are present that are not in the list</li>
 * </ul>
 * In addition, keys which are applicable to the whole schema are also checked
 * if they are present and correct.
 * 
 * @author dstaines
 *
 */
public abstract class BaseMetaKeys extends SingleDatabaseTestCase {

	/**
	 * Class encapsulating which keys are valid, mandatory and single value
	 * 
	 * @author dstaines
	 *
	 */
	protected static class KeyDefinition {
		public final Set<String> singleCardinality = new HashSet<>();
		public final Set<String> validKeys = new HashSet<>();
		public final Set<String> mandatoryKeys = new HashSet<>();

		public void add(String[] cols) {
			this.validKeys.add(cols[0]);
			if (cols[2].equals(TRUE)) {
				singleCardinality.add(cols[0]);
			}
			if (cols[3].equals(TRUE)) {
				mandatoryKeys.add(cols[0]);
			}
		}
	}

	/**
	 * TSV boolean value
	 */
	private static final String TRUE = "1";

	/**
	 * mapper class to populate a list of counts per key
	 */
	protected static final MapRowMapper<String, Integer> keyMapper = new MapRowMapper<String, Integer>() {

		@Override
		public void existingObject(Integer currentValue, ResultSet resultSet, int position) throws SQLException {
		}

		@Override
		public String getKey(ResultSet resultSet) throws SQLException {
			return resultSet.getString(1);
		}

		@Override
		public Map<String, Integer> getMap() {
			return new HashMap<>();
		}

		@Override
		public Integer mapRow(ResultSet resultSet, int position) throws SQLException {
			return resultSet.getInt(2);
		}
	};

	public BaseMetaKeys() {
		super();
	}

	/**
	 * Utility to compare a hash of keys vs a
	 * 
	 * @param dbre
	 *            since reporting requires it
	 * @param speciesString
	 *            species-specific message
	 * @param keys
	 *            keys with counts
	 * @param keyDefinition
	 *            definition to test against
	 * @return true if all is well
	 */
	protected boolean checkKeys(DatabaseRegistryEntry dbre, String speciesString, Map<String, Integer> keys,
			KeyDefinition keyDefinition) {
		boolean ok = true;
		// check mandatory are all present
		for (String key : keyDefinition.mandatoryKeys) {
			if (!keys.containsKey(key)) {
				ok = false;
				ReportManager.problem(this, dbre.getConnection(),
						"Mandatory meta key " + key + " not found" + speciesString);
			}
		}

		// check each key to see cardinality and if the keys are valid
		for (Entry<String, Integer> e : keys.entrySet()) {
			if (!keyDefinition.validKeys.contains(e.getKey())) {
				ok = false;
				ReportManager.problem(this, dbre.getConnection(),
						"Meta key " + e.getKey() + " not valid" + speciesString);

			}
			if (e.getValue() > 1 && keyDefinition.singleCardinality.contains(e.getKey())) {
				ok = false;
				ReportManager.problem(this, dbre.getConnection(),
						"Meta key " + e.getKey() + " should only have a single value" + speciesString);

			}
		}
		return ok;
	}

	/**
	 * @return resource path for TSV file containing key definitions
	 */
	protected abstract String getMetaFile();

	/**
	 * Parse a simple TSV file containing a list of meta keys and create sets
	 * containing different criteria. Could be replaced by call to production db?
	 * 
	 * @return pair of meta key usage definitions (schema-wide, per-species)
	 */
	protected Pair<KeyDefinition, KeyDefinition> readDefs() {
		KeyDefinition schemaKeys = new KeyDefinition();
		KeyDefinition speciesKeys = new KeyDefinition();
		for (String line : TestCaseUtils.resourceToStringList(getMetaFile())) {
			// format is key per_species(1/0) single_value(1/0) required(1/0)
			if (!line.startsWith("#")) {
				String[] cols = line.split("\t");
				if (cols.length != 4) {
					throw new IllegalArgumentException(
							"Cannot parse line " + line + " - should contain 4 columns but has " + cols.length + " ");
				}
				if (cols[1].equals(TRUE)) {
					speciesKeys.add(cols);
				} else {
					schemaKeys.add(cols);
				}
			}
		}
		return Pair.of(schemaKeys, speciesKeys);
	}

	@Override
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean passes = true;

		Pair<KeyDefinition, KeyDefinition> defs = readDefs();
		SqlTemplate template = DBUtils.getSqlTemplate(dbre);

		// check schema-wide keys
		passes &= checkKeys(dbre, StringUtils.EMPTY,
				template.queryForMap("select meta_key,count(*) from meta where species_id is null group by meta_key",
						keyMapper),
				defs.first());

		// check per species keys
		for (int speciesId : dbre.getSpeciesIds()) {
			passes &= checkKeys(dbre, " for species " + speciesId,
					template.queryForMap("select meta_key,count(*) from meta where species_id=? group by meta_key",
							keyMapper, speciesId),
					defs.second());
		}
		return passes;

	}

}
