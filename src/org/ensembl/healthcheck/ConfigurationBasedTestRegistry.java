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


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.ensembl.healthcheck.configuration.ConfigureTestGroups;
import org.ensembl.healthcheck.testcase.EnsTestCase;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;
import org.ensembl.healthcheck.testcase.OrderedDatabaseTestCase;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.CollectionUtils;

/**
 * A test registry that returns tests based on a configuration file instead of
 * letting the tests register themselves.
 *
 */
public class ConfigurationBasedTestRegistry implements TestRegistry {

	
	/** The logger to use for this class */
	protected static Logger logger = Logger.getLogger("HealthCheckLogger");
	//
	// Begin getters and setters
	//
	
	private GroupOfTests getUserDefinedGroupOfTests() {
		return userDefinedGroupOfTests;
	}

	private void setUserDefinedGroupOfTests(GroupOfTests userDefinedGroupOfTests) {
		this.userDefinedGroupOfTests = userDefinedGroupOfTests;
	}

	private List<SingleDatabaseTestCase> getSingleDatabaseTestCaseList() {
		return singleDatabaseTestCaseList;
	}

	private void setSingleDatabaseTestCaseList(
			List<SingleDatabaseTestCase> singleDatabaseTestCaseList) {
		this.singleDatabaseTestCaseList = singleDatabaseTestCaseList;
	}

	private List<MultiDatabaseTestCase> getMultiDatabaseTestCaseList() {
		return multiDatabaseTestCaseList;
	}

	private void setMultiDatabaseTestCaseList(
			List<MultiDatabaseTestCase> multiDatabaseTestCaseList) {
		this.multiDatabaseTestCaseList = multiDatabaseTestCaseList;
	}

	private List<OrderedDatabaseTestCase> getOrderedDatabaseTestCaseList() {
		return orderedDatabaseTestCaseList;
	}

	private void setOrderedDatabaseTestCaseList(
			List<OrderedDatabaseTestCase> orderedDatabaseTestCaseList) {
		this.orderedDatabaseTestCaseList = orderedDatabaseTestCaseList;
	}

	private List<EnsTestCase> getDatabaseTestCaseList() {
		return databaseTestCaseList;
	}

	private void setDatabaseTestCaseList(List<EnsTestCase> databaseTestCaseList) {
		this.databaseTestCaseList = databaseTestCaseList;
	}

	//
	// End getters and setters
	//

	private GroupOfTests userDefinedGroupOfTests;
	private List<EnsTestCase>             databaseTestCaseList;
	private List<SingleDatabaseTestCase>  singleDatabaseTestCaseList;
	private List<MultiDatabaseTestCase>   multiDatabaseTestCaseList;
	private List<OrderedDatabaseTestCase> orderedDatabaseTestCaseList;

	/**
	 * @param params ConfigurationUserParameters object
	 *  
	 */
	public ConfigurationBasedTestRegistry(ConfigureTestGroups params) 
		throws 
			UnknownTestTypeException, 
			InstantiationException, 
			IllegalAccessException, 
			ClassNotFoundException {
		
		initGroupSet(params);		
		this.setDatabaseTestCaseList(new ArrayList<EnsTestCase>(userDefinedGroupOfTests.getTests()));		
		initDatabaseLists();		
	}

	protected boolean isEmptyList(List<String> l) {		
		return l.size()==1 && l.get(0).equals("");
	}
	
	/**
	 * The user can specify 
	 * 
	 * - groups of tests which should be run
	 * - groups of tests which should not be run
	 * - single tests which should be run
	 * - single tests which should not be run
	 * 
	 * This method initialises the "userDefinedGroupOfTests" which has the 
	 * tests that satisfy theses conditions.
	 * 
	 */
	private void initGroupSet(ConfigureTestGroups params) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		
		// TODO: Must make this configurable
		String packageWithHealthchecks = "org.ensembl.healthcheck.testcase";
		String packageWithTestgroups   = "org.ensembl.healthcheck.testgroup";
		
		GroupOfTests userDefinedGroupOfTests = new GroupOfTests();
		TestInstantiator testInstantiator    = new TestInstantiator(
			packageWithHealthchecks, 
			packageWithTestgroups
		);

		// The following tests do
		//
		// if (params.isXXX() && params.getXXX().size()>0) {
		//     ...
		// }
		//
		// Which seems to be a redundant test, but it is not. A parameter
		// can be set like this:
		//
		// test = 
		//
		// Which would make params.isTest() to be true, but the following
		// attempt to load the class "" fail.
		
		if (params.isGroups() && !isEmptyList(params.getGroups())) {
			for (String groupName : params.getGroups()) {				

				//GroupOfTests groupOfTests = (GroupOfTests) testInstantiator.forName(groupName).newInstance();
				GroupOfTests groupOfTests = testInstantiator.instanceByName(groupName, GroupOfTests.class);
				userDefinedGroupOfTests.addTest(groupOfTests);
			}
		}
		
		if (params.isTests() && !isEmptyList(params.getTests())) {
			for (String testName : params.getTests()) {
				
				Class<EnsTestCase> singleTestClass = (Class<EnsTestCase>) testInstantiator.forName(testName);
				userDefinedGroupOfTests.addTest(singleTestClass);
			}
		}
		
		if (params.isExcludeGroups() && !isEmptyList(params.getExcludeGroups())) {
			for (String groupName : params.getExcludeGroups()) {				

				//GroupOfTests groupOfTests = (GroupOfTests) testInstantiator.forName(groupName).newInstance();
				GroupOfTests groupOfTests = testInstantiator.instanceByName(groupName, GroupOfTests.class);
				userDefinedGroupOfTests.removeTest(groupOfTests);
			}
		}
	
		if (params.isExcludeTests() && !isEmptyList(params.getExcludeTests())) {
			for (String testName : params.getExcludeTests()) {				

				Class<EnsTestCase> singleTestClass = (Class<EnsTestCase>) testInstantiator.forName(testName);
				userDefinedGroupOfTests.removeTest(singleTestClass);
			}
		}
		setUserDefinedGroupOfTests(userDefinedGroupOfTests);
	}
	
	/**
	 * The tests that should be run are divided here into three lists:
	 * 
	 * - SingleDatabaseTestCase
	 * - MultiDatabaseTestCase and 
	 * - OrderedDatabaseTestCase
	 * 
	 * which is how they will be retrieved by the users of this class.
	 * 
	 */
	private void initDatabaseLists() throws UnknownTestTypeException {
		
		List<EnsTestCase> listOfTests = this.getDatabaseTestCaseList();
		
		List<SingleDatabaseTestCase>  single  = new ArrayList<SingleDatabaseTestCase>();
		List<MultiDatabaseTestCase>   multi   = new ArrayList<MultiDatabaseTestCase>();
		List<OrderedDatabaseTestCase> ordered = new ArrayList<OrderedDatabaseTestCase>();
		
		for (EnsTestCase currentTest : listOfTests) {
			
			boolean testWasAddedToAList = false;
			
			if (currentTest instanceof SingleDatabaseTestCase) {
				single.add((SingleDatabaseTestCase) currentTest);
				testWasAddedToAList = true;
			}
			if (currentTest instanceof MultiDatabaseTestCase) {
				multi.add((MultiDatabaseTestCase) currentTest);
				testWasAddedToAList = true;
			}
			if (currentTest instanceof OrderedDatabaseTestCase) {
				ordered.add((OrderedDatabaseTestCase) currentTest);
				testWasAddedToAList = true;
			}
			
			if (!testWasAddedToAList) {
				throw new UnknownTestTypeException("Could not add this test to a list: " + currentTest);
			}
			
		}
		this.setSingleDatabaseTestCaseList(single);
		this.setMultiDatabaseTestCaseList(multi);
		this.setOrderedDatabaseTestCaseList(ordered);
	}
	
	public String toString() {
		
		StringBuffer result = new StringBuffer();
		
		result.append( "DatabaseTestCaseList: \n"        + listToString(this.getDatabaseTestCaseList()) + "\n" );
		result.append( "Single database testcases: \n"   + listToString(this.getAllSingle(null, null))  + "\n" );
		result.append( "Multi database testcases: \n"    + listToString(this.getAllMulti(null))         + "\n" );
		result.append( "Ordered database testcases: \n"  + listToString(this.getAllOrdered(null))       + "\n" );
		
		return result.toString();
	}
	
	protected String listToString(List<? extends EnsTestCase> l) {

		StringBuffer s = new StringBuffer(); 
		
		if (l.size()==0) return " - none - ";
		
		for (EnsTestCase e : l) {
			s.append("  - " + e.getClass().getName() + "\n");
		}
		return s.toString();
	}
	
	public List<EnsTestCase> getAll() {
		return new ArrayList<EnsTestCase>(this.getUserDefinedGroupOfTests().getTests());
	}

	public List<MultiDatabaseTestCase> getAllMulti(List<String> groupsToRun) {
		return new ArrayList<MultiDatabaseTestCase>(this.getMultiDatabaseTestCaseList());
	}

	public List<OrderedDatabaseTestCase> getAllOrdered(List<String> groups) {
		return new ArrayList<OrderedDatabaseTestCase>(this.getOrderedDatabaseTestCaseList());
	}

	public List<SingleDatabaseTestCase> getAllSingle(List<String> groupsToRun,
			DatabaseType type) {
		if(groupsToRun!=null && groupsToRun.size()>0) {
			throw new UnsupportedOperationException("Group selection not supported for "+this.getClass().getName()+".getAllSingle()");
		}
		return getSingle(type);
	}
	
	private Map<DatabaseType,List<SingleDatabaseTestCase>> singleTestsByType;
	protected Map<DatabaseType,List<SingleDatabaseTestCase>> getSingleTestsByType() {
		if(singleTestsByType==null) {
			singleTestsByType = CollectionUtils.createHashMap();
			for(SingleDatabaseTestCase test: this.getSingleDatabaseTestCaseList()) {
				for(DatabaseType type: test.getAppliesToTypes()) {
					List<SingleDatabaseTestCase> tests = singleTestsByType.get(type);
					if(tests==null) {
						tests = CollectionUtils.createArrayList();
						singleTestsByType.put(type,tests);
					}
					tests.add(test);
				} 
			}
		}
		return singleTestsByType;
	}
	public List<SingleDatabaseTestCase> getSingle(DatabaseType type) {
		if(type==null) {
			return this.getSingleDatabaseTestCaseList();
		} else {
			List<SingleDatabaseTestCase> ts = getSingleTestsByType().get(type);
			if(ts==null) {
				logger.warning("Couldn't find any tests for database type "+type.getName());
				ts = CollectionUtils.createArrayList();
			}
			return ts;
		}
	}

	public String[] getGroups(DatabaseType type) {
		throw new UnsupportedOperationException(this.getClass().getName()+".getGroups() not yet implemented");
	}

	public EnsTestCase[] getTestsInGroup(String string, DatabaseType type) {
		throw new UnsupportedOperationException(this.getClass().getName()+".getTestsInGroup() not yet implemented");
	}

	public DatabaseType[] getTypes() {
		throw new UnsupportedOperationException(this.getClass().getName()+".getTypes() not yet implemented");
	}
}
