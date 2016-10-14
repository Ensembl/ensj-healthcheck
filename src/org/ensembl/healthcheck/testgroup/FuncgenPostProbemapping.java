package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;

public class FuncgenPostProbemapping extends GroupOfTests {

  public FuncgenPostProbemapping() {

      addTest(
        org.ensembl.healthcheck.testcase.funcgen.ComparePreviousVersionProbes.class,
        org.ensembl.healthcheck.testcase.funcgen.ComparePreviousVersionArrayXrefs.class,
        org.ensembl.healthcheck.testcase.funcgen.ComparePreviousVersionProbeFeatures.class,
        org.ensembl.healthcheck.testcase.funcgen.ComparePreviousVersionProbeFeaturesByArray.class
      );
  }
}
