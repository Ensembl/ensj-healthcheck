package org.ensembl.healthcheck.testcase.funcgen;

import java.util.List;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;
import org.ensembl.healthcheck.util.SqlTemplate;

public class DuplicateProbesFromProbeSets extends AbstractTemplatedTestCase {
  
  public DuplicateProbesFromProbeSets() {
    setTeamResponsible(Team.FUNCGEN);
  }

  @Override
  protected boolean runTest(DatabaseRegistryEntry dbre) {
    
    SqlTemplate s = getTemplate(dbre);
    
    List<String> arraysWithDuplicateProbes = s.queryForDefaultObjectList(
        "select distinct array.name from array join array_chip using (array_id) join probe using (array_chip_id) where array.is_probeset_array = true group by array.name, probe.name, probe.probe_set_id having count(distinct probe.probe_id)>1",
        String.class
     );
    if (arraysWithDuplicateProbes.size()==0) {
      return true;
    }

    ReportManager.problem(this, dbre.getConnection(), 
          "There are " + arraysWithDuplicateProbes.size() + " arrays that have duplicate probes: " + String.join(", ", arraysWithDuplicateProbes)
    );
    return false;
  }

}
