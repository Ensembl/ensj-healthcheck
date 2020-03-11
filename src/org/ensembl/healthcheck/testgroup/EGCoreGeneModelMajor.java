/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2020] EMBL-European Bioinformatics Institute
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
import org.ensembl.healthcheck.testcase.eg_core.ExonBoundary;
import org.ensembl.healthcheck.testcase.eg_core.EgProteinFeatureTranslation;
import org.ensembl.healthcheck.testcase.eg_core.DuplicateProteinFeature;
import org.ensembl.healthcheck.testcase.eg_core.InappropriateTranslation;
import org.ensembl.healthcheck.testcase.eg_core.SeqRegionNaming;
import org.ensembl.healthcheck.testcase.generic.BiotypeGroups;
import org.ensembl.healthcheck.testcase.generic.DuplicateExons;
import org.ensembl.healthcheck.testcase.generic.DuplicateGenes;
import org.ensembl.healthcheck.testcase.generic.GeneCoordSystem;
import org.ensembl.healthcheck.testcase.generic.Pseudogene;
import org.ensembl.healthcheck.testcase.generic.SpeciesID;

/**
 * Group of tests that should be run to assess gene models for Ensembl Genomes.
 * Failures/warnings are acceptable with explanations.
 * 
 * @author dstaines
 * 
 */
public class EGCoreGeneModelMajor extends GroupOfTests {

	public EGCoreGeneModelMajor() {
		addTest(
				BiotypeGroups.class,
				DuplicateExons.class, 
				DuplicateGenes.class, 
				ExonBoundary.class,
				GeneCoordSystem.class, 
				Pseudogene.class, 
				SpeciesID.class,
				EgProteinFeatureTranslation.class,
                DuplicateProteinFeature.class,
				InappropriateTranslation.class,
				SeqRegionNaming.class);
	}
}
