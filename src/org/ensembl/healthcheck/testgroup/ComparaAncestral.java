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

/**
 * These are the tests that register themselves as compara-ancestral. The tests are:
 * 
 * <ul>
 *   <li> org.ensembl.healthcheck.testcase.generic.AncestralSequencesExtraChecks </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.AnalysisLogicName </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.AssemblyException </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.AssemblyMultipleOverlap </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.AssemblySeqregion </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.BlankCoordSystemVersions </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.BlankEnums </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.BlanksInsteadOfNulls </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.CoreForeignKeys </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.DuplicateAssembly </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.EmptyTables </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.Meta </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.MetaValues </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.NullStrings </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.SeqRegionCoordSystem </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.SeqRegionName </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.SingleDBCollations </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.SpeciesID </li> 
 * </ul>
 *
 * @author Thomas Maurel
 *
 */
public class ComparaAncestral extends GroupOfTests {
	
	public ComparaAncestral() {

		addTest(
			org.ensembl.healthcheck.testcase.generic.AncestralSequencesExtraChecks.class,
			org.ensembl.healthcheck.testcase.generic.AnalysisLogicName.class,
			org.ensembl.healthcheck.testcase.generic.AssemblyExceptionTableSeqRegionAttribute.class,
			org.ensembl.healthcheck.testcase.generic.AssemblyExceptionTableIntegrity.class,
			org.ensembl.healthcheck.testcase.generic.AssemblyExceptionTableSeqMapping.class,
			org.ensembl.healthcheck.testcase.generic.AssemblyExceptionTableUniqueRegion.class,
			org.ensembl.healthcheck.testcase.generic.AssemblyExceptionTableStartEnd.class,
			org.ensembl.healthcheck.testcase.generic.AssemblySeqregion.class,
			org.ensembl.healthcheck.testcase.generic.BlankCoordSystemVersions.class,
			org.ensembl.healthcheck.testcase.generic.BlankEnums.class,
			org.ensembl.healthcheck.testcase.generic.BlanksInsteadOfNulls.class,
			org.ensembl.healthcheck.testcase.generic.CoreForeignKeys.class,
			org.ensembl.healthcheck.testcase.generic.DuplicateAssembly.class,
			org.ensembl.healthcheck.testcase.generic.EmptyTables.class,
			org.ensembl.healthcheck.testcase.generic.Meta.class,
			org.ensembl.healthcheck.testcase.generic.MetaValues.class,
			org.ensembl.healthcheck.testcase.generic.MySQLStorageEngine.class,
			org.ensembl.healthcheck.testcase.generic.NullStrings.class,
			org.ensembl.healthcheck.testcase.generic.SeqRegionCoordSystem.class,
			org.ensembl.healthcheck.testcase.generic.SeqRegionName.class,
			org.ensembl.healthcheck.testcase.generic.SingleDBCollations.class,
			org.ensembl.healthcheck.testcase.generic.SpeciesID.class
		);
	}
}
