/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2021] EMBL-European Bioinformatics Institute
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
 * Copyright (C) 2003 EBI, GRL
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
package org.ensembl.healthcheck.testcase.generic;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Set;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.SqlTemplate;


/**
 * Check that the gene and transcript biotypes are consistent
 */

public class BiotypeGroups extends SingleDatabaseTestCase {

	/**
	 * Constructor.
	 */
	public BiotypeGroups() {

		setDescription("Check that the gene and transcript biotypes are consistent.");
		setPriority(Priority.AMBER);
		setEffect("Unknown/incorrect biotypes.");
		setTeamResponsible(Team.GENEBUILD);

	}

	/**
	 * This test Does not apply to sangervega dbs
	 */
	public void types() {
		removeAppliesToType(DatabaseType.SANGER_VEGA);
	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if the test passed.
	 * 
	 */
  public boolean run(DatabaseRegistryEntry dbre) {
    boolean result = true;
   if (dbre.getType() == DatabaseType.CORE) {
      result &= checkGrouping(dbre);
    }
    return result;
  }

  private <T extends CharSequence> boolean checkGrouping(DatabaseRegistryEntry dbre) {
    String databaseType = dbre.getType().getName();
    String[] table = {"gene"};
    Set<String> geneBiotypes = getBiotypesDb(dbre, table);

    List<String> annotatorGenes = new ArrayList<String>();
    if("vega".equals(databaseType) || "sangervega".equals(databaseType)) {
      annotatorGenes = getGenesWithAnnotatorBiotype(dbre);
    }
    ArrayList<String> transcriptErrors = new ArrayList<String>();
    ArrayList<String> biotypeGroupErrors = new ArrayList<String>();
    ArrayList<String> noGroupErrors = new ArrayList<String>();
    ArrayList<String> nonCodingErrors = new ArrayList<String>();
    ArrayList<String> pseudogeneErrors = new ArrayList<String>();

    for (String geneBiotype : geneBiotypes) {
      Set<String> transcriptBiotypes = getBiotypesTranscript(dbre, geneBiotype);
      String[] transcripts = transcriptBiotypes.toArray(new String[0]);
      String[] genes = new String[] {geneBiotype};
      Set<String> geneGrouping = getGrouping(dbre, genes, "gene", databaseType);
      Set<String> transcriptGrouping = getGrouping(dbre, transcripts, "transcript", databaseType);

      if (transcriptBiotypes.size() == 1 && !transcriptBiotypes.contains(geneBiotype) ) {
          transcriptErrors.add("Transcript biotype '" + transcriptBiotypes + "' does not match gene biotype '" + geneBiotype + "'");
      } else if (transcriptGrouping.size() == 1 && !geneGrouping.equals(transcriptGrouping)) {
          biotypeGroupErrors.add("Genes of biotype '" + geneBiotype + "' should not have transcripts of mismatched group '" + transcriptGrouping + "'");
      } else if (geneGrouping.contains("undefined") || geneGrouping.contains("non-coding")) {
        if (!(geneGrouping.contains("undefined") && transcriptGrouping.contains("undefined"))) {
          noGroupErrors.add("Genes of biotype '" + geneBiotype + "' should not have transcripts with biotypes in '" + transcriptBiotypes + "'");
        }
      } else if (geneGrouping.contains("pseudogene")) {
        if (transcriptGrouping.contains("coding") || transcriptGrouping.contains("undefined")) {
          nonCodingErrors.add("Some genes of biotype '" + geneBiotype + "' have transcripts in '" + transcriptBiotypes + "'");
        }
        List<String> allGenes = getGene(dbre, geneBiotype, databaseType);
        List<String> goodGenes = getGeneWithTranscript(dbre, geneGrouping, databaseType);
        goodGenes.addAll(annotatorGenes);
        pseudogeneErrors.addAll( checkMissing(dbre, allGenes, goodGenes, geneBiotype) );
      } else if (geneGrouping.contains("coding")) {
        List<String> allGenes = getGene(dbre, geneBiotype, databaseType);
        List<String> goodGenes = getGeneWithTranscript(dbre, geneGrouping, databaseType);
        goodGenes.addAll(annotatorGenes);
        pseudogeneErrors.addAll( checkMissing(dbre, allGenes, goodGenes, geneBiotype) );
        if (geneBiotype.contains("polymorphic_pseudogene")) {
          allGenes = getGeneP(dbre, "polymorphic_pseudogene", databaseType);
          goodGenes = getGeneWithTranscriptP(dbre, "polymorphic_pseudogene", databaseType);
          goodGenes.addAll(annotatorGenes);
          pseudogeneErrors.addAll( checkMissing(dbre, allGenes, goodGenes, geneBiotype) );
        }
      }
 
    }

    if ( processErrors(dbre, transcriptErrors) && processErrors(dbre,biotypeGroupErrors) 
      && processErrors(dbre, noGroupErrors) && processErrors(dbre,nonCodingErrors) && processErrors(dbre,pseudogeneErrors)) {
      return true;
    } else {
      return false;
    }
  }

  private boolean processErrors(DatabaseRegistryEntry dbre, List<String> errorList) {
    ListIterator<String> errorIt = errorList.listIterator();
    int i = 0;
    Boolean result = true;
    while (errorIt.hasNext()) {
      result = false;
      ReportManager.problem(this,dbre.getConnection(),errorIt.next());
      i++;
      if (i == 10) {
        ReportManager.problem(this,dbre.getConnection(), errorList.size() + " similar errors found in total.");
        break;
      }
    }
    return result;
  }

  private ArrayList<String> checkMissing(DatabaseRegistryEntry dbre, List<String> allGenes, List<String> goodGenes, String biotype) {
    Set<String> missing = new HashSet<String>(allGenes);
    ArrayList<String> unhappyGenes = new ArrayList<String>();
    missing.removeAll(goodGenes);
    for(CharSequence name: missing) {
      unhappyGenes.add(String.format("Gene '%s' of biotype '%s' has no transcript of same biotype group", name, biotype));
    }
    return unhappyGenes;
  }

  private Set<String> getBiotypesDb(DatabaseRegistryEntry dbre, String[] tables) {
    SqlTemplate t = DBUtils.getSqlTemplate(dbre);
    Set<String> results = new HashSet<String>();
    for (String table : tables) {
      String sql = "SELECT DISTINCT(biotype) FROM " + table;
      results.addAll(t.queryForDefaultObjectList(sql, String.class));
    }
    return results;
  }

  private Set<String> getGrouping(DatabaseRegistryEntry dbre, String[] biotypes, String table, String databaseType) {
    SqlTemplate t = DBUtils.getSqlTemplate(getProductionDatabase());
    Set<String> results = new HashSet<String>();
    for (String biotype : biotypes) {
      String sql = "SELECT biotype_group FROM biotype WHERE object_type='" + table + "' AND name ='" + biotype + "' AND FIND_IN_SET('" + databaseType + "', db_type) > 0";
      results.addAll(t.queryForDefaultObjectList(sql, String.class));
    }
    return results;
  }

  private Set<String> getBiotypeFromGrouping(DatabaseRegistryEntry dbre, Set<String> biotypeGroup, String table, String databaseType) {
    SqlTemplate t = DBUtils.getSqlTemplate(getProductionDatabase());
    Set<String> results = new HashSet<String>();
    for (String group : biotypeGroup) {
      String sql = "SELECT name FROM biotype WHERE object_type='" + table + "' AND biotype_group ='" + group + "' AND FIND_IN_SET('" + databaseType + "', db_type) > 0";
      results.addAll(t.queryForDefaultObjectList(sql, String.class));
    }
    return results;
  }

  private Set<String> getBiotypesTranscript(DatabaseRegistryEntry dbre, String geneBiotype) {
    SqlTemplate t = DBUtils.getSqlTemplate(dbre);
    String sql = "SELECT DISTINCT(t.biotype) FROM transcript t, gene g WHERE g.biotype= '" + geneBiotype + "' AND g.gene_id = t.gene_id ";
    List<String> results = t.queryForDefaultObjectList(sql, String.class);
    return new HashSet<String>(results);
  }

//  private boolean checkHasTranscriptBiotype(DatabaseRegistryEntry dbre, Set<String> biotypeGroup, String gene) {
//    SqlTemplate t = DBUtils.getSqlTemplate(dbre);
//    String databaseType = dbre.getType().getName();
//    boolean result = false;
//    Set<String> biotypes = getBiotypeFromGrouping(dbre, biotypeGroup, "transcript", databaseType);
//    String list = getListBiotypes(biotypes);
//    int rows = DBUtils.getRowCount(dbre.getConnection(), "SELECT COUNT(*) FROM transcript t, gene g where g.gene_id = t.gene_id and g.stable_id = '" + gene + "' and t.biotype in (" + list + ")");
//    if (rows > 0){
//      result = true;
//    }
//    return result;
//  }

  private List<String> getGene(DatabaseRegistryEntry dbre, String biotype, String databaseType) {
    SqlTemplate t = DBUtils.getSqlTemplate(dbre);
    String sql = "SELECT stable_id FROM gene where biotype = '" + biotype + "'";
    return t.queryForDefaultObjectList(sql, String.class);
  }

  private List<String> getGeneP(DatabaseRegistryEntry dbre, String biotype, String databaseType) {
    SqlTemplate t = DBUtils.getSqlTemplate(dbre);
    String sql = "SELECT stable_id FROM gene where biotype = '" + biotype + "'";
    return t.queryForDefaultObjectList(sql, String.class);
  }

  private List<String> getGeneWithTranscriptP(DatabaseRegistryEntry dbre, String biotype, String databaseType) {
    SqlTemplate t = DBUtils.getSqlTemplate(dbre);
    String sql = "SELECT g.stable_id from gene g, transcript t where g.gene_id = t.gene_id and t.biotype = '" + biotype + "' group by g.stable_id";
    return t.queryForDefaultObjectList(sql, String.class);
  }

  private List<String> getGenesWithAnnotatorBiotype(DatabaseRegistryEntry dbre) {
    SqlTemplate t = DBUtils.getSqlTemplate(dbre);
    String sql = "select g.stable_id from gene g join gene_attrib using (gene_id) join attrib_type at using (attrib_type_id) where at.code = 'hidden_remark' and value like 'ASB_%' and CONCAT('ASB_',g.biotype) = value";
    return t.queryForDefaultObjectList(sql,String.class);
  }

  private List<String> getGeneWithTranscript(DatabaseRegistryEntry dbre, Set<String> biotypeGroup, String databaseType) {
    SqlTemplate t = DBUtils.getSqlTemplate(dbre);
    Set<String> biotypesT = getBiotypeFromGrouping(dbre, biotypeGroup, "transcript", databaseType);
    Set<String> biotypesG = getBiotypeFromGrouping(dbre, biotypeGroup, "gene", databaseType);
    String listT = getListBiotypes(biotypesT);
    String listG = getListBiotypes(biotypesG);
    String sql = "SELECT g.stable_id from gene g, transcript t where g.gene_id = t.gene_id and g.biotype in (" + listG + ") and t.biotype in (" + listT + ") group by g.stable_id";
    return t.queryForDefaultObjectList(sql, String.class);
  }

  private String getListBiotypes(Set<String> biotypes) {
    StringBuilder list = new StringBuilder();
    for (String type : biotypes) {
      list.append("'");
      list.append(type);
      list.append("',");
    }
    list.append("''");
    return list.toString();
  }

}
