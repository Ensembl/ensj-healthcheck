/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2018] EMBL-European Bioinformatics Institute
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
import org.ensembl.healthcheck.testcase.eg_core.CircularAwareFeatureCoords;
import org.ensembl.healthcheck.testcase.eg_core.DuplicateTopLevel;
import org.ensembl.healthcheck.testcase.eg_core.EGCompareCoreSchema;
import org.ensembl.healthcheck.testcase.eg_core.ENASeqRegionSynonyms;
import org.ensembl.healthcheck.testcase.eg_core.EnaSeqRegionName;
import org.ensembl.healthcheck.testcase.eg_core.MultipleENASeqRegionSynonyms;
import org.ensembl.healthcheck.testcase.eg_core.PeptideTranslationAttribs;
import org.ensembl.healthcheck.testcase.eg_core.ProteinCodingGene;
import org.ensembl.healthcheck.testcase.eg_core.ProteinTranslation;
import org.ensembl.healthcheck.testcase.eg_core.SeqRegionCoordSystem;
import org.ensembl.healthcheck.testcase.eg_core.SimpleFeatureAnalysisTypes;
import org.ensembl.healthcheck.testcase.eg_core.ValidSeqEnd;
import org.ensembl.healthcheck.testcase.eg_core.VersionedExons;
import org.ensembl.healthcheck.testcase.eg_core.VersionedGenes;
import org.ensembl.healthcheck.testcase.eg_core.VersionedTranscripts;
import org.ensembl.healthcheck.testcase.eg_core.VersionedTranslations;
import org.ensembl.healthcheck.testcase.generic.AssemblyExceptionTableSeqRegionAttribute;
import org.ensembl.healthcheck.testcase.generic.AssemblyExceptionTableIntegrity;
import org.ensembl.healthcheck.testcase.generic.AssemblyExceptionTableSeqMapping;
import org.ensembl.healthcheck.testcase.generic.AssemblyExceptionTableUniqueRegion;
import org.ensembl.healthcheck.testcase.generic.AssemblyExceptionTableStartEnd;
import org.ensembl.healthcheck.testcase.generic.AssemblyMapping;
import org.ensembl.healthcheck.testcase.generic.AssemblySeqregion;
import org.ensembl.healthcheck.testcase.generic.CanonicalTranscriptCoding;
import org.ensembl.healthcheck.testcase.generic.CoreForeignKeys;
import org.ensembl.healthcheck.testcase.generic.DuplicateAssembly;
import org.ensembl.healthcheck.testcase.generic.ExonRank;
import org.ensembl.healthcheck.testcase.generic.ExonStrandOrder;
import org.ensembl.healthcheck.testcase.generic.ExonTranscriptStartEnd;
import org.ensembl.healthcheck.testcase.generic.GeneCoordSystem;
import org.ensembl.healthcheck.testcase.generic.Karyotype;
import org.ensembl.healthcheck.testcase.generic.NullTranscripts;
import org.ensembl.healthcheck.testcase.generic.SeqRegionAttribsPresent;
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
public class EGCoreGeneModelCritical extends GroupOfTests {

	public EGCoreGeneModelCritical() {
		addTest(
			AssemblyExceptionTableIntegrity.class,
			AssemblyExceptionTableSeqMapping.class,
			AssemblyExceptionTableSeqRegionAttribute.class,
			AssemblyExceptionTableUniqueRegion.class,
			AssemblyExceptionTableStartEnd.class, 
			//AssemblyMultipleOverlap.class,
			AssemblySeqregion.class, 
			CanonicalTranscriptCoding.class,
			CircularAwareFeatureCoords.class, 
			EGCompareCoreSchema.class,
			CoreForeignKeys.class,
			DuplicateAssembly.class, 
			DuplicateTopLevel.class,
			ExonRank.class, 
			ExonStrandOrder.class,
			ExonTranscriptStartEnd.class, 
			GeneCoordSystem.class,
			Karyotype.class, 
			NullTranscripts.class,
			PeptideTranslationAttribs.class, 
			ProteinCodingGene.class,
			SeqRegionAttribsPresent.class, 
			SeqRegionCoordSystem.class,
			SeqRegionsTopLevel.class,
			StableID.class, 
			Strand.class,
			TranscriptsTranslate.class, 
			TranslationStartEnd.class,
			TranslationStartEndExon.class, 
			ProteinTranslation.class,
			EnaSeqRegionName.class,
			SimpleFeatureAnalysisTypes.class,
			AssemblyMapping.class,
			ENASeqRegionSynonyms.class,
			MultipleENASeqRegionSynonyms.class,
		    ValidSeqEnd.class,
		    VersionedExons.class,
		    VersionedGenes.class,
			VersionedTranscripts.class, 
			VersionedTranslations.class
		);
	}
}
