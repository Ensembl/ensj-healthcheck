/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2017] EMBL-European Bioinformatics Institute
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
 * These are the tests required for a pre-site database. The tests are:
 * 
 * <ul>
 *   <li> org.ensembl.healthcheck.testcase.generic.ProductionAnalysisLogicName </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ProductionBiotypes </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ProductionMasterTables </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ProductionMeta </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.Meta </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.CoreForeignKeys </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.AnalysisDescription </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.AnalysisLogicName </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.AnalysisTypes </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.SourceTypes </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.AlignFeatureExternalDB </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.BlankEnums </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.TranslationStartEndExon </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.Strand </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.MTCodonTable </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.AssemblyMultipleOverlap </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.AssemblyNameLength </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.AssemblyMapping </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.MarkerFeatures </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.HitNameFormat </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.Retrotransposed </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.Pseudogene </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.DescriptionNewlines </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.AssemblyException </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.AssemblySeqregion </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.MultipleCompMetaCoordMapping </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ExonTranscriptStartEnd </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.NullTranscripts </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.BlankCoordSystemVersions </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.FrameshiftAttributes </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.AttribValues </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ExonRank </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ExonStrandOrder </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.BlanksInsteadOfNulls </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.SchemaType </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.SeqRegionsTopLevel </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.SeqRegionName </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.EmptyTables </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.MetaCoord </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.GeneTranscriptStartEnd </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.GeneCoordSystem </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.GeneCount </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.IsCurrent </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.Karyotype </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.SeqRegionCoordSystem </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.RepeatFeature </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.FeatureAnalysis </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.Ditag </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.FeatureCoords </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.TranslationStartEnd </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.RepeatConsensus </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.DuplicateAttributes </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.DuplicateAssembly </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.DisplayLabels </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.NullStrings </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.SpeciesID </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.TranscriptSupportingFeatures </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.SequenceLevel </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.NonGTACNSequence </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.BigGeneExon </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.TranscriptName </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.SingleDBCollations </li> 
 * </ul>
 *
 * @author Autogenerated
 *
 */
public class PreSite extends GroupOfTests {
	
	public PreSite() {

		addTest(
                        org.ensembl.healthcheck.testcase.generic.ProductionAnalysisLogicName.class,
                        org.ensembl.healthcheck.testcase.generic.ProductionBiotypes.class,
                        org.ensembl.healthcheck.testcase.generic.BiotypeGroups.class,
                        org.ensembl.healthcheck.testcase.generic.ProductionMasterTables.class,
                        org.ensembl.healthcheck.testcase.generic.ProductionMeta.class,
                        org.ensembl.healthcheck.testcase.generic.Meta.class,
			org.ensembl.healthcheck.testcase.generic.CoreForeignKeys.class,
                        org.ensembl.healthcheck.testcase.generic.AnalysisDescription.class,
                        org.ensembl.healthcheck.testcase.generic.AnalysisLogicName.class,
                        org.ensembl.healthcheck.testcase.generic.AnalysisTypes.class,
                        org.ensembl.healthcheck.testcase.generic.SourceTypes.class,
			org.ensembl.healthcheck.testcase.generic.AlignFeatureExternalDB.class,
			org.ensembl.healthcheck.testcase.generic.BlankEnums.class,
			org.ensembl.healthcheck.testcase.generic.TranslationStartEndExon.class,
			org.ensembl.healthcheck.testcase.generic.Strand.class,
			org.ensembl.healthcheck.testcase.generic.MTCodonTable.class,
			//org.ensembl.healthcheck.testcase.generic.AssemblyMultipleOverlap.class,
                        org.ensembl.healthcheck.testcase.generic.AssemblyNameLength.class,
                        org.ensembl.healthcheck.testcase.generic.AssemblyMapping.class,
			org.ensembl.healthcheck.testcase.generic.MarkerFeatures.class,
			org.ensembl.healthcheck.testcase.generic.HitNameFormat.class,
			org.ensembl.healthcheck.testcase.generic.Retrotransposed.class,
			org.ensembl.healthcheck.testcase.generic.Pseudogene.class,
			org.ensembl.healthcheck.testcase.generic.DescriptionNewlines.class,
			org.ensembl.healthcheck.testcase.generic.AssemblyExceptionTableSeqRegionAttribute.class,
			org.ensembl.healthcheck.testcase.generic.AssemblyExceptionTableIntegrity.class,
			org.ensembl.healthcheck.testcase.generic.AssemblyExceptionTableSeqMapping.class,
			org.ensembl.healthcheck.testcase.generic.AssemblyExceptionTableUniqueRegion.class,
			org.ensembl.healthcheck.testcase.generic.AssemblyExceptionTableStartEnd.class,
			org.ensembl.healthcheck.testcase.generic.AssemblySeqregion.class,
                        org.ensembl.healthcheck.testcase.generic.MultipleComponentAssemblyMapping.class,
			org.ensembl.healthcheck.testcase.generic.ExonTranscriptStartEnd.class,
			org.ensembl.healthcheck.testcase.generic.NullTranscripts.class,
			org.ensembl.healthcheck.testcase.generic.BlankCoordSystemVersions.class,
			org.ensembl.healthcheck.testcase.generic.FrameshiftAttributes.class,
                        org.ensembl.healthcheck.testcase.generic.AttribValues.class,
			org.ensembl.healthcheck.testcase.generic.ExonRank.class,
			org.ensembl.healthcheck.testcase.generic.ExonStrandOrder.class,
			org.ensembl.healthcheck.testcase.generic.BlanksInsteadOfNulls.class,
                        org.ensembl.healthcheck.testcase.generic.SchemaType.class,
			org.ensembl.healthcheck.testcase.generic.SeqRegionsTopLevel.class,
                        org.ensembl.healthcheck.testcase.generic.SeqRegionName.class,
                        org.ensembl.healthcheck.testcase.generic.EmptyTables.class,
                        org.ensembl.healthcheck.testcase.generic.MetaCoord.class,
			org.ensembl.healthcheck.testcase.generic.GeneTranscriptStartEnd.class,
                        org.ensembl.healthcheck.testcase.generic.GeneCoordSystem.class,
                        org.ensembl.healthcheck.testcase.generic.GeneCount.class,
                        org.ensembl.healthcheck.testcase.generic.IsCurrent.class,
			org.ensembl.healthcheck.testcase.generic.Karyotype.class,
			org.ensembl.healthcheck.testcase.generic.SeqRegionCoordSystem.class,
			org.ensembl.healthcheck.testcase.generic.RepeatFeature.class,
			org.ensembl.healthcheck.testcase.generic.FeatureAnalysis.class,
			org.ensembl.healthcheck.testcase.generic.Ditag.class,
			org.ensembl.healthcheck.testcase.generic.FeatureCoords.class,
			org.ensembl.healthcheck.testcase.generic.TranslationStartEnd.class,
			org.ensembl.healthcheck.testcase.generic.RepeatConsensus.class,
			org.ensembl.healthcheck.testcase.generic.DuplicateAttributes.class,
			org.ensembl.healthcheck.testcase.generic.DuplicateAssembly.class,
			org.ensembl.healthcheck.testcase.generic.DisplayLabels.class,
			org.ensembl.healthcheck.testcase.generic.NullStrings.class,
                        org.ensembl.healthcheck.testcase.generic.SpeciesID.class,
                        org.ensembl.healthcheck.testcase.generic.TranscriptSupportingFeatures.class,
                        org.ensembl.healthcheck.testcase.generic.SequenceLevel.class,
                        org.ensembl.healthcheck.testcase.generic.NonGTACNSequence.class,
			org.ensembl.healthcheck.testcase.generic.BigGeneExon.class,
                        org.ensembl.healthcheck.testcase.generic.TranscriptNames.class,
                        org.ensembl.healthcheck.testcase.generic.SingleDBCollations.class
		);
	}
}
