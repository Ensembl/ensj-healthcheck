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
import org.ensembl.healthcheck.util.*;

/**
 * Store information about a table.
 */
public class TableInfo {
  
  /** The name of this table */
  protected String name;
  /** A list of Strings representing the column names */
  protected List columns = new ArrayList();
  
  /**
   * Creates a new instance of TableInfo
   * @param name The name of the table.
   * @param columns A List of Strings representing the column names.
   */
  public TableInfo(String name, List columns) {
    
    this.name = name;
    this.columns = columns;
    
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
  
  /** Getter for property columns.
   * @return Value of property columns.
   *
   */
  public java.util.List getColumns() {
    return columns;
  }
  
  /** Setter for property columns.
   * @param columns New value of property columns.
   *
   */
  public void setColumns(java.util.List columns) {
    this.columns = columns;
  }
  
  // -------------------------------------------------------------------------
  /**
   * Check if this table has a particular column.
   * @param col The column to check.
   * @return true If this column is present (check is case insensitive)
   */
  public boolean hasColumn(String col) {
    
    return columns.contains(col);
    
  }
  
  // -------------------------------------------------------------------------
  /**
   * Generate a String representation of this TableInfo object.
   * @return A String representing this table; the name of the table and of
   * each of the columns is included.
   */
  public String toString() {
    
    StringBuffer buf = new StringBuffer();
    buf.append("Table: " );
    buf.append(name);
    buf.append(" Columns: ");
    buf.append(Utils.listToString(columns, " "));
    
    return buf.toString();
    
  }
  
} // TableInfo
