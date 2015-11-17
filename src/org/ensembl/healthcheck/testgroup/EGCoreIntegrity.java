package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.eg_core.CircularAwareFeatureCoords;
import org.ensembl.healthcheck.testcase.eg_core.ControlledTableAttribType;
import org.ensembl.healthcheck.testcase.eg_core.ControlledTableExternalDb;
import org.ensembl.healthcheck.testcase.eg_core.DbDisplayNameUniProt;
import org.ensembl.healthcheck.testcase.eg_core.DeprecatedEgMeta;
import org.ensembl.healthcheck.testcase.eg_core.DuplicateMetaKeys;
import org.ensembl.healthcheck.testcase.eg_core.DuplicateTopLevel;
import org.ensembl.healthcheck.testcase.eg_core.DuplicateXref;
import org.ensembl.healthcheck.testcase.eg_core.EGCompareCoreSchema;
import org.ensembl.healthcheck.testcase.eg_core.ExonBoundary;
import org.ensembl.healthcheck.testcase.eg_core.GeneBuildStartDate;
import org.ensembl.healthcheck.testcase.eg_core.GeneDescriptionUniProtSource;
import org.ensembl.healthcheck.testcase.eg_core.GeneGC;
import org.ensembl.healthcheck.testcase.eg_core.IgiXref;
import org.ensembl.healthcheck.testcase.eg_core.InappropriateTranslation;
import org.ensembl.healthcheck.testcase.eg_core.MetaForCompara;
import org.ensembl.healthcheck.testcase.eg_core.MultiDbCompareNames;
import org.ensembl.healthcheck.testcase.eg_core.MultiDbSpeciesNames;
import org.ensembl.healthcheck.testcase.eg_core.MultiDbStableId;
import org.ensembl.healthcheck.testcase.eg_core.PeptideTranslationAttribs;
import org.ensembl.healthcheck.testcase.eg_core.PermittedEgMeta;
import org.ensembl.healthcheck.testcase.eg_core.PositiveCoordinates;
import org.ensembl.healthcheck.testcase.eg_core.ProteinCodingGene;
import org.ensembl.healthcheck.testcase.eg_core.ProteinTranslation;
import org.ensembl.healthcheck.testcase.eg_core.RepeatAnalysesInMeta;
import org.ensembl.healthcheck.testcase.eg_core.RequiredEgMeta;
import org.ensembl.healthcheck.testcase.eg_core.SampleSetting;
import org.ensembl.healthcheck.testcase.eg_core.SchemaPatchesApplied;
import org.ensembl.healthcheck.testcase.eg_core.SeqRegionAttribForPolyploidGenome;
import org.ensembl.healthcheck.testcase.eg_core.SeqRegionAttribForPolyploidGenomeToplevelOnly;
import org.ensembl.healthcheck.testcase.eg_core.SeqRegionDna;
import org.ensembl.healthcheck.testcase.eg_core.SeqRegionLength;
import org.ensembl.healthcheck.testcase.eg_core.SeqRegionsConsistentWithComparaMaster;
import org.ensembl.healthcheck.testcase.eg_core.TranslationAttribType;
import org.ensembl.healthcheck.testcase.eg_core.UniProtExternalDbTypes;
import org.ensembl.healthcheck.testcase.eg_core.ValidSeqEnd;
import org.ensembl.healthcheck.testcase.eg_core.XrefDescriptionSpecialChars;
import org.ensembl.healthcheck.testcase.generic.AnalysisDescription;
import org.ensembl.healthcheck.testcase.generic.AnalysisLogicName;
import org.ensembl.healthcheck.testcase.generic.AssemblyExceptions;
import org.ensembl.healthcheck.testcase.generic.AssemblyMapping;
import org.ensembl.healthcheck.testcase.generic.AssemblyMultipleOverlap;
import org.ensembl.healthcheck.testcase.generic.AssemblySeqregion;
import org.ensembl.healthcheck.testcase.generic.BlankCoordSystemVersions;
import org.ensembl.healthcheck.testcase.generic.BlankEnums;
import org.ensembl.healthcheck.testcase.generic.BlankInfoType;
import org.ensembl.healthcheck.testcase.generic.CanonicalTranscriptCoding;
import org.ensembl.healthcheck.testcase.generic.CoreForeignKeys;
import org.ensembl.healthcheck.testcase.generic.DescriptionNewlines;
import org.ensembl.healthcheck.testcase.generic.DisplayLabels;
import org.ensembl.healthcheck.testcase.generic.DuplicateAssembly;
import org.ensembl.healthcheck.testcase.generic.ExonRank;
import org.ensembl.healthcheck.testcase.generic.ExonStrandOrder;
import org.ensembl.healthcheck.testcase.generic.ExonTranscriptStartEnd;
import org.ensembl.healthcheck.testcase.generic.GeneCoordSystem;
import org.ensembl.healthcheck.testcase.generic.GeneDescriptions;
import org.ensembl.healthcheck.testcase.generic.InterproDescriptions;
import org.ensembl.healthcheck.testcase.generic.IsCurrent;
import org.ensembl.healthcheck.testcase.generic.MetaCoord;
import org.ensembl.healthcheck.testcase.generic.NullTranscripts;
import org.ensembl.healthcheck.testcase.generic.ProductionAnalysisLogicName;
import org.ensembl.healthcheck.testcase.generic.ProductionBiotypes;
import org.ensembl.healthcheck.testcase.generic.ProductionMasterTables;
import org.ensembl.healthcheck.testcase.generic.SeqRegionAttribsPresent;
import org.ensembl.healthcheck.testcase.generic.SeqRegionsTopLevel;
import org.ensembl.healthcheck.testcase.generic.StableID;
import org.ensembl.healthcheck.testcase.generic.Strand;
import org.ensembl.healthcheck.testcase.generic.TranscriptsTranslate;
import org.ensembl.healthcheck.testcase.generic.TranslationStartEnd;
import org.ensembl.healthcheck.testcase.generic.TranslationStartEndExon;
import org.ensembl.healthcheck.testcase.generic.XrefCategories;
import org.ensembl.healthcheck.testcase.generic.XrefHTML;
import org.ensembl.healthcheck.testcase.generic.XrefIdentifiers;
import org.ensembl.healthcheck.testcase.generic.XrefTypes;

/**
 * Group containing tests that should never fail for Ensembl Genomes core
 * databases
 * 
 * @author dstaines
 *
 */
public class EGCoreIntegrity extends GroupOfTests {

	public EGCoreIntegrity() {
		addTest(AssemblyExceptions.class, AssemblyMultipleOverlap.class,
				AssemblySeqregion.class, CanonicalTranscriptCoding.class,
				CircularAwareFeatureCoords.class, EGCompareCoreSchema.class,
				CoreForeignKeys.class, DuplicateAssembly.class,
				DuplicateTopLevel.class, ExonRank.class, ExonStrandOrder.class,
				ExonTranscriptStartEnd.class, GeneCoordSystem.class,
				NullTranscripts.class, PeptideTranslationAttribs.class,
				ProteinCodingGene.class, SeqRegionAttribsPresent.class,
				SeqRegionsTopLevel.class, StableID.class, Strand.class,
				TranscriptsTranslate.class, TranslationStartEnd.class,
				TranslationStartEndExon.class, ProteinTranslation.class,
				AssemblyMapping.class, ValidSeqEnd.class,
				ExonBoundary.class, InappropriateTranslation.class,
				DescriptionNewlines.class, DisplayLabels.class,
				GeneDescriptions.class, PositiveCoordinates.class,
				GeneDescriptionUniProtSource.class, DbDisplayNameUniProt.class,
				XrefDescriptionSpecialChars.class,
				BlankCoordSystemVersions.class, BlankEnums.class,
				BlankInfoType.class, DuplicateXref.class, IgiXref.class,
				InterproDescriptions.class, IsCurrent.class,
				XrefCategories.class, XrefHTML.class, XrefIdentifiers.class,
				XrefTypes.class, UniProtExternalDbTypes.class,
				SeqRegionDna.class, RequiredEgMeta.class,
				DeprecatedEgMeta.class, GeneBuildStartDate.class,
				DuplicateMetaKeys.class, GeneGC.class, MetaCoord.class,
				SampleSetting.class, AnalysisDescription.class,
				MetaForCompara.class, TranslationAttribType.class,
				PermittedEgMeta.class, RepeatAnalysesInMeta.class,
				ProductionAnalysisLogicName.class, ProductionBiotypes.class,
				ProductionMasterTables.class, AnalysisLogicName.class,
				SeqRegionsConsistentWithComparaMaster.class,
				SeqRegionAttribForPolyploidGenome.class,
				SeqRegionAttribForPolyploidGenomeToplevelOnly.class,
				SeqRegionLength.class, SchemaPatchesApplied.class,
				MultiDbSpeciesNames.class, MultiDbStableId.class,
				MultiDbCompareNames.class);
	}

}
