package org.ensembl.healthcheck.reporter;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportLine;
import org.ensembl.healthcheck.Reporter;
import org.ensembl.healthcheck.testcase.EnsTestCase;
import org.ensembl.healthcheck.util.Utils;

public class DatabaseReporter implements Reporter {

	private long testStartTime;
	private static String TIMINGS_FILE = "timings.txt";

	/**
	 * Should be called just after a test case has been run.
	 * 
	 * @param testCase
	 *          The test case that was run.
	 * @param result
	 *          The result of testCase.
	 * @param dbre
	 *          The database which testCase was run on, or null of no/several
	 *          databases.
	 */
	public void finishTestCase(EnsTestCase testCase, boolean result, DatabaseRegistryEntry dbre) {

		long duration = System.currentTimeMillis() - testStartTime;

		String str = duration + "\t";
		if (dbre != null) {
			str += dbre.getName() + "\t";
		}
		str += testCase.getShortTestName() + "\t";
		str += Utils.formatTimeString(duration);

		Utils.writeStringToFile(TIMINGS_FILE, str, true, true);

	}

	/**
	 * 
	 * This never gets called, because the ReportManager has the lines
	 * 
	 * 	if (usingDatabase) {
	 * 
	 * 	   checkAndAddToDatabase(report);
	 *     return;
	 * 
	 *  }
	 * 
	 * before it.
	 * 
	 */
	public void message(ReportLine reportLine) {
		throw new RuntimeException("This should never be called!");
	}

	/**
	 * Called just before a test case is run.
	 * 
	 * @param testCase
	 *          The test case about to be run.
	 * @param dbre
	 *          The database which testCase is to be run on, or null of no/several
	 *          databases.
	 */
	public void startTestCase(EnsTestCase testCase, DatabaseRegistryEntry dbre) {

		testStartTime = System.currentTimeMillis();

	}

}
