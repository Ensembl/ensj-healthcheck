/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the
 * EMBL-European Bioinformatics Institute
 * Copyright [2016-2020] EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;

/**
 * These are the tests that should be run after the Regulatory Build is
 * finished.
 *
 * @author ilavidas
 */
public class FuncgenPostRegulatoryBuild extends GroupOfTests {

    public FuncgenPostRegulatoryBuild() {

      addTest(
        FuncgenIntegrity.class,
        org.ensembl.healthcheck.testcase.funcgen.ComparePreviousVersionRegulatoryFeatures.class,
        org.ensembl.healthcheck.testcase.funcgen.ComparePreviousVersionRegulatoryFeaturesPerType.class,
        org.ensembl.healthcheck.testcase.funcgen.EpigenomeHasSegmentationFile.class,
        org.ensembl.healthcheck.testcase.funcgen.SegmentationFileHasBigBed.class,
        org.ensembl.healthcheck.testcase.funcgen.RegulatoryFeatureIsActive.class,
        org.ensembl.healthcheck.testcase.funcgen.ComparePreviousVersionRegulatoryBuildEpigenomes.class,
        org.ensembl.healthcheck.testcase.funcgen.CurrentRegulatoryBuildHasEpigenomes.class
//              org.ensembl.healthcheck.testcase.funcgen.RegulatoryMotifFeatures.class,
      );
    }
}
