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
import org.ensembl.healthcheck.testcase.funcgen.AlignmentHasBamFile;

/**
 * These are the tests that register themselves as funcgen-release. The tests are:
 *
 * <ul>
 *   <li> org.ensembl.healthcheck.testcase.generic.AnalyseTables </li>
 *   <li> org.ensembl.healthcheck.testcase.funcgen.ArrayXrefs </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.BlankCoordSystemVersions </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.BlankEnums </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.BlankInfoType </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.BlanksInsteadOfNulls </li>
 *   <li> org.ensembl.healthcheck.testcase.funcgen.CheckResultSetDBFileLink </li>
 *   <li> org.ensembl.healthcheck.testcase.funcgen.CompareFuncgenSchema </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionAnalysisDescriptions </li>
 *   <li> org.ensembl.healthcheck.testcase.funcgen.ComparePreviousVersionArrayXrefs </li>
 *   <li> org.ensembl.healthcheck.testcase.funcgen.ComparePreviousVersionRegulatoryFeatures </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ExternalDBDisplayName </li>
 *   <li> org.ensembl.healthcheck.testcase.funcgen.FeaturePosition </li>
 *   <li> org.ensembl.healthcheck.testcase.funcgen.FuncgenAnalysisDescription </li>
 *   <li> org.ensembl.healthcheck.testcase.funcgen.FuncgenForeignKeys </li>
 *   <li> org.ensembl.healthcheck.testcase.funcgen.FuncgenStableID </li>
 *   <li> org.ensembl.healthcheck.testcase.funcgen.MetaCoord </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.NullStrings </li>
 *   <li> org.ensembl.healthcheck.testcase.funcgen.RegulatoryFeatureTypes </li>
 *   <li> org.ensembl.healthcheck.testcase.funcgen.RegulatoryMotifFeatures </li>
 *   <li> org.ensembl.healthcheck.testcase.funcgen.RegulatorySets </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.SpeciesID </li>
 * </ul>
 *
 * @author Thomas Maurel
 *
 */
public class FuncgenRelease extends GroupOfTests {

	public FuncgenRelease() {

		addTest(
			//org.ensembl.healthcheck.testcase.generic.AnalyseTables.class,
			org.ensembl.healthcheck.testcase.generic.BlankEnums.class,
			org.ensembl.healthcheck.testcase.generic.BlankInfoType.class,
			org.ensembl.healthcheck.testcase.generic.BlanksInsteadOfNulls.class,
			// org.ensembl.healthcheck.testcase.funcgen.CheckResultSetDBFileLink.class,
			org.ensembl.healthcheck.testcase.funcgen.CompareFuncgenSchema.class,
			org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionAnalysisDescriptions.class,
			// org.ensembl.healthcheck.testcase.funcgen.ComparePreviousVersionRegulatoryFeatures.class,
			org.ensembl.healthcheck.testcase.generic.ExternalDBDisplayName.class,
			org.ensembl.healthcheck.testcase.funcgen.FeaturePosition.class,
			// Deactivated until we delete the non current seq regions.
			//org.ensembl.healthcheck.testcase.funcgen.FeaturesMappedToCurrentSeqRegion.class,
			org.ensembl.healthcheck.testcase.funcgen.FuncgenAnalysisDescription.class,
			org.ensembl.healthcheck.testcase.funcgen.FuncgenForeignKeys.class,
			org.ensembl.healthcheck.testcase.funcgen.FuncgenStableID.class,
			org.ensembl.healthcheck.testcase.funcgen.MetaCoord.class,
			org.ensembl.healthcheck.testcase.generic.MySQLStorageEngine.class,
			org.ensembl.healthcheck.testcase.generic.NullStrings.class,
			// org.ensembl.healthcheck.testcase.funcgen.RegulatoryFeatureTypes.class,
			// org.ensembl.healthcheck.testcase.funcgen.RegulatoryMotifFeatures.class,
			// org.ensembl.healthcheck.testcase.funcgen.RegulatorySets.class,
			org.ensembl.healthcheck.testcase.generic.SchemaType.class,
			org.ensembl.healthcheck.testcase.generic.SpeciesID.class,
			org.ensembl.healthcheck.testgroup.FuncgenIntegrity.class,
			org.ensembl.healthcheck.testgroup.FuncgenPostERSA.class,
			org.ensembl.healthcheck.testgroup.FuncgenPostProbemapping.class,
			org.ensembl.healthcheck.testgroup.FuncgenPostRegulatoryBuild.class,
			org.ensembl.healthcheck.testcase.funcgen.ProbeIdsUnique.class,
			org.ensembl.healthcheck.testcase.funcgen.ProbeTranscriptMappingsUnique.class,
			org.ensembl.healthcheck.testcase.funcgen.ProbeSetTranscriptMappingsUnique.class,
			org.ensembl.healthcheck.testcase.funcgen.ExternalFeatureFilesExist.class
		);

		removeTest(
			// this test belongs to FuncgenPostERSA testgroup and is useful after
			// the ERSA run but since we do not hand over bam files 
			// it should not be part of the FuncgenRelease testgroup
			AlignmentHasBamFile.class
			);
	}
}
