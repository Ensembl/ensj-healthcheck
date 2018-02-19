package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;

public class FuncgenPostProbemapping extends GroupOfTests {

  public FuncgenPostProbemapping() {

      addTest(
        org.ensembl.healthcheck.testcase.funcgen.ArraysHaveProbes.class,
        org.ensembl.healthcheck.testcase.funcgen.ComparePreviousVersionProbes.class,
        // Whoa, this is slow
//        org.ensembl.healthcheck.testcase.funcgen.ComparePreviousVersionArrayXrefs.class,
        org.ensembl.healthcheck.testcase.funcgen.ComparePreviousVersionProbeFeatures.class,
//        org.ensembl.healthcheck.testcase.funcgen.ComparePreviousVersionProbeFeaturesByArray.class,
        org.ensembl.healthcheck.testcase.funcgen.ComparePreviousVersionTranscriptProbeFeaturesByArray.class,
        org.ensembl.healthcheck.testcase.funcgen.ComparePreviousVersionGenomicProbeFeaturesByArray.class,
        org.ensembl.healthcheck.testcase.funcgen.ComparePreviousVersionGenomicProbeFeaturesByArray.class,
        org.ensembl.healthcheck.testcase.funcgen.ComparePreviousVersionGenomicProbeFeaturesByArrayWithProbeSets.class,
        org.ensembl.healthcheck.testcase.funcgen.ComparePreviousVersionTranscriptProbeFeaturesByArray.class,
        org.ensembl.healthcheck.testcase.funcgen.ComparePreviousVersionTranscriptProbeFeaturesByArrayWithProbeSets.class,
		org.ensembl.healthcheck.testcase.funcgen.ProbeIdsUnique.class,
		org.ensembl.healthcheck.testcase.funcgen.DuplicateProbes.class,
		org.ensembl.healthcheck.testcase.funcgen.DuplicateProbesFromProbeSets.class,
		org.ensembl.healthcheck.testcase.funcgen.ProbeTranscriptMappingsUnique.class,
		org.ensembl.healthcheck.testcase.funcgen.ProbeSetTranscriptMappingsUnique.class
      );
  }
}
