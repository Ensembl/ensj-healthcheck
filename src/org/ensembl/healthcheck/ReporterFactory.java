package org.ensembl.healthcheck;

import org.ensembl.healthcheck.reporter.*;

public class ReporterFactory {

	/**
	 * An enumeration of the kinds of reporter objects that the 
	 * reporter object can produce.
	 *
	 */
	public static enum ReporterType {
		TEXT, DATABASE
	}

	/**
	 * 
	 * @param reporterType: One of "Text",  "Database"
	 * @return An implementation of a Reporter
	 * 
	 */
	public Reporter getTestReporter(ReporterType reporterType) {

		Reporter r = null;
		
		if (reporterType == ReporterType.TEXT) {
			r = new TextReporter();
		}
		if (reporterType == ReporterType.DATABASE) {
			r = new DatabaseReporter();
		}		
		return r;
	}
}
