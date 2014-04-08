/*
 * Copyright [1999-2014] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that gencode basic attributes are present
 */

public class GencodeAttributes extends SingleDatabaseTestCase {

  /**
   * Create a new  testcase.
   */
  public GencodeAttributes() {

    addToGroup("post_genebuild");
    addToGroup("pre-compara-handover");
    addToGroup("post-compara-handover");
                addToGroup("post-projection");

    setDescription("Check that Gencode basic geneset attributes have been added");
    setTeamResponsible(Team.GENEBUILD);
  }

  /**
   * Only applies to core dbs.
   */
  public void types() {

    List types = new ArrayList();

    types.add(DatabaseType.CORE);
                types.add(DatabaseType.PRE_SITE);

    setAppliesToTypes(types);

  }

  /**
   * Run the test.
   * 
   * @param dbre
   *            The database to use.
   * @return true if the test passed.
   * 
   */
  public boolean run(DatabaseRegistryEntry dbre) {

    boolean result = true;

    Connection con = dbre.getConnection();

    if (dbre.getSpecies() != Species.HOMO_SAPIENS && dbre.getSpecies() != Species.MUS_MUSCULUS) {
      return result;
    }

    int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM attrib_type at, transcript_attrib ta WHERE at.attrib_type_id=ta.attrib_type_id AND at.code='gencode_basic'");

    if (rows == 0) {
      ReportManager.problem(this, con, "No gencode basic transcript attributes found\n");
      result = false;
    } else {
      ReportManager.correct(this, con, rows + " gencode basic transcript attributes found");
    }

    return result;

  } // run

  // ----------------------------------------------------------------------

} // GencodeAttributes
