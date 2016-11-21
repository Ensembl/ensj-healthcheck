package org.ensembl.healthcheck.testcase.funcgen;

import java.util.Iterator;
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;
import org.ensembl.healthcheck.util.SqlTemplate;

public class StableIDsUnique extends AbstractTemplatedTestCase {
  
  final int max_number_problems_reported = 10;
  
  public StableIDsUnique() {
    setTeamResponsible(Team.FUNCGEN);
  }

  @Override
  protected boolean runTest(DatabaseRegistryEntry dbre) {
    
    SqlTemplate s = getTemplate(dbre);
    
    List<String> nonUniqueStableIds = s.queryForDefaultObjectList(
        "select stable_id from regulatory_feature group by regulatory_build_id, stable_id having count(regulatory_feature_id)!=1", 
        String.class
     );
    if (nonUniqueStableIds.size()==0) {
      return true;
    }
    Iterator<String> i = nonUniqueStableIds.iterator();
    
    int number_problems_reported = 0;
    
    while (i.hasNext() && number_problems_reported < max_number_problems_reported) {
      ReportManager.problem(this, dbre.getConnection(), 
          "The stable id " + i.next() + " is not unique."
      );
      number_problems_reported++;
    }
    return false;
  }

}
