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
import org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionAnalysisDescriptions;
import org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionAssociatedXrefGroups;
import org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionAssociatedXrefs;
import org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionBase;
import org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionBiotypes;
import org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionExonCoords;
import org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionGOXrefs;
import org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionRepeatTypes;
import org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionSynonyms;
import org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionTableRows;
import org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionXrefs;

/**
 * Set of tests to compare a core database with the previous release
 * 
 * @author dstaines
 * 
 */
public class EGCoreCompare extends GroupOfTests {

	public EGCoreCompare() {
		addTest(ComparePreviousVersionAnalysisDescriptions.class,
				ComparePreviousVersionAssociatedXrefs.class,
				ComparePreviousVersionAssociatedXrefGroups.class,
				ComparePreviousVersionBase.class,
				ComparePreviousVersionBiotypes.class,
				ComparePreviousVersionExonCoords.class,
				ComparePreviousVersionGOXrefs.class,
				ComparePreviousVersionRepeatTypes.class,
				ComparePreviousVersionSynonyms.class,
				ComparePreviousVersionTableRows.class,
				ComparePreviousVersionXrefs.class);
	}

}
