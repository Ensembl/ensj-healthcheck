package org.ensembl.healthcheck;

import org.ensembl.healthcheck.reporter.*;

public class ReporterFactory {

	/**
	 * An enumeration of the kinds of reporter objects that the 
	 * reporter object can produce.
	 *
	 */
	public static enum ReporterType {
		Text, Database
	}

	/**
	 * 
	 * @param reporterType: One of "Text",  "Database"
	 * @return An implementation of a Reporter
	 * 
	 */
	public Reporter getTestReporter(ReporterType reporterType) {

		Reporter r = null;
		
		if (reporterType == ReporterType.Text) {
			r = new TextReporter();
		}
		if (reporterType == ReporterType.Database) {
			r = new DatabaseReporter();
		}		
		return r;
	}
}
