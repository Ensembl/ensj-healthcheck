package org.ensembl.healthcheck.testcase.funcgen;

import java.util.List;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;
import org.ensembl.healthcheck.util.SqlTemplate;

public class ProbeSetTranscriptMappingsUnique extends AbstractTemplatedTestCase {
  
  public ProbeSetTranscriptMappingsUnique() {
    setTeamResponsible(Team.FUNCGEN);
  }

  @Override
  protected boolean runTest(DatabaseRegistryEntry dbre) {
    
    SqlTemplate s = getTemplate(dbre);
    
    List<String> probeSetTranscriptMappingsThatAreNotUnique = s.queryForDefaultObjectList(
        "select probe_set_id, stable_id from probe_set_transcript group by probe_set_id, stable_id having count(probe_set_transcript_id)>1", 
        String.class
     );
    if (probeSetTranscriptMappingsThatAreNotUnique.size()==0) {
      return true;
    }

    ReportManager.problem(this, dbre.getConnection(), 
          "There are " + probeSetTranscriptMappingsThatAreNotUnique.size() + " probe set transcript mappings that are not unique."
    );
    return false;
  }

}
