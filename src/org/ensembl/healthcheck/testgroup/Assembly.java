/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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
 * These are the tests that check integrity of the assembly. The tests are:
 * 
 * <ul>
 *   <li> org.ensembl.healthcheck.testcase.generic.AncestralSequencesExtraChecks </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.AssemblyExceptions </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.AssemblyMapping </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.AssemblyMultipleOverlap </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.AssemblyNameLength </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.AssemblySeqregion </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.BlankCoordSystemVersions </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.DuplicateAssembly </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.GeneCount </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.Karyotype </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.MTCodonTable </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.MultipleComponentAssemblyMapping </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.NonGTACNSequence </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.SeqRegionName </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.SeqRegionsTopLevel </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.SequenceLevel </li>
 * </ul>
 *
 * @author Thibaut Hourlier
 *
 */
public class Assembly extends GroupOfTests {
        
        public Assembly() {

                addTest(
                        org.ensembl.healthcheck.testcase.generic.AncestralSequencesExtraChecks.class,
                        org.ensembl.healthcheck.testcase.generic.AssemblyExceptions.class,
                        org.ensembl.healthcheck.testcase.generic.AssemblyMapping.class,
                        org.ensembl.healthcheck.testcase.generic.AssemblyMultipleOverlap.class,
                        org.ensembl.healthcheck.testcase.generic.AssemblyNameLength.class,
                        org.ensembl.healthcheck.testcase.generic.AssemblySeqregion.class,
                        org.ensembl.healthcheck.testcase.generic.BlankCoordSystemVersions.class,
                        org.ensembl.healthcheck.testcase.generic.DuplicateAssembly.class,
                        org.ensembl.healthcheck.testcase.generic.GeneCount.class,
                        org.ensembl.healthcheck.testcase.generic.Karyotype.class,
                        org.ensembl.healthcheck.testcase.generic.MTCodonTable.class,
                        org.ensembl.healthcheck.testcase.generic.MultipleComponentAssemblyMapping.class,
                        org.ensembl.healthcheck.testcase.generic.NonGTACNSequence.class,
                        org.ensembl.healthcheck.testcase.generic.SeqRegionName.class,
                        org.ensembl.healthcheck.testcase.generic.SeqRegionsTopLevel.class,
                        org.ensembl.healthcheck.testcase.generic.SequenceLevel.class
                );
        }
}
