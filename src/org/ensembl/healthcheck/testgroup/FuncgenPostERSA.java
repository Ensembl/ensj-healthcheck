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
import org.ensembl.healthcheck.testcase.funcgen.AlignmentHasBamFile;
import org.ensembl.healthcheck.testcase.funcgen.AlignmentHasBigWigFile;

/**
 * These are the tests that should be run after ERSA pipeline is finished.
 *
 * @author ilavidas
 */
public class FuncgenPostERSA extends GroupOfTests {

    public FuncgenPostERSA() {

        addTest(
          FuncgenIntegrity.class,
          org.ensembl.healthcheck.testcase.funcgen.ReadFileHasBeenAligned.class,
          AlignmentHasBamFile.class,
          AlignmentHasBigWigFile.class,
          org.ensembl.healthcheck.testcase.funcgen.ExperimentHasFeatureSet.class,
          org.ensembl.healthcheck.testcase.funcgen.FeaturePosition.class,
          org.ensembl.healthcheck.testcase.funcgen.StableIDsUnique.class
        );
    }
}
