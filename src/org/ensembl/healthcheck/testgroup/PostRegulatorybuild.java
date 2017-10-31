/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2017] EMBL-European Bioinformatics Institute
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
 * These are the tests that register themselves as post_regulatorybuild. The tests are:
 * 
 * <ul>
 *   <li> org.ensembl.healthcheck.testcase.funcgen.CheckResultSetDBFileLink </li> 
 *   <li> org.ensembl.healthcheck.testcase.funcgen.FuncgenStableID </li> 
 *   <li> org.ensembl.healthcheck.testcase.funcgen.FuncgenAnalysisDescription </li> 
 *   <li> org.ensembl.healthcheck.testcase.funcgen.FuncgenForeignKeys </li> 
 *   <li> org.ensembl.healthcheck.testcase.funcgen.RegulatoryFeatureTypes </li> 
 *   <li> org.ensembl.healthcheck.testcase.funcgen.RegulatoryMotifFeatures </li> 
 *   <li> org.ensembl.healthcheck.testcase.funcgen.RegulatorySets </li> 
 * </ul>
 *
 * @author Thomas Maurel
 *
 */
public class PostRegulatorybuild extends GroupOfTests {
	
	public PostRegulatorybuild() {

		addTest(
//			org.ensembl.healthcheck.testcase.funcgen.CheckResultSetDBFileLink.class,
			org.ensembl.healthcheck.testcase.funcgen.FuncgenStableID.class,
			org.ensembl.healthcheck.testcase.funcgen.FuncgenAnalysisDescription.class,
			org.ensembl.healthcheck.testcase.funcgen.FuncgenForeignKeys.class,
			org.ensembl.healthcheck.testcase.funcgen.RegulatoryFeatureTypes.class,
			org.ensembl.healthcheck.testcase.funcgen.RegulatoryMotifFeatures.class,
			org.ensembl.healthcheck.testcase.funcgen.RegulatorySets.class
		);
	}
}