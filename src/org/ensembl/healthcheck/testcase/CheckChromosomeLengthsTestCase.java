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

package org.ensembl.healthcheck.testcase;

import java.sql.*;

import org.ensembl.healthcheck.*;

import org.ensembl.healthcheck.util.*;

/**
 * Checks the metadata table to make sure it is OK.
 */
public class CheckChromosomeLengthsTestCase extends EnsTestCase {
  
  /**
   * Creates a new instance of CheckChromosomeLengthsTestCase
   */
  public CheckChromosomeLengthsTestCase() {
    addToGroup("db_constraints");
    setDescription("Check that the chromosome lengths from the chromosome table agree with both the assembly table and the karyotype table.");
  }
  
  /**
   * Check various aspects of the meta table.
   */
  public TestResult run() {
    
    boolean result = true;
    
    DatabaseConnectionIterator it = getDatabaseConnectionIterator();
    
    while (it.hasNext()) {
      
      Connection con = (Connection)it.next();
      
      String dbName = DBUtils.getShortDatabaseName(con);
      
      // check chromosome table has > 0 rows
      int rows = countRowsInTable(con, "chromosome");
      if (rows == 0) {
        result = false;
        ReportManager.problem(this, con, "chromosome table is empty");
      } else {
        ReportManager.correct(this, con, "chromosome table has data");
      }
      
      AssemblyNameInfo assembly = new AssemblyNameInfo(con);
      String defaultAssembly = assembly.getMetaTableAssemblyDefault();
      logger.finest("assembly.default from meta table: " + defaultAssembly);

      // ---------------------------------------------------
      // Find any chromosomes that have different lengths
      // in chromosome & assembly, for the default assembly.
      // NB chromosome length should always be equal to (or
      // possibly greater than) the maximum assembly length
      // The SQL returns failures
      // ----------------------------------------------------
      String sql =  "SELECT chr.name, chr.length FROM chromosome chr, assembly ass WHERE ass.type=\"" + defaultAssembly + "\" AND chr.chromosome_id=ass.chromosome_id GROUP BY ass.chromosome_id HAVING chr.length < MAX(ass.chr_end) ";

      String[] chrs = getColumnValues(con, sql);
      // TO DO: report the bad chrs at high log level

      if (chrs.length > 0){
	result = false;
	ReportManager.problem(this, con, "chromosome lengths are shorter in the chromosome table than in the assembly table");
      } else {
	ReportManager.correct(this, con, "chromosome lengths are equal or greater in the chromosome table compared to the assembly table");
      }

      // --------------------------------------------------
      // Find any chromosomes that have different lengths
      // in karyotype & chromosome tables.
      // The chr.length and karyotype.length should always 
      // be the same.
      // The SQL returns failures
      // --------------------------------------------------
      String karsql =  "SELECT chr.name, chr.length FROM chromosome chr, karyotype kar WHERE chr.chromosome_id=kar.chromosome_id GROUP BY kar.chromosome_id HAVING chr.length <> MAX(kar.chr_end) ";

      String[] kars = getColumnValues(con, karsql);
      // TO DO: report the bad chrs at high log level
      
      if (kars.length > 0){
	result = false;
	ReportManager.problem(this, con, "chromosome lengths differ between karyotype and chromosome tables");
      } else {
	ReportManager.correct(this, con, "chromosome lengths are the same in karyotype and chromosome tables");
      }

      // -------------------------------------------
    } // while connection
      // ------------------------------------------
    
    return new TestResult(getShortTestName(), result);
    
  } // run
  
} // CheckChromosomeLengthsTestCase
