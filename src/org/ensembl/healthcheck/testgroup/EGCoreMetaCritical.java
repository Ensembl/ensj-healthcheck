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
import org.ensembl.healthcheck.testcase.eg_core.AssemblyAccession;
import org.ensembl.healthcheck.testcase.eg_core.DeprecatedEgMeta;
import org.ensembl.healthcheck.testcase.eg_core.DuplicateMetaKeys;
import org.ensembl.healthcheck.testcase.eg_core.GeneBuildStartDate;
import org.ensembl.healthcheck.testcase.eg_core.GeneGC;
import org.ensembl.healthcheck.testcase.eg_core.GenomeStatistics;
import org.ensembl.healthcheck.testcase.eg_core.RequiredEgMeta;
import org.ensembl.healthcheck.testcase.eg_core.SampleSetting;
import org.ensembl.healthcheck.testcase.generic.MetaCoord;

public class EGCoreMetaCritical extends GroupOfTests {

	public EGCoreMetaCritical() {
		addTest(RequiredEgMeta.class, DeprecatedEgMeta.class,
				GeneBuildStartDate.class, DuplicateMetaKeys.class,
				GeneGC.class, MetaCoord.class, SampleSetting.class,
				AssemblyAccession.class, GenomeStatistics.class);
	}
}
