/*
 * Copyright [1999-2014] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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
import org.ensembl.healthcheck.testcase.eg_funcgen.EGArrayXrefs;
import org.ensembl.healthcheck.testcase.funcgen.AnalysisDescription;
import org.ensembl.healthcheck.testcase.funcgen.CompareFuncgenSchema;
import org.ensembl.healthcheck.testcase.funcgen.ComparePreviousVersionArrayXrefs;
import org.ensembl.healthcheck.testcase.funcgen.FuncgenForeignKeys;
import org.ensembl.healthcheck.testcase.funcgen.FuncgenStableID;
import org.ensembl.healthcheck.testcase.funcgen.MetaCoord;
import org.ensembl.healthcheck.testcase.funcgen.RegulatoryFeatureTypes;
import org.ensembl.healthcheck.testcase.generic.BlankCoordSystemVersions;
import org.ensembl.healthcheck.testcase.generic.BlankEnums;
import org.ensembl.healthcheck.testcase.generic.BlankInfoType;
import org.ensembl.healthcheck.testcase.generic.BlanksInsteadOfNulls;
import org.ensembl.healthcheck.testcase.generic.ExternalDBDisplayName;
import org.ensembl.healthcheck.testcase.generic.NullStrings;

/**
 * Tests to run on an Ensembl Genomes funcgen database
 * 
 */
public class EGFuncgen extends GroupOfTests {

	public EGFuncgen() {

		addTest(

			EGCommon.class,
			
			BlankEnums.class, 
			BlankInfoType.class, 
			MetaCoord.class,
			ExternalDBDisplayName.class, 
			EGArrayXrefs.class,
			FuncgenForeignKeys.class, 
			// Removed, because schema doesn't allow for nulls in version
			// column
			//BlankCoordSystemVersions.class,
			BlanksInsteadOfNulls.class, 
			RegulatoryFeatureTypes.class,
			ComparePreviousVersionArrayXrefs.class,
			AnalysisDescription.class, 
			FuncgenStableID.class,
			NullStrings.class, 
			CompareFuncgenSchema.class
		);
	}
}