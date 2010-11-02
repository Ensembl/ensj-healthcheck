/*
 Copyright (C) 2003 EBI, GRL
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
	 * @return
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
