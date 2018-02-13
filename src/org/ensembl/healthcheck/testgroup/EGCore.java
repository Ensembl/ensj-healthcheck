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
import org.ensembl.healthcheck.testcase.generic.*;

import org.ensembl.healthcheck.testcase.generic.AnalysisTypes;
import org.ensembl.healthcheck.testcase.generic.ProductionBiotypes;
import org.ensembl.healthcheck.testcase.generic.TranscriptNames;

import org.ensembl.healthcheck.testcase.eg_core.NoRepeatFeatures;
import org.ensembl.healthcheck.testcase.generic.CompareSchema;
import org.ensembl.healthcheck.testcase.eg_core.SeqRegionLength;
import org.ensembl.healthcheck.testcase.eg_core.SeqRegionsConsistentWithComparaMaster;
import org.ensembl.healthcheck.testcase.eg_core.SeqRegionAttribForPolyploidGenome;
import org.ensembl.healthcheck.testcase.eg_core.SeqRegionAttribForPolyploidGenomeToplevelOnly;

/**
 * Supergroup of tests for Ensembl Genomes (incorporates {@link EGCoreGeneModel}
 * , {@link EGCoreMeta}, {@link EGCoreAnnotation} and {@link EGCoreCompare})
 * 
 * @author dstaines
 * 
 */
public class EGCore extends GroupOfTests {

	public EGCore() {

		setDescription("Supergroup of tests for core databases from Ensembl Genomes.");

		addTest(
			EGCoreGeneModel.class, 
			EGCoreMeta.class,
			EGCoreAnnotation.class, 
			EGCoreCompare.class, 
			EGCommon.class,
			EGCoreMulti.class,
			AnalysisTypes.class,
			ProductionAnalysisLogicName.class,
			ProductionBiotypes.class,
			TranscriptNames.class, 
			ProductionMasterTables.class,
			AnalysisLogicName.class,
			SeqRegionsConsistentWithComparaMaster.class,
			SeqRegionAttribForPolyploidGenome.class,
			SeqRegionAttribForPolyploidGenomeToplevelOnly.class,
			SeqRegionLength.class,
			NoRepeatFeatures.class,
			CompareSchema.class
		);
	}
}
