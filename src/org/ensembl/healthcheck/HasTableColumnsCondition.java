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

/**
 * Subclass of SchemaMatchCondition to check if a schema has a table and certain columns.
 */
public class HasTableColumnsCondition extends SchemaMatchCondition {
  
  private String tableName;
  private String[] columns;
  
  /**
   * Creates a new instance of HasTableColumnsCondition
   * @param tableName The name of the table to look at.
   * @param columns The column names to check.
   */
  public HasTableColumnsCondition(String tableName, String[] columns) {
    
    this.tableName = tableName;
    this.columns = columns;
    
  }
  
  /**
   * Check if a schema has the specified table and columns. Note that the
   * table may have other columns; this method will still return true as long
   * as the columns parameter set in the constructor is a subset of the columns 
   * that are present.
   * @param s The schema to check.
   * @return true if the table exists <em>and</em> all the columns are present.
   */
  public boolean matches(SchemaInfo s) {
    
    boolean result = true;
    
    SchemaMatchCondition tableNameCondition = new HasTableCondition(tableName);
    
    if (tableNameCondition.matches(s)) {
      
      // get the table in question
      TableInfo ti = s.getTable(tableName);
      
      // check the columns
      for (int i = 0; i < columns.length; i++) {
        
        result &= ti.hasColumn(columns[i]);
        
      }
      
    } else {
      
      result = false;
      
    }
    
    return result;
    
  }
  
} // HasTableColumnsCondition
