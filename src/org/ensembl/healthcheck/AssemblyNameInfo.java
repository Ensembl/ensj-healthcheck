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


package org.ensembl.healthcheck;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Logger;

import org.ensembl.healthcheck.util.DBUtils;

/**
 * Stores information about an assembly, and has a method for getting that
 * information from a database.
 */
public class AssemblyNameInfo {

    private String metaTableAssemblyDefault = null;

    private String metaTableAssemblyPrefix = null;

    private String metaTableAssemblyVersion = null;

    private String dbNameAssemblyVersion = null;

    private static Logger logger = Logger.getLogger("HealthCheckLogger");

    /**
     * Creates a new instance of AssemblyNameInfo.
     *
     * @param con
     *          The database connection to get the information from.
     */
    public AssemblyNameInfo(final Connection con) {

        queryConnection(con);

    }

    // -------------------------------------------------------------------------
    /**
     * Gets the full value of the default assembly from the meta table.
     * 
     * @return The default assembly, e.g. NCBI31, or null if the value cannot
     *         be read from the meta table.
     */
    public final String getMetaTableAssemblyDefault() {

        return metaTableAssemblyDefault;

    }

    // -------------------------------------------------------------------------
    /**
     * Get the prefix (the part before the version) of the assembly from the
     * meta table.
     * 
     * @return The assembly prefix, e.g. NCBI, or null if the value cannot be
     *         read from the meta table.
     */
    public final String getMetaTableAssemblyPrefix() {

        return metaTableAssemblyPrefix;

    }

    // -------------------------------------------------------------------------
    /**
     * Get the version (the numeric part at the end) of the assembly from the
     * meta table.
     * 
     * @return The version, e.g. 31, or null if the value cannot be read from
     *         the meta table.
     */
    public final String getMetaTableAssemblyVersion() {

        return metaTableAssemblyVersion;

    }

    // -------------------------------------------------------------------------
    /**
     * Get the assembly version as referred to in the database name (<em>not</em>
     * from the meta table)
     * 
     * @return The version, e.g. homo_sapiens_core_12_31 -> 31, or null if the
     *         value cannot be read from the meta table.
     */
    public final String getDBNameAssemblyVersion() {

        return dbNameAssemblyVersion;

    }

    // -------------------------------------------------------------------------
    /**
     * Gets the metaTable* and dbName* info from the meta table.
     * 
     * @param con
     *          The database connection to look in.
     */
    private void queryConnection(final Connection con) {

        // ----------------------------------------
        // Get the default assembly from the table

        metaTableAssemblyDefault = "";

        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT meta_value FROM meta WHERE meta_key='assembly.default'");
            if (rs != null && rs.first()) {
                metaTableAssemblyDefault = rs.getString(1);
            }

        } catch (Exception e) {
            logger.severe("Could not get assembly information from database.");
            e.printStackTrace();
        }

        // ----------------------------------------
        // Split the assembly into prefix + version
        if (metaTableAssemblyDefault != null) {

            metaTableAssemblyPrefix = metaTableAssemblyDefault.replaceAll("[0-9._]+$", "");
            metaTableAssemblyVersion = metaTableAssemblyDefault.replaceAll("^\\D+", "");

            // -----------------------------------------
            // Get the version number from the db name

            String dbName = DBUtils.getShortDatabaseName(con);
            dbNameAssemblyVersion = dbName.substring(dbName.lastIndexOf('_') + 1);

            //logger.finest(metaTableAssemblyDefault + " " +
            // metaTableAssemblyPrefix + " " +
            // metaTableAssemblyVersion + " " + dbNameAssemblyVersion);

        } else {

            logger.severe("Value for assembly.default in meta table for " + DBUtils.getShortDatabaseName(con)
                    + " seems to be null");

        }

    } // queryConnection

    // -------------------------------------------------------------------------

} // AssemblyNameInfo
