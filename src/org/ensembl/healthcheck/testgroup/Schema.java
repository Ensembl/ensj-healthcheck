/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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
 * These are the tests that checking schema and mysql. The tests are:
 * 
 * <ul>
 *   <li> org.ensembl.healthcheck.testcase.generic.AnalyseTables </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.AutoIncrement </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.CompareSchema </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.MySQLStorageEngine </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.PartitionedTables </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.SchemaType </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.SingleDBCollations </li>
 * </ul>
 *
 * @author Thibaut Hourlier
 *
 */
public class Schema extends GroupOfTests {
	
	public Schema() {

		addTest(
                        org.ensembl.healthcheck.testcase.generic.AnalyseTables.class,
                        org.ensembl.healthcheck.testcase.generic.AutoIncrement.class,
                        org.ensembl.healthcheck.testcase.generic.CompareSchema.class,
                        org.ensembl.healthcheck.testcase.generic.MySQLStorageEngine.class,
                        org.ensembl.healthcheck.testcase.generic.PartitionedTables.class,
                        org.ensembl.healthcheck.testcase.generic.SchemaType.class,
                        org.ensembl.healthcheck.testcase.generic.SingleDBCollations.class
		);
	}
}
