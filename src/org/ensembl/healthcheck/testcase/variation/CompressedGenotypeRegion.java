/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2017] EMBL-European Bioinformatics Institute
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

/*
 * Copyright (C) 2012 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.ensembl.healthcheck.testcase.variation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;


/**
 * Count the number of compressed genotype region entries for the population 1000 Genomes CEU in the current database, by chromosome.
 */

public class CompressedGenotypeRegion extends SingleDatabaseTestCase {
  
	// The name of one of the 1000 Genomes sub-population
  private static String POP_NAME = "1000GENOMES:phase_1_CEU";
	
  // The minimum of genotype entries for a 1000 Genomes sub-population (e.g. CEU) in the compressed_genotype_region table
	// The number correspond to a number slightly lower than the number of entries for the chromosome 22.
  private static final int MIN_GENOTYPE = 70000;
	
  private String msg = "none";
  
  /**
   * Creates a new instance of CompressedGenotypeRegion TestCase
   */
  public CompressedGenotypeRegion() {

    addToGroup("variation-release");

    setDescription("Checks that the compressed_genotype_region table is valid for the 1000 Genomes data");

    setTeamResponsible(Team.VARIATION);

  }  
  
  // ---------------------------------------------------------------------

  /**
   * Store the SQL queries in a Properties object.
   */
  private Properties getSQLQueries() {
    
    // Store all the needed SQL statements in a Properties object
    Properties sqlQueries = new Properties();
    String query;

    // Query getting the id of a population
    query = "SELECT population_id FROM population vs WHERE name = ? LIMIT 1";
    sqlQueries.setProperty("popId", query);    
    
    // Query counting the number of genotypes of a population, by chromosome
    query = "SELECT s.name, COUNT(*) FROM compressed_genotype_region c, seq_region s WHERE s.seq_region_id=c.seq_region_id AND c.sample_id IN (SELECT sample_id FROM sample_population WHERE population_id=?) GROUP BY s.seq_region_id";  
    sqlQueries.setProperty("genotype_region", query);  
    
    return sqlQueries;
  }
  
  // ---------------------------------------------------------------------
  
  /**
   * Check that the variation set data makes sense and has a valid tree structure.
   * 
   * @param dbre
   *          The database to check.
   * @return true if the test passed.
   */
  public boolean run(DatabaseRegistryEntry dbre) {
    
    boolean result = true;
    
    String species = dbre.getSpecies();
    
		if (species.equals(DatabaseRegistryEntry.HOMO_SAPIENS)) {  
      
      Connection con = dbre.getConnection();
      Properties sqlQueries = getSQLQueries();
      
      int count  = 0;
      int pop_id = 0;
			
      try {
        
        PreparedStatement pStmt1 = con.prepareStatement(sqlQueries.getProperty("popId"));
        pop_id = getPopulationId(pStmt1);
        
        
      } catch (Exception e) {
        ReportManager.problem(this, con, "HealthCheck caused an exception: " + e.getMessage());
      }
      
      try {
        
        PreparedStatement pStmt2 = con.prepareStatement(sqlQueries.getProperty("genotype_region"));
        count = countGenotypeByRegion(pStmt2,pop_id);
        
        
      } catch (Exception e) {
        ReportManager.problem(this, con, "HealthCheck caused an exception: " + e.getMessage());
      }
      
      if (count > 0) {
        result = false;
        ReportManager.problem(this, con, "There are " + String.valueOf(count)
            + " region(s) ("+msg+") with a low number of genotypes for the 1000 Genomes population "+POP_NAME+" in the table compressed_genotype_region");
      }    
    }
    return result;
  }
  
  
  // -----------------------------------------------------------------

  private int getPopulationId(PreparedStatement pStmt) throws Exception {
    pStmt.setString(1, POP_NAME);
    ResultSet rs = pStmt.executeQuery();
  
    int pop_id = 0;
    if (rs.next()) {
      pop_id = rs.getInt(1);
    }
      
    return pop_id;
  } // getPopulationId
  
  
  // -----------------------------------------------------------------

  private int countGenotypeByRegion(PreparedStatement pStmt, int pop_id) throws Exception {
    pStmt.setInt(1, pop_id);
    ResultSet rs = pStmt.executeQuery();
   
    int count = 0;
    String region_name;;
    int count_genotype = 0;
    while (rs.next()) {
      region_name = rs.getString(1);
      count_genotype = rs.getInt(2);

      // exclude short sequences from this check
      if (count_genotype < MIN_GENOTYPE && !region_name.equals("MT") && !region_name.matches(".*PATCH")  && !region_name.matches("HSCHR.*")) {  
				count++;
        if (msg.equals("none")) {
          msg=region_name;
        } else {
          msg+=", "+region_name;;
        }
      }
    }
    
    return count;
  } // countGenotypeByRegion
  
}
