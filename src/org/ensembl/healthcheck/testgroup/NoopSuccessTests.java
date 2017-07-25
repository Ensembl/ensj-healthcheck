package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.generic.NoopSuccessTestCase;


/**
 * Group of tests that always succeed. Use for testing.
 * @author dstaines
 *
 */
public class NoopSuccessTests extends GroupOfTests {

    public NoopSuccessTests() {
        this.addTest(NoopSuccessTestCase.class);
    }

}
