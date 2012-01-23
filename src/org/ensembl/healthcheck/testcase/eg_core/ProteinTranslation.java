/**
 * File: ProteinTranslation.java
 * Created by: dstaines
 * Created on: Mar 23, 2010
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractPerlModuleBasedTestCase;

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
}
