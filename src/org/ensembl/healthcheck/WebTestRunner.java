/*
 * Copyright [1999-2014] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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

/*
 * Copyright (C) 2002 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.ensembl.healthcheck.testcase.EnsTestCase;
import org.ensembl.healthcheck.util.ConnectionPool;
import org.ensembl.healthcheck.util.LogFormatter;
import org.ensembl.healthcheck.util.MyStreamHandler;
import org.ensembl.healthcheck.util.Utils;

/**
 * TestRunner optimised for outputting results to HTML.
 */
public class WebTestRunner extends TestRunner implements Reporter {

	private boolean debug = false;

	private String configFile = "web.properties";

	private long testStartTime, appStartTime;

	private static String TIMINGS_FILE = "timings.txt";

	// ---------------------------------------------------------------------
	/**
	 * Main run method.
	 * 
	 * @param args
	 *          Command-line arguments.
	 */
	private void run(String[] args) {

		// deleteTimingsFile();

		appStartTime = System.currentTimeMillis();

		ReportManager.setReporter(this);

		parseCommandLine(args);

		setupLogging();

		Utils.readPropertiesFileIntoSystem(getPropertiesFile(), false);

		Utils.readPropertiesFileIntoSystem(configFile, false);

		parseProperties();

		groupsToRun = getGroupsFromProperties();

		List databaseRegexps = getDatabasesFromProperties();

		outputLevel = setOutputLevelFromProperties();

		TestRegistry testRegistry = new DiscoveryBasedTestRegistry();

		DatabaseRegistry databaseRegistry = new DatabaseRegistry(databaseRegexps, null, null, false);
		if (databaseRegistry.getAll().length == 0) {
			logger.warning("Warning: no database names matched any of the database regexps given");
		}

		runAllTests(databaseRegistry, testRegistry, false);

		printOutput();

		ConnectionPool.closeAll();

	} // run

	// ---------------------------------------------------------------------

	/**
	 * Command-line entry point.
	 * 
	 * @param args
	 *          Command line args.
	 */
	public static void main(String[] args) {

		new WebTestRunner().run(args);

	} // main

	// -------------------------------------------------------------------------

	private void parseCommandLine(String[] args) {

		for (int i = 0; i < args.length; i++) {

			if (args[i].equals("-h")) {

				printUsage();
				System.exit(0);

			} else if (args[i].equals("-config")) {

				configFile = args[++i];

			} else if (args[i].equals("-debug")) {

				debug = true;
				logger.finest("Running in debug mode");

			}
		}

	} // parseCommandLine

	// -------------------------------------------------------------------------

	private void printUsage() {

		System.out.println("\nUsage: WebTestRunner {options} \n");
		System.out.println("Options:");
		System.out.println("  -config <file>  Properties file to use instead of web.properties");
		System.out.println("  -h              This message.");
		System.out.println("  -debug          Print debugging info");
		System.out.println();
		System.out.println("All configuration information is read from the files database.properties and web.properties. ");
		System.out.println("web.properties should contain the following properties:");
		System.out.println("  webtestrunner.groups=       A comma-separated list of the groups, or individual tests, to run");
		System.out.println("  webtestrunner.databases=    A comma-separated list of database regexps to match");
		System.out.println("  webtestrunner.file=         The name of the output file to write to ");
		System.out
				.println("  webtestrunner.outputlevel=  How much output to write. Should be one of all, info, warning, correct or problem");

	}

	// ---------------------------------------------------------------------

	private void setupLogging() {

		// stop parent logger getting the message
		logger.setUseParentHandlers(false);

		Handler myHandler = new MyStreamHandler(System.out, new LogFormatter());

		logger.addHandler(myHandler);
		logger.setLevel(Level.WARNING);

		if (debug) {

			logger.setLevel(Level.FINEST);

		}

	} // setupLogging

	// -------------------------------------------------------------------------
	// Implementation of Reporter interface

	/**
	 * Called when a message is to be stored in the report manager.
	 * 
	 * @param reportLine
	 *          The message to store.
	 */
	public void message(ReportLine reportLine) {

	}

	// ---------------------------------------------------------------------

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

	// ---------------------------------------------------------------------

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

	// ---------------------------------------------------------------------

	private void parseProperties() {

		if (System.getProperty("webtestrunner.groups") == null) {
			System.err.println("No tests or groups specified in " + configFile);
			System.exit(1);
		}

		if (System.getProperty("webtestrunner.databases") == null) {
			System.err.println("No databases specified in " + configFile);
			System.exit(1);
		}

		if (System.getProperty("webtestrunner.file") == null) {
			System.err.println("No output file specified in " + configFile);
			System.exit(1);
		}

	}

	// ---------------------------------------------------------------------

	/**
	 * Get a list of test groups by parsing the appropriate property.
	 * 
	 * @return the list of group or test names.
	 */
	private List getGroupsFromProperties() {

		String[] groups = System.getProperty("webtestrunner.groups").split(",");

		return Arrays.asList(groups);

	}

	// ---------------------------------------------------------------------

	/**
	 * Get a list of databases by parsing the appropriate property.
	 * 
	 * @return The list of database names or patterns.
	 */
	private List getDatabasesFromProperties() {

		String[] dbs = System.getProperty("webtestrunner.databases").split(",");

		return Arrays.asList(dbs);

	}

	// ---------------------------------------------------------------------

	private int setOutputLevelFromProperties() {

		String lstr = System.getProperty("webtestrunner.outputlevel").toLowerCase();

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
			System.err.println("Output level " + lstr + " not recognised; using 'all'");
		}

		return outputLevel;

	}

	// ---------------------------------------------------------------------

	/**
	 * Print formatted output held in outputBuffer to file specified in System
	 * property file.
	 */
	private void printOutput() {

		String file = System.getProperty("webtestrunner.file");

		try {

			PrintWriter pw = new PrintWriter(new FileOutputStream(file));

			printHeader(pw);

			printNavigation(pw);

			printExecutiveSummary(pw);

			printSummaryByDatabase(pw);

			printSummaryByTest(pw);

			printReportsByDatabase(pw);

			printReportsByTest(pw);

			printFooter(pw);

			pw.close();

		} catch (Exception e) {
			System.err.println("Error writing to " + file);
			e.printStackTrace();
		}

	}

	// ---------------------------------------------------------------------

	private void printHeader(PrintWriter pw) {

		print(pw, "<html>");
		print(pw, "<head>");
		print(pw, "<style type=\"text/css\" media=\"all\">");
		print(pw, "@import url(http://www.ensembl.org/css/ensembl.css);");
		print(pw, "@import url(http://www.ensembl.org/css/content.css);");
		print(pw, "#page ul li { list-style-type:none; list-style-image: none; margin-left: -2em }");
		print(pw, "</style>");

		print(pw, "<title>" + System.getProperty("webtestrunner.title") + "</title>");
		print(pw, "</head>");
		print(pw, "<body>");

		print(pw, "<div id='page'><div id='i1'><div id='i2'><div class='sptop'>&nbsp;</div>");

		print(pw, "<div id='release'>" + System.getProperty("webtestrunner.title") + "</div>");

		print(pw, "<hr>");

	}

	// ---------------------------------------------------------------------
	private void printReportsByDatabase(PrintWriter pw) {

		print(pw, "<h2>Detailed reports by database</h2>");

		Map reportsByDB = ReportManager.getAllReportsByDatabase(outputLevel);

		TreeSet dbs = new TreeSet(reportsByDB.keySet());
		Iterator it = dbs.iterator();
		while (it.hasNext()) {

			String database = (String) it.next();

			List reports = (List) reportsByDB.get(database);
			Iterator it2 = reports.iterator();

			if (!reports.isEmpty()) {

				String link = "<a name=\"" + database + "\">";
				print(pw, "<h3 class='boxed'>" + link + database + "</a></h3>");
				print(pw, "<p>");

				String lastTest = "";
				while (it2.hasNext()) {

					ReportLine line = (ReportLine) it2.next();
					String test = line.getShortTestCaseName();

					if (!lastTest.equals("") && !test.equals(lastTest)) {
						print(pw, "</p><p>");
					}
					lastTest = test;

					String linkTarget = "<a name=\"" + database + ":" + test + "\"></a> ";
					String s = linkTarget + getFontForReport(line) + "<strong>" + test + ": </strong>" + line.getMessage() + "</font>"
							+ "<br>";

					print(pw, s);

				} // while it2

				print(pw, "</p>");

			}

		} // while it

		print(pw, "<hr>");

	}

	// ---------------------------------------------------------------------
	private void printReportsByTest(PrintWriter pw) {

		print(pw, "<h2>Detailed reports by test case</h2>");

		Map reportsByTC = ReportManager.getAllReportsByTestCase(outputLevel);

		TreeSet dbs = new TreeSet(reportsByTC.keySet());
		
		Iterator it = dbs.iterator();
		while (it.hasNext()) {

			String test = (String) it.next();

			List reports = (List) reportsByTC.get(test);
			Iterator it2 = reports.iterator();

			if (!reports.isEmpty()) {

				String link = "<a name=\"" + test + "\">";
				print(pw, "<h3 class='boxed'>" + link + test + "</a></h3>");

				print(pw, "<p>");

				String lastDB = "";
				while (it2.hasNext()) {

					ReportLine line = (ReportLine) it2.next();
					String database = line.getDatabaseName();

					if (!lastDB.equals("") && !database.equals(lastDB)) {
						print(pw, "</p><p>");
					}
					lastDB = database;

					String linkTarget = "<a name=\"" + line.getShortTestCaseName() + ":" + database + "\"></a> ";
					String s = linkTarget + getFontForReport(line) + "<strong>" + database + ": </strong>" + line.getMessage() + "</font>"
							+ "<br>";

					print(pw, s);

				} // while it2

				print(pw, "</p>");

			}

		} // while it

		print(pw, "<hr>");
	}

	// ---------------------------------------------------------------------

	private String getFontForReport(ReportLine line) {

		String s1 = "";

		switch (line.getLevel()) {
		case (ReportLine.PROBLEM):
			s1 = "<font color='red'>";
			break;
		case (ReportLine.WARNING):
			s1 = "<font color='black'>";
			break;
		case (ReportLine.INFO):
			s1 = "<font color='grey'>";
			break;
		case (ReportLine.CORRECT):
			s1 = "<font color='green'>";
			break;
		default:
			s1 = "<font color='black'>";
		}

		return s1;

	}

	// ---------------------------------------------------------------------

	private void printFooter(PrintWriter pw) {

		long runTime = System.currentTimeMillis() - appStartTime;
		String runStr = Utils.formatTimeString(runTime);
		print(pw, "<p>Test run was started at " + new Date(appStartTime).toString() + " and finished at " + new Date().toString()
				+ "<br>");
		print(pw, " Run time " + runStr + "</p>");

		print(pw, "<h4>Configuration used:</h4>");
		print(pw, "<pre>");
		print(pw, "Tests/groups run:   " + System.getProperty("webtestrunner.groups") + "<br>");
		print(pw, "Database host:      " + System.getProperty("host") + ":" + System.getProperty("port") + "<br>");
		print(pw, "Database names:     " + System.getProperty("webtestrunner.databases") + "<br>");
		print(pw, "Output file:        " + System.getProperty("webtestrunner.file") + "<br>");
		print(pw, "Output level:       " + System.getProperty("webtestrunner.outputlevel") + "<br>");
		print(pw, "</pre>");

		print(pw, "</div>");

		print(pw, "</body>");
		print(pw, "</html>");

		print(pw, "<hr>");

	} // ---------------------------------------------------------------------

	private void print(PrintWriter pw, String s) {

		pw.write(s + "\n");

	}

	// ---------------------------------------------------------------------

	private void printSummaryByDatabase(PrintWriter pw) {

		print(pw, "<h2>Summary by database</h2>");

		print(pw, "<p><table class='ss'>");
		print(pw, "<tr><th>Database</th><th>Passed</th><th>Failed</th></tr>");

		Map reportsByDB = ReportManager.getAllReportsByDatabase();
		TreeSet databases = new TreeSet(reportsByDB.keySet());
		Iterator it = databases.iterator();
		while (it.hasNext()) {
			String database = (String) it.next();
			String link = "<a href=\"#" + database + "\">";
			int[] passesAndFails = ReportManager.countPassesAndFailsDatabase(database);
			String s = (passesAndFails[1] == 0) ? passFont() : failFont();
			String[] t = { link + s + database + "</font></a>", passFont() + passesAndFails[0] + "</font>",
					failFont() + passesAndFails[1] + "</font>" };
			printTableLine(pw, t);
		}

		print(pw, "</table></p>");

		print(pw, "<hr>");

	}

	// ---------------------------------------------------------------------

	private void printSummaryByTest(PrintWriter pw) {

		print(pw, "<h2>Summary by test</h2>");

		print(pw, "<p><table class='ss'>");
		print(pw, "<tr><th>Test</th><th>Passed</th><th>Failed</th></tr>");

		Map reports = ReportManager.getAllReportsByTestCase();
		TreeSet tests = new TreeSet(reports.keySet());
		Iterator it = tests.iterator();
		while (it.hasNext()) {
			String test = (String) it.next();
			String link = "<a href=\"#" + test + "\">";
			int[] passesAndFails = ReportManager.countPassesAndFailsTest(test);
			String s = (passesAndFails[1] == 0) ? passFont() : failFont();
			String[] t = { link + s + test + "</font></a>", passFont() + passesAndFails[0] + "</font>",
					failFont() + passesAndFails[1] + "</font>" };
			printTableLine(pw, t);
		}

		print(pw, "</table></p>");

		print(pw, "<hr>");

	}

	// ---------------------------------------------------------------------

	private void printExecutiveSummary(PrintWriter pw) {

		print(pw, "<h2>Summary</h2>");

		int[] result = ReportManager.countPassesAndFailsAll();

		StringBuffer s = new StringBuffer();
		s.append("<p><strong>");
		s.append(passFont() + result[0] + "</font> tests passed and ");
		s.append(failFont() + result[1] + "</font> failed out of a total of ");
		s.append((result[0] + result[1]) + " tests run.</strong></p>");

		print(pw, s.toString());

		print(pw, "<hr>");

	}

	// ---------------------------------------------------------------------

	private void printTableLine(PrintWriter pw, String[] s) {

		pw.write("<tr>");

		for (int i = 0; i < s.length; i++) {
			pw.write("<td>" + s[i] + "</td>");
		}
		pw.write("</tr>\n");

	}

	// ---------------------------------------------------------------------

	private void printNavigation(PrintWriter pw) {

		print(pw, "<div id='related'><div id='related-box'>");

		print(pw, "<h2>Results by database</h2>");
		print(pw, "<ul>");

		Map reportsByDB = ReportManager.getAllReportsByDatabase();
		TreeSet databases = new TreeSet(reportsByDB.keySet());
		Iterator it = databases.iterator();
		while (it.hasNext()) {
			String database = (String) it.next();

			String link = "<a href=\"#" + database + "\">";
			print(pw, "<li>" + link + Utils.truncateDatabaseName(database) + "</a></li>");
		}
		print(pw, "</ul>");

		print(pw, "<h2>Results by test</h2>");
		print(pw, "<ul>");

		Map reports = ReportManager.getAllReportsByTestCase();
		TreeSet tests = new TreeSet(reports.keySet());
		it = tests.iterator();
		while (it.hasNext()) {
			String test = (String) it.next();
			String name = test.substring(test.lastIndexOf('.') + 1);
			String link = "<a href=\"#" + name + "\">";
			print(pw, "<li>" + link + Utils.truncateTestName(name) + "</a></li>");
		}
		print(pw, "</ul>");

		print(pw, "</div></div>");

	}

	// ---------------------------------------------------------------------

	private String passFont() {

		return "<font color='green' size=-1>";

	}

	// ---------------------------------------------------------------------

	private String failFont() {

		return "<font color='red' size=-1>";

	}

	// ---------------------------------------------------------------------

	
} // WebTestRunner
