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

package org.ensembl.healthcheck.testcase.generic;

import org.ensembl.healthcheck.testcase.*;
import org.ensembl.healthcheck.*;

/**
 * EnsEMBL Healthcheck test case that ensures that the results of the SQL
 * query <code>DESCRIBE external_db</code> are the same for a set of databases.
 */

public class ExternalDBDescribe extends MultiDatabaseTestCase {

	private DatabaseType[] types = { DatabaseType.CORE, DatabaseType.EST, DatabaseType.ESTGENE, DatabaseType.VEGA};
	
  /** Creates a new instance of ExternalDBDescribe */
  public ExternalDBDescribe() {
  	
    addToGroup("pre_release");
    setDescription("Check that the external_db table is the same in all databases.");
    
  }
  
  /**
   * Check that the external_db tables are the same for each matched database.
   * @return Result.
   */
  public boolean run(DatabaseRegistry dbr) {
    
   return checkSQLAcrossSpecies("SELECT * FROM external_db ORDER BY external_db_id", dbr, types);
    
  } // run
  
} // ExternalDBDescribe
