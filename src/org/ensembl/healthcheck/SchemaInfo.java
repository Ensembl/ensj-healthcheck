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
  
} // SchemaInfo
