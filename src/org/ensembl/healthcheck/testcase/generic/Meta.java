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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ensembl.healthcheck.AssemblyNameInfo;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.Utils;

/**
 * Checks the metadata table to make sure it is OK. Only one meta table at a time is done here; checks for the consistency of the meta table across
 * species are done in MetaCrossSpecies.
 */
public class Meta extends SingleDatabaseTestCase {

    // format for genebuild.version
    private static final String GBV_REGEXP = "[0-9]{4}[a-zA-Z]*";

    /**
     * Creates a new instance of CheckMetaDataTableTestCase
     */
    public Meta() {

        addToGroup("post_genebuild");
        addToGroup("release");
        setDescription("Check that the meta table exists, has data, the entries correspond to the "
                + "database name, and that the values in assembly.type match what's in the meta table");
    }

    /**
     * Check various aspects of the meta table.
     * 
     * @param dbre The database to check.
     * @return True if the test passed.
     */
    public boolean run(final DatabaseRegistryEntry dbre) {

        boolean result = true;

        Connection con = dbre.getConnection();

        result &= checkTableExists(con);

        result &= tableHasRows(con);

        result &= checkKeysPresent(con);

        result &= checkSpeciesClassification(dbre);

        result &= checkAssemblyMapping(con);

        result &= checkTaxonomyID(dbre);

        result &= checkGenebuildVersion(con);

        result &= checkCoordSystemTableCases(con);

        if (dbre.getType() == DatabaseType.CORE) {
        	result &= checkGenebuildID(con);
        }
        
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

        if (metaTableAssemblyVersion == null || metaTableAssemblyDefault == null || metaTableAssemblyPrefix == null || dbNameAssemblyVersion == null) {

            ReportManager.problem(this, con, "Cannot get all information from meta table - check for null values");

        } else {

            // check that assembly.default matches the version of the coord_system with the lowest
            // rank value
            String lowestRankCS = getRowColumnValue(con, "SELECT version FROM coord_system WHERE version IS NOT NULL ORDER BY rank DESC LIMIT 1");
            if (!lowestRankCS.equals(metaTableAssemblyDefault)) {
                if (lowestRankCS.length() > 0) {
                    ReportManager.problem(this, con, "assembly.default from meta table is " + metaTableAssemblyDefault
                            + " but lowest ranked coordinate system has version " + lowestRankCS);
                } else {

                    ReportManager.problem(this, con, "assembly.default from meta table is " + metaTableAssemblyDefault
                            + " but lowest ranked coordinate system has blank or missing version");
                }
            }

            // ----------------------------------------
            // Check that assembly prefix is valid and corresponds to this species
            // Prefix is OK as long as it starts with the valid one
            Species dbSpecies = dbre.getSpecies();
            String correctPrefix = Species.getAssemblyPrefixForSpecies(dbSpecies);
            if (!metaTableAssemblyPrefix.toUpperCase().startsWith(correctPrefix.toUpperCase())) {
                ReportManager.problem(this, con, "Database species is " + dbSpecies + " but assembly prefix " + metaTableAssemblyPrefix
                        + " should have prefix beginning with " + correctPrefix);
                result = false;
            } else {
                ReportManager.correct(this, con, "Meta table assembly prefix (" + metaTableAssemblyPrefix + ") is correct for " + dbSpecies);
            }
        }

        // -------------------------------------------

        return result;

    } // run

    // ---------------------------------------------------------------------

    private boolean checkTableExists(Connection con) {

        boolean result = true;

        if (!checkTableExists(con, "meta")) {
            result = false;
            ReportManager.problem(this, con, "Meta table not present");
        } else {
            ReportManager.correct(this, con, "Meta table present");
        }

        return result;

    }

    // ---------------------------------------------------------------------

    private boolean tableHasRows(Connection con) {

        boolean result = true;

        int rows = countRowsInTable(con, "meta");
        if (rows == 0) {
            result = false;
            ReportManager.problem(this, con, "meta table is empty");
        } else {
            ReportManager.correct(this, con, "meta table has data");
        }

        return result;

    }

    // ---------------------------------------------------------------------

    private boolean checkKeysPresent(Connection con) {

        boolean result = true;

        // check that there are species, classification and taxonomy_id entries
        String[] metaKeys = { "assembly.default", "species.classification", "species.common_name", "species.taxonomy_id" };
        for (int i = 0; i < metaKeys.length; i++) {
            String metaKey = metaKeys[i];
            int rows = getRowCount(con, "SELECT COUNT(*) FROM meta WHERE meta_key='" + metaKey + "'");
            if (rows == 0) {
                result = false;
                ReportManager.problem(this, con, "No entry in meta table for " + metaKey);
            } else {
                ReportManager.correct(this, con, metaKey + " entry present");
            }
        }

        return result;
    }

    // ---------------------------------------------------------------------

    private boolean checkSpeciesClassification(DatabaseRegistryEntry dbre) {

        boolean result = true;

        String dbName = dbre.getName();
        Connection con = dbre.getConnection();

        // Check that species.classification matches database name
        String[] metaTableSpeciesGenusArray = getColumnValues(con,
                                                              "SELECT LCASE(meta_value) FROM meta WHERE meta_key='species.classification' ORDER BY meta_id LIMIT 2");
        // if all is well, metaTableSpeciesGenusArray should contain the
        // species and genus
        // (in that order) from the meta table

        if (metaTableSpeciesGenusArray != null && metaTableSpeciesGenusArray.length == 2 && metaTableSpeciesGenusArray[0] != null
                && metaTableSpeciesGenusArray[1] != null) {

            String[] dbNameGenusSpeciesArray = dbName.split("_");
            String dbNameGenusSpecies = dbNameGenusSpeciesArray[0] + "_" + dbNameGenusSpeciesArray[1];
            String metaTableGenusSpecies = metaTableSpeciesGenusArray[1] + "_" + metaTableSpeciesGenusArray[0];
            logger.finest("Classification from DB name:" + dbNameGenusSpecies + " Meta table: " + metaTableGenusSpecies);
            if (!dbNameGenusSpecies.equalsIgnoreCase(metaTableGenusSpecies)) {
                result = false;
                // warn(con, "Database name does not correspond to
                // species/genus data from meta
                // table");
                ReportManager.problem(this, con, "Database name does not correspond to species/genus data from meta table");
            } else {
                ReportManager.correct(this, con, "Database name corresponds to species/genus data from meta table");
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

        Pattern assemblyMappingPattern = Pattern.compile("^([a-zA-Z0-9.]+)(:[a-zA-Z0-9._]+)?[\\|#]([a-zA-Z0-9.]+)(:[a-zA-Z0-9.]+)?([\\|#]([a-zA-Z0-9.]+)(:[a-zA-Z0-9._]+)?)?$");
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
                String cs2 = matcher.group(3);
                String cs3 = matcher.group(6);
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

                // check that coord systems are specified in lower-case
                result &= checkCoordSystemCase(con, cs1, "meta assembly.mapping");
                result &= checkCoordSystemCase(con, cs2, "meta assembly.mapping");
                result &= checkCoordSystemCase(con, cs3, "meta assembly.mapping");

            }
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
            ReportManager.problem(this, con, "Taxonomy ID " + dbTaxonID + " in database is not correct - should be " + Species.getTaxonomyID(species)
                    + " for " + species.toString());
        }
        return result;

    }

    // -------------------------------------------------------------------------

    private boolean checkGenebuildVersion(Connection con) {

        String gbv = getRowColumnValue(con, "SELECT meta_value FROM meta WHERE meta_key='genebuild.version'");
        logger.finest("genebuild.version from database: " + gbv);

        if (gbv == null || gbv.length() == 0) {

            ReportManager.problem(this, con, "No genebuild.version entry in meta table");
            return false;

        } else {

            if (!gbv.matches(GBV_REGEXP)) {

                ReportManager.problem(this, con, "genebuild.version " + gbv + " is not in correct format - should match " + GBV_REGEXP);
                return false;

            } else {

                int year = Integer.parseInt(gbv.substring(0, 2));
                if (year < 0 || year > 99) {
                    ReportManager.problem(this, con, "Year part of genebuild.version (" + year + ") is incorrect");
                    return false;
                }
                int month = Integer.parseInt(gbv.substring(2, 4));
                if (month < 1 || month > 12) {
                    ReportManager.problem(this, con, "Month part of genebuild.version (" + month + ") is incorrect");
                    return false;
                }

            }

        }

        ReportManager.correct(this, con, "genebuild.version " + gbv + " is present & in a valid format");

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

} // Meta
