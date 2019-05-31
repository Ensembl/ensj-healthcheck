package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;

import org.ensembl.healthcheck.testcase.eg_core.AssemblyDefault;
import org.ensembl.healthcheck.testcase.eg_core.CircularAwareFeatureCoords;
import org.ensembl.healthcheck.testcase.eg_core.DbDisplayNameUniProt;
import org.ensembl.healthcheck.testcase.eg_core.DeprecatedEgMeta;
import org.ensembl.healthcheck.testcase.eg_core.DuplicateMetaKeys;
import org.ensembl.healthcheck.testcase.eg_core.DuplicateTopLevel;
import org.ensembl.healthcheck.testcase.eg_core.DuplicateXref;
import org.ensembl.healthcheck.testcase.eg_core.EvidenceFreeGO;
import org.ensembl.healthcheck.testcase.eg_core.ExonBoundary;
import org.ensembl.healthcheck.testcase.eg_core.GeneBuildStartDate;
import org.ensembl.healthcheck.testcase.eg_core.GeneDescriptionUniProtSource;
import org.ensembl.healthcheck.testcase.eg_core.GeneGC;
import org.ensembl.healthcheck.testcase.eg_core.IgiXref;
import org.ensembl.healthcheck.testcase.eg_core.InappropriateTranslation;
import org.ensembl.healthcheck.testcase.eg_core.IncorrectExonRank;
import org.ensembl.healthcheck.testcase.eg_core.MetaForCompara;
import org.ensembl.healthcheck.testcase.eg_core.PeptideTranslationAttribs;
import org.ensembl.healthcheck.testcase.eg_core.PermittedEgMeta;
import org.ensembl.healthcheck.testcase.eg_core.PositiveCoordinates;
import org.ensembl.healthcheck.testcase.eg_core.ProteinCodingGene;
import org.ensembl.healthcheck.testcase.eg_core.ProteinTranslation;
import org.ensembl.healthcheck.testcase.eg_core.RepeatAnalysesInMeta;
import org.ensembl.healthcheck.testcase.eg_core.RequiredEgMeta;
import org.ensembl.healthcheck.testcase.eg_core.SampleSetting;
import org.ensembl.healthcheck.testcase.eg_core.SeqRegionAttribForPolyploidGenome;
import org.ensembl.healthcheck.testcase.eg_core.SeqRegionAttribForPolyploidGenomeToplevelOnly;
import org.ensembl.healthcheck.testcase.eg_core.SeqRegionDna;
import org.ensembl.healthcheck.testcase.eg_core.SeqRegionLength;
import org.ensembl.healthcheck.testcase.eg_core.SeqRegionsConsistentWithComparaMaster;
import org.ensembl.healthcheck.testcase.eg_core.TranslationAttribType;
import org.ensembl.healthcheck.testcase.eg_core.UniProtExternalDbTypes;
import org.ensembl.healthcheck.testcase.eg_core.ValidSeqEnd;
import org.ensembl.healthcheck.testcase.eg_core.VersionedGenes;
import org.ensembl.healthcheck.testcase.eg_core.VersionedTranscripts;
import org.ensembl.healthcheck.testcase.eg_core.VersionedTranslations;
import org.ensembl.healthcheck.testcase.eg_core.XrefDescriptionSpecialChars;
import org.ensembl.healthcheck.testcase.generic.AnalysisDescription;
import org.ensembl.healthcheck.testcase.generic.AnalysisLogicName;
import org.ensembl.healthcheck.testcase.generic.AssemblyExceptionTableSeqRegionAttribute;
import org.ensembl.healthcheck.testcase.generic.AssemblyExceptionTableIntegrity;
import org.ensembl.healthcheck.testcase.generic.AssemblyExceptionTableSeqMapping;
import org.ensembl.healthcheck.testcase.generic.AssemblyExceptionTableUniqueRegion;
import org.ensembl.healthcheck.testcase.generic.AssemblyExceptionTableStartEnd;
import org.ensembl.healthcheck.testcase.generic.AssemblyMapping;
import org.ensembl.healthcheck.testcase.generic.AssemblySeqregion;
import org.ensembl.healthcheck.testcase.generic.BlankCoordSystemVersions;
import org.ensembl.healthcheck.testcase.generic.BlankEnums;
import org.ensembl.healthcheck.testcase.generic.BlankInfoType;
import org.ensembl.healthcheck.testcase.generic.CanonicalTranscriptCoding;
import org.ensembl.healthcheck.testcase.generic.CompareSchema;
import org.ensembl.healthcheck.testcase.generic.CoreForeignKeys;
import org.ensembl.healthcheck.testcase.generic.DensityFeatures;
import org.ensembl.healthcheck.testcase.generic.DescriptionNewlines;
import org.ensembl.healthcheck.testcase.generic.DisplayLabels;
import org.ensembl.healthcheck.testcase.generic.DuplicateAssembly;
import org.ensembl.healthcheck.testcase.generic.ExonRank;
import org.ensembl.healthcheck.testcase.generic.ExonTranscriptStartEnd;
import org.ensembl.healthcheck.testcase.generic.GeneCoordSystem;
import org.ensembl.healthcheck.testcase.generic.GeneDescriptions;
import org.ensembl.healthcheck.testcase.generic.InterproDescriptions;
import org.ensembl.healthcheck.testcase.generic.IsCurrent;
import org.ensembl.healthcheck.testcase.generic.MetaCoord;
import org.ensembl.healthcheck.testcase.generic.NullTranscripts;
import org.ensembl.healthcheck.testcase.generic.ProductionBiotypes;
import org.ensembl.healthcheck.testcase.generic.ProductionMasterTables;
import org.ensembl.healthcheck.testcase.generic.SeqRegionAttribsPresent;
import org.ensembl.healthcheck.testcase.generic.SeqRegionsTopLevel;
import org.ensembl.healthcheck.testcase.generic.StableID;
import org.ensembl.healthcheck.testcase.generic.Strand;
import org.ensembl.healthcheck.testcase.generic.SubCodonTranscript;
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
 */
public class EGCoreIntegrity extends GroupOfTests {

    public EGCoreIntegrity() {
        addTest(
                AssemblyExceptionTableSeqRegionAttribute.class,
                AssemblyExceptionTableIntegrity.class,
                AssemblyExceptionTableSeqMapping.class,
                AssemblyExceptionTableUniqueRegion.class,
                AssemblyExceptionTableStartEnd.class,
                AssemblySeqregion.class,
                CanonicalTranscriptCoding.class, AssemblyDefault.class, CircularAwareFeatureCoords.class,
                CoreForeignKeys.class, DuplicateAssembly.class,
                DuplicateTopLevel.class, ExonRank.class,
                ExonTranscriptStartEnd.class, GeneCoordSystem.class,
                NullTranscripts.class, PeptideTranslationAttribs.class,
                ProteinCodingGene.class, SeqRegionAttribsPresent.class,
                SeqRegionsTopLevel.class, StableID.class, Strand.class,
                TranscriptsTranslate.class, TranslationStartEnd.class,
                TranslationStartEndExon.class, ProteinTranslation.class,
                AssemblyMapping.class, ValidSeqEnd.class,
                ExonBoundary.class, InappropriateTranslation.class,
                DensityFeatures.class, DescriptionNewlines.class, DisplayLabels.class, GeneDescriptions.class, PositiveCoordinates.class,
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
                ProductionBiotypes.class,
                ProductionMasterTables.class, AnalysisLogicName.class,
                SeqRegionsConsistentWithComparaMaster.class,
                SeqRegionAttribForPolyploidGenome.class,
                SeqRegionAttribForPolyploidGenomeToplevelOnly.class, SeqRegionLength.class, EvidenceFreeGO.class,
                VersionedGenes.class, VersionedTranscripts.class, VersionedTranslations.class, IncorrectExonRank.class, CompareSchema.class,
                SubCodonTranscript.class);

    }

}
