package org.ensembl.healthcheck.eg_gui;

import java.util.List;
import java.util.logging.Logger;

import javax.swing.JFrame;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.EnsTestCase;
import org.ensembl.healthcheck.util.Clazz;
import org.ensembl.healthcheck.util.FileNameFilter;
import org.ensembl.healthcheck.util.Jar;


public class GuiStarter {
	
    /** The logger to use for this class */
    protected static Logger logger = Logger.getLogger("HealthCheckLogger");

    /**
     * 
     * <p>
     * 	Searches the jarFile for classes that are true subclasses of 
     * GroupOfTests and returns them as a List of GroupOfTests objects. 
     * </p>
     * 
     * @param jarFile
     * @return List<GroupOfTests>
     * 
     */
    protected List<GroupOfTests> createListOfAvailableTestGroupsFromJar(String jarFile) {

		List<String> classesInJar = Jar.findAllClassesInJar(jarFile);
		
		List<GroupOfTests> testGroupList = Clazz.instantiateListOfTestGroups(
			FileNameFilter.filterForTrueSubclassesOf(
					classesInJar,
					GroupOfTests.class
			)
		);
		return testGroupList;
    }
    
    protected List<Class<EnsTestCase>> createListOfAvailableTestsFromJar(String jarFile) {

		List<String> classesInJar = Jar.findAllClassesInJar(jarFile);
		
		List<Class<EnsTestCase>> testGroupList = Clazz.classloadListOfClasses(
				FileNameFilter.filterForTrueSubclassesOf(
						classesInJar,
						EnsTestCase.class
				)
		);
		return testGroupList;
    }
    
	public void run() {
		
		String jarFile = "lib/ensj-healthcheck.jar";
		
		List<GroupOfTests> testGroupList = createListOfAvailableTestGroupsFromJar(jarFile);
		
		List<Class<EnsTestCase>> allTestsList = createListOfAvailableTestsFromJar(jarFile);
		
		// Create a group that has all tests and add it to the testGroupList 
		// for the user to select from.
		//
		GroupOfTests allGroups = new GroupOfTests();
		allGroups.addTest(allTestsList);
		allGroups.setName(Constants.ALL_TESTS_GROUP_NAME);
		
		testGroupList.add(allGroups);
		
		String packageWithHealthchecks = "org.ensembl.healthcheck.testcase";
		String packageWithTestgroups   = "org.ensembl.healthcheck.testgroup";
			
		TestInstantiatorDynamic testInstantiator = new TestInstantiatorDynamic(
				packageWithHealthchecks, 
				packageWithTestgroups
			);
			
		testInstantiator.addDynamicGroups(Constants.ALL_TESTS_GROUP_NAME, allGroups);
		
		JFrame frame = new GuiTestRunnerFrame(testGroupList, testInstantiator);
	    frame.setVisible(true);
	}

	public static void main(String argv[]) {
		
		GuiStarter t = new GuiStarter();
		t.run();
	}
}



