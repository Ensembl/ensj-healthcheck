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

/**
 * These are the tests that run after handover to production. The tests are:
 * 
 * <ul>
 *   <li> org.ensembl.healthcheck.testcase.variation.CompareVariationSchema </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.ForeignKeyCoreId </li>
 *   <li> org.ensembl.healthcheck.testcase.variation.ForeignKeyFuncgenId </li>
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
			org.ensembl.healthcheck.testcase.variation.CompareVariationSchema.class,
			org.ensembl.healthcheck.testcase.variation.ForeignKeyCoreId.class,
			org.ensembl.healthcheck.testcase.variation.ForeignKeyFuncgenId.class,
      org.ensembl.healthcheck.testcase.generic.MySQLStorageEngine.class,
      org.ensembl.healthcheck.testcase.generic.SchemaType.class
		);
	}
}
