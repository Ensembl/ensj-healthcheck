package org.ensembl.healthcheck.testcase.funcgen;

import java.util.Iterator;
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;
import org.ensembl.healthcheck.util.SqlTemplate;

public class BrokenFeatureSetToFeatureTypeLinks extends AbstractTemplatedTestCase {
  
  public BrokenFeatureSetToFeatureTypeLinks() {
    this.setTeamResponsible(Team.FUNCGEN);
  }

  @Override
  protected boolean runTest(DatabaseRegistryEntry dbre) {
    
    SqlTemplate s = getTemplate(dbre);
    
    List<String> featureSetWithoutFeatureType = s.queryForDefaultObjectList(
        "select feature_set.name from feature_set left join feature_type using (feature_type_id) where feature_type.feature_type_id is null", 
        String.class
     );
    if (featureSetWithoutFeatureType.size()==0) {
      return true;
    }
    Iterator<String> i = featureSetWithoutFeatureType.iterator();
    while (i.hasNext()) {
      ReportManager.problem(this, dbre.getConnection(), 
          "The feature set " + i.next() + " is not linked to a feature type."
      );
    }
    return false;
  }

}
