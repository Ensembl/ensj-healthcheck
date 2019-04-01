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
import org.ensembl.healthcheck.testcase.eg_core.DuplicateObjectXref;
import org.ensembl.healthcheck.testcase.eg_core.DuplicateRepeatFeature;
import org.ensembl.healthcheck.testcase.eg_core.DuplicateXref;
import org.ensembl.healthcheck.testcase.eg_core.EponineFeatures;
import org.ensembl.healthcheck.testcase.eg_core.GOslimXrefs;
import org.ensembl.healthcheck.testcase.eg_core.GeneStableIdDisplayXref;
import org.ensembl.healthcheck.testcase.eg_core.IgiXref;
import org.ensembl.healthcheck.testcase.eg_core.ProteinFeatureAnalysisDb;
import org.ensembl.healthcheck.testcase.eg_core.SeqRegionDna;
import org.ensembl.healthcheck.testcase.eg_core.TranscriptDisplayXrefSuffix;
import org.ensembl.healthcheck.testcase.eg_core.TranscriptStableIdDisplayXref;
import org.ensembl.healthcheck.testcase.eg_core.UniParc_Coverage;
import org.ensembl.healthcheck.testcase.eg_core.UniProtExternalDbTypes;
import org.ensembl.healthcheck.testcase.eg_core.UniProtKB_Coverage;
import org.ensembl.healthcheck.testcase.eg_core.UniProtKB_DisplayXrefIds;
import org.ensembl.healthcheck.testcase.generic.AnalysisTypes;
import org.ensembl.healthcheck.testcase.generic.BlankCoordSystemVersions;
import org.ensembl.healthcheck.testcase.generic.BlankEnums;
import org.ensembl.healthcheck.testcase.generic.BlankInfoType;
import org.ensembl.healthcheck.testcase.generic.BlanksInsteadOfNulls;
import org.ensembl.healthcheck.testcase.generic.InterproDescriptions;
import org.ensembl.healthcheck.testcase.generic.InterproFeatures;
import org.ensembl.healthcheck.testcase.generic.IsCurrent;
import org.ensembl.healthcheck.testcase.generic.NullStrings;
import org.ensembl.healthcheck.testcase.generic.XrefCategories;
import org.ensembl.healthcheck.testcase.generic.XrefHTML;
import org.ensembl.healthcheck.testcase.generic.XrefIdentifiers;
import org.ensembl.healthcheck.testcase.generic.XrefLevels;
import org.ensembl.healthcheck.testcase.generic.XrefTypes;
import org.ensembl.healthcheck.testcase.generic.XrefVersions;

public class EGCoreAnnotationMajor extends GroupOfTests {

	public EGCoreAnnotationMajor() {
		addTest(AnalysisTypes.class, BlankCoordSystemVersions.class,
				BlankEnums.class, BlankInfoType.class,
				BlanksInsteadOfNulls.class, DuplicateXref.class, IgiXref.class,
				InterproDescriptions.class, InterproFeatures.class, ProteinFeatureAnalysisDb.class,
				IsCurrent.class, NullStrings.class, XrefCategories.class,
				XrefHTML.class, XrefIdentifiers.class, XrefLevels.class,
				XrefTypes.class, XrefVersions.class,
				UniProtKB_DisplayXrefIds.class, UniProtKB_Coverage.class,
				UniParc_Coverage.class, GeneStableIdDisplayXref.class,
				TranscriptDisplayXrefSuffix.class,
				TranscriptStableIdDisplayXref.class, 
				DuplicateRepeatFeature.class, EponineFeatures.class,
				UniProtExternalDbTypes.class, GOslimXrefs.class,
				DuplicateObjectXref.class,
				SeqRegionDna.class);
	}

}
