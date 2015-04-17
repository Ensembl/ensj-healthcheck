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
 * These are the tests that are required to guarantee integrity of the geneset. The tests are:
 * 
 * <ul>
 *   <li> org.ensembl.healthcheck.testcase.generic.AnalysisTypes </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.AnalysisXrefs </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.BigGeneExon </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.CanonicalTranscriptCoding </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.CoreForeignKeys </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.DuplicateAttributes </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.DuplicateExons </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.DuplicateGenes </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ExonRank </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ExonStrandOrder </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ExonSupportingFeatures </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ExonTranscriptStartEnd </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.FeatureAnalysis </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.FeatureCoords </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.GeneCoordSystem </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.GeneTranscriptStartEnd </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.HitNameFormat </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.IsCurrent </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.Meta </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.MetaCoord </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.MetaValues </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.NullStrings </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.NullTranscripts </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.PredictionTranscriptHasExons </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.Pseudogene </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.RepeatConsensus </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.RepeatFeature </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.Retrotransposed </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.SourceTypes </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.SpeciesID </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.StableID </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.Strand </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.TranscriptSupportingFeatures </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.TranslationStartEnd </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.TranslationStartEndExon </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.UTR </li>
 * </ul>
 *
 * @author Thibaut Hourlier
 *
 */
public class Geneset extends GroupOfTests {
        
        public Geneset() {

                addTest(
                        org.ensembl.healthcheck.testcase.generic.AnalysisTypes.class,
                        org.ensembl.healthcheck.testcase.generic.AnalysisXrefs.class,
                        org.ensembl.healthcheck.testcase.generic.BigGeneExon.class,
                        org.ensembl.healthcheck.testcase.generic.CanonicalTranscriptCoding.class,
                        org.ensembl.healthcheck.testcase.generic.CoreForeignKeys.class,
                        org.ensembl.healthcheck.testcase.generic.DuplicateAttributes.class,
                        org.ensembl.healthcheck.testcase.generic.DuplicateExons.class,
                        org.ensembl.healthcheck.testcase.generic.DuplicateGenes.class,
                        org.ensembl.healthcheck.testcase.generic.ExonRank.class,
                        org.ensembl.healthcheck.testcase.generic.ExonStrandOrder.class,
                        org.ensembl.healthcheck.testcase.generic.ExonSupportingFeatures.class,
                        org.ensembl.healthcheck.testcase.generic.ExonTranscriptStartEnd.class,
                        org.ensembl.healthcheck.testcase.generic.FeatureAnalysis.class,
                        org.ensembl.healthcheck.testcase.generic.FeatureCoords.class,
                        org.ensembl.healthcheck.testcase.generic.GeneCoordSystem.class,
                        org.ensembl.healthcheck.testcase.generic.GeneTranscriptStartEnd.class,
                        org.ensembl.healthcheck.testcase.generic.HitNameFormat.class,
                        org.ensembl.healthcheck.testcase.generic.IsCurrent.class,
                        org.ensembl.healthcheck.testcase.generic.Meta.class,
                        org.ensembl.healthcheck.testcase.generic.MetaCoord.class,
                        org.ensembl.healthcheck.testcase.generic.MetaValues.class,
                        org.ensembl.healthcheck.testcase.generic.NullStrings.class,
                        org.ensembl.healthcheck.testcase.generic.NullTranscripts.class,
                        org.ensembl.healthcheck.testcase.generic.PredictionTranscriptHasExons.class,
                        org.ensembl.healthcheck.testcase.generic.Pseudogene.class,
                        org.ensembl.healthcheck.testcase.generic.RepeatConsensus.class,
                        org.ensembl.healthcheck.testcase.generic.RepeatFeature.class,
                        org.ensembl.healthcheck.testcase.generic.Retrotransposed.class,
                        org.ensembl.healthcheck.testcase.generic.SourceTypes.class,
                        org.ensembl.healthcheck.testcase.generic.SpeciesID.class,
                        org.ensembl.healthcheck.testcase.generic.StableID.class,
                        org.ensembl.healthcheck.testcase.generic.Strand.class,
                        org.ensembl.healthcheck.testcase.generic.TranscriptSupportingFeatures.class,
                        org.ensembl.healthcheck.testcase.generic.TranslationStartEnd.class,
                        org.ensembl.healthcheck.testcase.generic.TranslationStartEndExon.class,
                        org.ensembl.healthcheck.testcase.generic.UTR.class
                );
        }
}
