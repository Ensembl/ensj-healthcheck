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
import java.util.ArrayList;
import java.util.List;
import java.sql.Statement;
import java.sql.ResultSet;

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that transcripts which need supporting features have them.
 */

public class ImportedDataSets extends SingleDatabaseTestCase {

  List<String> allowedRegexEns = new ArrayList<String>();
  List<String> allowedInEns = new ArrayList<String>();

  /**
   * Create a new ImportedDataSets testcase.
   */
  public ImportedDataSets() {

    setDescription("Check that none of the imported set have an ENS stable id and that RefSeq import has XMs and NMs.");
    setPriority(Priority.AMBER);
    setTeamResponsible(Team.GENEBUILD);

    allowedRegexEns.add("%est");
    allowedRegexEns.add("%cdna");
    allowedRegexEns.add("%ensembl_protein");

    allowedInEns.add("estgene");

  }

  public void types() {

    setAppliesToType(DatabaseType.OTHERFEATURES);

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

    Connection con = dbre.getConnection();

    // list of analysis logic_names which are allowed to have ENS stable ids
    String allowedIn = "'" + StringUtils.join(allowedInEns, "','") + "'";
    String allowedRegex = "a.logic_name NOT LIKE '" + StringUtils.join(allowedRegexEns, "' AND a.logic_name NOT LIKE '") + "'";


    String sql = null;
    String[] tables = {"gene", "transcript"};
    String query = "SELECT COUNT(*), a.logic_name FROM analysis a LEFT JOIN %s t ON t.analysis_id = a.analysis_id WHERE %s AND a.logic_name NOT IN (%s) AND t.stable_id LIKE 'ENS%%' GROUP BY a.logic_name";


    for (String table:tables) {
      sql = String.format(query, table, allowedRegex, allowedIn);
      result &= checkStableIds(con, sql);
    }

    // Working on translation
    query = "SELECT COUNT(*), a.logic_name FROM analysis a LEFT JOIN transcript t ON t.analysis_id = a.analysis_id LEFT JOIN translation l ON t.transcript_id = l.transcript_id WHERE %s AND a.logic_name NOT IN (%s) AND l.stable_id LIKE 'ENS%%' GROUP BY a.logic_name";
    sql = String.format(query, allowedRegex, allowedIn);
    result &= checkStableIds(con, sql);

    // Working on exon
    query = "SELECT COUNT(*), a.logic_name FROM analysis a LEFT JOIN transcript t ON t.analysis_id = a.analysis_id LEFT JOIN exon_transcript et ON t.transcript_id = et.transcript_id LEFT JOIN exon e ON e.exon_id = et.exon_id WHERE %s AND a.logic_name NOT IN (%s) AND e.stable_id LIKE 'ENS%%' GROUP BY a.logic_name";
    sql = String.format(query, allowedRegex, allowedIn);
    result &= checkStableIds(con, sql);

    // Checking RefSeq imports, only on transcript and translations
		int count_all = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM analysis a LEFT JOIN transcript t ON t.analysis_id = a.analysis_id WHERE a.logic_name = 'refseq_import'");
    int count = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM analysis a LEFT JOIN transcript t ON t.analysis_id = a.analysis_id WHERE a.logic_name = 'refseq_import' AND t.stable_id REGEXP '[NX][MR]_[0-9]+'");
    if (count > (count_all*.9)) {
      ReportManager.correct(this, con, "More than 90% of your transcripts have a RefSeq accession in 'refseq_import'");
    }
    else {
      ReportManager.problem(this, con, "Only "+ count +" of "+ count_all +" transcripts have a RefSeq accession as stable id in 'refseq_import'");
      result = false;
    }

		count_all = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM analysis a LEFT JOIN transcript t ON t.analysis_id = a.analysis_id LEFT JOIN translation l ON t.transcript_id = l.transcript_id WHERE a.logic_name = 'refseq_import' AND l.transcript_id IS NOT NULL");
    count = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM analysis a LEFT JOIN transcript t ON t.analysis_id = a.analysis_id LEFT JOIN translation l ON t.transcript_id = l.transcript_id WHERE a.logic_name = 'refseq_import' AND l.transcript_id IS NOT NULL AND l.stable_id REGEXP '[NX]P_[0-9]+'");
    if (count == count_all) {
      ReportManager.correct(this, con, "All your translation have a RefSeq accession in 'refseq_import'");
    }
    else {
      ReportManager.problem(this, con, "Only "+ count +" of "+ count_all +" translations have a RefSeq accession as stable id in 'refseq_import'");
      result = false;
    }

    return result;

  } // run

  private boolean checkStableIds (Connection con, String sql) {

    boolean result = true;

    try {
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(sql);

      while (rs.next()) {

        long rows = rs.getLong(1);
        String analysis = rs.getString(2);

        ReportManager.problem(this, con, rows + " stable ids should not have the Ensembl prefix 'ENS' for analysis " + analysis);
        result = false;

      }

      if (result) {
        ReportManager.correct(this, con, "No stable id have the Ensembl prefix 'ENS'");
      }
    } catch (Exception e) {
      result = false;
      e.printStackTrace();
    }

    return result;
  }
} // ImportedDataSets
