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
 * These are the tests that register themselves as post_merge. The tests are:
 * 
 * <ul>
 *   <li> org.ensembl.healthcheck.testcase.generic.Accession </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.AnalysisTypes </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.AssemblyExceptions </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.AssemblyMultipleOverlap </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.AssemblySeqregion </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.AutoIncrement </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.BigGeneExon </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.BlankCoordSystemVersions </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.BlankEnums </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.BlankInfoType </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.BlanksInsteadOfNulls </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.CoreForeignKeys </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.DescriptionNewlines </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.DisplayLabels </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.DisplayXref </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.Ditag </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.DNAEmpty </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.DuplicateAssembly </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.DuplicateAttributes </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.DuplicateExons </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.DuplicateGenes </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.EmptyTables </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ESTStableID </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ExonRank </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ExonStrandOrder </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ExonSupportingFeatures </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ExonTranscriptStartEnd </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ExternalSynonymArray </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.FeatureAnalysis </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.FeatureCoords </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.FrameshiftAttributes </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.GeneCoordSystem </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.GeneDescriptions </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.GeneTranscriptStartEnd </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.HitNameFormat </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.IsCurrent </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.MetaCoord </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.MTCodonTable </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.NullStrings </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.NullTranscripts </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.Pseudogene </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.Retrotransposed </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.SeqRegionName </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.SeqRegionsTopLevel </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.SequenceLevel </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.SingleDBCollations </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.SourceTypes </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.Strand </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.TranscriptNames </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.TranscriptsSameName </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.TranscriptSupportingFeatures </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.TranslationStartEnd </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.TranslationStartEndExon </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.UTR </li>
 * </ul>
 *
 * @author Thibaut Hourlier
 *
 */
public class PostMerge extends GroupOfTests {
	
	public PostMerge() {

		addTest(
			org.ensembl.healthcheck.testcase.generic.Accession.class,
			org.ensembl.healthcheck.testcase.generic.AnalysisTypes.class,
			org.ensembl.healthcheck.testcase.generic.AssemblyExceptions.class,
			org.ensembl.healthcheck.testcase.generic.AssemblyMultipleOverlap.class,
			org.ensembl.healthcheck.testcase.generic.AssemblySeqregion.class,
			org.ensembl.healthcheck.testcase.generic.AutoIncrement.class,
			org.ensembl.healthcheck.testcase.generic.BigGeneExon.class,
			org.ensembl.healthcheck.testcase.generic.BlankCoordSystemVersions.class,
			org.ensembl.healthcheck.testcase.generic.BlankEnums.class,
			org.ensembl.healthcheck.testcase.generic.BlankInfoType.class,
			org.ensembl.healthcheck.testcase.generic.BlanksInsteadOfNulls.class,
			org.ensembl.healthcheck.testcase.generic.CoreForeignKeys.class,
			org.ensembl.healthcheck.testcase.generic.DescriptionNewlines.class,
			org.ensembl.healthcheck.testcase.generic.DisplayLabels.class,
			org.ensembl.healthcheck.testcase.generic.DisplayXref.class,
			org.ensembl.healthcheck.testcase.generic.Ditag.class,
			org.ensembl.healthcheck.testcase.generic.DNAEmpty.class,
			org.ensembl.healthcheck.testcase.generic.DuplicateAssembly.class,
			org.ensembl.healthcheck.testcase.generic.DuplicateAttributes.class,
			org.ensembl.healthcheck.testcase.generic.DuplicateExons.class,
			org.ensembl.healthcheck.testcase.generic.DuplicateGenes.class,
			org.ensembl.healthcheck.testcase.generic.EmptyTables.class,
			org.ensembl.healthcheck.testcase.generic.ESTStableID.class,
			org.ensembl.healthcheck.testcase.generic.ExonRank.class,
			org.ensembl.healthcheck.testcase.generic.ExonStrandOrder.class,
			org.ensembl.healthcheck.testcase.generic.ExonSupportingFeatures.class,
			org.ensembl.healthcheck.testcase.generic.ExonTranscriptStartEnd.class,
			org.ensembl.healthcheck.testcase.generic.ExternalSynonymArray.class,
			org.ensembl.healthcheck.testcase.generic.FeatureAnalysis.class,
			org.ensembl.healthcheck.testcase.generic.FeatureCoords.class,
			org.ensembl.healthcheck.testcase.generic.FrameshiftAttributes.class,
			org.ensembl.healthcheck.testcase.generic.GeneCoordSystem.class,
			org.ensembl.healthcheck.testcase.generic.GeneDescriptions.class,
			org.ensembl.healthcheck.testcase.generic.GeneTranscriptStartEnd.class,
			org.ensembl.healthcheck.testcase.generic.HitNameFormat.class,
			org.ensembl.healthcheck.testcase.generic.IsCurrent.class,
			org.ensembl.healthcheck.testcase.generic.MetaCoord.class,
			org.ensembl.healthcheck.testcase.generic.MTCodonTable.class,
			org.ensembl.healthcheck.testcase.generic.NullStrings.class,
			org.ensembl.healthcheck.testcase.generic.NullTranscripts.class,
			org.ensembl.healthcheck.testcase.generic.Pseudogene.class,
			org.ensembl.healthcheck.testcase.generic.Retrotransposed.class,
			org.ensembl.healthcheck.testcase.generic.SeqRegionName.class,
			org.ensembl.healthcheck.testcase.generic.SeqRegionsTopLevel.class,
			org.ensembl.healthcheck.testcase.generic.SequenceLevel.class,
			org.ensembl.healthcheck.testcase.generic.SingleDBCollations.class,
			org.ensembl.healthcheck.testcase.generic.SourceTypes.class,
			org.ensembl.healthcheck.testcase.generic.Strand.class,
			org.ensembl.healthcheck.testcase.generic.TranscriptNames.class,
			org.ensembl.healthcheck.testcase.generic.TranscriptsSameName.class,
			org.ensembl.healthcheck.testcase.generic.TranscriptSupportingFeatures.class,
			org.ensembl.healthcheck.testcase.generic.TranslationStartEnd.class,
			org.ensembl.healthcheck.testcase.generic.TranslationStartEndExon.class,
			org.ensembl.healthcheck.testcase.generic.UTR.class
		);
	}
}
