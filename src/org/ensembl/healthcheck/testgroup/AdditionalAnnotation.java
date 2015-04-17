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
 * These are the tests that check additional annotations not essential to the geneset. The tests are:
 * 
 * <ul>
 *   <li> org.ensembl.healthcheck.testcase.generic.Accession </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.AlignFeatureExternalDB </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.AnalysisDescription </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.AnalysisLogicName </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.Archive </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.BlankEnums </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.BlanksInsteadOfNulls </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionDensityFeatures </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.DataFiles </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.DensityFeatures </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.Ditag </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.DNAEmpty </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.EmptyTables </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ESTStableID </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.FrameshiftAttributes </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.GencodeAttributes </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.MappingSession </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.MarkerFeatures </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ProteinFeatures </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ProteinFeatureTranslation </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.SeqRegionAttribsPresent </li> 
 * </ul>
 *
 * @author Thomas Maurel
 *
 */
public class AdditionalAnnotation extends GroupOfTests {
        
        public AdditionalAnnotation() {

                addTest(
                        org.ensembl.healthcheck.testcase.generic.Accession.class,
                        org.ensembl.healthcheck.testcase.generic.AlignFeatureExternalDB.class,
                        org.ensembl.healthcheck.testcase.generic.AnalysisDescription.class,
                        org.ensembl.healthcheck.testcase.generic.AnalysisLogicName.class,
                        org.ensembl.healthcheck.testcase.generic.Archive.class,
                        org.ensembl.healthcheck.testcase.generic.BlankEnums.class,
                        org.ensembl.healthcheck.testcase.generic.BlanksInsteadOfNulls.class,
                        org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionDensityFeatures.class,
                        org.ensembl.healthcheck.testcase.generic.DataFiles.class,
                        org.ensembl.healthcheck.testcase.generic.DensityFeatures.class,
                        org.ensembl.healthcheck.testcase.generic.Ditag.class,
                        org.ensembl.healthcheck.testcase.generic.DNAEmpty.class,
                        org.ensembl.healthcheck.testcase.generic.EmptyTables.class,
                        org.ensembl.healthcheck.testcase.generic.ESTStableID.class,
                        org.ensembl.healthcheck.testcase.generic.FrameshiftAttributes.class,
                        org.ensembl.healthcheck.testcase.generic.GencodeAttributes.class,
                        org.ensembl.healthcheck.testcase.generic.MappingSession.class,
                        org.ensembl.healthcheck.testcase.generic.MarkerFeatures.class,
                        org.ensembl.healthcheck.testcase.generic.ProteinFeatures.class,
                        org.ensembl.healthcheck.testcase.generic.ProteinFeatureTranslation.class,
                        org.ensembl.healthcheck.testcase.generic.SeqRegionAttribsPresent.class
                );
        }
}
