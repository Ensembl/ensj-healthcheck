/*
  Copyright (C) 2004 EBI, GRL
 
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.
 
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.ensembl.healthcheck;

import java.sql.*;
import java.util.logging.*;

import org.ensembl.healthcheck.util.*;

/**
 * Stores information about an assembly, and has a method for getting that information from a database.
 */
public class AssemblyNameInfo {
  
  private String metaTableAssemblyDefault, metaTableAssemblyPrefix, metaTableAssemblyVersion, dbNameAssemblyVersion = null;
  
  private static Logger logger = Logger.getLogger("HealthCheckLogger");
  
  /**
   * Creates a new instance of AssemblyNameInfo.
   * @param con The database connection to get the information from.
   */
  public AssemblyNameInfo(Connection con) {
    
    queryConnection(con);
    
  }
  
  // -------------------------------------------------------------------------
  /**
   * Gets the full value of the default assembly from the meta table.
   * @return The default assembly, e.g. NCBI31, or null if the value cannot be read from the meta table.
   */
  public String getMetaTableAssemblyDefault() {
    
    return metaTableAssemblyDefault;
    
  }
  
  // -------------------------------------------------------------------------
  /**
   * Get the prefix (the part before the version) of the assembly from the meta table.
   * @return The assembly prefix, e.g. NCBI, or null if the value cannot be read from the meta table.
   */
  public String getMetaTableAssemblyPrefix() {
    
    return metaTableAssemblyPrefix;
    
  }
  
  // -------------------------------------------------------------------------
  /**
   * Get the version (the numeric part at the end) of the assembly from the meta table.
   * @return The version, e.g. 31, or null if the value cannot be read from the meta table.
   */
  public String getMetaTableAssemblyVersion() {
    
    return metaTableAssemblyVersion;
    
  }
  
  // -------------------------------------------------------------------------
  /**
   * Get the assembly version as referred to in the database name (<em>not</em> from the meta table)
   * @return The version, e.g. homo_sapiens_core_12_31 -> 31, or null if the value cannot be read from the meta table.
   */
  public String getDBNameAssemblyVersion() {
    
    return dbNameAssemblyVersion;
    
  }
  
  // -------------------------------------------------------------------------
  /**
   * Gets the metaTable* and dbName* info from the meta table.
   * @param con The database connection to look in.
   */
  private void queryConnection(Connection con) {
    
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
      
    metaTableAssemblyPrefix = metaTableAssemblyDefault.replaceAll("\\d+$", "");
    metaTableAssemblyVersion = metaTableAssemblyDefault.replaceAll("^\\D+", "");
    
    // -----------------------------------------
    // Get the version number from the db name
    
    String dbName = DBUtils.getShortDatabaseName(con);
    dbNameAssemblyVersion = dbName.substring(dbName.lastIndexOf('_') + 1);
    
    //logger.finest(metaTableAssemblyDefault + " " + metaTableAssemblyPrefix + " " + metaTableAssemblyVersion + " " + dbNameAssemblyVersion);
    
    } else {
      
      logger.severe("Value for assembly.default in meta table for " + DBUtils.getShortDatabaseName(con)  + " seems to be null");
      
    }
    
    
  } // queryConnection
  
  
  // -------------------------------------------------------------------------
  
} // AssemblyNameInfo
