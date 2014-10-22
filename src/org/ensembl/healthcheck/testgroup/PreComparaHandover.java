/*
 * Copyright [1999-2014] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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
 * These are the tests that register themselves as pre-compara-handover. The tests are:
 * 
 * <ul>
 *   <li> org.ensembl.healthcheck.testcase.generic.AlignFeatureExternalDB </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.AnalysisDescription </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.AnalyseTables </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.AnalysisLogicName </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.AnalysisTypes </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.AnalysisXrefs </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.AncestralSequencesExtraChecks </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.Archive </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.AssemblyExceptions </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.AssemblyMapping </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.AssemblyMultipleOverlap </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.AssemblyNameLength </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.AssemblySeqregion </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.AssemblyTablesAcrossSpecies </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.AttribTypeAcrossSpecies </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.AutoIncrement </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.BigGeneExon </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.BlankCoordSystemVersions </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.BlankEnums </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.BlankInfoType </li>  
 *   <li> org.ensembl.healthcheck.testcase.generic.BlanksInsteadOfNulls </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.CanonicalTranscriptCoding </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.CheckDeclarations </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousDatabases </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionBiotypes </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionCCDS </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionExonCoords </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionGeneNames </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionRegionSynonyms </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionRepeatTypes </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionSynonyms </li>  
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionTableRows </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionXrefs </li>  
 *   <li> org.ensembl.healthcheck.testcase.generic.CompareSchema </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.CoordSystemAcrossSpecies </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.CoreForeignKeys </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.DNAEmpty </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.DataFiles </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.DescriptionNewlines </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.DisplayLabels </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.DisplayXref </li>  
 *   <li> org.ensembl.healthcheck.testcase.generic.Ditag </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.DuplicateAssembly </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.DuplicateAttributes </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.DuplicateExons </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.DuplicateGenes </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.EmptyTables </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.EntrezGeneNumeric </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ExonRank </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ExonSupportingFeatures </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ExonTranscriptStartEnd </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ExternalSynonymArray </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.FeatureAnalysis </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.FeatureCoords </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.FrameshiftAttributes </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.GencodeAttributes </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.GeneCoordSystem </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.GeneCount </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.GeneDescriptions </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.GeneStatus </li>  
 *   <li> org.ensembl.healthcheck.testcase.generic.GeneTranscriptStartEnd </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.GOXrefs </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.HGNCMultipleGenes </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.HGNCNumeric </li>  
 *   <li> org.ensembl.healthcheck.testcase.generic.HitNameFormat </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.IdentityXrefCigarLines </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.InterproDescriptions </li>  
 *   <li> org.ensembl.healthcheck.testcase.generic.IsCurrent </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.Karyotype </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.LRG </li>  
 *   <li> org.ensembl.healthcheck.testcase.generic.MTCodonTable </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.MappingSession </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.MarkerFeatures </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.Meta </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.MetaCoord </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.MetaCrossSpecies </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.MetaValues </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.MultipleComponentAssemblyMapping </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.MySQLStorageEngine </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.NonGTACNSequence </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.NullStrings </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.NullTranscripts </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.PredictedXrefs </li>  
 *   <li> org.ensembl.healthcheck.testcase.generic.ProductionAnalysisLogicName </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ProductionBiotypes </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ProductionMasterTables </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ProductionMeta </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ProductionSpeciesAlias </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ProteinFeatureTranslation </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.Pseudogene </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.RepeatConsensus </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.RepeatFeature </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.Retrotransposed </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.SchemaType </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.SeqRegionAcrossSpecies </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.SeqRegionAttribAcrossSpecies </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.SeqRegionName </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.SeqRegionsTopLevel </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.SequenceLevel </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.SourceTypes </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.SpeciesID </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.StableID </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.Strand </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.TranscriptNames </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.TranscriptsSameName </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.TranscriptSupportingFeatures </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.TranslationStartEnd </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.TranslationStartEndExon </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.UnreviewedXrefs </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.UTR </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.XrefCategories </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.XrefHTML </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.XrefIdentifiers </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.XrefLevels </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.XrefPrefixes </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.XrefTypes </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.XrefVersions </li>  
 * </ul>
 *
 * @author Thomas Maurel
 *
 */
public class PreComparaHandover extends GroupOfTests {
	
	public PreComparaHandover() {

		addTest(
			org.ensembl.healthcheck.testcase.generic.AlignFeatureExternalDB.class,
			org.ensembl.healthcheck.testcase.generic.AnalyseTables.class,
			org.ensembl.healthcheck.testcase.generic.AnalysisDescription.class,
			org.ensembl.healthcheck.testcase.generic.AnalysisLogicName.class,
			org.ensembl.healthcheck.testcase.generic.AnalysisTypes.class,
			org.ensembl.healthcheck.testcase.generic.AnalysisXrefs.class,
			org.ensembl.healthcheck.testcase.generic.AncestralSequencesExtraChecks.class,
			org.ensembl.healthcheck.testcase.generic.Archive.class,
			org.ensembl.healthcheck.testcase.generic.AssemblyExceptions.class,
			org.ensembl.healthcheck.testcase.generic.AssemblyMapping.class,
			org.ensembl.healthcheck.testcase.generic.AssemblyMultipleOverlap.class,
			org.ensembl.healthcheck.testcase.generic.AssemblyNameLength.class,
			org.ensembl.healthcheck.testcase.generic.AssemblySeqregion.class,
			org.ensembl.healthcheck.testcase.generic.AssemblyTablesAcrossSpecies.class,
			org.ensembl.healthcheck.testcase.generic.AttribTypeAcrossSpecies.class,
			org.ensembl.healthcheck.testcase.generic.AutoIncrement.class,
			org.ensembl.healthcheck.testcase.generic.BigGeneExon.class,
			org.ensembl.healthcheck.testcase.generic.BlankCoordSystemVersions.class,
			org.ensembl.healthcheck.testcase.generic.BlankEnums.class,
			org.ensembl.healthcheck.testcase.generic.BlankInfoType.class,
			org.ensembl.healthcheck.testcase.generic.BlanksInsteadOfNulls.class,
			org.ensembl.healthcheck.testcase.generic.CanonicalTranscriptCoding.class,
			org.ensembl.healthcheck.testcase.generic.CheckDeclarations.class,
			org.ensembl.healthcheck.testcase.generic.ComparePreviousDatabases.class,
			org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionBiotypes.class,
			org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionCCDS.class,
			org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionExonCoords.class,
			org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionGeneNames.class,
			org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionRegionSynonyms.class,
			org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionRepeatTypes.class,
			org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionSynonyms.class,
			org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionTableRows.class,
			org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionXrefs.class,
			org.ensembl.healthcheck.testcase.generic.CompareSchema.class,
			org.ensembl.healthcheck.testcase.generic.CoordSystemAcrossSpecies.class,
			org.ensembl.healthcheck.testcase.generic.CoreForeignKeys.class,
			org.ensembl.healthcheck.testcase.generic.DNAEmpty.class,
			org.ensembl.healthcheck.testcase.generic.DataFiles.class,
			org.ensembl.healthcheck.testcase.generic.DescriptionNewlines.class,
			org.ensembl.healthcheck.testcase.generic.DisplayLabels.class,
			org.ensembl.healthcheck.testcase.generic.DisplayXref.class,
			org.ensembl.healthcheck.testcase.generic.Ditag.class,
			org.ensembl.healthcheck.testcase.generic.DuplicateAssembly.class,
			org.ensembl.healthcheck.testcase.generic.DuplicateAttributes.class,
			org.ensembl.healthcheck.testcase.generic.DuplicateExons.class,
			org.ensembl.healthcheck.testcase.generic.DuplicateGenes.class,
			org.ensembl.healthcheck.testcase.generic.EmptyTables.class,
			org.ensembl.healthcheck.testcase.generic.EntrezGeneNumeric.class,
			org.ensembl.healthcheck.testcase.generic.ExonRank.class,
			org.ensembl.healthcheck.testcase.generic.ExonSupportingFeatures.class,
			org.ensembl.healthcheck.testcase.generic.ExonTranscriptStartEnd.class,
			org.ensembl.healthcheck.testcase.generic.ExternalSynonymArray.class,
			org.ensembl.healthcheck.testcase.generic.FeatureAnalysis.class,
			org.ensembl.healthcheck.testcase.generic.FeatureCoords.class,
			org.ensembl.healthcheck.testcase.generic.FrameshiftAttributes.class,
			org.ensembl.healthcheck.testcase.generic.GencodeAttributes.class,
			org.ensembl.healthcheck.testcase.generic.GeneCoordSystem.class,
			org.ensembl.healthcheck.testcase.generic.GeneCount.class,
			org.ensembl.healthcheck.testcase.generic.GeneDescriptions.class,
			org.ensembl.healthcheck.testcase.generic.GeneStatus.class,
			org.ensembl.healthcheck.testcase.generic.GeneTranscriptStartEnd.class,
			org.ensembl.healthcheck.testcase.generic.GOXrefs.class,
			org.ensembl.healthcheck.testcase.generic.HGNCMultipleGenes.class,
			org.ensembl.healthcheck.testcase.generic.HGNCNumeric.class,
			org.ensembl.healthcheck.testcase.generic.HitNameFormat.class,
			org.ensembl.healthcheck.testcase.generic.IdentityXrefCigarLines.class,
			org.ensembl.healthcheck.testcase.generic.InterproDescriptions.class,
			org.ensembl.healthcheck.testcase.generic.IsCurrent.class,
			org.ensembl.healthcheck.testcase.generic.Karyotype.class,
			org.ensembl.healthcheck.testcase.generic.LRG.class,
			org.ensembl.healthcheck.testcase.generic.MTCodonTable.class,
			org.ensembl.healthcheck.testcase.generic.MappingSession.class,
			org.ensembl.healthcheck.testcase.generic.MarkerFeatures.class,
			org.ensembl.healthcheck.testcase.generic.Meta.class,
			org.ensembl.healthcheck.testcase.generic.MetaCoord.class,
			org.ensembl.healthcheck.testcase.generic.MetaCrossSpecies.class,
			org.ensembl.healthcheck.testcase.generic.MetaValues.class,
			org.ensembl.healthcheck.testcase.generic.MultipleComponentAssemblyMapping.class,
			org.ensembl.healthcheck.testcase.generic.MySQLStorageEngine.class,
			org.ensembl.healthcheck.testcase.generic.NonGTACNSequence.class,
			org.ensembl.healthcheck.testcase.generic.NullStrings.class,
			org.ensembl.healthcheck.testcase.generic.NullTranscripts.class,
			org.ensembl.healthcheck.testcase.generic.PredictedXrefs.class,
			org.ensembl.healthcheck.testcase.generic.ProductionAnalysisLogicName.class,
			org.ensembl.healthcheck.testcase.generic.ProductionBiotypes.class,
			org.ensembl.healthcheck.testcase.generic.ProductionMasterTables.class,
			org.ensembl.healthcheck.testcase.generic.ProductionMeta.class,
			org.ensembl.healthcheck.testcase.generic.ProductionSpeciesAlias.class,
			org.ensembl.healthcheck.testcase.generic.ProteinFeatureTranslation.class,
			org.ensembl.healthcheck.testcase.generic.Pseudogene.class,
			org.ensembl.healthcheck.testcase.generic.RepeatConsensus.class,
			org.ensembl.healthcheck.testcase.generic.RepeatFeature.class,
			org.ensembl.healthcheck.testcase.generic.Retrotransposed.class,
			org.ensembl.healthcheck.testcase.generic.SchemaType.class,
			org.ensembl.healthcheck.testcase.generic.SeqRegionAcrossSpecies.class,
			org.ensembl.healthcheck.testcase.generic.SeqRegionAttribAcrossSpecies.class,
			org.ensembl.healthcheck.testcase.generic.SeqRegionName.class,
			org.ensembl.healthcheck.testcase.generic.SeqRegionsTopLevel.class,
			org.ensembl.healthcheck.testcase.generic.SequenceLevel.class,
			org.ensembl.healthcheck.testcase.generic.SourceTypes.class,
			org.ensembl.healthcheck.testcase.generic.SpeciesID.class,
			org.ensembl.healthcheck.testcase.generic.StableID.class,
			org.ensembl.healthcheck.testcase.generic.Strand.class,
			org.ensembl.healthcheck.testcase.generic.TranscriptNames.class,
			org.ensembl.healthcheck.testcase.generic.TranscriptsSameName.class,
			org.ensembl.healthcheck.testcase.generic.TranscriptSupportingFeatures.class,
			org.ensembl.healthcheck.testcase.generic.TranslationStartEnd.class,
			org.ensembl.healthcheck.testcase.generic.TranslationStartEndExon.class,
			org.ensembl.healthcheck.testcase.generic.UnreviewedXrefs.class,
			org.ensembl.healthcheck.testcase.generic.UTR.class,
			org.ensembl.healthcheck.testcase.generic.XrefCategories.class,
			org.ensembl.healthcheck.testcase.generic.XrefHTML.class,
			org.ensembl.healthcheck.testcase.generic.XrefIdentifiers.class,
			org.ensembl.healthcheck.testcase.generic.XrefLevels.class,
			org.ensembl.healthcheck.testcase.generic.XrefPrefixes.class,
			org.ensembl.healthcheck.testcase.generic.XrefTypes.class,
			org.ensembl.healthcheck.testcase.generic.XrefVersions.class
		);
	}
}
