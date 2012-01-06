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
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ensembl.healthcheck.AssemblyNameInfo;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.Utils;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Checks that meta_value contents in the meta table are OK. Only one meta table at a time is done here; checks for the consistency of the
 * meta table across species are done in MetaCrossSpecies.
 */
public class MetaValues extends SingleDatabaseTestCase {
	private boolean isSangerVega = false;

	
	public MetaValues() {

		addToGroup("post_genebuild");
		addToGroup("release");
		addToGroup("compara-ancestral");
		addToGroup("pre-compara-handover");
		addToGroup("post-compara-handover");
		
		setTeamResponsible(Team.RELEASE_COORDINATOR);
		setSecondTeamResponsible(Team.GENEBUILD);
		setDescription("Check that meta_value contents in the meta table are OK");
	}

	/**
	 * Checks that meta_value contents in the meta table are OK.
	 * 
	 * @param dbre
	 *          The database to check.
	 * @return True if the test passed.
	 */
	public boolean run(final DatabaseRegistryEntry dbre) {
		isSangerVega = dbre.getType() == DatabaseType.SANGER_VEGA;
		boolean result = true;

		Connection con = dbre.getConnection();

		Species species = dbre.getSpecies();

		if (species == Species.ANCESTRAL_SEQUENCES) {
			// The rest of the tests are not relevant for the ancestral sequences DB
			return result;
			
		}

		if (!isSangerVega) {// do not check for sanger_vega
			result &= checkOverlappingRegions(con);
		}
		
		result &= checkSpeciesClassification(dbre);

		result &= checkAssemblyMapping(con);

		result &= checkTaxonomyID(dbre);

		if (dbre.getType() == DatabaseType.CORE) {
			result &= checkDates(dbre);
		}

		result &= checkCoordSystemTableCases(con);

		if (dbre.getType() == DatabaseType.CORE) {
			result &= checkGenebuildID(con);
		}

		result &= checkBuildLevel(dbre);

		// ----------------------------------------
		//Use an AssemblyNameInfo object to get the assembly information
		
			
		AssemblyNameInfo assembly = new AssemblyNameInfo(con);

		String metaTableAssemblyDefault = assembly.getMetaTableAssemblyDefault();
		logger.finest("assembly.default from meta table: " + metaTableAssemblyDefault);
		String dbNameAssemblyVersion = assembly.getDBNameAssemblyVersion();
		logger.finest("Assembly version from DB name: " + dbNameAssemblyVersion);
		String metaTableAssemblyVersion = assembly.getMetaTableAssemblyVersion();
		logger.finest("meta table assembly version: " + metaTableAssemblyVersion);
		String metaTableAssemblyPrefix = assembly.getMetaTableAssemblyPrefix();
		logger.finest("meta table assembly prefix: " + metaTableAssemblyPrefix);

		if (metaTableAssemblyVersion == null || metaTableAssemblyDefault == null || metaTableAssemblyPrefix == null || dbNameAssemblyVersion == null) {

			ReportManager.problem(this, con, "Cannot get all information from meta table - check for null values");

		} else {

			// ----------------------------------------
			// Check that assembly prefix is valid and corresponds to this species
			// Prefix is OK as long as it starts with the valid one
			
			Species dbSpecies = dbre.getSpecies();
			String correctPrefix = Species.getAssemblyPrefixForSpecies(dbSpecies);

			if (!isSangerVega) {// do not check this for sanger_vega
				if (correctPrefix == null) {
					logger.info("Can't get correct assembly prefix for " + dbSpecies.toString());
				} else {
					if (metaTableAssemblyPrefix != null) {
						if (!metaTableAssemblyPrefix.toUpperCase().startsWith(correctPrefix.toUpperCase())) {
							ReportManager.problem(this, con, "Database species is " + dbSpecies + " but assembly prefix " + metaTableAssemblyPrefix + " should have prefix beginning with " + correctPrefix);
							result = false;
						} else {
							ReportManager.correct(this, con, "Meta table assembly prefix (" + metaTableAssemblyPrefix + ") is correct for " + dbSpecies);
						}
					} else {
						ReportManager.problem(this, con, "Can't get assembly prefix from meta table");
					}
				}
			}
		}

		// -------------------------------------------

		result &= checkSpacesInSpeciesClassification(dbre);

		// -------------------------------------------

		result &= checkGenebuildMethod(dbre);
		
		// -------------------------------------------
		
		result &= checkAssemblyAccessionUpdate(dbre);		
		
		// -------------------------------------------
		
		result &= checkRepeatAnalysis(dbre);

		return result;

	} // run

	// ---------------------------------------------------------------------

	// this HC will check the Meta table contains the assembly.overlapping_regions and
	// that it is set to false (so no overlapping regions in the genome)
	private boolean checkOverlappingRegions(Connection con) {

		boolean result = true;

		// check that certain keys exist
		String[] metaKeys = { "assembly.overlapping_regions" };
		for (int i = 0; i < metaKeys.length; i++) {
			String metaKey = metaKeys[i];
			int rows = getRowCount(con, "SELECT COUNT(*) FROM meta WHERE meta_key='" + metaKey + "'");
			if (rows == 0) {
				result = false;
				ReportManager.problem(this, con, "No entry in meta table for " + metaKey + ". It might need to run the misc-scripts/overlapping_regions.pl script");
			} else {
				String[] metaValue = getColumnValues(con, "SELECT meta_value FROM meta WHERE meta_key='" + metaKey + "'");
				if (metaValue[0].equals("1")) {
					// there are overlapping regions !! API might behave oddly
					ReportManager.problem(this, con, "There are overlapping regions in the database (e.g. two versions of the same chromosomes). The API"
							+ " might have unexpected results when trying to map features to that coordinate system.");
					result = false;
				} else {
					ReportManager.correct(this, con, metaKey + " entry present");
				}
			}
		}

		return result;
	}

	// ---------------------------------------------------------------------

	private boolean checkSpeciesClassification(DatabaseRegistryEntry dbre) {

		boolean result = true;

		String dbName = dbre.getName();
		Connection con = dbre.getConnection();

		// no point checking this for multi-species databases as they don't have the
		// genus & species in the database name
		if (dbre.isMultiSpecies()) {
			return true;
		}

		// Check that species.classification matches database name
		String[] metaTableSpeciesGenusArray = getColumnValues(con, "SELECT LCASE(meta_value) FROM meta WHERE meta_key='species.classification' ORDER BY meta_id LIMIT 2");
		// if all is well, metaTableSpeciesGenusArray should contain the
		// species and genus
		// (in that order) from the meta table

		if (metaTableSpeciesGenusArray != null && metaTableSpeciesGenusArray.length == 2 && metaTableSpeciesGenusArray[0] != null && metaTableSpeciesGenusArray[1] != null) {

			String[] dbNameGenusSpeciesArray = dbName.split("_");
			String dbNameGenusSpecies = dbNameGenusSpeciesArray[0] + "_" + dbNameGenusSpeciesArray[1];
			;

			String metaTableGenusSpecies = metaTableSpeciesGenusArray[1] + "_" + metaTableSpeciesGenusArray[0];
			logger.finest("Classification from DB name:" + dbNameGenusSpecies + " Meta table: " + metaTableGenusSpecies);

			if (!isSangerVega) {// do not check this for sanger_vega
				if (!dbNameGenusSpecies.equalsIgnoreCase(metaTableGenusSpecies)) {
					result = false;
					// warn(con, "Database name does not correspond to
					// species/genus data from meta
					// table");
					ReportManager.problem(this, con, "Database name does not correspond to species/genus data from meta table");
				} else {
					ReportManager.correct(this, con, "Database name corresponds to species/genus data from meta table");
				}
			}

		} else {
			// logger.warning("Cannot get species information from meta
			// table");
			ReportManager.problem(this, con, "Cannot get species information from meta table");
		}

		return result;
	}

	// ---------------------------------------------------------------------

	private boolean checkAssemblyMapping(Connection con) {

		boolean result = true;

		// Check formatting of assembly.mapping entries; should be of format
		// coord_system1{:default}|coord_system2{:default} with optional third
		// coordinate system
		// and all coord systems should be valid from coord_system
		// can also have # instead of | as used in unfinished contigs etc

		Pattern assemblyMappingPattern = Pattern.compile("^([a-zA-Z0-9.]+):?([a-zA-Z0-9._]+)?[\\|#]([a-zA-Z0-9._]+):?([a-zA-Z0-9._]+)?([\\|#]([a-zA-Z0-9.]+):?([a-zA-Z0-9._]+)?)?$");
		String[] validCoordSystems = getColumnValues(con, "SELECT name FROM coord_system");

		String[] mappings = getColumnValues(con, "SELECT meta_value FROM meta WHERE meta_key='assembly.mapping'");
		for (int i = 0; i < mappings.length; i++) {
			Matcher matcher = assemblyMappingPattern.matcher(mappings[i]);
			if (!matcher.matches()) {
				result = false;
				ReportManager.problem(this, con, "Coordinate system mapping " + mappings[i] + " is not in the correct format");
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
					ReportManager.problem(this, con, "Source co-ordinate system " + cs1 + " is not in the coord_system table");
				}
				if (!Utils.stringInArray(cs2, validCoordSystems, false)) {
					valid = false;
					ReportManager.problem(this, con, "Target co-ordinate system " + cs2 + " is not in the coord_system table");
				}
				// third coordinate system is optional
				if (cs3 != null && !Utils.stringInArray(cs3, validCoordSystems, false)) {
					valid = false;
					ReportManager.problem(this, con, "Third co-ordinate system in mapping (" + cs3 + ") is not in the coord_system table");
				}
				if (valid) {
					ReportManager.correct(this, con, "Coordinate system mapping " + mappings[i] + " is OK");
				}

				result &= valid;

				// check that coord_system:version pairs listed here exist in the coord_system table
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
	 * Check that coordinate system:assembly pairs in assembly.mappings match what's in the coord system table
	 */
	private boolean checkCoordSystemVersionPairs(Connection con, String cs1, String assembly1, String cs2, String assembly2, String cs3, String assembly3) {

		boolean result = true;

		List<String> coordSystemsAndVersions = getColumnValuesList(con, "SELECT CONCAT_WS(':',name,version) FROM coord_system");

		result &= checkCoordSystemPairInList(con, cs1, assembly1, coordSystemsAndVersions);

		result &= checkCoordSystemPairInList(con, cs2, assembly2, coordSystemsAndVersions);

		if (cs3 != null) {

			result &= checkCoordSystemPairInList(con, cs3, assembly3, coordSystemsAndVersions);

		}

		return result;

	}

	// ---------------------------------------------------------------------
	/**
	 * Check if a particular coordinate system:version pair is in a list. Deal with nulls appropriately.
	 */
	private boolean checkCoordSystemPairInList(Connection con, String cs, String assembly, List<String> coordSystems) {

		boolean result = true;

		String toCompare = (assembly != null) ? cs + ":" + assembly : cs;

		if (!coordSystems.contains(toCompare)) {

			ReportManager.problem(this, con, "Coordinate system name/version " + toCompare + " in assembly.mapping does not appear in coord_system table.");
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

		if (cs.equals(cs.toLowerCase())) {

			ReportManager.correct(this, con, "Co-ordinate system name " + cs + " all lower case in " + desc);
			result = true;

		} else {

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

		String[] coordSystems = getColumnValues(con, "SELECT name FROM coord_system");

		for (int i = 0; i < coordSystems.length; i++) {

			result &= checkCoordSystemCase(con, coordSystems[i], "coord_system");

		}

		return result;

	}

	// ---------------------------------------------------------------------

	private boolean checkTaxonomyID(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		// Check that the taxonomy ID matches a known one.
		// The taxonomy ID-species mapping is held in the Species class.

		Species species = dbre.getSpecies();
		String dbTaxonID = getRowColumnValue(con, "SELECT meta_value FROM meta WHERE meta_key='species.taxonomy_id'");
		logger.finest("Taxonomy ID from database: " + dbTaxonID);

		if (dbTaxonID.equals(Species.getTaxonomyID(species))) {
			ReportManager.correct(this, con, "Taxonomy ID " + dbTaxonID + " is correct for " + species.toString());
		} else {
			result = false;
			ReportManager.problem(this, con, "Taxonomy ID " + dbTaxonID + " in database is not correct - should be " + Species.getTaxonomyID(species) + " for " + species.toString());
		}
		return result;

	}

	// ---------------------------------------------------------------------

	private boolean checkDates(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		String[] keys = { "genebuild.start_date", "assembly.date", "genebuild.initial_release_date", "genebuild.last_geneset_update" };

		String date = "[0-9]{4}-[0-9]{2}";
		String[] regexps = { date + "-[a-zA-Z]*", date, date, date };

		for (int i = 0; i < keys.length; i++) {

			String key = keys[i];
			String regexp = regexps[i];

			String value = getRowColumnValue(con, "SELECT meta_value FROM meta WHERE meta_key='" + key + "'");
			if (value == null || value.length() == 0) {

				ReportManager.problem(this, con, "No " + key + " entry in meta table");
				result = false;

			}

			result &= checkMetaKey(con, key, value, regexp);

			if (result) {
				result &= checkDateFormat(con, key, value);
			}

			if (result) {
				ReportManager.correct(this, con, key + " is present & in a valid format");
			}
		}

		// some more checks for sanity of dates
		int startDate = Integer.valueOf(getRowColumnValue(con, "SELECT meta_value FROM meta WHERE meta_key='genebuild.start_date'").replaceAll("[^0-9]", "")).intValue();
		int initialReleaseDate = Integer.valueOf(getRowColumnValue(con, "SELECT meta_value FROM meta WHERE meta_key='genebuild.initial_release_date'").replaceAll("[^0-9]", "")).intValue();
		int lastGenesetUpdate = Integer.valueOf(getRowColumnValue(con, "SELECT meta_value FROM meta WHERE meta_key='genebuild.last_geneset_update'").replaceAll("[^0-9]", "")).intValue();

		// check for genebuild.start_date >= genebuild.initial_release_date (not allowed as we cannot release a gene set before
		// downloaded the evidence)
		if (startDate >= initialReleaseDate) {
			result = false;
			ReportManager.problem(this, con, "genebuild.start_date is greater than or equal to genebuild.initial_release_date");
		}

		// check for genebuild.initial_release_date > genebuild.last_geneset_update (not allowed as we cannot update a gene set before
		// its initial public release)
		if (initialReleaseDate > lastGenesetUpdate) {
			result = false;
			ReportManager.problem(this, con, "genebuild.initial_release_date is greater than or equal to genebuild.last_geneset_update");
		}

		// check for current genebuild.last_geneset_update <= previous release genebuild.last_geneset_update
		// AND the number of genes or transcripts or exons between the two releases has changed
		// If the gene set has changed in any way since the previous release then the date should have been updated.
		DatabaseRegistryEntry previous = getEquivalentFromSecondaryServer(dbre);
		if (previous == null) {
			return result;
		}

		Connection previousCon = previous.getConnection();

		String previousLastGenesetUpdateString = getRowColumnValue(previousCon, "SELECT meta_value FROM meta WHERE meta_key='genebuild.last_geneset_update'").replaceAll("-", "");

		if (previousLastGenesetUpdateString == null || previousLastGenesetUpdateString.length() == 0) {

			ReportManager.problem(this, con, "Problem parsing last geneset update entry from previous database.");
			return false;

		}

		int previousLastGenesetUpdate;

		try {

			previousLastGenesetUpdate = Integer.valueOf(previousLastGenesetUpdateString).intValue();

		} catch (NumberFormatException e) {

			ReportManager.problem(this, con, "Problem parsing last geneset update entry from previous database: " + Arrays.toString(e.getStackTrace()));
			return false;

		}

		if (lastGenesetUpdate <= previousLastGenesetUpdate) {

			int currentGeneCount = getRowCount(con, "SELECT COUNT(*) FROM gene");
			int currentTranscriptCount = getRowCount(con, "SELECT COUNT(*) FROM transcript");
			int currentExonCount = getRowCount(con, "SELECT COUNT(*) FROM exon");

			int previousGeneCount = getRowCount(previousCon, "SELECT COUNT(*) FROM gene");
			int previousTranscriptCount = getRowCount(previousCon, "SELECT COUNT(*) FROM transcript");
			int previousExonCount = getRowCount(previousCon, "SELECT COUNT(*) FROM exon");

			if (currentGeneCount != previousGeneCount || currentTranscriptCount != previousTranscriptCount || currentExonCount != previousExonCount) {

				ReportManager.problem(this, con, "Last geneset update entry is the same or older than the equivalent entry in the previous release and the number of genes, transcripts or exons has changed.");
				result = false;

			}
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

		String gbid = getRowColumnValue(con, "SELECT meta_value FROM meta WHERE meta_key='genebuild.id'");
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
		String[] Tables = { "gene", "transcript", "exon", "repeat_feature", "dna_align_feature", "protein_align_feature", "simple_feature", "prediction_transcript", "prediction_exon" };

		int exists = getRowCount(con, "SELECT COUNT(*) FROM meta where meta_key like '%build.level'");
		if (exists == 0) {
			ReportManager.problem(this, con, "GB: No %build.level entries in the meta table - run ensembl/misc-scripts/meta_levels.pl");
		}
		int count = 0;
		for (int i = 0; i < Tables.length; i++) {
			String Table = Tables[i];
			int rows = getRowCount(con, "SELECT COUNT(*) FROM " + Table);
			int key = getRowCount(con, "SELECT COUNT(*) FROM meta WHERE meta_key = '" + Table + "build.level' ");
			int toplevel = getRowCount(con, "SELECT COUNT(*) FROM " + Table
					+ " t, seq_region_attrib sra, attrib_type at WHERE t.seq_region_id = sra.seq_region_id AND sra.attrib_type_id = at.attrib_type_id AND at.code = 'toplevel' ");
			if (rows != 0) {
				if (key == 0) {
					if (rows == toplevel) {
						ReportManager.problem(this, con, "Table " + Table + " should have a toplevel flag - run ensembl/misc-scripts/meta_levels.pl");
					} else {
						count++;
					}
				} else {
					if (rows != toplevel) {
						ReportManager.problem(this, con, "Table " + Table + " has some non toplevel regions, should not have a toplevel flag - run ensembl/misc-scripts/meta_levels.pl");
					} else {
						count++;
					}
				}
			} else {
				if (key != 0) {
					ReportManager.problem(this, con, "Empty table " + Table + " should not have a toplevel flag - run ensembl/misc-scripts/meta_levels.pl");
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
	 * Check for species.classification entries that contain spaces.
	 */
	private boolean checkSpacesInSpeciesClassification(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		int rows = getRowCount(con, "SELECT COUNT(*) FROM meta WHERE meta_key='species.classification' AND meta_value LIKE '% %'");

		if (rows > 0) {
			ReportManager.problem(this, con, rows + " species.classification entries have values with spaces");
			result = false;
		} else {
			ReportManager.correct(this, con, "No species.classification entries contain spaces");
		}

		return result;

	}

	// ---------------------------------------------------------------------

	/**
	 * Check that the genebuild.method entry exists and has one of the allowed values.
	 */
	private boolean checkGenebuildMethod(DatabaseRegistryEntry dbre) {

		boolean result = true;

		// only valid for core databases
		if (dbre.getType() != DatabaseType.CORE) {
			return true;
		}

		String[] allowedMethods = { "full_genebuild", "projection_build", "import", "mixed_strategy_build" };

		Connection con = dbre.getConnection();
		String method = getRowColumnValue(con, "SELECT meta_value FROM meta WHERE meta_key='genebuild.method'");

		if (method.equals("")) {
			ReportManager.problem(this, con, "No genebuild.method entry present in Meta table");
			return false;
		}

		if (!Utils.stringInArray(method, allowedMethods, true)) {
			ReportManager.problem(this, con, "genebuild.method value " + method + " is not in list of allowed methods");
			result = false;
		} else {
			ReportManager.correct(this, con, "genebuild.method " + method + " is valid");
		}

		return result;

	}
	// ---------------------------------------------------------------------

	
	private boolean checkAssemblyAccessionUpdate(DatabaseRegistryEntry dbre) {
		
		boolean result = true;
		
		// only valid for core databases
		if (dbre.getType() != DatabaseType.CORE) {
			return true;
		}
		
		Connection con = dbre.getConnection();
		String currentAssemblyAccession = DBUtils.getMetaValue(con, "assembly.accession");
		String currentAssemblyName = DBUtils.getMetaValue(con, "assembly.name");

		if (currentAssemblyAccession.equals("")) {
			ReportManager.problem(this, con, "No assembly.accession entry present in Meta table");
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
		
		long currentAssemblyChecksum = getChecksum(con, "assembly");
		long previousAssemblyChecksum = getChecksum(previousCon, "assembly");
				
		boolean assemblyChanged = false;
		boolean assemblyTableChanged = false;
		boolean assemblyExceptionTableChanged = false;
		
		if (currentAssemblyChecksum != previousAssemblyChecksum) {
			assemblyTableChanged = true;
		} else {
			if (dbre.getSpecies() != Species.HOMO_SAPIENS) {
						
				// compare assembly_exception tables (patches only) from each database
				try {

					Statement previousStmt = previousCon.createStatement();
					Statement currentStmt = con.createStatement();

					String sql = "SELECT * FROM assembly_exception WHERE exc_type LIKE ('PATCH_%') ORDER BY assembly_exception_id";
					ResultSet previousRS = previousStmt.executeQuery(sql);
					ResultSet currentRS = currentStmt.executeQuery(sql);

					boolean assExSame = DBUtils.compareResultSets(currentRS, previousRS, this, "", false, false, "assembly_exception", false);

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

		if (assemblyChanged == previousAssemblyAccession.equals(currentAssemblyAccession) && previousAssemblyName.equals(currentAssemblyName) ) {
			result = false;
			String errorMessage = "assembly.accession and assembly.name values need to be updated when "
					+ "the assembly table changes or new patches are added to the assembly exception table\n"
					+ "previous assembly.accession: " + previousAssemblyAccession + " assembly.name: " + previousAssemblyName 
					+ " current assembly.accession: " + currentAssemblyAccession + " assembly.name: " + currentAssemblyName + "\n"
					+ "assembly table changed:";
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
	 * Check that all meta_values with meta_key 'repeat.analysis' reference analysis.logic_name
	 */
	private boolean checkRepeatAnalysis(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();
		String[] repeatAnalyses = getColumnValues(con, "SELECT meta_value FROM meta LEFT JOIN analysis ON meta_value = logic_name WHERE meta_key = 'repeat.analysis' AND analysis_id IS NULL");
		if (repeatAnalyses.length > 0) {
			ReportManager.problem(this, con, "The following values for meta_key repeat.analysis don't have a corresponding logic_name entry in the analysis table: " + Utils.arrayToString(repeatAnalyses,",") );
		} else {
			ReportManager.correct(this, con, "All values for meta_key repeat.analysis have a corresponding logic_name entry in the analysis table");
		}

		return result;

	}
	
} // MetaValues
