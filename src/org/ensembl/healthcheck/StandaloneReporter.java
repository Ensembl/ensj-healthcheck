/*
 * Copyright [1999-2016] EMBL-European Bioinformatics Institute
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.ensembl.healthcheck.testcase.EnsTestCase;
import org.ensembl.healthcheck.util.CollectionUtils;

/**
 * Reporter that captures messages in a hash
 * 
 * @author dstaines
 *
 */
public class StandaloneReporter implements Reporter {

	private final Logger logger;

	public StandaloneReporter() {
		this.logger = Logger.getLogger(StandaloneReporter.class.getSimpleName());
	}

	public StandaloneReporter(Logger logger) {
		this.logger = logger;
	}

	private final Map<String, List<String>> failures = CollectionUtils.createHashMap();
	private final Map<String, Map<String, List<String>>> output = CollectionUtils.createHashMap();
	private final Map<String, List<String>> successes = CollectionUtils.createHashMap();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.healthcheck.Reporter#finishTestCase(org.ensembl.healthcheck.
	 * testcase.EnsTestCase, boolean,
	 * org.ensembl.healthcheck.DatabaseRegistryEntry)
	 */
	@Override
	public void finishTestCase(EnsTestCase testCase, boolean result, DatabaseRegistryEntry dbre) {
		if (result) {
			List<String> dbSuccess = successes.get(dbre.getName());
			if (dbSuccess == null) {
				dbSuccess = CollectionUtils.createArrayList();
				successes.put(dbre.getName(), dbSuccess);
			}
			dbSuccess.add(testCase.getTestName());
		} else {
			List<String> dbFailure = failures.get(dbre.getName());
			if (dbFailure == null) {
				dbFailure = CollectionUtils.createArrayList();
				failures.put(dbre.getName(), dbFailure);
			}
			dbFailure.add(testCase.getTestName());
		}
	}

	public Map<String, List<String>> getFailures() {
		return failures;
	}

	public Map<String, Map<String, List<String>>> getOutput() {
		return output;
	}

	public Map<String, List<String>> getSuccesses() {
		return successes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.healthcheck.Reporter#message(org.ensembl.healthcheck.
	 * ReportLine)
	 */
	@Override
	public void message(ReportLine reportLine) {
		logger.fine(reportLine.toString());
		Map<String, List<String>> dbOutput = output.get(reportLine.getDatabaseName());
		if (dbOutput == null) {
			dbOutput = CollectionUtils.createHashMap();
			output.put(reportLine.getDatabaseName(), dbOutput);
		}
		List<String> testList = dbOutput.get(reportLine.getTestCase().getTestName());
		if (testList == null) {
			testList = CollectionUtils.createArrayList();
			dbOutput.put(reportLine.getTestCase().getTestName(), testList);
		}
		testList.add(reportLine.getLevelAsString() + ": " + reportLine.getMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.healthcheck.Reporter#startTestCase(org.ensembl.healthcheck.
	 * testcase.EnsTestCase, org.ensembl.healthcheck.DatabaseRegistryEntry)
	 */
	@Override
	public void startTestCase(EnsTestCase testCase, DatabaseRegistryEntry dbre) {
		Map<String, List<String>> dbOutput = output.get(dbre.getName());
		if (dbOutput == null) {
			dbOutput = CollectionUtils.createHashMap();
			output.put(dbre.getName(), dbOutput);
		}
		dbOutput.put(testCase.getTestName(),  new ArrayList<String>());
	}

}
