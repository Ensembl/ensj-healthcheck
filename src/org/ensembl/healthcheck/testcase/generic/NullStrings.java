/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2020] EMBL-European Bioinformatics Institute
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

package org.ensembl.healthcheck.testcase.generic;

import static java.lang.String.format;
import static org.ensembl.healthcheck.util.CollectionUtils.createHashMap;
import static org.ensembl.healthcheck.util.CollectionUtils.createLinkedHashSet;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
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

		setTeamResponsible(Team.GENEBUILD);
		setDescription("Check for rows that contain the *string* NULL - should probably be the database primitive NULL.");

	}

	public void types() {
		addAppliesToType(DatabaseType.FUNCGEN);
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
      List<String[]> columnsAndTypes = DBUtils.getTableInfo(con, table, new String[]{"varchar", "text"});
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
