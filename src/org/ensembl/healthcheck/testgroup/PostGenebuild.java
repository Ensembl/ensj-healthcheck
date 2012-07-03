package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;

/**
 * These are the tests that register themselves as post_genebuild. The tests are:
 * 
 * <ul>
 *   <li> org.ensembl.healthcheck.testcase.generic.ProductionAnalysisLogicName </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ProductionBiotypes </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ProductionMasterTables </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ProductionMeta </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.CoreForeignKeys </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.TranscriptsTranslate </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.Accession </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.XrefTypes </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.AlignFeatureExternalDB </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ProjectedXrefGenes </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.DNAEmpty </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.BlankEnums </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.IsCurrent </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.TranslationStartEndExon </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionTableRows </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.Strand </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.MTCodonTable </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.TranscriptsSameName </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.XrefCategories </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.DuplicateExons </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.PredictedXrefs </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.BlankInfoType </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ProteinFeatures </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionCCDS </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionRepeatTypes </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.GeneDescriptions </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.AssemblyMultipleOverlap </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.CanonicalTranscriptCoding </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.MarkerFeatures </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ExternalSynonymArray </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.HitNameFormat </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.Retrotransposed </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.Pseudogene </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.LogicNamesDisplayable </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.MetaCrossSpecies </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.DescriptionNewlines </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.AssemblyException </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.IdentityXrefCigarLines </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.AssemblySeqregion </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ExonTranscriptStartEnd </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.XrefVersions </li> 
 *   <li> org.ensembl.healthcheck.testcase.funcgen.ArrayXrefs </li> 
 *   <li> org.ensembl.healthcheck.testcase.funcgen.FuncgenForeignKeys </li> 
 *   <li> org.ensembl.healthcheck.testcase.funcgen.FuncgenAnalysisDescription </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.NullTranscripts </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.BlankCoordSystemVersions </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.FrameshiftAttributes </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.DuplicateGenes </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ExonRank </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ExonStrandOrder </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.BlanksInsteadOfNulls </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.XrefIdentifiers </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.SeqRegionsTopLevel </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.HGNCMultipleGenes </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.InterproDescriptions </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.GeneTranscriptStartEnd </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.XrefPrefixes </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.Karyotype </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.SeqRegionCoordSystem </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.RepeatFeature </li> 
 *   <li> org.ensembl.healthcheck.testcase.xref.ForeignKeys </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.FeatureAnalysis </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.Ditag </li> 
 *   <li> org.ensembl.healthcheck.testcase.funcgen.ComparePreviousVersionArrayXrefs </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.DisplayXref </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.FeatureCoords </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.TranslationStartEnd </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.HGNCTypes </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.RepeatConsensus </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.DuplicateAttributes </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ESTStableID </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.DuplicateAssembly </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ProteinFeatureTranslation </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.DisplayLabels </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionBiotypes </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.GOXrefs </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.NullStrings </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.StableID </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.BigGeneExon </li> 
 * </ul>
 *
 * @author Autogenerated
 *
 */
public class PostGenebuild extends GroupOfTests {
	
	public PostGenebuild() {

		addTest(
                        org.ensembl.healthcheck.testcase.generic.ProductionAnalysisLogicName.class,
                        org.ensembl.healthcheck.testcase.generic.ProductionBiotypes.class,
                        org.ensembl.healthcheck.testcase.generic.ProductionMasterTables.class,
                        org.ensembl.healthcheck.testcase.generic.ProductionMeta.class,
			org.ensembl.healthcheck.testcase.generic.CoreForeignKeys.class,
			org.ensembl.healthcheck.testcase.generic.TranscriptsTranslate.class,
			org.ensembl.healthcheck.testcase.generic.Accession.class,
			org.ensembl.healthcheck.testcase.generic.XrefTypes.class,
			org.ensembl.healthcheck.testcase.generic.AlignFeatureExternalDB.class,
			org.ensembl.healthcheck.testcase.generic.ProjectedXrefGenes.class,
			org.ensembl.healthcheck.testcase.generic.DNAEmpty.class,
			org.ensembl.healthcheck.testcase.generic.BlankEnums.class,
			org.ensembl.healthcheck.testcase.generic.IsCurrent.class,
			org.ensembl.healthcheck.testcase.generic.TranslationStartEndExon.class,
			org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionTableRows.class,
			org.ensembl.healthcheck.testcase.generic.Strand.class,
			org.ensembl.healthcheck.testcase.generic.MTCodonTable.class,
			org.ensembl.healthcheck.testcase.generic.TranscriptsSameName.class,
			org.ensembl.healthcheck.testcase.generic.XrefCategories.class,
			org.ensembl.healthcheck.testcase.generic.DuplicateExons.class,
			org.ensembl.healthcheck.testcase.generic.PredictedXrefs.class,
			org.ensembl.healthcheck.testcase.generic.BlankInfoType.class,
			org.ensembl.healthcheck.testcase.generic.ProteinFeatures.class,
			org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionCCDS.class,
			org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionRepeatTypes.class,
			org.ensembl.healthcheck.testcase.generic.GeneDescriptions.class,
			org.ensembl.healthcheck.testcase.generic.AssemblyMultipleOverlap.class,
			org.ensembl.healthcheck.testcase.generic.CanonicalTranscriptCoding.class,
			org.ensembl.healthcheck.testcase.generic.MarkerFeatures.class,
			org.ensembl.healthcheck.testcase.generic.ExternalSynonymArray.class,
			org.ensembl.healthcheck.testcase.generic.HitNameFormat.class,
			org.ensembl.healthcheck.testcase.generic.Retrotransposed.class,
			org.ensembl.healthcheck.testcase.generic.Pseudogene.class,
			org.ensembl.healthcheck.testcase.generic.LogicNamesDisplayable.class,
			org.ensembl.healthcheck.testcase.generic.MetaCrossSpecies.class,
			org.ensembl.healthcheck.testcase.generic.DescriptionNewlines.class,
			org.ensembl.healthcheck.testcase.generic.AssemblyExceptions.class,
			org.ensembl.healthcheck.testcase.generic.IdentityXrefCigarLines.class,
			org.ensembl.healthcheck.testcase.generic.AssemblySeqregion.class,
			org.ensembl.healthcheck.testcase.generic.ExonTranscriptStartEnd.class,
			org.ensembl.healthcheck.testcase.generic.XrefVersions.class,
			org.ensembl.healthcheck.testcase.funcgen.ArrayXrefs.class,
			org.ensembl.healthcheck.testcase.funcgen.FuncgenForeignKeys.class,
			org.ensembl.healthcheck.testcase.funcgen.FuncgenAnalysisDescription.class,
			org.ensembl.healthcheck.testcase.generic.NullTranscripts.class,
			org.ensembl.healthcheck.testcase.generic.BlankCoordSystemVersions.class,
			org.ensembl.healthcheck.testcase.generic.FrameshiftAttributes.class,
			org.ensembl.healthcheck.testcase.generic.DuplicateGenes.class,
			org.ensembl.healthcheck.testcase.generic.ExonRank.class,
			org.ensembl.healthcheck.testcase.generic.ExonStrandOrder.class,
			org.ensembl.healthcheck.testcase.generic.BlanksInsteadOfNulls.class,
			org.ensembl.healthcheck.testcase.generic.XrefIdentifiers.class,
			org.ensembl.healthcheck.testcase.generic.SeqRegionsTopLevel.class,
			org.ensembl.healthcheck.testcase.generic.HGNCMultipleGenes.class,
			org.ensembl.healthcheck.testcase.generic.InterproDescriptions.class,
			org.ensembl.healthcheck.testcase.generic.GeneTranscriptStartEnd.class,
			org.ensembl.healthcheck.testcase.generic.XrefPrefixes.class,
			org.ensembl.healthcheck.testcase.generic.Karyotype.class,
			org.ensembl.healthcheck.testcase.generic.SeqRegionCoordSystem.class,
			org.ensembl.healthcheck.testcase.generic.RepeatFeature.class,
			org.ensembl.healthcheck.testcase.xref.ForeignKeys.class,
			org.ensembl.healthcheck.testcase.generic.FeatureAnalysis.class,
			org.ensembl.healthcheck.testcase.generic.Ditag.class,
			org.ensembl.healthcheck.testcase.funcgen.ComparePreviousVersionArrayXrefs.class,
			org.ensembl.healthcheck.testcase.generic.DisplayXref.class,
			org.ensembl.healthcheck.testcase.generic.FeatureCoords.class,
			org.ensembl.healthcheck.testcase.generic.TranslationStartEnd.class,
			org.ensembl.healthcheck.testcase.generic.HGNCTypes.class,
			org.ensembl.healthcheck.testcase.generic.RepeatConsensus.class,
			org.ensembl.healthcheck.testcase.generic.DuplicateAttributes.class,
			org.ensembl.healthcheck.testcase.generic.ESTStableID.class,
			org.ensembl.healthcheck.testcase.generic.DuplicateAssembly.class,
			org.ensembl.healthcheck.testcase.generic.ProteinFeatureTranslation.class,
			org.ensembl.healthcheck.testcase.generic.DisplayLabels.class,
			org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionBiotypes.class,
			org.ensembl.healthcheck.testcase.generic.GOXrefs.class,
			org.ensembl.healthcheck.testcase.generic.NullStrings.class,
			org.ensembl.healthcheck.testcase.generic.StableID.class,
			org.ensembl.healthcheck.testcase.generic.BigGeneExon.class
		);
	}
}
