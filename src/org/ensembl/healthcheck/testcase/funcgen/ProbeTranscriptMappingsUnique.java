
package org.ensembl.healthcheck.testcase.funcgen;

import java.util.List;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;
import org.ensembl.healthcheck.util.SqlTemplate;

public class ProbeTranscriptMappingsUnique extends AbstractTemplatedTestCase {
  
  public ProbeTranscriptMappingsUnique() {
    setTeamResponsible(Team.FUNCGEN);
  }

  @Override
  protected boolean runTest(DatabaseRegistryEntry dbre) {
    
    SqlTemplate s = getTemplate(dbre);
    
    List<String> probeTranscriptMappingsThatAreNotUnique = s.queryForDefaultObjectList(
        "select probe_id, stable_id from probe_transcript group by probe_id, stable_id having count(probe_transcript_id)>1", 
        String.class
     );
    if (probeTranscriptMappingsThatAreNotUnique.size()==0) {
      return true;
    }

    ReportManager.problem(this, dbre.getConnection(), 
          "There are " + probeTranscriptMappingsThatAreNotUnique.size() + " probe transcript mappings that are not unique."
    );
    return false;
  }

}
