package org.ensembl.healthcheck.testcase.generic;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Test that always succeeds. Use for testing.
 * 
 * @author dstaines
 *
 */
public class NoopSuccessTestCase extends SingleDatabaseTestCase {

    @Override
    public boolean run(DatabaseRegistryEntry dbre) {
        ReportManager.correct(this, dbre.getConnection(), "Success");
        return true;
    }

}
