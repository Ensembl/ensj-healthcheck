/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ensembl.healthcheck.reporter;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportLine;
import org.ensembl.healthcheck.Reporter;
import org.ensembl.healthcheck.testcase.EnsTestCase;

public class TextReporter implements Reporter {

	private ArrayList<String> outputBuffer = new ArrayList<String>();
	protected int outputLevel = ReportLine.PROBLEM;
	protected static Logger logger = Logger.getLogger("HealthCheckLogger");
	private String lastDatabase = "";
	private int outputLineLength = 65;
	
	/**
	 * Should be called just after a test case has been run.
	 * 
	 * @param testCase
	 *          The test case that was run.
	 * @param result
	 *          The result of testCase.
	 * @param dbre
	 *          The database which testCase was run on, or null of no/several databases.
	 */
	public void finishTestCase(EnsTestCase testCase, boolean result, DatabaseRegistryEntry dbre) {

		System.out.println((result ? " PASSED" : " FAILED"));
	}

	protected void setOutputLevel(String str) {

		String lstr = str.toLowerCase();
		if (lstr.equals("all")) {
			outputLevel = ReportLine.ALL;
		} else if (lstr.equals("none")) {
			outputLevel = ReportLine.NONE;
		} else if (lstr.equals("problem")) {
			outputLevel = ReportLine.PROBLEM;
		} else if (lstr.equals("correct")) {
			outputLevel = ReportLine.CORRECT;
		} else if (lstr.equals("warning")) {
			outputLevel = ReportLine.WARNING;
		} else if (lstr.equals("info")) {
			outputLevel = ReportLine.INFO;
		} else {
			logger.warning("Output level " + str
					+ " not recognised; using 'all'");
		}

	} // setOutputLevel
	
	/**
	 * Called when a message is to be stored in the report manager.
	 * 
	 * @param reportLine
	 *          The message to store.
	 */
	public void message(ReportLine reportLine) {

		String level = "ODD    ";

		System.out.print(".");
		System.out.flush();

		if (reportLine.getLevel() < outputLevel) {
			return;
		}

		if (!reportLine.getDatabaseName().equals(lastDatabase)) {
			outputBuffer.add("  " + reportLine.getDatabaseName());
			lastDatabase = reportLine.getDatabaseName();
		}

		switch (reportLine.getLevel()) {
		case (ReportLine.PROBLEM):
			level = "PROBLEM";
			break;
		case (ReportLine.WARNING):
			level = "WARNING";
			break;
		case (ReportLine.INFO):
			level = "INFO   ";
			break;
		case (ReportLine.CORRECT):
			level = "CORRECT";
			break;
		default:
			level = "PROBLEM";
		}

		outputBuffer.add("    " + level + ":  " + lineBreakString(reportLine.getMessage(), outputLineLength, "              "));
	}

	private String lineBreakString(String mesg, int maxLen, String indent) {

		if (mesg.length() <= maxLen || maxLen == 0) {
			return mesg;
		}

		int lastSpace = mesg.lastIndexOf(" ", maxLen);
		if (lastSpace > 15) {
			return mesg.substring(0, lastSpace) + "\n" + indent + lineBreakString(mesg.substring(lastSpace + 1), maxLen, indent);
		} else {
			return mesg.substring(0, maxLen) + "\n" + indent + lineBreakString(mesg.substring(maxLen), maxLen, indent);
		}
	}

	/**
	 * Called just before a test case is run.
	 * 
	 * @param testCase
	 *          The test case about to be run.
	 * @param dbre
	 *          The database which testCase is to be run on, or null of no/several databases.
	 */
	public void startTestCase(EnsTestCase testCase, DatabaseRegistryEntry dbre) {

		String name;
		name = testCase.getClass().getName();
		name = name.substring(name.lastIndexOf(".") + 1);
		System.out.print(name + " ");
		if (dbre != null) {
			System.out.print("[" + dbre.getName() + "] ");
		}
		System.out.flush();

	}

}
