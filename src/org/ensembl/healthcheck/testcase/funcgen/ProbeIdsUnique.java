package org.ensembl.healthcheck.testcase.funcgen;

import java.util.List;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;
import org.ensembl.healthcheck.util.SqlTemplate;

public class ProbeIdsUnique extends AbstractTemplatedTestCase {
  
  public ProbeIdsUnique() {
    setTeamResponsible(Team.FUNCGEN);
  }

  @Override
  protected boolean runTest(DatabaseRegistryEntry dbre) {
    
    SqlTemplate s = getTemplate(dbre);
    
    List<String> probeIdsThatAreNotUnique = s.queryForDefaultObjectList(
        "select probe_id from probe group by probe_id having count(*) > 1", 
        String.class
     );
    if (probeIdsThatAreNotUnique.size()==0) {
      return true;
    }

    ReportManager.problem(this, dbre.getConnection(), 
          "There are " + probeIdsThatAreNotUnique.size() + " probes that don't have a unique probe id."
    );
    return false;
  }

}
