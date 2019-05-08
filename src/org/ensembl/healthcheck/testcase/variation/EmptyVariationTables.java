/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
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
 * Copyright (C) 2004 EBI, GRL
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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that all tables have data.
 */
public class EmptyVariationTables extends SingleDatabaseTestCase {

  /**
   * Creates a new instance of EmptyVariationTablesTestCase
   */
  public EmptyVariationTables() {


    setDescription("Checks that all tables have data");
    setTeamResponsible(Team.VARIATION);

  }

  // ---------------------------------------------------------------------

  /**
   * Define what tables are to be checked.
   */
  private String[] getTablesToCheck(final DatabaseRegistryEntry dbre) {

    String[] tables = getTableNames(dbre.getConnection());
    Species species = dbre.getSpecies();

    String[] unusedTables           = { "coord_system", "strain_gtype_poly" };
    String[] humanOnlyTables        = { "protein_function_predictions", "phenotype", "associate_study", "translation_md5" };
    String[] svTables               = { "study", "structural_variation", "structural_variation_feature", "structural_variation_association", "structural_variation_sample", "variation_set_structural_variation", "failed_structural_variation" };
    String[] sampleTables           = { "population_genotype", "population_structure", "population_synonym", "individual_synonym", "sample", "individual",  };
    String[] setTables              = { "variation_set_structure" };
    String[] genotypeTables         = { "compressed_genotype_region", "compressed_genotype_var" };
    String[] regulatoryTables       = { "motif_feature_variation", "regulatory_feature_variation", "display_group" };
    String[] citationTables         = { "publication", "variation_citation" };
    String[] largeTables            = { "MTMP_transcript_variation", "transcript_variation" };

    // first drop the unused tables

    tables = remove(tables, unusedTables);

    // then human specific ones unless we're running on human

    if (species != Species.HOMO_SAPIENS) {
      tables = remove(tables, humanOnlyTables);
      tables = remove(tables, setTables);
    }

    // Exclude large tables as they throw a Java MySQLDataException
    if (species == Species.HOMO_SAPIENS) {
      tables = remove(tables, largeTables);
    }

    // only these species have structural variation data

    if (species != Species.HOMO_SAPIENS && species != Species.MUS_MUSCULUS && species != Species.BOS_TAURUS && species != Species.EQUUS_CABALLUS && species != Species.MACACA_MULATTA && species != Species.DANIO_RERIO && species != Species.OVIS_ARIES) {
      tables = remove(tables, svTables);
    }
        
    // only these species do not have sample data
  
    if (species == Species.ANOPHELES_GAMBIAE || species == Species.ORNITHORHYNCHUS_ANATINUS || species == Species.PONGO_ABELII || species == Species.TETRAODON_NIGROVIRIDIS) {
      tables = remove(tables, sampleTables);
    }

    // only these species have regulatory data

    if (species != Species.HOMO_SAPIENS && species != Species.MUS_MUSCULUS) {
      tables = remove(tables, regulatoryTables);
    }
    
    // only these species have citation data

    if (species != Species.HOMO_SAPIENS && species != Species.MUS_MUSCULUS  && species != Species.BOS_TAURUS && species != Species.RATTUS_NORVEGICUS && species != Species.CANIS_FAMILIARIS && species != Species.GALLUS_GALLUS && species != Species.SUS_SCROFA && species != Species.OVIS_ARIES ) {
      tables = remove(tables, citationTables);
    }
    return tables;
  }

  // ---------------------------------------------------------------------

  /**
   * Check that every table has more than 0 rows.
   * 
   * @param dbre
   *          The database to check.
   * @return true if the test passed.
   */
  public boolean run(DatabaseRegistryEntry dbre) {

    boolean result = true;

    String[] tables = getTablesToCheck(dbre);
    Connection con = dbre.getConnection();

    for (int i = 0; i < tables.length; i++) {

      String table = tables[i];
      // logger.finest("Checking that " + table + " has rows");

      if (!tableHasRows(con, table)) {

        ReportManager.problem(this, con, table + " has zero rows");
        result = false;

      }
    }

    if (result) {
      ReportManager.correct(this, con, "All required tables have data");
    }

    return result;

  } // run

  // -----------------------------------------------------------------

  private String[] remove(final String[] tables, final String table) {

    String[] result = new String[tables.length - 1];
    int j = 0;
    for (int i = 0; i < tables.length; i++) {
      if (!tables[i].equalsIgnoreCase(table)) {
        if (j < result.length) {
          result[j++] = tables[i];
        } else {
          logger.severe("Cannot remove " + table + " since it's not in the list!");
        }
      }
    }

    return result;

  }

  // -----------------------------------------------------------------

  private String[] remove(final String[] src, final String[] tablesToRemove) {

    String[] result = src;

    for (int i = 0; i < tablesToRemove.length; i++) {
      result = remove(result, tablesToRemove[i]);
    }

    return result;

  }

  // -----------------------------------------------------------------

  /**
   * This only applies to variation databases.
   */
  public void types() {

    removeAppliesToType(DatabaseType.OTHERFEATURES);
    removeAppliesToType(DatabaseType.CDNA);
    removeAppliesToType(DatabaseType.CORE);
    removeAppliesToType(DatabaseType.VEGA);

  }

} // EmptyVariationTablesTestCase
