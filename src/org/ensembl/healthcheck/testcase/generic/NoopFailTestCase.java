package org.ensembl.healthcheck.testcase.generic;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Test that always fails. Use for testing.
 * @author dstaines
 *
 */
public class NoopFailTestCase extends SingleDatabaseTestCase {

    @Override
    public boolean run(DatabaseRegistryEntry dbre) {
        ReportManager.problem(this, dbre.getConnection(), "Failure");
        return false;
    }
    
    

}
