/*
 * Copyright [1999-2019] EMBL-European Bioinformatics Institute
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

import org.ensembl.healthcheck.testcase.eg_core.GeneGC;
import org.ensembl.healthcheck.testcase.eg_core.ProteinCodingGene;
import org.ensembl.healthcheck.testcase.eg_core.ProteinTranslation;
import org.ensembl.healthcheck.testcase.eg_core.SeqRegionDna;
import org.ensembl.healthcheck.testcase.generic.CanonicalTranscriptCoding;
import org.ensembl.healthcheck.testcase.generic.DNAEmpty;
import org.ensembl.healthcheck.testcase.generic.InterproDescriptions;
import org.ensembl.healthcheck.testcase.generic.SeqRegionAttribsPresent;
import org.ensembl.healthcheck.testcase.generic.StableID;

/**
 * Test to be run on otherfeatures at handover time
 * 
 * @author dstaines
 *
 */
public class EGOtherFeaturesHandover extends EGCoreHandover {

	public EGOtherFeaturesHandover() {
		super();
		this.removeTest(GeneGC.class, ProteinCodingGene.class, SeqRegionDna.class, CanonicalTranscriptCoding.class,
				InterproDescriptions.class, SeqRegionAttribsPresent.class, StableID.class, ProteinTranslation.class);
		this.addTest(DNAEmpty.class);
	}

}
