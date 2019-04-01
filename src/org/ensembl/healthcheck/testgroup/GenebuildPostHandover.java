/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
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
 * These are the critical checks to run once Genebuild have handed over the core
 * databases.
 */
public class GenebuildPostHandover extends GroupOfTests {

  public GenebuildPostHandover() {
    addTest(
      org.ensembl.healthcheck.testcase.generic.AltAllele.class,
      org.ensembl.healthcheck.testcase.generic.AltAlleleGroup.class,
      org.ensembl.healthcheck.testcase.generic.AssemblyExceptionTableIntegrity.class,
      org.ensembl.healthcheck.testcase.generic.AssemblyExceptionTableSeqMapping.class,
      org.ensembl.healthcheck.testcase.generic.AssemblyExceptionTableSeqRegionAttribute.class,
      org.ensembl.healthcheck.testcase.generic.AssemblyExceptionTableStartEnd.class,
      org.ensembl.healthcheck.testcase.generic.AssemblyExceptionTableUniqueRegion.class,
      org.ensembl.healthcheck.testcase.generic.AssemblySeqregion.class,
      org.ensembl.healthcheck.testcase.generic.AssemblyTablesAcrossSpecies.class,
      org.ensembl.healthcheck.testcase.generic.CompareSchema.class,
      org.ensembl.healthcheck.testcase.generic.CoordSystemAcrossSpecies.class,
      org.ensembl.healthcheck.testcase.generic.DNAEmpty.class,
      org.ensembl.healthcheck.testcase.generic.EmptyTables.class,
      org.ensembl.healthcheck.testcase.generic.ExonRank.class,
      org.ensembl.healthcheck.testcase.generic.ExonStrandOrder.class,
      org.ensembl.healthcheck.testcase.generic.ExonTranscriptStartEnd.class,
      org.ensembl.healthcheck.testcase.generic.GeneTranscriptStartEnd.class,
      org.ensembl.healthcheck.testcase.generic.MTCodonTable.class,
      org.ensembl.healthcheck.testcase.generic.NullTranscripts.class,
      org.ensembl.healthcheck.testcase.generic.PredictionTranscriptHasExons.class,
      org.ensembl.healthcheck.testcase.eg_core.ProteinTranslation.class,
      org.ensembl.healthcheck.testcase.generic.Pseudogene.class,
      org.ensembl.healthcheck.testcase.generic.RepeatFeature.class,
      org.ensembl.healthcheck.testcase.generic.SpeciesID.class,
      org.ensembl.healthcheck.testcase.generic.TranscriptsTranslate.class,
      org.ensembl.healthcheck.testcase.generic.TranslationCheckSeqStart.class,
      org.ensembl.healthcheck.testcase.generic.TranslationCheckZeroLength.class,
      org.ensembl.healthcheck.testcase.generic.TranslationStartEnd.class,
      org.ensembl.healthcheck.testcase.generic.TranslationStartEndExon.class
    );
  }
}
