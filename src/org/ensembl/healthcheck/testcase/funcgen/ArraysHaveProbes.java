package org.ensembl.healthcheck.testcase.funcgen;

import java.util.Iterator;
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;
import org.ensembl.healthcheck.util.SqlTemplate;

public class ArraysHaveProbes extends AbstractTemplatedTestCase {
  
  public ArraysHaveProbes() {
    this.setTeamResponsible(Team.FUNCGEN);
  }

  @Override
  protected boolean runTest(DatabaseRegistryEntry dbre) {
    
    SqlTemplate s = getTemplate(dbre);
    
    List<String> arraysWithoutProbes = s.queryForDefaultObjectList(
        "select distinct array.name from array join array_chip using (array_id) left join probe using (array_chip_id) where probe.probe_id is null", 
        String.class
     );
    if (arraysWithoutProbes.size()==0) {
      return true;
    }
    Iterator<String> i = arraysWithoutProbes.iterator();
    while (i.hasNext()) {
      ReportManager.problem(this, dbre.getConnection(), 
          "The array " + i.next() + " has no probes."
      );
    }
    return false;
  }

}
