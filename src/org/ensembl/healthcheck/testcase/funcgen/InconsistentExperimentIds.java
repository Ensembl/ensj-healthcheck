package org.ensembl.healthcheck.testcase.funcgen;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * @author mnuhn
 * 
 * See the description in the constructor.
 *
 */
public class InconsistentExperimentIds extends SingleDatabaseTestCase {
  
  protected final int max_errors_reported = 10;
  public InconsistentExperimentIds() {
      setTeamResponsible(Team.FUNCGEN);
      setDescription(
        "Tests for inconsistencies between the experiment id in the result "
        + "set table and the corresponding one in the inpus subset table. "
        + "This tends to happen when the epigenome and feature type of the "
        + "two referenced experiments are identical and only differ in the "
        + "experimental_group."
      );
  }
  @Override
  public boolean run(DatabaseRegistryEntry dbre) {

    String sql = 
        " select" + "\n"
      + "   result_set.name,"+ "\n"
      + "   result_set.experiment_id,"+ "\n"
      + "   result_set_experiment.name,"+ "\n"
      + "   input_subset.name,"+ "\n"
      + "   input_subset.experiment_id,"+ "\n"
      + "   input_subset_experiment.name"+ "\n"
      + " from "+ "\n"
      + "   result_set"+ "\n"
      + "   join experiment result_set_experiment using (experiment_id)"+ "\n"
      + "   join result_set_input using (result_set_id)"+ "\n"
      + "   join input_subset on (input_subset_id=table_id)"+ "\n"
      + "   join experiment input_subset_experiment on (input_subset.experiment_id=input_subset_experiment.experiment_id)"+ "\n"
      + " where"+ "\n"
      + "   input_subset_experiment.is_control=0"+ "\n"
      + "   and result_set.experiment_id != input_subset.experiment_id"+ "\n"
      + ";"
    ;

    Connection con = dbre.getConnection();
    int number_of_errors_reported = 0;

    try {
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(sql);
      
      while(rs.next() && number_of_errors_reported<max_errors_reported) {
        
        String resultSetName              = rs.getString("result_set.name");
        String resultSetExperimentId      = rs.getString("result_set.experiment_id");
        String resultSetExperimentName    = rs.getString("result_set_experiment.name");
        String inputSubsetName            = rs.getString("input_subset.name");
        String inputSubsetExperimentId    = rs.getString("input_subset.experiment_id");
        String inputSubsetExperimentName  = rs.getString("input_subset_experiment.name");
        
        ReportManager.problem(this, con, 
            "\nThe result set "        + resultSetName   + "\n"
            + "and the linked input subset " + inputSubsetName + "\n"
            + "should have the same experiment id."     + "\n"
            + "But they have different ones: ("+resultSetExperimentId+" vs "+inputSubsetExperimentId+")" + "\n"
            + "The result set   is pointing to the experiment " + resultSetExperimentName   + "\n"
            + "The input subset is pointing to the experiment " + inputSubsetExperimentName + "\n"
            + "\n"
            + "Useful SQL: "
            + generateUsefulSql(resultSetExperimentId, inputSubsetExperimentId) + "\n"
        );
        number_of_errors_reported++;
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
    boolean passes = number_of_errors_reported == 0;
    
    if (!passes) {
      ReportManager.problem(this, con, 
        "Find all instances with this problem like this:\n"
        + sql
      );
    }
    
    return passes;
  }
  
  protected String generateUsefulSql(
        String resultSetExperimentId, 
        String inputSubsetExperimentId
      ) {
    return
      "select * from experiment where experiment_id in ("
      + resultSetExperimentId   +", "
      + inputSubsetExperimentId +");";
  }
}
