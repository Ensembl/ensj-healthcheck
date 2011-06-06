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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.Properties;

import org.ensembl.healthcheck.util.ConnectionPool;
import org.ensembl.healthcheck.util.LogFormatter;
import org.ensembl.healthcheck.util.MyStreamHandler;
import org.ensembl.healthcheck.util.Utils;

/**
 * Submit multiple NodeDatabaseTestRunners in parallel.
 */
public class ParallelDatabaseTestRunner extends TestRunner {

	protected boolean debug = false;

	// ---------------------------------------------------------------------
	/**
	 * Main run method.
	 * 
	 * @param args
	 *          Command-line arguments.
	 */
	protected void run(String[] args) {

		parseCommandLine(args);

		setupLogging();

		Utils.readPropertiesFileIntoSystem(getPropertiesFile(), false);

		parseProperties();

		ReportManager.connectToOutputDatabase();

		ReportManager.createDatabaseSession();

		List<String> databasesAndGroups = getDatabasesAndGroups();

		submitJobs(databasesAndGroups, ReportManager.getSessionID());

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

		new ParallelDatabaseTestRunner().run(args);

	} // main

	// -------------------------------------------------------------------------

	private void parseCommandLine(String[] args) {

		for (String arg : args) {

			if (arg.equals("-h")) {

				printUsage();
				System.exit(0);

			} else if (arg.equals("-debug")) {

				debug = true;
				logger.finest("Running in debug mode");

			}

		}

	} // parseCommandLine

	// -------------------------------------------------------------------------

	private void printUsage() {

		System.out.println("\nUsage: ParallelDatabaseTestRunner {options} \n");
		System.out.println("Options:");
		System.out.println("  -h              This message.");
		System.out.println("  -debug          Print debugging info");
		System.out.println();
		System.out.println("All configuration information is read from the file database.properties. ");
		System.out.println("See the comments in that file for information on which options to set.");

	}

	// ---------------------------------------------------------------------

	protected void setupLogging() {

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

		if (System.getProperty("output.databases") == null) {
			System.err.println("No databases specified in " + getPropertiesFile());
			System.exit(1);
		}

		if (System.getProperty("output.release") == null) {
			System.err.println("No release specified in " + getPropertiesFile() + " - please add an output.release property");
			System.exit(1);
		}

	}

	// ---------------------------------------------------------------------
	/**
	 * Create a list of databases and groups from the properties file. Multiple properties can be used as long as they start with
	 * output.databases, e.g. <code>
	 * output.databases1 = ^[a-k].*_core_63.*:release 
	 * output.databases2 = ^[l-z].*_core_63.*:release 
	 * </code> Individual properties can also contain multiple comma-separated sets.
	 */
	private List<String> getDatabasesAndGroups() {

		List<String> list = new ArrayList<String>();

		Properties props = System.getProperties();

		for (Enumeration<?> en = props.propertyNames(); en.hasMoreElements();) {

			String key = (String) en.nextElement();

			if (key.startsWith("output.databases")) {
				
				String value = props.getProperty(key);
				list.addAll(Arrays.asList(value.split(",")));
				
			}

		}

		return list;

	}

	/**
	 * <p>
	 * Creates the bsub comand that sets the end_time columns in the database indicating when the session has ended. It will create
	 * something looking like this:
	 * </p>
	 * 
	 * <code>
	 * 	bsub -w 'ended("Job_0") && ended("Job_1")' /homes/mnuhn/workspaceDeleteMeWhenDone/ensj-healthcheck-session/run-healthcheck-node.sh -endDbSession -session 18
	 * </code>
	 * 
	 * @param jobNames
	 * @param runNodeDBTestRunnerScript
	 * @return
	 * 
	 */
	protected String createSessionEndTimeCmd(final List<String> jobNames, String runNodeDBTestRunnerScript) {

		StringBuffer bsubConditionClause = new StringBuffer();
		Iterator<String> jobNameIterator = jobNames.iterator();

		while (jobNameIterator.hasNext()) {

			String currentJobName = jobNameIterator.next();
			bsubConditionClause.append("ended(\"" + currentJobName + "\")");

			if (jobNameIterator.hasNext()) {
				bsubConditionClause.append(" && ");
			}
		}

		String finalJob = "bsub -w " + "'" + bsubConditionClause + "' " + runNodeDBTestRunnerScript + " -endDbSession -session " + ReportManager.getSessionID();

		return finalJob;
	}

	// ---------------------------------------------------------------------

	private void submitJobs(List<String> databasesAndGroups, long sessionID) {

		String dir = System.getProperty("user.dir");
		String runNodeDBTestRunnerScript = dir + File.separator + "run-healthcheck-node.sh";

		int jobNumber = 0;
		List<String> jobNames = new ArrayList<String>();

		for (Iterator<String> it = databasesAndGroups.iterator(); it.hasNext(); jobNumber++) {

			String[] databaseAndGroup = it.next().split(":");
			String database = databaseAndGroup[0];
			String group = databaseAndGroup[1];

			String currentJobName = "Job_" + jobNumber;

			// TODO EG: Need to push out LSF commands into separate file if we want to use them
			String[] cmd = { "bsub", "-J", currentJobName, "-q", "long", "-R", "select[myens_staging1<=800]", "-R", "select[myens_staging2<=800]", "-R", "select[myens_livemirror<=300]", "-R",
					"select[lustre && linux]", "-R", "order[ut:mem]", "-R", "rusage[myens_staging1=10:myens_staging1=10:myens_livemirror=50]", "-o", "healthcheck_%J.out", "-e", "healthcheck_%J.err",
					runNodeDBTestRunnerScript, "-d", database, "-group", group, "-session", "" + sessionID };

			jobNames.add(currentJobName);

			System.out.println(join(cmd));

			boolean submitJobsToLSF = false;

			if (submitJobsToLSF) {

				execCmd(cmd);
				System.out.println("Submitted job with database regexp " + database + " and group " + group + ", session ID " + sessionID);
			}
		}

		String sessionEndTimeCmd = createSessionEndTimeCmd(jobNames, runNodeDBTestRunnerScript);
		execCmd(sessionEndTimeCmd);

	}

	/**
	 * Used for executing bsub commands.
	 * 
	 * @param cmd
	 * 
	 */
	protected void execCmd(String... cmd) {

		// Set for debugging purposes. Commands will only be printed
		// but not executed.
		boolean onlyPrintCmd = true;

		if (onlyPrintCmd) {
			System.out.println(join(cmd));
			return;
		}

		try {

			Process p = Runtime.getRuntime().exec(cmd);

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

			String s = null;

			while ((s = stdInput.readLine()) != null) {
				System.out.println(s);
			}

			while ((s = stdError.readLine()) != null) {
			}

			stdInput.close();
			stdError.close();

		} catch (Exception ioe) {
			System.err.println("Error in head job " + ioe.getMessage());
		}
	}

	protected String join(String[] s) {
		return join(s, " ");
	}

	protected String join(String[] s, String delimiter) {

		StringBuffer sb = new StringBuffer();

		for (String item : s) {
			sb.append(item);
			sb.append(delimiter);
		}

		return sb.toString();
	}

	// ---------------------------------------------------------------------

} // ParallelDatabaseTestRunner
