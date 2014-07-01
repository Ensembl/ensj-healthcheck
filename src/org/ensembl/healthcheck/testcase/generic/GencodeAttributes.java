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

    int gencodeGenes = DBUtils.getRowCount(con, "SELECT COUNT(distinct gene_id) FROM transcript t, attrib_type at, transcript_attrib ta WHERE t.transcript_id = ta.transcript_id AND at.attrib_type_id=ta.attrib_type_id AND at.code='gencode_basic'");

    int rows = DBUtils.getRowCount(con, "SELECT COUNT(distinct gene_id) FROM transcript WHERE biotype NOT IN ('LRG_gene')");

    if (rows > gencodeGenes) {
      ReportManager.problem(this, con, (rows - gencodeGenes) + " genes do not have any transcripts with the gencode_basic attribute\n");
      result = false;
    } else {
      ReportManager.correct(this, con, rows + " gencode basic transcript attributes found");
    }

    int genes = DBUtils.getRowCount(con, "SELECT COUNT(distinct g.gene_id) FROM gene g, seq_region s, coord_system cs WHERE g.seq_region_id = s.seq_region_id AND " +
                                            "s.coord_system_id = cs.coord_system_id AND cs.name = 'chromosome' AND cs.attrib = 'default_version' AND s.name NOT LIKE 'LRG%' " +
                                            "AND s.name != 'MT' AND s.seq_region_id NOT IN (SELECT seq_region_id FROM assembly_exception WHERE exc_type in ('PATCH_NOVEL', 'PATCH_FIX', 'HAP'))");

    int refseqGenes = DBUtils.getRowCount(con, "SELECT COUNT(distinct g.gene_id) FROM gene g, seq_region s, coord_system cs, gene_attrib ga, attrib_type at WHERE g.seq_region_id = s.seq_region_id AND " +
                                            "s.coord_system_id = cs.coord_system_id AND cs.name = 'chromosome' AND cs.attrib = 'default_version' AND s.name NOT LIKE 'LRG%' " +
                                            "AND s.name != 'MT' AND s.seq_region_id NOT IN (SELECT seq_region_id FROM assembly_exception WHERE exc_type in ('PATCH_NOVEL', 'PATCH_FIX', 'HAP')) " +
                                            "AND g.seq_region_id = s.seq_region_id AND ga.gene_id = g.gene_id AND ga.attrib_type_id = at.attrib_type_id AND code = 'refseq_compare'");

    if (genes > refseqGenes) {
      ReportManager.problem(this, con, (genes - refseqGenes) + " genes do not have the refseq_compare attribute");
      result = false;
    } else {
      ReportManager.correct(this, con, refseqGenes + " genes found with refseq_compare attribute");
    }

    return result;

  } // run

  // ----------------------------------------------------------------------

} // GencodeAttributes
