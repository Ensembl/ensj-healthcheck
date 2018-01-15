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

/**
 * These are the tests that register themselves as variation-release. The tests are:
 * 
 * <ul>
 *   <li> org.ensembl.healthcheck.testcase.variation.CheckChar </li> 
 *   <li> org.ensembl.healthcheck.testcase.variation.ComparePreviousVersionAlleles </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.ComparePreviousVersionConsequenceType </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.ComparePreviousVersionGenotypes </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.ComparePreviousVersionPhenotypeFeatures </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.ComparePreviousVersionReadCoverage </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.ComparePreviousVersionSampleDisplay </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.ComparePreviousVersionSources </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.ComparePreviousVersionStructuralVariations </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.ComparePreviousVersionValidationStatus </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.ComparePreviousVersionVariationFeatures </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.ComparePreviousVersionVariationSets </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.ComparePreviousVersionVariationSynonyms </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.ComparePreviousVersionVariations </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.CompareVariationSchema </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.CompressedGenotypeRegion </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.Denormalized </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.EmptyVariationTables </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.ForeignKeyCoreId </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.IndividualType </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.Meta </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.Meta_coord </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.Phenotype </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.PhenotypeFeatureAttrib </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.Population </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.Publication </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.StructuralVariation </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.TranscriptVariation </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.Variation </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.VariationClasses </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.VariationFeature </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.VariationForeignKeys </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.VariationSet </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.VariationSynonym </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.MySQLStorageEngine </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.SchemaType </li>
 * </ul>
 *
 * @author Thomas Maurel
 *
 */
public class VariationRelease extends GroupOfTests {
	
	public VariationRelease() {

		addTest(
			org.ensembl.healthcheck.testcase.variation.CheckChar.class,
			org.ensembl.healthcheck.testcase.variation.ComparePreviousVersionAlleles.class,
			org.ensembl.healthcheck.testcase.variation.ComparePreviousVersionConsequenceType.class,
			org.ensembl.healthcheck.testcase.variation.ComparePreviousVersionGenotypes.class,
			org.ensembl.healthcheck.testcase.variation.ComparePreviousVersionPhenotypeFeatures.class,
			org.ensembl.healthcheck.testcase.variation.ComparePreviousVersionReadCoverage.class,
			org.ensembl.healthcheck.testcase.variation.ComparePreviousVersionSampleDisplay.class,
			org.ensembl.healthcheck.testcase.variation.ComparePreviousVersionSources.class,
			org.ensembl.healthcheck.testcase.variation.ComparePreviousVersionStructuralVariations.class,
			org.ensembl.healthcheck.testcase.variation.ComparePreviousVersionValidationStatus.class,
			org.ensembl.healthcheck.testcase.variation.ComparePreviousVersionVariationClasses.class,
			org.ensembl.healthcheck.testcase.variation.ComparePreviousVersionVariationFeatures.class,
			org.ensembl.healthcheck.testcase.variation.ComparePreviousVersionVariationSets.class,
			org.ensembl.healthcheck.testcase.variation.ComparePreviousVersionVariationSynonyms.class,
			org.ensembl.healthcheck.testcase.variation.ComparePreviousVersionVariations.class,
			org.ensembl.healthcheck.testcase.variation.CompareVariationSchema.class,
			org.ensembl.healthcheck.testcase.variation.CompressedGenotypeRegion.class,
			org.ensembl.healthcheck.testcase.variation.Denormalized.class,
			org.ensembl.healthcheck.testcase.variation.EmptyVariationTables.class,
			org.ensembl.healthcheck.testcase.variation.ForeignKeyCoreId.class,
			org.ensembl.healthcheck.testcase.variation.IndividualType.class,
			org.ensembl.healthcheck.testcase.variation.Meta.class,
			org.ensembl.healthcheck.testcase.variation.Meta_coord.class,
			org.ensembl.healthcheck.testcase.variation.Phenotype.class,
			org.ensembl.healthcheck.testcase.variation.PhenotypeFeatureAttrib.class,
			org.ensembl.healthcheck.testcase.variation.Population.class,
			org.ensembl.healthcheck.testcase.variation.Publication.class,
			org.ensembl.healthcheck.testcase.variation.StructuralVariation.class,
			org.ensembl.healthcheck.testcase.variation.TranscriptVariation.class,
			org.ensembl.healthcheck.testcase.variation.Variation.class,
			org.ensembl.healthcheck.testcase.variation.VariationClasses.class,
			org.ensembl.healthcheck.testcase.variation.VariationFeature.class,
			org.ensembl.healthcheck.testcase.variation.VariationForeignKeys.class,
      org.ensembl.healthcheck.testcase.variation.VariationFeatureAlleles.class,
			org.ensembl.healthcheck.testcase.variation.VariationSet.class,
			org.ensembl.healthcheck.testcase.variation.VariationSynonym.class,
      org.ensembl.healthcheck.testcase.generic.MySQLStorageEngine.class,
      org.ensembl.healthcheck.testcase.generic.SchemaType.class
		);
	}
}
