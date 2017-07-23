package org.ensembl.healthcheck.testcase;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;

/**
 * Test that always succeeds. Use for testing.
 * @author dstaines
 *
 */
public class NoopSuccessTestCase extends SingleDatabaseTestCase {

    public NoopSuccessTestCase() {
    }

    @Override
    public boolean run(DatabaseRegistryEntry dbre) {
        ReportManager.correct(this, dbre.getConnection(), "Success");
        return true;
    }
    
    

}
