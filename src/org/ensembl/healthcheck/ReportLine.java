/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2021] EMBL-European Bioinformatics Institute
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

package org.ensembl.healthcheck;

import org.ensembl.healthcheck.DatabaseRegistryEntry.DatabaseInfo;
import org.ensembl.healthcheck.testcase.EnsTestCase;

/**
 * A single line of a report. Each ReportLine stores the names of the test case
 * and database (as Strings) a message, and a level. See the constants defined
 * by this class for the different levels. Levels are represented as ints to
 * allow easy comparison and setting of thresholds. EG: For improved report
 * tracing, include DatabaseInfo and Species in ReportLine
 */
public class ReportLine {

	/** The output level of this report */
	protected int level;

	/** The test case that this report refers to */
	protected String testCaseName;

	public EnsTestCase getTestCase() {
		return testCase;
	}

	public void setTestCase(EnsTestCase testCase) {
		this.testCase = testCase;
	}

	protected EnsTestCase testCase;

	/** The database name that this report refers to */
	protected String databaseName;
	protected String speciesName;
	protected DatabaseType type;

	/** The message that this report contains */
	protected String message;

	/** The team responsible for this report */
	protected Team teamResponsible;
	protected Team secondTeamResponsible;

	/** Output level that is higher than all the others */
	public static final int NONE = 2000;

	/** Output level representing a problem with a test */
	public static final int PROBLEM = 1000;

	/** Output level representing a test that has passed */
	public static final int WARNING = 750;

	/**
	 * Output level representing something that should be included in the test
	 * summary
	 */
	public static final int INFO = 500;

	/** Output level representing something that is for information only */
	public static final int CORRECT = 100;

	/** Output level that is lower than all others */
	public static final int ALL = 0;

	/** For log messages. */
	public static final int LOG_MESSAGE = -10;

	/**
	 * Creates a new instance of ReportLine
	 * 
	 * @param testCase
	 *            The test case to refer to.
	 * @param name
	 *            The database name involved.
	 * @param level
	 *            The level of this report.
	 * @param message
	 *            The message to report.
	 */
	public ReportLine(EnsTestCase testCase, String name, int level, String message, Team teamResponsible,
			Team secondTeamResponsible) {

		this.testCase = testCase;
		this.testCaseName = testCase.getTestName();
		DatabaseInfo info = DatabaseRegistryEntry.getInfoFromName(name);
		this.databaseName = info.getName();
		if (info.getSpecies() != DatabaseRegistryEntry.UNKNOWN) {
			speciesName = info.getSpecies().toString();
		} else {
			speciesName = info.getAlias();
		}
		this.type = info.getType();
		this.level = level;
		this.message = message;
		this.teamResponsible = teamResponsible;
		this.secondTeamResponsible = secondTeamResponsible;

	} // constructor

	// -------------------------------------------------------------------------
	/**
	 * Get the level of this ReportLine.
	 * 
	 * @return level The level.
	 */
	public int getLevel() {

		return level;

	}

	/**
	 * Set the level of this report.
	 * 
	 * @param l
	 *            The new level.
	 */
	public void setLevel(int l) {

		level = l;

	}

	/**
	 * Get the report message.
	 * 
	 * @return The report message.
	 */
	public String getMessage() {

		return message;

	}

	/**
	 * Set the report message.
	 * 
	 * @param s
	 *            The new message.
	 */
	public void setMessage(String s) {

		message = s;

	}

	/**
	 * Get the name of the test case that this report line is associated with.
	 * 
	 * @return The name of the test case.
	 */
	public String getTestCaseName() {

		return testCaseName;

	}

	/**
	 * Set the name of the test case that this report is associated with.
	 * 
	 * @param s
	 *            The new name.
	 */
	public void setTestCaseName(String s) {

		testCaseName = s;

	}

	/**
	 * Get the name of the database that this report line is associated with.
	 * 
	 * @return The database name
	 */
	public String getDatabaseName() {

		return databaseName;

	}

	/**
	 * Set the name of the database that this report line is associated with.
	 * 
	 * @param s
	 *            The new name.
	 */
	public void setDatabaseName(String s) {

		databaseName = s;

	}

	// -------------------------------------------------------------------------
	/**
	 * Get the short test name (without the package name) of the test associated
	 * with this report.
	 * 
	 * @return The short test name.
	 */
	public String getShortTestCaseName() {

		return testCaseName.substring(testCaseName.lastIndexOf(".") + 1);

	} // setShortTestCaseName

	// -------------------------------------------------------------------------
	/**
	 * Get the level of this report as a String ("PROBLEM", "CORRECT" etc).
	 * 
	 * @return The level as a string.
	 */
	public String getLevelAsString() {

		String result = "";

		switch (level) {
		case CORRECT:
			result = "CORRECT";
			break;
		case INFO:
			result = "INFO";
			break;
		case WARNING:
			result = "WARNING";
			break;
		case PROBLEM:
			result = "PROBLEM";
			break;
		case NONE:
			result = "NONE";
			break;
		case LOG_MESSAGE:
			result = "LOG_MESSAGE";
			break;
		default:
			System.err.println("Can't get text equivalent for report level " + level);
			break;
		}

		return result;

	} // getLevelAsString

	public Team getTeamResponsible() {
		return teamResponsible;
	}

	public void setTeamResponsible(Team teamResponsible) {
		this.teamResponsible = teamResponsible;
	}

	public Team getSecondTeamResponsible() {
		return secondTeamResponsible;
	}

	public void setSecondTeamResponsible(Team teamResponsible) {
		this.secondTeamResponsible = teamResponsible;
	}

	public String getPrintableTeamResponsibleString() {

		Team team = getTeamResponsible();
		String teamName;

		if (team == null) {
			teamName = "No team specified.";
		} else {
			teamName = getTeamResponsible().toString();

			if (getSecondTeamResponsible() != null) {
				teamName += " and " + getSecondTeamResponsible();
			}
		}
		return teamName;
	}

	public String getSpeciesName() {
		return speciesName;
	}

	public void setSpecies(String species) {
		this.speciesName = species;
	}

	public DatabaseType getType() {
		return type;
	}

	public void setType(DatabaseType type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return String.format("%s %s %s : %s", this.getDatabaseName(), this.getTestCaseName(), this.getLevelAsString(),
				this.getMessage());
	}

	// -------------------------------------------------------------------------

} // ReportLine
