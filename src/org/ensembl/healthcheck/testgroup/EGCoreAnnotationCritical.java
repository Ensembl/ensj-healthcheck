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
import org.ensembl.healthcheck.testcase.eg_core.DbDisplayNameUniProt;
import org.ensembl.healthcheck.testcase.eg_core.GeneDescriptionSource;
import org.ensembl.healthcheck.testcase.eg_core.GeneDescriptionUniProtSource;
import org.ensembl.healthcheck.testcase.eg_core.GeneSource;
import org.ensembl.healthcheck.testcase.eg_core.GoTermCount;
import org.ensembl.healthcheck.testcase.eg_core.InterproHitCount;
import org.ensembl.healthcheck.testcase.eg_core.OntologyLevel;
import org.ensembl.healthcheck.testcase.eg_core.PositiveCoordinates;
import org.ensembl.healthcheck.testcase.eg_core.SharedDisplayXref;
import org.ensembl.healthcheck.testcase.eg_core.TranscriptSource;
import org.ensembl.healthcheck.testcase.eg_core.UniprotGeneNameObjectXref;
import org.ensembl.healthcheck.testcase.eg_core.XrefDescriptionSpecialChars;
import org.ensembl.healthcheck.testcase.generic.DescriptionNewlines;
import org.ensembl.healthcheck.testcase.generic.DisplayLabels;
import org.ensembl.healthcheck.testcase.generic.DisplayXref;
import org.ensembl.healthcheck.testcase.generic.GeneDescriptions;
import org.ensembl.healthcheck.testcase.generic.TranscriptsSameName;

public class EGCoreAnnotationCritical extends GroupOfTests {

	public EGCoreAnnotationCritical() {
		
		addTest(
			DescriptionNewlines.class, 
			DisplayLabels.class,
			DisplayXref.class, 
			GeneDescriptions.class,
			GeneDescriptionSource.class, 
			SharedDisplayXref.class,
			TranscriptsSameName.class, 
			OntologyLevel.class,
			PositiveCoordinates.class,
			UniprotGeneNameObjectXref.class,
			GeneDescriptionUniProtSource.class, 
			DbDisplayNameUniProt.class,
			InterproHitCount.class,
			GoTermCount.class,
			XrefDescriptionSpecialChars.class,
			GeneSource.class,
			TranscriptSource.class
		);
	}
}
