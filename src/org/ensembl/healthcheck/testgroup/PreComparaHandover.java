package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;

/**
 * These are the tests required for a staging database before compara handover. The tests are:
 * 
 * <ul>
 *   <li> org.ensembl.healthcheck.testcase.generic.AnalyseTables </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.AnalysisLogicName </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.AnalysisTypes </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.AnalysisXrefs </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.AncestralSequencesExtraChecks </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.Archive </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.AssemblyExceptions </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.AssemblyMultipleOverlap </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.AssemblyNameLength </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.AssemblySeqregion </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.AssemblyTablesAcrossSpecies </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.AttribTypeAcrossSpecies </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.AutoIncrement </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.BigGeneExon </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.BlankCoordSystemVersions </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.BlankEnums </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.BlanksInsteadOfNulls </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.CanonicalTranscriptCoding </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousDatabases </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionBiotypes </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionCCDS </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionExonCoords </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionRepeatTypes </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionTableRows </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionXrefs </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.CompareSchema </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.CoordSystemAcrossSpecies </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.CoreForeignKeys </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.Ditag </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.DNAEmpty </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.DuplicateAssembly </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.DuplicateAttributes </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.DuplicateExons </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.DuplicateGenes </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.EmptyTables </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ExonRank </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ExonTranscriptStartEnd </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.FeatureAnalysis </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.FeatureCoords </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.FrameshiftAttributes </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.GeneCoordSystem </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.GeneCount </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.GeneTranscriptStartEnd </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.IsCurrent </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.MappingSession </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.MetaCoord </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.MetaCrossSpecies </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.Meta </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.MetaValues </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.MTCodonTable </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.NullStrings </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.NullTranscripts </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ProductionAnalysisLogicName </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ProductionBiotypes </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ProductionMasterTables </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ProductionMeta </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ProteinFeatures </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ProteinFeatureTranslation </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.Pseudogene </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.RepeatConsensus </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.RepeatFeature </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.Retrotransposed </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.SchemaType </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.SeqRegionAcrossSpecies </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.SeqRegionAttribAcrossSpecies </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.SeqRegionCoordSystem </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.SeqRegionName </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.SeqRegionsTopLevel </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.SequenceLevel </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.SpeciesID </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.StableID </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.Strand </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.TranscriptNames </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.TranscriptsTranslate </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.TranscriptSupportingFeatures </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.TranslationStartEndExon </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.TranslationStartEnd </li>
 * </ul>
 *
 * @author Autogenerated
 *
 */
public class PreComparaHandover extends GroupOfTests {

        public PreComparaHandover() {

                addTest(
			org.ensembl.healthcheck.testcase.generic.AnalyseTables.class,
			org.ensembl.healthcheck.testcase.generic.AnalysisLogicName.class,
			org.ensembl.healthcheck.testcase.generic.AnalysisTypes.class,
			org.ensembl.healthcheck.testcase.generic.AnalysisXrefs.class,
			org.ensembl.healthcheck.testcase.generic.AncestralSequencesExtraChecks.class,
			org.ensembl.healthcheck.testcase.generic.Archive.class,
			org.ensembl.healthcheck.testcase.generic.AssemblyExceptions.class,
			org.ensembl.healthcheck.testcase.generic.AssemblyMultipleOverlap.class,
			org.ensembl.healthcheck.testcase.generic.AssemblyNameLength.class,
			org.ensembl.healthcheck.testcase.generic.AssemblySeqregion.class,
			org.ensembl.healthcheck.testcase.generic.AssemblyTablesAcrossSpecies.class,
			org.ensembl.healthcheck.testcase.generic.AttribTypeAcrossSpecies.class,
			org.ensembl.healthcheck.testcase.generic.AutoIncrement.class,
			org.ensembl.healthcheck.testcase.generic.BigGeneExon.class,
			org.ensembl.healthcheck.testcase.generic.BlankCoordSystemVersions.class,
			org.ensembl.healthcheck.testcase.generic.BlankEnums.class,
			org.ensembl.healthcheck.testcase.generic.BlanksInsteadOfNulls.class,
			org.ensembl.healthcheck.testcase.generic.CanonicalTranscriptCoding.class,
			org.ensembl.healthcheck.testcase.generic.ComparePreviousDatabases.class,
			org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionBiotypes.class,
			org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionCCDS.class,
			org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionExonCoords.class,
			org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionRepeatTypes.class,
			org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionTableRows.class,
			org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionXrefs.class,
			org.ensembl.healthcheck.testcase.generic.CompareSchema.class,
			org.ensembl.healthcheck.testcase.generic.CoordSystemAcrossSpecies.class,
			org.ensembl.healthcheck.testcase.generic.CoreForeignKeys.class,
			org.ensembl.healthcheck.testcase.generic.Ditag.class,
			org.ensembl.healthcheck.testcase.generic.DNAEmpty.class,
			org.ensembl.healthcheck.testcase.generic.DuplicateAssembly.class,
			org.ensembl.healthcheck.testcase.generic.DuplicateAttributes.class,
			org.ensembl.healthcheck.testcase.generic.DuplicateExons.class,
			org.ensembl.healthcheck.testcase.generic.DuplicateGenes.class,
			org.ensembl.healthcheck.testcase.generic.EmptyTables.class,
			org.ensembl.healthcheck.testcase.generic.ExonRank.class,
			org.ensembl.healthcheck.testcase.generic.ExonTranscriptStartEnd.class,
			org.ensembl.healthcheck.testcase.generic.FeatureAnalysis.class,
			org.ensembl.healthcheck.testcase.generic.FeatureCoords.class,
			org.ensembl.healthcheck.testcase.generic.FrameshiftAttributes.class,
			org.ensembl.healthcheck.testcase.generic.GeneCoordSystem.class,
			org.ensembl.healthcheck.testcase.generic.GeneCount.class,
			org.ensembl.healthcheck.testcase.generic.GeneTranscriptStartEnd.class,
			org.ensembl.healthcheck.testcase.generic.IsCurrent.class,
			org.ensembl.healthcheck.testcase.generic.MappingSession.class,
			org.ensembl.healthcheck.testcase.generic.MetaCoord.class,
			org.ensembl.healthcheck.testcase.generic.MetaCrossSpecies.class,
			org.ensembl.healthcheck.testcase.generic.Meta.class,
			org.ensembl.healthcheck.testcase.generic.MetaValues.class,
			org.ensembl.healthcheck.testcase.generic.MTCodonTable.class,
			org.ensembl.healthcheck.testcase.generic.NullStrings.class,
			org.ensembl.healthcheck.testcase.generic.NullTranscripts.class,
			org.ensembl.healthcheck.testcase.generic.ProductionAnalysisLogicName.class,
			org.ensembl.healthcheck.testcase.generic.ProductionBiotypes.class,
			org.ensembl.healthcheck.testcase.generic.ProductionMasterTables.class,
			org.ensembl.healthcheck.testcase.generic.ProductionMeta.class,
			org.ensembl.healthcheck.testcase.generic.ProteinFeatures.class,
			org.ensembl.healthcheck.testcase.generic.ProteinFeatureTranslation.class,
			org.ensembl.healthcheck.testcase.generic.Pseudogene.class,
			org.ensembl.healthcheck.testcase.generic.RepeatConsensus.class,
			org.ensembl.healthcheck.testcase.generic.RepeatFeature.class,
			org.ensembl.healthcheck.testcase.generic.Retrotransposed.class,
			org.ensembl.healthcheck.testcase.generic.SchemaType.class,
			org.ensembl.healthcheck.testcase.generic.SeqRegionAcrossSpecies.class,
			org.ensembl.healthcheck.testcase.generic.SeqRegionAttribAcrossSpecies.class,
			org.ensembl.healthcheck.testcase.generic.SeqRegionCoordSystem.class,
			org.ensembl.healthcheck.testcase.generic.SeqRegionName.class,
			org.ensembl.healthcheck.testcase.generic.SeqRegionsTopLevel.class,
			org.ensembl.healthcheck.testcase.generic.SequenceLevel.class,
			org.ensembl.healthcheck.testcase.generic.SpeciesID.class,
			org.ensembl.healthcheck.testcase.generic.StableID.class,
			org.ensembl.healthcheck.testcase.generic.Strand.class,
			org.ensembl.healthcheck.testcase.generic.TranscriptNames.class,
			org.ensembl.healthcheck.testcase.generic.TranscriptsTranslate.class,
			org.ensembl.healthcheck.testcase.generic.TranscriptSupportingFeatures.class,
			org.ensembl.healthcheck.testcase.generic.TranslationStartEndExon.class,
			org.ensembl.healthcheck.testcase.generic.TranslationStartEnd.class
                );
        }
}


