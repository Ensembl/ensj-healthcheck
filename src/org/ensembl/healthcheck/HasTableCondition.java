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
 * Check if a schema has a particular table.
 */
public class HasTableCondition extends SchemaMatchCondition {
  
  private String tableName;
  
  /**
   * Creates a new instance of hasTableCondition.
   * @param tableName The name of the table to look for.
   */
  public HasTableCondition(String tableName) {
    
    this.tableName = tableName;
    
  }
 
  /**
   * Check if the schema in question has a table with a name that matches
   * that set in this class' constructor. The comparison is case insensitive.
   */
  public boolean matches(SchemaInfo s) {
    
    boolean result = false;
    
    List tables = s.getTables();
    Iterator it = tables.iterator();
    while (it.hasNext()) {
      String currentTable = (String)it.next();
      result |= currentTable.equalsIgnoreCase(tableName);
    }
    return result;
    
  }
  
} // hasTableCondition
