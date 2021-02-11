/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2021] EMBL-European Bioinformatics Institute
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
import org.ensembl.healthcheck.testcase.eg_funcgen.EGCompareFuncgenSchema;
import org.ensembl.healthcheck.testcase.funcgen.ComparePreviousVersionArrayXrefs;
import org.ensembl.healthcheck.testcase.funcgen.FuncgenForeignKeys;
import org.ensembl.healthcheck.testcase.funcgen.FuncgenStableID;
import org.ensembl.healthcheck.testcase.funcgen.MetaCoord;
import org.ensembl.healthcheck.testcase.funcgen.ProbeIdsUnique;
import org.ensembl.healthcheck.testcase.funcgen.ProbeSetTranscriptMappingsUnique;
import org.ensembl.healthcheck.testcase.funcgen.ProbeTranscriptMappingsUnique;
import org.ensembl.healthcheck.testcase.funcgen.RegulatoryFeatureTypes;

/**
 * Tests to run on an Ensembl Genomes funcgen database
 * 
 */
public class EGFuncgen extends GroupOfTests {

	public EGFuncgen() {
		addTest(EGCommon.class, MetaCoord.class,
				FuncgenForeignKeys.class,
				RegulatoryFeatureTypes.class,
				ComparePreviousVersionArrayXrefs.class, FuncgenStableID.class,
				EGCompareFuncgenSchema.class,
				ProbeIdsUnique.class,
				ProbeTranscriptMappingsUnique.class,
				ProbeSetTranscriptMappingsUnique.class);
	}
}
