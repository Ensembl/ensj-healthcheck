package org.ensembl.healthcheck.testcase.funcgen;

import java.util.Iterator;
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;
import org.ensembl.healthcheck.util.SqlTemplate;

public class SampleRegulatoryFeatureExists extends AbstractTemplatedTestCase {
  
  public SampleRegulatoryFeatureExists() {
    this.setTeamResponsible(Team.FUNCGEN);
  }

  @Override
  protected boolean runTest(DatabaseRegistryEntry dbre) {
    
    SqlTemplate s = getTemplate(dbre);
    
    List<String> regulatoryBuildWithoutSampleRegulatoryFeature = s.queryForDefaultObjectList(
        "select regulatory_build.name from regulatory_build join regulatory_feature on (regulatory_build.sample_regulatory_feature_id=regulatory_feature.regulatory_feature_id and regulatory_build.regulatory_build_id=regulatory_feature.regulatory_build_id) where regulatory_feature.stable_id is null", 
        String.class
     );
    if (regulatoryBuildWithoutSampleRegulatoryFeature.size()==0) {
      return true;
    }
    Iterator<String> i = regulatoryBuildWithoutSampleRegulatoryFeature.iterator();
    while (i.hasNext()) {
      ReportManager.problem(this, dbre.getConnection(), 
          "The regulatory build " + i.next() + " does not have a sample regulatory feature."
      );
    }
    return false;
  }

}
