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
 * Hold information about database schemas.
 */
public class SchemaManager {
  
  private static List schemas = new ArrayList();
  
  public static List getAllSchemas() {
    
   return schemas;
   
  }
  
  public static void addSchema(SchemaInfo si) {
  
    schemas.add(si);
    
  }
  
  public static void removeSchema(SchemaInfo si) {
    
    schemas.remove(si);
    
  }
  
  public static boolean hasSchema(SchemaInfo si) {
   
    return schemas.contains(si);
    
  }
  
  // -------------------------------------------------------------------------
  /**
   * Get a schema from the cache by name.
   * @param The name of the schema to return.
   * @return The schema named name, or null
   */
  public static SchemaInfo getSchema(String name) {
    
    SchemaInfo result = null;
    
    Iterator it = schemas.iterator();
    while (it.hasNext()) {
      SchemaInfo si = (SchemaInfo)it.next();
      if (si.getName().equalsIgnoreCase(name)) {
        result = si;
        break;
      }
    }
    
    return result;
    
  }
  
  // -------------------------------------------------------------------------
  /** 
   * Get a schema that corresponds to the Connection. Note that this method
   * retrieves the schema from the cache, it does <em>not</em> create it - see 
   * the SchemaInfo(Connection con) constructor for that.
   * @param con The connection relating to the schema required.
   * @return The SchemaInfo object relating to con, or null if none is found.
   */
  public static SchemaInfo getSchema(Connection con) {
    
    return getSchema(DBUtils.getShortDatabaseName(con));
    
  }
  
  // -------------------------------------------------------------------------

} // SchemaManager
