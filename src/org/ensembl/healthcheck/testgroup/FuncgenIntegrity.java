package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;

public class FuncgenIntegrity extends GroupOfTests {
/*

When changing the tests in the class, please notify: 

ensembl-production@ebi.ac.uk

Dear Production Team,

We have made a change to our FuncgenIntegrity class:

< insert link to github commit here >

Cheers,
< your name >

*/
  public FuncgenIntegrity() {

      addTest(
        org.ensembl.healthcheck.testcase.funcgen.ArraysHaveProbes.class,
        org.ensembl.healthcheck.testcase.funcgen.BrokenFeatureSetToFeatureTypeLinks.class,
        org.ensembl.healthcheck.testcase.funcgen.CompareFuncgenSchema.class,
        org.ensembl.healthcheck.testcase.funcgen.CurrentRegulatoryBuildHasEpigenomes.class,
        org.ensembl.healthcheck.testcase.funcgen.EpigenomeHasSegmentationFile.class,
        org.ensembl.healthcheck.testcase.funcgen.ExternalFeatureFilesExist.class,
        org.ensembl.healthcheck.testcase.funcgen.FeaturePosition.class,
        org.ensembl.healthcheck.testcase.funcgen.FuncgenAnalysisDescription.class,
        org.ensembl.healthcheck.testcase.funcgen.FuncgenForeignKeys.class,
        org.ensembl.healthcheck.testcase.funcgen.FuncgenStableID.class,
        org.ensembl.healthcheck.testcase.funcgen.MetaCoord.class,
        org.ensembl.healthcheck.testcase.funcgen.ProbeIdsUnique.class,
        org.ensembl.healthcheck.testcase.funcgen.ProbeSetTranscriptMappingsUnique.class,
        org.ensembl.healthcheck.testcase.funcgen.ProbeTranscriptMappingsUnique.class,
        org.ensembl.healthcheck.testcase.funcgen.RegulatoryFeatureIsActive.class,
        org.ensembl.healthcheck.testcase.funcgen.SampleRegulatoryFeatureExists.class,
        org.ensembl.healthcheck.testcase.funcgen.SegmentationFileHasBigBed.class,
        org.ensembl.healthcheck.testcase.funcgen.StableIDsUnique.class,
        org.ensembl.healthcheck.testcase.generic.ExternalDBDisplayName.class,
        org.ensembl.healthcheck.testcase.generic.SchemaType.class,
        org.ensembl.healthcheck.testcase.generic.SpeciesID.class
        // org.ensembl.healthcheck.testcase.funcgen.DuplicateProbes.class,
        // org.ensembl.healthcheck.testcase.funcgen.DuplicateProbesFromProbeSets.class,
        // org.ensembl.healthcheck.testcase.funcgen.EpigenomeLinkedToEFO.class
      );
  }
}
