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

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.LogFormatter;
import org.ensembl.healthcheck.util.MyStreamHandler;
import org.ensembl.healthcheck.util.Utils;

/**
 * Pull healthcheck results from a database and write to an HTML file.
 */
public class DatabaseToHTML {

	private boolean debug = false;

	private String PROPERTIES_FILE = "database.properties";

	private String outputDir = ".";

	private long sessionID = -1;

	protected static Logger logger = Logger.getLogger("HealthCheckLogger");

	/**
	 * Command-line entry point.
	 * 
	 * @param args
	 *          Command line args.
	 */
	public static void main(String[] args) {

		new DatabaseToHTML().run(args);

	}

	// ---------------------------------------------------------------------
	/**
	 * Main run method.
	 * 
	 * @param args
	 *          Command-line arguments.
	 */
	private void run(String[] args) {

		parseCommandLine(args);

		setupLogging();

		Utils.readPropertiesFileIntoSystem(PROPERTIES_FILE);

		parseProperties();

		Connection con = connectToOutputDatabase();

		sessionID = getSessionID(sessionID, con);

		printOutput(con);

	} // run

	// ---------------------------------------------------------------------

	private void parseCommandLine(String[] args) {

		for (int i = 0; i < args.length; i++) {

			if (args[i].equals("-h")) {

				printUsage();
				System.exit(0);

			} else if (args[i].equals("-output")) {

				i++;
				outputDir = args[i];
				logger.finest("Will write output to " + outputDir);

			} else if (args[i].equals("-session")) {

				i++;
				sessionID = Long.parseLong(args[i]);
				logger.finest("Will use session ID " + sessionID);

			} else if (args[i].equals("-debug")) {

				debug = true;

			}

		}

	} // parseCommandLine

	// -------------------------------------------------------------------------

	private void printUsage() {

		System.out.println("\nUsage: DatabaseToHTML {options} \n");
		System.out.println("Options:");
		System.out.println("  -output         Specify output directory. Default is current directory.");
		System.out.println("  -h              This message.");
		System.out.println("  -debug          Print debugging info");
		System.out.println();
		System.out.println("All other configuration information is read from the file database.properties. ");
		System.out.println("See the comments in that file for information on which options to set.");

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

	// ---------------------------------------------------------------------

	private void parseProperties() {

	}

	// ---------------------------------------------------------------------

	private Connection connectToOutputDatabase() {

		Connection con = DBUtils.openConnection(System.getProperty("output.driver"), System.getProperty("output.databaseURL")
				+ System.getProperty("output.database"), System.getProperty("output.user"), System.getProperty("output.password"));

		logger.fine("Connecting to " + System.getProperty("output.databaseURL") + System.getProperty("output.database") + " as "
				+ System.getProperty("output.user") + " password " + System.getProperty("output.password"));

		return con;

	}

	// ---------------------------------------------------------------------
	/**
	 * Get the most recent session ID from the database, or use the one defined on
	 * the command line.
	 */
	private long getSessionID(long s, Connection con) {

		long sess = -1;

		String sql = "SELECT MAX(session_id) FROM session";

		if (s > 0) {

			sess = s;

		} else {

			try {

				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(sql);

				if (rs.next()) {
					sess = rs.getLong(1);
					logger.finest("Maximum session ID from database: " + sess);
				}

			} catch (SQLException e) {

				System.err.println("Error executing:\n" + sql);
				e.printStackTrace();

			}

		}

		if (sess == 1) {
			logger.severe("Can't get session ID from command-line or database.");
		}

		return sess;

	}

	// ---------------------------------------------------------------------

	/**
	 * Print formatted output.
	 */
	private void printOutput(Connection con) {

		try {

			PrintWriter pwIntro = new PrintWriter(new FileOutputStream(outputDir + File.separator + "healthcheck_summary.html"));

			printIntroPage(pwIntro, con);

			// now loop over each species and print the detailed output
			String sql = "SELECT DISTINCT(species) FROM report WHERE session_id=" + sessionID + " ORDER BY species ";

			try {

				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(sql);

				while (rs.next()) {
					String species = rs.getString("species");

					PrintWriter pw = new PrintWriter(new FileOutputStream(outputDir + File.separator + species + ".html"));

					printHeader(pw, species, con);

					printNavigation(pw, species, con);

					// printExecutiveSummary(pw);

					printSummaryByDatabase(pw, species, con);

					printSummaryByTest(pw, species, con);

					printReportsByDatabase(pw, species, con);

					printFooter(pw);

					pw.close();
				}

			} catch (SQLException e) {

				System.err.println("Error executing:\n" + sql);
				e.printStackTrace();

			}

			pwIntro.close();

		} catch (Exception e) {
			System.err.println("Error writing output");
			e.printStackTrace();
		}

	}

	// ---------------------------------------------------------------------

	private void printIntroPage(PrintWriter pw, Connection con) {

		// header
		print(pw, "<html>");
		print(pw, "<head>");
		print(pw, "<style type='text/css' media='all'>");
		print(pw, "@import url(http://www.ensembl.org/css/ensembl.css);");
		print(pw, "@import url(http://www.ensembl.org/css/content.css);");
		print(pw, "#page ul li { list-style-type:none; list-style-image: none; margin-left: -2em }");
		print(pw, "</style>");
		print(pw, "<title>Healthcheck Results</title>");
		print(pw, "</head>");
		print(pw, "<body>");
		print(pw, "<div id='page'><div id='i1'><div id='i2'><div class='sptop'>&nbsp;</div>");
		print(pw, "<div id='release'>Healthcheck results</div>");
		print(pw, "<hr>");
		print(pw, "");
		print(pw, "<h2>Results by species</h2>");
		print(pw, "<ul>");

		// now loop over each species
		String sql = "SELECT DISTINCT(species) FROM report WHERE session_id = " + sessionID + " ORDER BY species";

		try {

			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String species = rs.getString("species");
				print(pw, "<li><p><a href='" + species + ".html'>" + Utils.ucFirst(species) + "</a></p></li>");
			}

		} catch (SQLException e) {

			System.err.println("Error executing:\n" + sql);
			e.printStackTrace();

		}

		// footer
		print(pw, "</ul>");
		print(pw, "");
		print(pw, "<hr>");
		print(pw, "");
		print(pw, "<h3>Previous releases</h3>");
		print(pw, "");
		print(pw, "<ul>");
		print(pw, "<li><a href='previous/39/web_healthcheck_summary.html'>39</a> (June 2006)</li>");
		print(pw, "<li><a href='previous/38/web_healthcheck_summary.html'>38</a> (April 2006)</li>");
		print(pw, "</ul>");
		print(pw, "<hr>");

		sql = "SELECT start_time, end_time, host, groups, database_regexp FROM session WHERE session_id = " + sessionID;

		try {

			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			if (rs.next()) {

				print(pw, "<p>Test run was started at " + rs.getString("start_time") + " and finished at " + rs.getString("end_time")
						+ "<br>");

				print(pw, "<h4>Configuration used:</h4>");
				print(pw, "<pre>");
				print(pw, "Tests/groups run:   " + rs.getString("groups") + "<br>");
				print(pw, "Database host:      " + rs.getString("host") + "<br>");
				print(pw, "Database names:     " + rs.getString("database_regexp") + "<br>");
				print(pw, "</pre>");

			}

		} catch (SQLException e) {

			System.err.println("Error executing:\n" + sql);
			e.printStackTrace();

		}

		print(pw, "</body>");
		print(pw, "</html>");
		print(pw, "<hr>");

	}

	// ---------------------------------------------------------------------

	private void printHeader(PrintWriter pw, String species, Connection con) {

		print(pw, "<html>");
		print(pw, "<head>");
		print(pw, "<style type=\"text/css\" media=\"all\">");
		print(pw, "@import url(http://www.ensembl.org/css/ensembl.css);");
		print(pw, "@import url(http://www.ensembl.org/css/content.css);");
		print(pw, "#page ul li { list-style-type:none; list-style-image: none; margin-left: -2em }");
		print(pw, "td { font-size: 9pt}");
		print(pw, "</style>");

		print(pw, "<title>Healthcheck results for " + Utils.ucFirst(species) + "</title>");
		print(pw, "</head>");
		print(pw, "<body>");

		print(pw, "<div id='page'><div id='i1'><div id='i2'><div class='sptop'>&nbsp;</div>");

		print(pw, "<div id='release'>Healthcheck results for " + Utils.ucFirst(species) + "</div>");

		print(pw, "<hr>");

	}

	// ---------------------------------------------------------------------

	private void printFooter(PrintWriter pw) {

		print(pw, "</div>");

		print(pw, "</body>");
		print(pw, "</html>");

	}

	// ---------------------------------------------------------------------

	private void printNavigation(PrintWriter pw, String species, Connection con) {

		print(pw, "<div id='related'><div id='related-box'>");

		// results by database
		print(pw, "<h2>Results by database</h2>");
		print(pw, "<ul>");

		String sql = "SELECT DISTINCT(database_name) FROM report WHERE species='" + species + "' AND session_id=" + sessionID;

		try {

			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {

				String database = rs.getString(1);

				String link = "<a href=\"#" + database + "\">";
				print(pw, "<li>" + link + Utils.truncateDatabaseName(database) + "</a></li>");

			}

			print(pw, "</ul>");

			// results by test
			print(pw, "<h2>Failures by test</h2>");
			print(pw, "<ul>");

			sql = "SELECT DISTINCT(testcase) FROM report WHERE species='" + species + "' AND result IN ('PROBLEM','WARNING','INFO') AND session_id=" + sessionID;

			rs = stmt.executeQuery(sql);

			while (rs.next()) {

				String test = rs.getString(1);
				String name = test.substring(test.lastIndexOf('.') + 1);
				String link = "<a href=\"#" + name + "\">";
				print(pw, "<li>" + link + Utils.truncateTestName(name) + "</a></li>");

			}

		} catch (SQLException e) {

			System.err.println("Error executing:\n" + sql);
			e.printStackTrace();

		}

		print(pw, "</ul>");

		print(pw, "</div></div>");

	}

	// ---------------------------------------------------------------------

	private void printSummaryByDatabase(PrintWriter pw, String species, Connection con) {

		print(pw, "<h2>Summary of results by database</h2>");

		print(pw, "<p><table class='ss'>");
		print(pw, "<tr><th>Database</th><th>Passed</th><th>Failed</th></tr>");

		String sql = "SELECT DISTINCT(database_name) FROM report WHERE species='" + species + "' AND session_id=" + sessionID;

		try {

			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {

				String database = rs.getString(1);

				String link = "<a href=\"#" + database + "\">";
				int[] passesAndFails = countPassesAndFailsDatabase(database, con);
				String s = (passesAndFails[1] == 0) ? passFont() : failFont();
				String[] t = { link + s + database + "</font></a>", passFont() + passesAndFails[0] + "</font>",
						failFont() + passesAndFails[1] + "</font>" };
				printTableLine(pw, t);

			}

		} catch (SQLException e) {

			System.err.println("Error executing:\n" + sql);
			e.printStackTrace();

		}

		print(pw, "</table></p>");

		print(pw, "<hr>");

	}

	// ---------------------------------------------------------------------

	private void print(PrintWriter pw, String s) {

		pw.write(s + "\n");
		pw.flush();

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

	private String passFont() {

		return "<font color='green' size=-1>";

	}

	// ---------------------------------------------------------------------

	private String failFont() {

		return "<font color='red' size=-1>";

	}

	// ---------------------------------------------------------------------

	private int[] countPassesAndFailsDatabase(String database, Connection con) {

		int[] result = new int[2];

		int total = -1, failed = -1;

		// get count of total tests run
		String sql = "SELECT COUNT(DISTINCT(testcase)) FROM report WHERE database_name='" + database + "' AND session_id=" + sessionID;

		try {

			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			if (rs.next()) {
				total = rs.getInt(1);
			}

			sql = "SELECT COUNT(DISTINCT(testcase)) FROM report WHERE database_name='" + database
					+ "' AND result = 'PROBLEM' AND session_id=" + sessionID;
			rs = stmt.executeQuery(sql);

			if (rs.next()) {
				failed = rs.getInt(1);
			}
		} catch (SQLException e) {

			System.err.println("Error executing:\n" + sql);
			e.printStackTrace();

		}

		result[1] = failed;
		result[0] = total - failed;

		return result;

	}

	// ------
	private void printSummaryByTest(PrintWriter pw, String species, Connection con) {

		print(pw, "<h2>Summary of failures by test</h2>");

		print(pw, "<p><table class='ss'>");
		print(pw, "<tr><th>Test</th><th>Result</th></tr>");

		// group failed tests before passed ones via ORDER BY
		String sql = "SELECT DISTINCT(testcase), result FROM report WHERE species='" + species + "' AND session_id=" + sessionID
				+ " AND result IN ('PROBLEM','WARNING','INFO') ORDER BY result";

		try {

			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {

				String testcase = rs.getString(1);
				String result = rs.getString(2);

				String link = "<a href=\"#" + testcase + "\">";

				String s = (result.equals("CORRECT")) ? passFont() : failFont();
				String r = (result.equals("CORRECT")) ? "Passed" : "Failed";
				String[] t = { link + s + testcase + "</a></font>", s + r + "</font>" };
				printTableLine(pw, t);

			}

		} catch (SQLException e) {

			System.err.println("Error executing:\n" + sql);
			e.printStackTrace();

		}

		print(pw, "</table></p>");

		print(pw, "<hr>");

	}

	// ---------------------------------------------------------------------
	private void printReportsByDatabase(PrintWriter pw, String species, Connection con) {

		print(pw, "<h2>Detailed failure reports by database</h2>");

		String sql = "SELECT r.report_id, r.database_name, r.testcase, r.result, r.text, a.person, a.action, a.reason, a.comment FROM report r LEFT JOIN annotation a ON r.report_id=a.report_id WHERE r.species='" + species + "' AND r.session_id=" + sessionID + " AND r.result IN ('PROBLEM','WARNING','INFO') ORDER BY r.database_name, r.testcase";
		
		try {

			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			String lastDatabase = "";
			String lastTest = "";
			
			while (rs.next()) {

				String database = rs.getString("database_name");
				String testcase = rs.getString("testcase");
				String result = rs.getString("result");
				String text = rs.getString("text");
				String person = stringOrBlank(rs.getString("person"));
				String action = stringOrBlank(rs.getString("action"));
				String reason = stringOrBlank(rs.getString("reason"));
				String comment = stringOrBlank(rs.getString("comment"));
				
				if (!database.equals(lastDatabase)) {
					
					if (lastDatabase != "") {
						print(pw, "</table>");
					}
					String link = "<a name=\"" + database + "\">";
					print(pw, "<h3 class='boxed'>" + link + database + "</a></h3>");
					
					print(pw, "<table>");
					
					lastDatabase = database;
					
				}
				
				String f1 = getFontForResult(result, action);
				String f2 = "</font>";
				
				if (!testcase.equals(lastTest)) {
					String linkTarget = "<a name=\"" + database + ":" + testcase + "\"></a> ";
					print(pw, "<tr><td colspan='6'>" + linkTarget + f1 + "<strong>" + testcase + "</strong>" + f2 + "</td></tr>");
				}
				lastTest = testcase;
				
				String[] s = {"&nbsp;&nbsp;&nbsp;&nbsp;", f1 + text + f2, f1 + person + f2, f1 + action + f2, f1 + comment + f2};
				
				printTableLine(pw, s);
				
			}
		} catch (SQLException e) {

			System.err.println("Error executing:\n" + sql);
			e.printStackTrace();

		}

	}

	// ---------------------------------------------------------------------

	private String getFontForResult(String result, String action) {

		String s1 = "<font color='black'>";

		if (result.equals("PROBLEM")) {
			s1 = "<font color='red'>";
		}
		
		if (result.equals("WARNING")) {
			s1 = "<font color='black'>";
		}
		
		if (result.equals("INFO")) {
			s1 = "<font color='black'>";
		}
		
		if (result.equals("CORRECT")) {
			s1 = "<font color='green'>";
		}
		
		if (result.equals("")) {
			s1 = "<font color='red'>";
		}
		
		if (result.equals("PROBLEM")) {
			s1 = "<font color='red'>";
		}
		
		// override if there has been an annotation
		if (action != null) {
			if(action.equals("ignore") || action.equals("normal")) {
				s1 = "<font color='grey'>";
			}
		}
		
		return s1;

	}
	
	//---------------------------------------------------------------------
	
		private String stringOrBlank(String s) {
			
			return (s != null) ? s : "";
			
		}
	
	// ---------------------------------------------------------------------

} // DatabaseToHTML
