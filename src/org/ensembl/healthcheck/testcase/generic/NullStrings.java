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
package org.ensembl.healthcheck.testcase.generic;

import static java.lang.String.format;
import static org.ensembl.healthcheck.util.CollectionUtils.createHashMap;
import static org.ensembl.healthcheck.util.CollectionUtils.createLinkedHashSet;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check for rows that contain the *string* NULL - should probably be the database primitive NULL.
 */

public class NullStrings extends SingleDatabaseTestCase {

	/**
	 * Create a new NullStrings testcase.
	 */
	public NullStrings() {

		addToGroup("post_genebuild");
		addToGroup("id_mapping");
		addToGroup("release");
		addToGroup("funcgen-release");
		addToGroup("funcgen");
		addToGroup("compara-ancestral");
		addToGroup("pre-compara-handover");
		addToGroup("post-compara-handover");
		
		setTeamResponsible(Team.GENEBUILD);
		setDescription("Check for rows that contain the *string* NULL - should probably be the database primitive NULL.");

	}

	public Map<String,Set<String>> getExclusions() {
    Map<String,Set<String>> map = createHashMap();
    map.put("dnac", createLinkedHashSet("n_line"));
    return map;
  }
	
	public boolean run(DatabaseRegistryEntry dbre) {
    boolean ok = true;
    Connection con = dbre.getConnection();

    Map<String,Set<String>> globalExclusions = getExclusions();
    String[] tables = DBUtils.getTableNames(con);
    
    for (String table: tables) {
      Set<String> exclusions = globalExclusions.get(table.toLowerCase());
      List<String[]> columnsAndTypes = DBUtils.getTableInfo(con, table, "varchar", "text");
      for(String[] columnInfo: columnsAndTypes) {
        String column = columnInfo[0];
        String allowedNull = columnInfo[2];
        
        //If we were told to skip this column then do so
        if(exclusions != null && exclusions.contains(column.toLowerCase())) {
          continue;
        }
        
        //If we didn't allow for NULLs then skip
        if(allowedNull.toUpperCase().equals("NO")) {
          continue;
        }
        
        Object[] sqlArgs = new Object[]{table, column};
        
        String sql = String.format("SELECT COUNT(*) FROM %1$s WHERE %2$s = 'NULL'", sqlArgs);
        int rows = DBUtils.getRowCount(con, sql);
        if (rows > 0) {
          String lb = System.getProperty("line.separator");
          String usefulSql = format("UPDATE %1$s SET %2$s = NULL WHERE %2$s = '' OR %2$s = 'NULL';", sqlArgs);
          Object[] args = new Object[]{rows, table, column, lb, usefulSql};
          String str = format("%d rows in %s.%s have their value set to " +
          		"the String 'NULL', should be the database primative NULL%s" +
          		"   Useful SQL: %s", args);
          ReportManager.problem(this, con, str);
          ok = false;
        }
      }
    }
    
    return ok;
  }

} // NullStrings
