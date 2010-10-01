package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.eg_core.PeptideTranslationAttribs;
import org.ensembl.healthcheck.testcase.eg_core.ProteinCodingGene;
import org.ensembl.healthcheck.testcase.generic.AssemblyException;
import org.ensembl.healthcheck.testcase.generic.AssemblyMultipleOverlap;
import org.ensembl.healthcheck.testcase.generic.AssemblySeqregion;
import org.ensembl.healthcheck.testcase.generic.CanonicalTranscriptCoding;
import org.ensembl.healthcheck.testcase.generic.CompareSchema;
import org.ensembl.healthcheck.testcase.generic.CoreForeignKeys;
import org.ensembl.healthcheck.testcase.generic.DuplicateAssembly;
import org.ensembl.healthcheck.testcase.generic.ExonRank;
import org.ensembl.healthcheck.testcase.generic.ExonStrandOrder;
import org.ensembl.healthcheck.testcase.generic.ExonTranscriptStartEnd;
import org.ensembl.healthcheck.testcase.generic.FeatureCoords;
import org.ensembl.healthcheck.testcase.generic.GeneCoordSystem;
import org.ensembl.healthcheck.testcase.generic.Karyotype;
import org.ensembl.healthcheck.testcase.generic.NullTranscripts;
import org.ensembl.healthcheck.testcase.generic.SeqRegionAttribsPresent;
import org.ensembl.healthcheck.testcase.generic.SeqRegionCoordSystem;
import org.ensembl.healthcheck.testcase.generic.SeqRegionsTopLevel;
import org.ensembl.healthcheck.testcase.generic.StableID;
import org.ensembl.healthcheck.testcase.generic.Strand;
import org.ensembl.healthcheck.testcase.generic.TranscriptsTranslate;
import org.ensembl.healthcheck.testcase.generic.TranslationStartEnd;
import org.ensembl.healthcheck.testcase.generic.TranslationStartEndExon;

/**
 * Group of tests that should be run to assess gene models for Ensembl Genomes.
 * Failures/warnings are not acceptable.
 * 
 * @author dstaines
 * 
 */
public class EGGeneModelCritical extends GroupOfTests {

	public EGGeneModelCritical() {
		addTest(AssemblyException.class, AssemblyMultipleOverlap.class,
				AssemblySeqregion.class, CanonicalTranscriptCoding.class,
				CompareSchema.class, CoreForeignKeys.class,
				DuplicateAssembly.class, ExonRank.class, ExonStrandOrder.class,
				ExonTranscriptStartEnd.class, FeatureCoords.class,
				GeneCoordSystem.class, Karyotype.class, NullTranscripts.class,
				PeptideTranslationAttribs.class, ProteinCodingGene.class,
				SeqRegionAttribsPresent.class, SeqRegionCoordSystem.class,
				SeqRegionsTopLevel.class, StableID.class, Strand.class,
				TranscriptsTranslate.class, TranslationStartEnd.class,
				TranslationStartEndExon.class);
	}

}
