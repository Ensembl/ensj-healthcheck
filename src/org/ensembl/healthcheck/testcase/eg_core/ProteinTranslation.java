/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2018] EMBL-European Bioinformatics Institute
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

/**
 * File: ProteinTranslation.java
 * Created by: dstaines
 * Created on: Mar 23, 2010
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import java.io.File;
import java.util.Arrays;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractPerlModuleBasedTestCase;
import org.ensembl.healthcheck.util.ChecksumDatabase;

/**
 * @author dstaines
 *
 */
public class ProteinTranslation extends AbstractPerlModuleBasedTestCase {

	public ProteinTranslation() {
		super();
		setSpeciesIdAware(true);
		setTeamResponsible(Team.GENEBUILD);
		setSpeciesIdAware(true);
	}

	@Override
	protected String getModule() {
		return "Bio::EnsEMBL::Healthcheck::Translation";
	}
	
	@Override
	public boolean run(final DatabaseRegistryEntry dbre) {
		ChecksumDatabase db = new ChecksumDatabase(
				dbre, new File("db_checksums/"+this.getClass().getSimpleName()),
				Arrays.asList(new String[] { "seq_region", "gene",
						"transcript", "translation", "exon", "exon_transcript",
						"transcript_attrib", "translation_attrib",
						"seq_region_attrib","dna","assembly" }));
		boolean passed = true;
		if(db.isUpdated()) {
			passed = super.run(dbre);
			if(passed) {
				db.setRead();
			}
		} else {
			// Checksums are only updated, if the test has passed
			ReportManager.correct(this, dbre.getConnection(), "Database has not changed since last check so assuming OK");
		}
		return passed;
	}
}
