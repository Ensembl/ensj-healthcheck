package org.ensembl.healthcheck.testcase;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;

/**
 * Test that always fails. Use for testing.
 * @author dstaines
 *
 */
public class NoopFailTestCase extends SingleDatabaseTestCase {

    public NoopFailTestCase() {
    }

    @Override
    public boolean run(DatabaseRegistryEntry dbre) {
        ReportManager.problem(this, dbre.getConnection(), "Failure");
        return false;
    }
    
    

}
