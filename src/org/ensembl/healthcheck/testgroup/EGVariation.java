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
import org.ensembl.healthcheck.testcase.eg_core.EGCompareVariationSchema;
import org.ensembl.healthcheck.testcase.eg_variation.EGVariationFeature;
import org.ensembl.healthcheck.testcase.variation.AlleleFrequencies;
import org.ensembl.healthcheck.testcase.variation.CompareVariationSchema;
import org.ensembl.healthcheck.testcase.variation.EmptyVariationTables;
import org.ensembl.healthcheck.testcase.variation.FlankingUpDownSeq;
import org.ensembl.healthcheck.testcase.variation.ForeignKeyCoreId;
import org.ensembl.healthcheck.testcase.variation.IndividualType;
import org.ensembl.healthcheck.testcase.variation.Meta;
import org.ensembl.healthcheck.testcase.variation.Meta_coord;
import org.ensembl.healthcheck.testcase.variation.StructuralVariation;
import org.ensembl.healthcheck.testcase.variation.TranscriptVariation;
import org.ensembl.healthcheck.testcase.variation.VariationForeignKeys;
import org.ensembl.healthcheck.testcase.variation.VariationSet;
import org.ensembl.healthcheck.testcase.variation.VFCoordinates;

/**
 * Group of tests for variation databases
 * 
 * @author dstaines
 * 
 */
public class EGVariation extends GroupOfTests {

	public EGVariation() {
		addTest(
			EGCommon.class,
			AlleleFrequencies.class,
			EGCompareVariationSchema.class,
			EGVariationFeature.class,
			EmptyVariationTables.class, 
			ForeignKeyCoreId.class, 
			IndividualType.class, 
			Meta_coord.class,
			Meta.class, 
			StructuralVariation.class,
			TranscriptVariation.class, 
			VariationForeignKeys.class,
			VariationSet.class, 
			VFCoordinates.class
		);
	}
}
