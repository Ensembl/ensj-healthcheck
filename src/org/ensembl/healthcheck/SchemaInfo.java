/*
  Copyright (C) 2003 EBI, GRL
 
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

import java.util.*;
import java.sql.*;
import org.ensembl.healthcheck.util.*;

/**
 * Store information about a database schema. Lighter-weight and simpler than
 * DatabaseMetaData.
 */
public class SchemaInfo {
  
  /** The name of this schema */
  protected String name = "";
  /** A List of TableInfo objects representing the tables */
  protected List tables = new ArrayList();
  
  /**
   * Creates a new instance of SchemaInfo.
   * @param name The name of this schema.
   * @param tables A List of TableInfo objects representing the tables.
   */
  public SchemaInfo(String name, List tables) {
    
    this.name = name;
    this.tables = tables;
    
  }
  
  /**
   * Create a SchemaInfo object from a database connection.
   * @param con The connection to analyse.
   */
  public SchemaInfo(Connection con) {
    
    this.name = DBUtils.getShortDatabaseName(con);
    
    List tableNames = DBUtils.getTableNames(con);
    
    Iterator tableIterator = tableNames.iterator();
    while(tableIterator.hasNext()) {
      String tableName = (String)tableIterator.next();
      List columns = DBUtils.getColumnsInTable(con, tableName);
      TableInfo ti = new TableInfo(tableName, columns);
      this.tables.add(ti);
    }
    
  }
  
  /** Getter for property name.
   * @return Value of property name.
   *
   */
  public java.lang.String getName() {
    return name;
  }
  
  /** Setter for property name.
   * @param name New value of property name.
   *
   */
  public void setName(java.lang.String name) {
    this.name = name;
  }
  
  /** Getter for property tables.
   * @return Value of property tables.
   *
   */
  public java.util.List getTables() {
    return tables;
  }
  
  /** Setter for property tables.
   * @param tables New value of property tables.
   *
   */
  public void setTables(java.util.List tables) {
    this.tables = tables;
  }
  
  public String toString() {
    
    StringBuffer buf = new StringBuffer();
    buf.append("Schema: " );
    buf.append(name);
    buf.append(" Tables:\n");
    Iterator it = tables.iterator();
    while (it.hasNext()) {
      buf.append(" " + ((TableInfo)it.next()).toString());
      buf.append("\n");
    }
    
    return buf.toString();
    
  }
  
  // -------------------------------------------------------------------------
  /**
   * Check if this schema matches a particular condition.
   * @param cond The condition to check.
   * @return true if this schema fulfils cond, false otherwise.
   */
  public boolean matches(SchemaMatchCondition cond) {
    
    return cond.matches(this);
    
  }
  
  // -------------------------------------------------------------------------
  /**
   * Check if this schema matches a set of conditions.
   * @param conds The conditions to check.
   * @return true if this schema fulfils all the conditions, false otherwise.
   */
  public boolean matchesAll(List conds) {
    
    boolean result = true;
    
    Iterator it = conds.iterator();
    while (it.hasNext()) {
      SchemaMatchCondition cond = (SchemaMatchCondition)it.next();
      result &= matches(cond);
    }
    return result;
    
  }
  // -------------------------------------------------------------------------
  /**
   * Get a particular table by name.
   * @param tableName The table to get.
   * @return The TableInfo object corresponding to tableName, or null.
   */ 
   public TableInfo getTable(String tableName) {
     
    TableInfo result = null;
    
    Iterator it = tables.iterator();
    while (it.hasNext()) {
      TableInfo ti = (TableInfo)it.next();
      if (ti.getName().equalsIgnoreCase(tableName)) {
        result = ti;
      }
    }
    
    return result;
    
   }
  // -------------------------------------------------------------------------
  
} // SchemaInfo
