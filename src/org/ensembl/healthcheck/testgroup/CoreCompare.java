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
 * These are the tests that run comparisons between databases. The tests are:
 * 
 * <ul>
 *   <li> org.ensembl.healthcheck.testcase.generic.AssemblyTablesAcrossSpecies </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.AttribTypeAcrossSpecies </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionBiotypes </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousDatabases </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionExonCoords </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionRegionSynonyms </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionRepeatTypes </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionTableRows </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.CoordSystemAcrossSpecies </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.MetaCrossSpecies </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.SeqRegionAcrossSpecies </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.SeqRegionAttribAcrossSpecies </li>
 * </ul>
 *
 * @author Thibaut Hourlier
 *
 */
public class CoreCompare extends GroupOfTests {
	
	public CoreCompare() {

		addTest(
                        org.ensembl.healthcheck.testcase.generic.AssemblyTablesAcrossSpecies.class,
                        org.ensembl.healthcheck.testcase.generic.AttribTypeAcrossSpecies.class,
                        org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionBiotypes.class,
                        org.ensembl.healthcheck.testcase.generic.ComparePreviousDatabases.class,
                        org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionExonCoords.class,
                        org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionRegionSynonyms.class,
                        org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionRepeatTypes.class,
                        org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionTableRows.class,
                        org.ensembl.healthcheck.testcase.generic.CoordSystemAcrossSpecies.class,
                        org.ensembl.healthcheck.testcase.generic.MetaCrossSpecies.class,
                        org.ensembl.healthcheck.testcase.generic.SeqRegionAcrossSpecies.class,
                        org.ensembl.healthcheck.testcase.generic.SeqRegionAttribAcrossSpecies.class
		);
	}
}
