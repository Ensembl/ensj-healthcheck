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
 * These are the tests that should be run after xref projection. The tests are:
 * 
 * <ul>
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionGOXrefs </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionProjectedGeneNames </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionProjectedSynonyms </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ProjectedXrefs </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.VariationDensity </li>
 * </ul>
 *
 * @author Thomas Maurel
 *
 */
public class Projection extends GroupOfTests {
        
        public Projection() {

                addTest(
                        org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionGOXrefs.class,
                        org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionProjectedGeneNames.class,
                        org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionProjectedSynonyms.class,
                        org.ensembl.healthcheck.testcase.generic.ProjectedXrefs.class,
                        org.ensembl.healthcheck.testcase.generic.VariationDensity.class
                );
        }
}
