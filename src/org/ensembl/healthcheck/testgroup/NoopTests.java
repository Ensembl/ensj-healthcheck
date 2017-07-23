package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.NoopFailTestCase;
import org.ensembl.healthcheck.testcase.NoopSuccessTestCase;

/**
 * Group of tests that do nothing. Use for testing.
 * @author dstaines
 *
 */
public class NoopTests extends GroupOfTests {

    public NoopTests() {
        this.addTest(NoopSuccessTestCase.class, NoopFailTestCase.class);
    }

}
