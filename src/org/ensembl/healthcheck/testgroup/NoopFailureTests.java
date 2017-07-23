package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.NoopFailTestCase;

/**
 * Group of tests that always fail. Use for testing.
 * @author dstaines
 *
 */
public class NoopFailureTests extends GroupOfTests {

    public NoopFailureTests() {
        this.addTest(NoopFailTestCase.class);
    }

}
