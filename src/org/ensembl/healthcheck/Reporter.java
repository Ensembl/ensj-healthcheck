package org.ensembl.healthcheck;

import org.ensembl.healthcheck.testcase.EnsTestCase;

public interface Reporter {
    public void message( ReportLine reportLine );
    public void startTestCase( EnsTestCase testCase );
    public void finishTestCase( EnsTestCase testCase, TestResult result );
}
