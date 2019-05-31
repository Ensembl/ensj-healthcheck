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
import org.ensembl.healthcheck.testcase.eg_core.DuplicateTaxonomyIds;
import org.ensembl.healthcheck.testcase.eg_core.EnaProvider;
import org.ensembl.healthcheck.testcase.eg_core.GoTermCount;
import org.ensembl.healthcheck.testcase.eg_core.InterproHitCount;
import org.ensembl.healthcheck.testcase.eg_core.NoRepeatFeatures;
import org.ensembl.healthcheck.testcase.eg_core.RepeatAnalysesInMeta;
import org.ensembl.healthcheck.testcase.eg_core.SeqRegionsConsistentWithComparaMaster;
import org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionExonCoords;
import org.ensembl.healthcheck.testcase.generic.Karyotype;
import org.ensembl.healthcheck.testcase.generic.StableID;
import org.ensembl.healthcheck.testcase.generic.XrefVersions;

/**
 * Supergroup of tests for Ensembl Bacteria - based on {@link EGCore} but
 * removing tests that are inappropriate for Ensembl Bacteria.
 * 
 * @author dstaines
 * 
 */
public class EBCore extends GroupOfTests {

	public EBCore() {

		setDescription("Supergroup of tests for core databases from Ensembl Bacteria.");

		addTest(EGCore.class);
		removeTest(DuplicateTaxonomyIds.class, EnaProvider.class,
				Karyotype.class, GoTermCount.class, InterproHitCount.class,
				NoRepeatFeatures.class,
				RepeatAnalysesInMeta.class, StableID.class, XrefVersions.class,
				SeqRegionsConsistentWithComparaMaster.class,
				ComparePreviousVersionExonCoords.class, Karyotype.class,
				EnaProvider.class);
	}
}
