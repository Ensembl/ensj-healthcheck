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

import java.sql.Connection;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;



import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.Utils;
import org.ensembl.healthcheck.util.SqlTemplate;


/**
 * Check that the gene and transcript biotypes match the valid current ones in the production database.
 */

public class ProductionBiotypes extends SingleDatabaseTestCase {

	/**
	 * Constructor.
	 */
	public ProductionBiotypes() {

		addToGroup("production");
		addToGroup("release");
		addToGroup("pre-compara-handover");
		addToGroup("post-compara-handover");
		
		setDescription("Check that the gene and transcript biotypes match the valid current ones in the production database.");
		setPriority(Priority.AMBER);
		setEffect("Unknown/incorrect biotypes.");
		setTeamResponsible(Team.RELEASE_COORDINATOR);

	}

	/**
	 * This test Does not apply to sanger_vega dbs
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
    String databaseType = dbre.getType().getName(); // will be core, otherfeatures etc
    String[] tables = {"gene", "transcript"};
    Set<String> coreBiotypes = getBiotypesDb(dbre, tables);
    Set<String> productionBiotypes = getBiotypesProduction(dbre, databaseType);
    result &= checkBiotypeExists(dbre, coreBiotypes, productionBiotypes, "production");
    result &= checkGrouping(dbre);
    return result;
  }

  private <T extends CharSequence> boolean checkBiotypeExists(DatabaseRegistryEntry dbre, Collection<T> core, Collection<T> production, String type) {
    Set<T> missing = new HashSet<T>(core);
    missing.removeAll(production);
    if(missing.isEmpty()) {
      ReportManager.correct(this, dbre.getConnection(), "Set of biotypes matches the current valid list in the production database.");
      return true;
    }
    for(CharSequence name: missing) {
      String msg = String.format("The biotype '%s' is missing from %s", name, type);
      ReportManager.problem(this, dbre.getConnection(), msg);
    }
    return false;
  }

  private <T extends CharSequence> boolean checkGrouping(DatabaseRegistryEntry dbre) {
    boolean result = true;
    String databaseType = dbre.getType().getName();
    String[] table = {"gene"};
    Set<String> geneBiotypes = getBiotypesDb(dbre, table); 
    for (String geneBiotype : geneBiotypes) {
      Set<String> transcriptBiotypes = getBiotypesTranscript(dbre, geneBiotype);
      String[] transcripts = transcriptBiotypes.toArray(new String[0]);
      String[] genes = new String[] {geneBiotype};
      Set<String> geneGrouping = getGrouping(dbre, genes, "gene", databaseType);
      Set<String> transcriptGrouping = getGrouping(dbre, transcripts, "transcript", databaseType);

      if (transcriptBiotypes.size() == 1) {
        if (!transcriptBiotypes.contains(geneBiotype)) {
          result = false;
          ReportManager.problem(this, dbre.getConnection(), "Transcript biotype '" + transcriptBiotypes + "' does not match gene biotype '" + geneBiotype + "'");
        }
      } else if (transcriptGrouping.size() == 1) {
        if (!geneGrouping.equals(transcriptGrouping)) {
          result = false;
          ReportManager.problem(this, dbre.getConnection(), "Genes of biotype '" + geneBiotype + "' should not have transcripts of mismatched group '" + transcriptGrouping + "'");
        }
      } else if (geneGrouping.contains("undefined") || geneGrouping.contains("non-coding")) {
        result = false;
        ReportManager.problem(this, dbre.getConnection(), "Genes of biotype '" + geneBiotype + "' should not have transcripts with biotypes in '" + transcriptBiotypes + "'");
      } else if (geneGrouping.contains("pseudogene")) {
        if (transcriptGrouping.contains("coding") || transcriptGrouping.contains("undefined")) {
          result = false;
          ReportManager.problem(this, dbre.getConnection(), "Some genes of biotype '" + geneBiotype + "' have transcripts in '" + transcriptBiotypes + "'");
        }
        List<String> allGenes = getGene(dbre, geneGrouping, databaseType);
        List<String> goodGenes = getGeneWithTranscript(dbre, geneGrouping, databaseType);
        result = checkMissing(dbre, allGenes, goodGenes, geneBiotype);
      } else if (geneGrouping.contains("coding")) {
        List<String> allGenes = getGene(dbre, geneGrouping, databaseType);
        List<String> goodGenes = getGeneWithTranscript(dbre, geneGrouping, databaseType);
        result = checkMissing(dbre, allGenes, goodGenes, geneBiotype);
      } else if (geneGrouping.contains("polymorphic_pseudogene")) {
        List<String> allGenes = getGene(dbre, geneGrouping, databaseType);
        List<String> goodGenes = getGeneWithTranscript(dbre, geneGrouping, databaseType);
        result = checkMissing(dbre, allGenes, goodGenes, geneBiotype);
      }
    }
    return result;
  }


  private boolean checkMissing(DatabaseRegistryEntry dbre, List<String> allGenes, List<String> goodGenes, String biotype) {
    SqlTemplate t = DBUtils.getSqlTemplate(dbre);
    Set<String> missing = new HashSet<String>(allGenes);
    missing.removeAll(goodGenes);
    if(missing.isEmpty()) {
      return true;
    }
    for(CharSequence name: missing) {
      String msg = String.format("Gene '%s' of biotype '%s' has no transcript of same biotype group", name, biotype);
      ReportManager.problem(this, dbre.getConnection(), msg);
    }
    return false;
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

  private Set<String> getBiotypesProduction(DatabaseRegistryEntry dbre, String databaseType) {
    SqlTemplate t = DBUtils.getSqlTemplate(getProductionDatabase());
    String[] tables = { "gene", "transcript" };
    Set<String> results = new HashSet<String>();
    for (String table : tables) {
      String sql = "SELECT name FROM biotype WHERE object_type='" + table + "' AND is_current = 1 AND FIND_IN_SET('" + databaseType + "', db_type) > 0";
      results.addAll(t.queryForDefaultObjectList(sql, String.class));
    }
    return results;
  }

  private Set<String> getGrouping(DatabaseRegistryEntry dbre, String[] biotypes, String table, String databaseType) {
    SqlTemplate t = DBUtils.getSqlTemplate(getProductionDatabase());
    Set<String> results = new HashSet<String>();
    for (String biotype : biotypes) {
      String sql = "SELECT biotype_group FROM biotype WHERE object_type='" + table + "' AND is_current = 1 AND name ='" + biotype + "' AND FIND_IN_SET('" + databaseType + "', db_type) > 0";
      results.addAll(t.queryForDefaultObjectList(sql, String.class));
    }
    return results;
  }

  private Set<String> getBiotypeFromGrouping(DatabaseRegistryEntry dbre, Set<String> biotypeGroup, String table, String databaseType) {
    SqlTemplate t = DBUtils.getSqlTemplate(getProductionDatabase());
    Set<String> results = new HashSet<String>();
    for (String group : biotypeGroup) {
      String sql = "SELECT name FROM biotype WHERE object_type='" + table + "' AND is_current = 1 AND biotype_group ='" + group + "' AND FIND_IN_SET('" + databaseType + "', db_type) > 0";
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

  private boolean checkHasTranscriptBiotype(DatabaseRegistryEntry dbre, Set<String> biotypeGroup, String gene) {
    SqlTemplate t = DBUtils.getSqlTemplate(dbre);
    String databaseType = dbre.getType().getName();
    boolean result = false;
    Set<String> biotypes = getBiotypeFromGrouping(dbre, biotypeGroup, "transcript", databaseType);
    String list = getListBiotypes(biotypes);
    int rows = DBUtils.getRowCount(dbre.getConnection(), "SELECT COUNT(*) FROM transcript t, gene g where g.gene_id = t.gene_id and g.stable_id = '" + gene + "' and t.biotype in (" + list + ")");
    if (rows > 0){
      result = true;
    }
    return result;
  }

  private List<String> getGene(DatabaseRegistryEntry dbre, Set<String> biotypeGroup, String databaseType) {
    SqlTemplate t = DBUtils.getSqlTemplate(dbre);
    Set<String> biotypes = getBiotypeFromGrouping(dbre, biotypeGroup, "gene", databaseType);
    String list = getListBiotypes(biotypes);
    String sql = "SELECT stable_id FROM gene where biotype in (" + list + ")";
    return t.queryForDefaultObjectList(sql, String.class);
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
