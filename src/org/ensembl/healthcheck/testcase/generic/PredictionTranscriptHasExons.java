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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
* Genscan generates predicted transcripts and predicted exons. 
* We need the predicted transcripts to have predicted exons associated with them.
*/

public class PredictionTranscriptHasExons extends SingleDatabaseTestCase {

  public PredictionTranscriptHasExons() {
    setDescription("Verify that all predicted transcripts have predicted exons.");
    setTeamResponsible(Team.GENEBUILD);
    setSecondTeamResponsible(Team.RELEASE_COORDINATOR);
  }  


  public boolean run(DatabaseRegistryEntry dbre) {
    // Left join forces a null for the exon id if there isn't one
    String sql = "SELECT COUNT(t.prediction_transcript_id) FROM prediction_transcript t LEFT JOIN prediction_exon e ON t.prediction_transcript_id = e.prediction_transcript_id WHERE e.prediction_exon_id is null";
    Boolean pass = false;
    try {
      Connection con = dbre.getConnection();
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(sql);
    
      rs.next();
      int count = rs.getInt(1);
      rs.close();
      stmt.close();
      if (count > 0) {
        ReportManager.problem(this, con, count + " prediction_transcripts have no prediction_exons");
        ReportManager.problem(this, con, "USEFUL SQL: " + sql);      
      } else {
        pass = true;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    

    return pass;
  }


}
