/**
 * File: SampleMetaTestCase.java
 * Created by: dstaines
 * Created on: May 1, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;

/**
 * Base class for EnsemblGenomes core healthchecks
 * 
 * @author dstaines
 * 
 */
public abstract class AbstractEgCoreTestCase extends AbstractTemplatedTestCase {

	public final static String EG_GROUP = "ensembl_genomes";

	public AbstractEgCoreTestCase() {
		super();
		this.addToGroup(EG_GROUP);
		this.appliesToType(DatabaseType.CORE);
		this.setTeamResponsible(Team.ENSEMBL_GENOMES);
	}
}
