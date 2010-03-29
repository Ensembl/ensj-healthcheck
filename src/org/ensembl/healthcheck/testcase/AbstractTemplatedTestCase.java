/**
 * File: SampleMetaTestCase.java
 * Created by: dstaines
 * Created on: May 1, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.ConnectionBasedSqlTemplateImpl;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * Base class for healthchecks that checks for uncaught exceptions and also
 * always adds correct if test passes. Also provides helper for getting {@link SqlTemplate}
 * 
 * @author dstaines
 * 
 */
public abstract class AbstractTemplatedTestCase extends SingleDatabaseTestCase {

	public AbstractTemplatedTestCase() {
		super();
	}

	protected SqlTemplate getTemplate(DatabaseRegistryEntry dbre) {
		return new ConnectionBasedSqlTemplateImpl(dbre.getConnection());
	}

	protected abstract boolean runTest(DatabaseRegistryEntry dbre);

	public boolean run(DatabaseRegistryEntry dbre) {

		boolean passes = false;
		try {
			passes = runTest(dbre);
			if (passes) {
				ReportManager
						.correct(this, dbre.getConnection(), "Test passed");
			}
		} catch (Throwable e) {
			e.printStackTrace();
			ReportManager
					.problem(this, dbre.getConnection(),
							"Test failed due to unexpected exception "
									+ e.getMessage());
		}
		return passes;
	}

}
