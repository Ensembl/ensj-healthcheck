package org.ensembl.healthcheck;


import java.util.List;
import java.util.ArrayList;
import org.ensembl.healthcheck.configuration.ConfigureTestGroups;
import org.ensembl.healthcheck.testcase.EnsTestCase;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;
import org.ensembl.healthcheck.testcase.OrderedDatabaseTestCase;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * A test registry that returns tests based on a configuration file instead of
 * letting the tests register themselves.
 *
 */
public class ConfigurationBasedTestRegistry implements TestRegistry {

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
	 * @param A ConfigurationUserParameters object
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
		
		if (params.isGroup() && !isEmptyList(params.getGroup())) {
			for (String groupName : params.getGroup()) {				
				//GroupOfTests groupOfTests = (GroupOfTests) Class.forName(groupName).newInstance();
				GroupOfTests groupOfTests = (GroupOfTests) testInstantiator.forName(groupName).newInstance();
				userDefinedGroupOfTests.addTest(groupOfTests);
			}
		}
		
		if (params.isTest() && !isEmptyList(params.getTest())) {
			for (String testName : params.getTest()) {
				//Class<EnsTestCase> singleTestClass = (Class<EnsTestCase>) Class.forName(testName);
				Class<EnsTestCase> singleTestClass = (Class<EnsTestCase>) testInstantiator.forName(testName);
				userDefinedGroupOfTests.addTest(singleTestClass);
			}
		}
		
		if (params.isLess() && !isEmptyList(params.getLess())) {
			for (String groupName : params.getLess()) {				
				//GroupOfTests groupOfTests = (GroupOfTests) Class.forName(groupName).newInstance();
				GroupOfTests groupOfTests = (GroupOfTests) testInstantiator.forName(groupName).newInstance();
				userDefinedGroupOfTests.removeTest(groupOfTests);
			}
		}
	
		if (params.isNotest() && !isEmptyList(params.getNotest())) {
			for (String testName : params.getNotest()) {				
				//Class<EnsTestCase> singleTestClass = (Class<EnsTestCase>) Class.forName(testName);
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
		
		result.append( "DatabaseTestCaseList: \n"        + ListToString(this.getDatabaseTestCaseList()) + "\n" );
		result.append( "Single database testcases: \n"   + ListToString(this.getAllSingle(null, null))  + "\n" );
		result.append( "Multi database testcases: \n"    + ListToString(this.getAllMulti(null))         + "\n" );
		result.append( "Ordered database testcases: \n"  + ListToString(this.getAllOrdered(null))       + "\n" );
		
		return result.toString();
	}
	
	protected String ListToString(List<EnsTestCase> l) {

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

	public List<EnsTestCase> getAllMulti(List<String> groupsToRun) {
		return new ArrayList<EnsTestCase>(this.getMultiDatabaseTestCaseList());
	}

	public List<EnsTestCase> getAllOrdered(List<String> groups) {
		return new ArrayList<EnsTestCase>(this.getOrderedDatabaseTestCaseList());
	}

	public List<EnsTestCase> getAllSingle(List<String> groupsToRun,
			DatabaseType type) {
		return new ArrayList<EnsTestCase>(this.getSingleDatabaseTestCaseList());
	}

	public String[] getGroups(DatabaseType type) {
		throw new RuntimeException("getGroups has not been implemented yet!");
		//return null;
	}

	public EnsTestCase[] getTestsInGroup(String string, DatabaseType type) {
		throw new RuntimeException("getTestsInGroup has not been implemented yet!");
	}

	public DatabaseType[] getTypes() {
		throw new RuntimeException("getTypes has not been implemented yet!");
	}
}
