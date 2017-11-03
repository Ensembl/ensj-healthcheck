package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;

public class FuncgenIntegrity extends GroupOfTests {

  public FuncgenIntegrity() {

      addTest(
        org.ensembl.healthcheck.testcase.funcgen.InconsistentExperimentIds.class,
        org.ensembl.healthcheck.testcase.funcgen.BrokenFeatureSetToFeatureTypeLinks.class,
        org.ensembl.healthcheck.testcase.funcgen.SampleRegulatoryFeatureExists.class,
        org.ensembl.healthcheck.testcase.funcgen.EpigenomeLinkedToEFO.class
        
      );
  }
}
