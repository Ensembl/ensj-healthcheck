/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2018] EMBL-European Bioinformatics Institute
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

import java.util.List;

import org.ensembl.healthcheck.testcase.EnsTestCase;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;
import org.ensembl.healthcheck.testcase.OrderedDatabaseTestCase;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Hold information about tests. Can also find tests in a particular location.
 */
public interface TestRegistry {
	/**
	 * @param groupsToRun
	 * @param type
	 * @return list of tests
	 */
	public List<SingleDatabaseTestCase> getAllSingle(List<String> groupsToRun,
			DatabaseType type);

	/**
	 * @param groupsToRun
	 * @return list of tests belonging to those groups
	 */
	public List<MultiDatabaseTestCase> getAllMulti(List<String> groupsToRun);

	/**
	 * @param groups list of group names
	 * @return list of tests belonging to those groups
	 */
	public List<OrderedDatabaseTestCase> getAllOrdered(List<String> groups);

	/**
	 * @return all tests in the registry
	 */
	public List<EnsTestCase> getAll();

	/**
	 * @return all types covered by the registered tests
	 */
	public DatabaseType[] getTypes();

	/**
	 * @param type 
	 * @return groups applicable to the supplied type
	 */
	public String[] getGroups(DatabaseType type);

	/**
	 * Return list of tests for the given group
	 * @param string group name
	 * @param type database type
	 * @return list of test objects
	 */
	public EnsTestCase[] getTestsInGroup(String string, DatabaseType type);

}
