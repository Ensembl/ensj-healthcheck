package org.ensembl.healthcheck.testcase.funcgen;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

public class RedundantAnnotatedFeatureSets extends SingleDatabaseTestCase {
  
  protected final int max_errors_reported = 10;
  public RedundantAnnotatedFeatureSets() {
      setTeamResponsible(Team.FUNCGEN);
  }
  @Override
  public boolean run(DatabaseRegistryEntry dbre) {

    String sql = 
        "select "
            + "  epigenome.display_label, "
            + "    feature_type.name, "
            + "    analysis.logic_name, "
            + "    count(feature_set_id) c "
            + "  from  "
            + "    feature_set "
            + "    join epigenome using (epigenome_id) "
            + "    join feature_type using (feature_type_id) "
            + "    join analysis on (analysis.analysis_id=feature_set.analysis_id) "
            + "  where "
            + "   feature_set.type='annotated' "
            + "  group by "
            + "    feature_type_id, "
            + "    epigenome_id "
            + "  having "
            + "    c > 1 ";

    Connection con = dbre.getConnection();
    int number_of_errors_reported = 0;

    try {
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(sql);
      
      while(rs.next() && number_of_errors_reported<max_errors_reported) {
        
        String epigenomeDisplayLabel = rs.getString("epigenome.display_label");
        String featureTypeName       = rs.getString("feature_type.name");
        String analysisLogicName     = rs.getString("analysis.logic_name");
        int    c                     = rs.getInt("c");
        
        ReportManager.problem(this, con, 
            "The combination of"
            + " epigenome "        + epigenomeDisplayLabel
            + " and feature type " + featureTypeName
            + " and analysis "     + analysisLogicName
            + " has " + c 
            + " annotated feature sets. There should only be one.\n"
            + "Useful SQL:\n"
            + generateUsefulSql(
                epigenomeDisplayLabel, featureTypeName, analysisLogicName
              )
        );
        number_of_errors_reported++;
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
    boolean passes = number_of_errors_reported == 0;
    return passes;
  }
  
  protected String generateUsefulSql(String epigenomeDisplayLabel, String featureTypeName, String analysisLogicName) {
    return
          " select"
        + "  feature_set.*"
        + " from"
        + "  feature_set"
        + "  join feature_type using (feature_type_id)"
        + "  join epigenome using (epigenome_id)"
        + "  join analysis on (analysis.analysis_id=feature_set.analysis_id)"
        + " where"
        + "  feature_set.type='annotated'"
        + "  and epigenome.display_label='" + epigenomeDisplayLabel + "'"
        + "  and feature_type.name='"       + featureTypeName       + "'"
        + "  and logic_name='"              + analysisLogicName     + "'";
  }
  
}
