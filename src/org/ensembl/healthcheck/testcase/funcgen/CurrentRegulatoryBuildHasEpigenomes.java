package org.ensembl.healthcheck.testcase.funcgen;

import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;
import org.ensembl.healthcheck.util.SqlTemplate;

public class CurrentRegulatoryBuildHasEpigenomes extends AbstractTemplatedTestCase {
 
  public CurrentRegulatoryBuildHasEpigenomes() {
    this.setTeamResponsible(Team.FUNCGEN);
  }

  @Override
  protected boolean runTest(DatabaseRegistryEntry dbre) {
    
    SqlTemplate s = getTemplate(dbre);
    
    List<String> dbHasRegulatoryBuild = s.queryForDefaultObjectList(
        "select name from regulatory_build where is_current=true;", 
        String.class
     );
    if (dbHasRegulatoryBuild.size()==0) {
      ReportManager.info(this, dbre.getConnection(),
          "The database has no regulatory build, so the test will be skipped."
      );
      return true;
    }
    
    List<String> epigenomeInRegulatoryBuild = s.queryForDefaultObjectList(
        "select epigenome.name from regulatory_build join regulatory_build_epigenome using (regulatory_build_id) join epigenome using (epigenome_id) where is_current=true;", 
        String.class
     );
    if (epigenomeInRegulatoryBuild.size()==0) {
      ReportManager.problem(this, dbre.getConnection(), 
          "The current regulatory build has no epigenomes according to the regulatory_build_epigenome table."
      );
      return false;
    }
    return true;
  }

}
