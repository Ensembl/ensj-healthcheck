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
 * These are the tests that verify xref related entries. The tests are:
 * 
 * <ul>
 *   <li> org.ensembl.healthcheck.testcase.generic.BlankInfoType </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionCCDS </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionGeneNames </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionSynonyms </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionXrefs </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.DescriptionNewlines </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.DescriptionXrefs </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.DisplayLabels </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.DisplayLabelsMIM </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.DisplayXref </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.EntrezGeneNumeric </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ExternalSynonymArray </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.GeneDescriptions </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.GeneStatus </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.GOXrefs </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.HGNCMultipleGenes </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.HGNCNumeric </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.HGNCTypes </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.IdentityXrefCigarLines </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.InterproDescriptions </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.LRG </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.PredictedXrefs </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.TranscriptNames </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.TranscriptsSameName </li>
 *   <li> org.ensembl.healthcheck.testcase.generic.UnreviewedXrefs </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.XrefCategories </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.XrefHTML </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.XrefIdentifiers </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.XrefLevels </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.XrefPrefixes </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.XrefTypes </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.XrefVersions </li> 
 * </ul>
 *
 * @author Thomas Maurel
 *
 */
public class CoreXrefs extends GroupOfTests {
	
	public CoreXrefs() {

		addTest(
			org.ensembl.healthcheck.testcase.generic.BlankInfoType.class,
			org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionCCDS.class,
			org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionGeneNames.class,
			org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionSynonyms.class,
			org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionXrefs.class,
                        org.ensembl.healthcheck.testcase.generic.DescriptionNewlines.class,
			org.ensembl.healthcheck.testcase.generic.DescriptionXrefs.class,
			org.ensembl.healthcheck.testcase.generic.DisplayLabels.class,
                        org.ensembl.healthcheck.testcase.generic.DisplayLabelsMIM.class,
			org.ensembl.healthcheck.testcase.generic.DisplayXref.class,
			org.ensembl.healthcheck.testcase.generic.EntrezGeneNumeric.class,
			org.ensembl.healthcheck.testcase.generic.ExternalSynonymArray.class,
			org.ensembl.healthcheck.testcase.generic.GeneDescriptions.class,
			org.ensembl.healthcheck.testcase.generic.GeneStatus.class,
			org.ensembl.healthcheck.testcase.generic.GOXrefs.class,
			org.ensembl.healthcheck.testcase.generic.HGNCMultipleGenes.class,
			org.ensembl.healthcheck.testcase.generic.HGNCNumeric.class,
			org.ensembl.healthcheck.testcase.generic.HGNCTypes.class,
			org.ensembl.healthcheck.testcase.generic.IdentityXrefCigarLines.class,
			org.ensembl.healthcheck.testcase.generic.InterproDescriptions.class,
                        org.ensembl.healthcheck.testcase.generic.LRG.class,
			org.ensembl.healthcheck.testcase.generic.PredictedXrefs.class,
                        org.ensembl.healthcheck.testcase.generic.TranscriptNames.class,
			org.ensembl.healthcheck.testcase.generic.TranscriptsSameName.class,
			org.ensembl.healthcheck.testcase.generic.UnreviewedXrefs.class,
			org.ensembl.healthcheck.testcase.generic.XrefCategories.class,
			org.ensembl.healthcheck.testcase.generic.XrefHTML.class,
			org.ensembl.healthcheck.testcase.generic.XrefIdentifiers.class,
			org.ensembl.healthcheck.testcase.generic.XrefLevels.class,
			org.ensembl.healthcheck.testcase.generic.XrefPrefixes.class,
			org.ensembl.healthcheck.testcase.generic.XrefTypes.class,
			org.ensembl.healthcheck.testcase.generic.XrefVersions.class  
		);
	}
}
