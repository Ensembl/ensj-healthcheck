/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2018] EMBL-European Bioinformatics Institute
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

/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ensembl.healthcheck.AssemblyNameInfo;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.RowMapper;
import org.ensembl.healthcheck.util.SqlTemplate;
import org.ensembl.healthcheck.util.Utils;

/**
 * Checks that meta_value contents in the meta table are OK. Only one meta table
 * at a time is done here; checks for the consistency of the meta table across
 * species are done in MetaCrossSpecies.
 */
public class MetaValues extends SingleDatabaseTestCase {

	public MetaValues() {

		setTeamResponsible(Team.GENEBUILD);
		setSecondTeamResponsible(Team.RELEASE_COORDINATOR);
		setDescription("Check that meta_value contents in the meta table are OK");
	}

	/**
	 * Checks that meta_value contents in the meta table are OK.
	 * 
	 * @param dbre
	 *            The database to check.
	 * @return True if the test passed.
	 */
	public boolean run(final DatabaseRegistryEntry dbre) {
		boolean result = true;

		Connection con = dbre.getConnection();

		DatabaseRegistryEntry sec = getEquivalentFromSecondaryServer(dbre);

		String species = dbre.getSpecies();

		if (species.equals(DatabaseRegistryEntry.ANCESTRAL_SEQUENCES)) {
			// The rest of the tests are not relevant for the ancestral sequences DB
			return result;

		}

		result &= checkAssemblyMapping(con);

		// we can no longer check taxon ID vs species
		// result &= checkTaxonomyID(dbre);

		result &= checkAssemblyWeb(dbre);

		if (dbre.getType() == DatabaseType.CORE) {
			result &= checkDates(dbre);
			result &= checkGenebuildID(con);
			result &= checkGenebuildMethod(dbre);
			result &= checkAssemblyAccessionUpdate(dbre);
			result &= checkGenes(dbre, sec);
		}

		result &= checkCoordSystemTableCases(con);

		result &= checkBuildLevel(dbre);

		result &= checkSample(dbre);

		// ----------------------------------------
		// Use an AssemblyNameInfo object to get the assembly information

		AssemblyNameInfo assembly = new AssemblyNameInfo(con);

		String metaTableAssemblyDefault = assembly.getMetaTableAssemblyDefault();
		logger.finest("assembly.default from meta table: " + metaTableAssemblyDefault);
		String dbNameAssemblyVersion = assembly.getDBNameAssemblyVersion();
		logger.finest("Assembly version from DB name: " + dbNameAssemblyVersion);
		String metaTableAssemblyVersion = assembly.getMetaTableAssemblyVersion();
		logger.finest("meta table assembly version: " + metaTableAssemblyVersion);
		String metaTableAssemblyPrefix = assembly.getMetaTableAssemblyPrefix();
		logger.finest("meta table assembly prefix: " + metaTableAssemblyPrefix);

		if (metaTableAssemblyVersion == null || metaTableAssemblyDefault == null || metaTableAssemblyPrefix == null
				|| dbNameAssemblyVersion == null) {

			ReportManager.problem(this, con, "Cannot get all information from meta table - check for null values");
			result = false;

		}

		// -------------------------------------------

		result &= checkRepeatAnalysis(dbre);

		// -------------------------------------------

		result &= checkForSchemaPatchLineBreaks(dbre);

		return result;
	} // run

	// ---------------------------------------------------------------------

	private boolean checkAssemblyMapping(Connection con) {

		boolean result = true;

		// Check formatting of assembly.mapping entries; should be of format
		// coord_system1{:default}|coord_system2{:default} with optional third
		// coordinate system
		// and all coord systems should be valid from coord_system
		// can also have # instead of | as used in unfinished contigs etc

		Pattern assemblyMappingPattern = Pattern.compile(
				"^([a-zA-Z0-9.]+):?([a-zA-Z0-9._-]+)?[\\|#]([a-zA-Z0-9._-]+):?([a-zA-Z0-9._-]+)?([\\|#]([a-zA-Z0-9.]+):?([a-zA-Z0-9._-]+)?)?$");
		String[] validCoordSystems = DBUtils.getColumnValues(con, "SELECT name FROM coord_system");

		String[] mappings = DBUtils.getColumnValues(con,
				"SELECT meta_value FROM meta WHERE meta_key='assembly.mapping'");
		for (int i = 0; i < mappings.length; i++) {
			Matcher matcher = assemblyMappingPattern.matcher(mappings[i]);
			if (!matcher.matches()) {
				result = false;
				ReportManager.problem(this, con,
						"Coordinate system mapping " + mappings[i] + " is not in the correct format");
			} else {
				// if format is OK, check coord systems are valid
				boolean valid = true;
				String cs1 = matcher.group(1);
				String assembly1 = matcher.group(2);
				String cs2 = matcher.group(3);
				String assembly2 = matcher.group(4);
				String cs3 = matcher.group(6);
				String assembly3 = matcher.group(7);

				if (!Utils.stringInArray(cs1, validCoordSystems, false)) {
					valid = false;
					ReportManager.problem(this, con,
							"Source co-ordinate system " + cs1 + " is not in the coord_system table");
				}
				if (!Utils.stringInArray(cs2, validCoordSystems, false)) {
					valid = false;
					ReportManager.problem(this, con,
							"Target co-ordinate system " + cs2 + " is not in the coord_system table");
				}
				// third coordinate system is optional
				if (cs3 != null && !Utils.stringInArray(cs3, validCoordSystems, false)) {
					valid = false;
					ReportManager.problem(this, con,
							"Third co-ordinate system in mapping (" + cs3 + ") is not in the coord_system table");
				}

				result &= valid;

				// check that coord_system:version pairs listed here exist in the coord_system
				// table
				result &= checkCoordSystemVersionPairs(con, cs1, assembly1, cs2, assembly2, cs3, assembly3);

				// check that coord systems are specified in lower-case
				result &= checkCoordSystemCase(con, cs1, "meta assembly.mapping");
				result &= checkCoordSystemCase(con, cs2, "meta assembly.mapping");
				result &= checkCoordSystemCase(con, cs3, "meta assembly.mapping");

			}
		}

		return result;
	}

	// ---------------------------------------------------------------------
	/**
	 * Check that coordinate system:assembly pairs in assembly.mappings match what's
	 * in the coord system table
	 */
	private boolean checkCoordSystemVersionPairs(Connection con, String cs1, String assembly1, String cs2,
			String assembly2, String cs3, String assembly3) {

		boolean result = true;

		List<String> coordSystemsAndVersions = DBUtils.getColumnValuesList(con,
				"SELECT CONCAT_WS(':',name,version) FROM coord_system");

		result &= checkCoordSystemPairInList(con, cs1, assembly1, coordSystemsAndVersions);

		result &= checkCoordSystemPairInList(con, cs2, assembly2, coordSystemsAndVersions);

		if (cs3 != null) {

			result &= checkCoordSystemPairInList(con, cs3, assembly3, coordSystemsAndVersions);

		}

		return result;

	}

	// ---------------------------------------------------------------------
	/**
	 * Check if a particular coordinate system:version pair is in a list. Deal with
	 * nulls appropriately.
	 */
	private boolean checkCoordSystemPairInList(Connection con, String cs, String assembly, List<String> coordSystems) {

		boolean result = true;

		String toCompare = (assembly != null) ? cs + ":" + assembly : cs;

		if (!coordSystems.contains(toCompare)) {

			ReportManager.problem(this, con, "Coordinate system name/version " + toCompare
					+ " in assembly.mapping does not appear in coord_system table.");
			result = false;

		}

		return result;

	}

	// --------------------------------------------------------------------
	/**
	 * @return true if cs is all lower case (or null), false otherwise.
	 */
	private boolean checkCoordSystemCase(Connection con, String cs, String desc) {

		if (cs == null) {

			return true;

		}

		boolean result = true;

		if (!cs.equals(cs.toLowerCase())) {
			ReportManager.problem(this, con, "Co-ordinate system name " + cs + " is not all lower case in " + desc);
			result = false;

		}

		return result;

	}

	// --------------------------------------------------------------------
	/**
	 * Check that all coord systems in the coord_system table are lower case.
	 */
	private boolean checkCoordSystemTableCases(Connection con) {

		// TODO - table name in report
		boolean result = true;

		String[] coordSystems = DBUtils.getColumnValues(con, "SELECT name FROM coord_system");

		for (int i = 0; i < coordSystems.length; i++) {

			result &= checkCoordSystemCase(con, coordSystems[i], "coord_system");

		}

		return result;

	}

	// ---------------------------------------------------------------------

	private boolean checkAssemblyWeb(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		// Check that the taxonomy ID matches a known one.
		// The taxonomy ID-species mapping is held in the Species class.

		String[] allowedTypes = { "GenBank Assembly ID", "EMBL-Bank WGS Master" };
		String[] allowedSources = { "NCBI", "ENA", "DDBJ" };
		String WebType = DBUtils.getRowColumnValue(con,
				"SELECT meta_value FROM meta WHERE meta_key='assembly.web_accession_type'");
		String WebSource = DBUtils.getRowColumnValue(con,
				"SELECT meta_value FROM meta WHERE meta_key='assembly.web_accession_source'");

		if (WebType.length() > 0) {
			if (!Utils.stringInArray(WebType, allowedTypes, true)) {
				result = false;
				ReportManager.problem(this, con, "Web accession type " + WebType + " is not allowed");
			}
		}

		if (WebSource.length() > 0) {
			if (!Utils.stringInArray(WebSource, allowedSources, true)) {
				result = false;
				ReportManager.problem(this, con, "Web accession source " + WebSource + " is not allowed");
			}
		}
		return result;

	}

	// ---------------------------------------------------------------------

	private boolean checkDates(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		String[] keys = { "genebuild.start_date", "assembly.date", "genebuild.initial_release_date",
				"genebuild.last_geneset_update" };

		String date = "[0-9]{4}-[0-9]{2}";
		String[] regexps = { date + "-[a-zA-Z]*", date, date, date };

		for (int i = 0; i < keys.length; i++) {

			String key = keys[i];
			String regexp = regexps[i];

			String value = DBUtils.getRowColumnValue(con, "SELECT meta_value FROM meta WHERE meta_key='" + key + "'");
			if (value == null || value.length() == 0) {

				ReportManager.problem(this, con, "No " + key + " entry in meta table");
				result = false;

			}

			if (result) {
				result &= checkMetaKey(con, key, value, regexp);
			}

			if (result) {
				result &= checkDateFormat(con, key, value);
			}

		}

		if (!result) {
			return result;
		}

		// some more checks for sanity of dates
		int startDate = Integer.valueOf(
				DBUtils.getRowColumnValue(con, "SELECT meta_value FROM meta WHERE meta_key='genebuild.start_date'")
						.replaceAll("[^0-9]", ""))
				.intValue();
		int initialReleaseDate = Integer.valueOf(DBUtils
				.getRowColumnValue(con, "SELECT meta_value FROM meta WHERE meta_key='genebuild.initial_release_date'")
				.replaceAll("[^0-9]", "")).intValue();
		int lastGenesetUpdate = Integer.valueOf(DBUtils
				.getRowColumnValue(con, "SELECT meta_value FROM meta WHERE meta_key='genebuild.last_geneset_update'")
				.replaceAll("[^0-9]", "")).intValue();

		// check for genebuild.start_date >= genebuild.initial_release_date (not allowed
		// as we cannot release a gene set before
		// downloaded the evidence)
		if (startDate >= initialReleaseDate) {
			result = false;
			ReportManager.problem(this, con,
					"genebuild.start_date is greater than or equal to genebuild.initial_release_date");
		}

		// check for genebuild.initial_release_date > genebuild.last_geneset_update (not
		// allowed as we cannot update a gene set before
		// its initial public release)
		if (initialReleaseDate > lastGenesetUpdate) {
			result = false;
			ReportManager.problem(this, con,
					"genebuild.initial_release_date is greater than or equal to genebuild.last_geneset_update");
		}

		return result;

	}

	// ---------------------------------------------------------------------

	private boolean checkMetaKey(Connection con, String key, String s, String regexp) {

		if (regexp != null) {

			if (!s.matches(regexp)) {

				ReportManager.problem(this, con, key + " " + s + " is not in correct format - should match " + regexp);
				return false;
			}

		}

		return true;

	}

	// ---------------------------------------------------------------------

	private boolean checkDateFormat(Connection con, String key, String s) {

		int year = Integer.parseInt(s.substring(0, 4));
		if (year < 2003 || year > 2050) {
			ReportManager.problem(this, con, "Year part of " + key + " (" + year + ") is incorrect");
			return false;
		}
		int month = Integer.parseInt(s.substring(5, 7));
		if (month < 1 || month > 12) {
			ReportManager.problem(this, con, "Month part of " + key + " (" + month + ") is incorrect");
			return false;
		}

		return true;

	}

	// ---------------------------------------------------------------------

	private boolean checkGenebuildID(Connection con) {

		String gbid = DBUtils.getRowColumnValue(con, "SELECT meta_value FROM meta WHERE meta_key='genebuild.id'");
		logger.finest("genebuild.id from database: " + gbid);

		if (gbid == null || gbid.length() == 0) {

			ReportManager.problem(this, con, "No genebuild.id entry in meta table");
			return false;

		} else if (!gbid.matches("[0-9]+")) {

			ReportManager.problem(this, con, "genebuild.id " + gbid + " is not numeric");
			return false;

		}

		ReportManager.correct(this, con, "genebuild.id " + gbid + " is present and numeric");

		return true;

	}

	// ---------------------------------------------------------------------
	/**
	 * Check that at least some sort of genebuild.level-type key is present.
	 */
	private boolean checkBuildLevel(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();
		String[] Tables = { "gene", "transcript", "exon", "repeat_feature", "dna_align_feature",
				"protein_align_feature", "simple_feature", "prediction_transcript", "prediction_exon" };

		int exists = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM meta where meta_key like '%build.level'");
		if (exists == 0) {
			ReportManager.problem(this, con,
					"GB: No %build.level entries in the meta table - run ensembl/misc-scripts/meta_levels.pl");
			result = false;
		}
		int count = 0;
		for (int i = 0; i < Tables.length; i++) {
			String Table = Tables[i];
			int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM " + Table);
			int key = DBUtils.getRowCount(con,
					"SELECT COUNT(*) FROM meta WHERE meta_key = '" + Table + "build.level' ");
			int toplevel = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM " + Table
					+ " t, seq_region_attrib sra, attrib_type at WHERE t.seq_region_id = sra.seq_region_id AND sra.attrib_type_id = at.attrib_type_id AND at.code = 'toplevel' ");
			if (rows != 0) {
				if (key == 0) {
					if (rows == toplevel) {
						ReportManager.problem(this, con, "Table " + Table
								+ " should have a toplevel flag - run ensembl/misc-scripts/meta_levels.pl");
						result = false;
					} else {
						count++;
					}
				} else {
					if (rows != toplevel) {
						ReportManager.problem(this, con, "Table " + Table
								+ " has some non toplevel regions, should not have a toplevel flag - run ensembl/misc-scripts/meta_levels.pl");
						result = false;
					} else {
						count++;
					}
				}
			} else {
				if (key != 0) {
					ReportManager.problem(this, con, "Empty table " + Table
							+ " should not have a toplevel flag - run ensembl/misc-scripts/meta_levels.pl");
					result = false;
				} else {
					count++;
				}
			}
		}
		if (count == Tables.length) {
			ReportManager.correct(this, con, "Toplevel flags correctly set");
			result = true;
		}
		return result;

	}

	// ---------------------------------------------------------------------

	/**
	 * Check that the genebuild.method entry exists and has one of the allowed
	 * values.
	 */
	private boolean checkGenebuildMethod(DatabaseRegistryEntry dbre) {

		boolean result = true;

		String[] allowedMethods = { "full_genebuild", "projection_build", "import", "mixed_strategy_build",
				"external_annotation_import" };

		Connection con = dbre.getConnection();
		String method = DBUtils.getRowColumnValue(con, "SELECT meta_value FROM meta WHERE meta_key='genebuild.method'");

		if (method.equals("")) {
			ReportManager.problem(this, con, "No genebuild.method entry present in Meta table");
			return false;
		}

		if (!Utils.stringInArray(method, allowedMethods, true)) {
			ReportManager.problem(this, con, "genebuild.method value " + method + " is not in list of allowed methods");
			result = false;
		}

		return result;

	}
	// ---------------------------------------------------------------------

	private boolean checkAssemblyAccessionUpdate(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();
		String currentAssemblyAccession = DBUtils.getMetaValue(con, "assembly.accession");
		String currentAssemblyName = DBUtils.getMetaValue(con, "assembly.name");

		if (currentAssemblyAccession.equals("")) {
			ReportManager.problem(this, con, "No assembly.accession entry present in Meta table");
			return false;
		}
		if (!currentAssemblyAccession.matches("^GC.*")) {
			ReportManager.problem(this, con, "Meta key assembly.accession does not start with GC");
			return false;
		}
		if (currentAssemblyName.equals("")) {
			ReportManager.problem(this, con, "No assembly.name entry present in Meta table");
			return false;
		}

		DatabaseRegistryEntry sec = getEquivalentFromSecondaryServer(dbre);

		if (sec == null) {

			logger.warning("Can't get equivalent database for " + dbre.getName());
			return true;
		}

		logger.finest("Equivalent database on secondary server is " + sec.getName());

		Connection previousCon = sec.getConnection();
		String previousAssemblyAccession = DBUtils.getMetaValue(previousCon, "assembly.accession");
		String previousAssemblyName = DBUtils.getMetaValue(previousCon, "assembly.name");

		long currentAssemblyChecksum = DBUtils.getChecksum(con, "assembly");
		long previousAssemblyChecksum = DBUtils.getChecksum(previousCon, "assembly");

		boolean assemblyChanged = false;
		boolean assemblyTableChanged = false;
		boolean assemblyExceptionTableChanged = false;

		if (currentAssemblyChecksum != previousAssemblyChecksum) {
			assemblyTableChanged = true;
		} else {
			if (!dbre.getSpecies().equals(DatabaseRegistryEntry.HOMO_SAPIENS)) {

				// compare assembly_exception tables (patches only) from each database
				try {

					Statement previousStmt = previousCon.createStatement();
					Statement currentStmt = con.createStatement();

					String sql = "SELECT * FROM assembly_exception WHERE exc_type LIKE ('PATCH_%') ORDER BY assembly_exception_id";
					ResultSet previousRS = previousStmt.executeQuery(sql);
					ResultSet currentRS = currentStmt.executeQuery(sql);

					boolean assExSame = DBUtils.compareResultSets(currentRS, previousRS, this, "", false, false,
							"assembly_exception", false);

					currentRS.close();
					previousRS.close();
					currentStmt.close();
					previousStmt.close();

					assemblyExceptionTableChanged = !assExSame;

				} catch (SQLException e) {
					e.printStackTrace();
				}

			}
		}

		assemblyChanged = assemblyTableChanged || assemblyExceptionTableChanged;

		if (assemblyChanged == previousAssemblyAccession.equals(currentAssemblyAccession)
				&& previousAssemblyName.equals(currentAssemblyName)) {
			result = false;
			String errorMessage = "assembly.accession and assembly.name values need to be updated when "
					+ "the assembly table changes or new patches are added to the assembly exception table\n"
					+ "previous assembly.accession: " + previousAssemblyAccession + " assembly.name: "
					+ previousAssemblyName + " current assembly.accession: " + currentAssemblyAccession
					+ " assembly.name: " + currentAssemblyName + "\n" + "assembly table changed:";
			if (assemblyTableChanged) {
				errorMessage += " yes;";
			} else {
				errorMessage += " no;";
			}
			errorMessage += " assembly exception patches changed:";
			if (assemblyExceptionTableChanged) {
				errorMessage += " yes";
			} else {
				errorMessage += " no";
			}
			ReportManager.problem(this, con, errorMessage);
		}

		if (result) {
			ReportManager.correct(this, con, "assembly.accession and assembly.name values are correct");
		}

		return result;
	}

	// ---------------------------------------------------------------------
	/**
	 * Check that all meta_values with meta_key 'repeat.analysis' reference
	 * analysis.logic_name Also check that repeatmask is one of them
	 */
	private boolean checkRepeatAnalysis(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();
		String[] repeatAnalyses = DBUtils.getColumnValues(con,
				"SELECT meta_value FROM meta LEFT JOIN analysis ON meta_value = logic_name WHERE meta_key = 'repeat.analysis' AND analysis_id IS NULL");
		if (repeatAnalyses.length > 0) {
			result = false;
			ReportManager.problem(this, con,
					"The following values for meta_key repeat.analysis don't have a corresponding logic_name entry in the analysis table: "
							+ Utils.arrayToString(repeatAnalyses, ","));
		} else {
			ReportManager.correct(this, con,
					"All values for meta_key repeat.analysis have a corresponding logic_name entry in the analysis table");
		}

		if (dbre.getType() == DatabaseType.CORE) {

			int repeatMask = DBUtils.getRowCount(con,
					"SELECT count(*) FROM meta WHERE meta_key = 'repeat.analysis' AND (meta_value like 'repeatmask_repbase%' or meta_value = 'repeatmask')");
			if (repeatMask == 0) {
				result = false;
				ReportManager.problem(this, con, "There is no entry in meta for repeatmask repeat.analysis");
			} else {
				ReportManager.correct(this, con, "Repeatmask is present in meta table for repeat.analysis");
			}
		}

		return result;

	}

	private boolean checkGenes(DatabaseRegistryEntry dbre, DatabaseRegistryEntry sec) {

		boolean result = true;

		Connection con = dbre.getConnection();
		if (sec == null) {
			logger.warning("Can't get equivalent database for " + dbre.getName());
			return true;
		}
		Connection previousCon = sec.getConnection();

		SqlTemplate t = getSqlTemplate(dbre);

		RowMapper<Set<Object>> rowMapper = new RowMapper<Set<Object>>() {
			public Set<Object> mapRow(ResultSet rs, int position) throws SQLException {
				Set<Object> set = new HashSet<Object>();
				for (int i = 1; i <= 10; i++) {
					set.add(rs.getObject(i));
				}
				return set;
			}
		};

		String sql = "SELECT biotype, analysis_id, seq_region_id, seq_region_start, seq_region_end, seq_region_end, seq_region_strand, stable_id, is_current, version FROM gene WHERE biotype NOT IN ('LRG_gene')";
		Set<Set<Object>> currentGenes = t.queryForSet(sql, rowMapper);
		Set<Set<Object>> previousGenes = getSqlTemplate(sec).queryForSet(sql, rowMapper);

		String genesetUpdate = DBUtils.getRowColumnValue(con,
				"SELECT meta_value FROM meta WHERE meta_key = 'genebuild.last_geneset_update'");
		String previousGenesetUpdate = DBUtils.getRowColumnValue(previousCon,
				"SELECT meta_value FROM meta WHERE meta_key = 'genebuild.last_geneset_update'");

		String[] gencodeWebdata = DBUtils.getColumnValues(con,
				"SELECT web_data FROM analysis_description ad, analysis a WHERE a.analysis_id = ad.analysis_id AND logic_name in ('ensembl_havana_gene', 'ensembl_havana_ig_gene', 'ensembl_lincrna')");
		String[] previousGencodeWebdata = DBUtils.getColumnValues(previousCon,
				"SELECT web_data FROM analysis_description ad, analysis a WHERE a.analysis_id = ad.analysis_id AND logic_name in ('ensembl_havana_gene', 'ensembl_havana_ig_gene', 'ensembl_lincrna')");

		String gencode = DBUtils.getRowColumnValue(con,
				"SELECT meta_value FROM meta WHERE meta_key = 'gencode.version'");
		String previousGencode = DBUtils.getRowColumnValue(previousCon,
				"SELECT meta_value FROM meta WHERE meta_key = 'gencode.version'");

		if (!currentGenes.equals(previousGenes)) {
			if (genesetUpdate.equals(previousGenesetUpdate)) {
				ReportManager.problem(this, con, "Gene set has changed but last_geneset_update has not been updated");
				result = false;
			}
			if (dbre.getSpecies().equals(DatabaseRegistryEntry.HOMO_SAPIENS)
					|| dbre.getSpecies().equals(DatabaseRegistryEntry.MUS_MUSCULUS)) {
				if (gencode.equals(previousGencode)) {
					ReportManager.problem(this, con, "Gene set has changed but gencode.version has not been updated");
					result = false;
				}
				for (int i = 0; i < gencodeWebdata.length; i++) {
					if (gencodeWebdata[i].equals(previousGencodeWebdata[i])) {
						ReportManager.problem(this, con,
								"Gene set has changed but gencode version in web_data has not been updated");
						result = false;
					}
				}
			}
		}

		return result;

	}

	private boolean checkForSchemaPatchLineBreaks(DatabaseRegistryEntry dbre) {
		SqlTemplate t = DBUtils.getSqlTemplate(dbre);
		String metaKey = "patch";
		String sql = "select meta_id from meta where meta_key =? and species_id IS NULL and meta_value like ?";
		List<Integer> ids = t.queryForDefaultObjectList(sql, Integer.class, metaKey, "%\n%");
		if (!ids.isEmpty()) {
			String idsJoined = Utils.listToString(ids, ",");
			String usefulSql = "select * from meta where meta_id IN (" + idsJoined + ")";
			String msg = String.format("The meta ids [%s] had values with linebreaks.\nUSEFUL SQL: %s", idsJoined,
					usefulSql);
			ReportManager.problem(this, dbre.getConnection(), msg);
			return false;
		}
		return true;
	}

	private boolean checkSample(DatabaseRegistryEntry dbre) {
		SqlTemplate t = DBUtils.getSqlTemplate(dbre);
		String metaKey = "sample.location_text";
		String sql = "select meta_value from meta where meta_key = ?";
		List<String> value = t.queryForDefaultObjectList(sql, String.class, metaKey);
		if (!value.isEmpty()) {
			String linkedKey = "sample.location_param";
			String linkedSql = "select meta_value from meta where meta_key = ?";
			List<String> linkedValue = t.queryForDefaultObjectList(linkedSql, String.class, linkedKey);
			if (!linkedValue.equals(value)) {
				ReportManager.problem(this, dbre.getConnection(),
						"Keys " + metaKey + " and " + linkedKey + " do not have same value");
				return false;
			}
		}
		return true;
	}

} // MetaValues
