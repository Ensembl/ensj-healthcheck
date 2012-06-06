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
			ReportManager.correct(this, dbre.getConnection(), "Database has not changed since last check so assuming OK");
		}
		return passed;
	}
}
