package org.ensembl.healthcheck.eg_gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.configuration.ConfigureHost;
import org.ensembl.healthcheck.testcase.EnsTestCase;
import org.ensembl.healthcheck.util.ClassFileFilter;
import org.ensembl.healthcheck.util.Clazz;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.Jar;

public class SetupTabBuilder {
	
	protected SetupTab setupTab;
	
	public SetupTabBuilder(
		ActionListener actionListener,
		String jarFile
	) {
		this.setupTab = new SetupTab();
		
		setupTab.actionListener = actionListener;
		setupTab.jarFile = jarFile;
	}

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
			ClassFileFilter.filterForTrueSubclassesOf(
					classesInJar,
					GroupOfTests.class
			)
		);
		return testGroupList;
	}

	protected List<Class<EnsTestCase>> createListOfAvailableTestsFromJar(String jarFile) {

		List<String> classesInJar = Jar.findAllClassesInJar(jarFile);
		
		List<Class<EnsTestCase>> testGroupList = Clazz.classloadListOfClasses(
				ClassFileFilter.filterForTrueSubclassesOf(
						classesInJar,
						EnsTestCase.class
				)
		);
		return testGroupList;
	}
	
	public void buildTestGroupList() {
		setupTab.testGroupList = createListOfAvailableTestGroupsFromJar(setupTab.jarFile);
	}
	
	public void buildAllTestsList() {
		setupTab.allTestsList = createListOfAvailableTestsFromJar(setupTab.jarFile);
	}
	
	public void buildAllTests() {
		
		if (setupTab.testGroupList==null) {
			throw new NullPointerException("testGroupList has not been built.");
		}
		
		// Create a group that has all tests and add it to the testGroupList 
		// for the user to select from.
		//
		GroupOfTests allGroups = new GroupOfTests();
		allGroups.addTest(setupTab.allTestsList);
		allGroups.setName(Constants.ALL_TESTS_GROUP_NAME);
		
		setupTab.testGroupList.add(allGroups);
	}
	
	protected void buildTestInstantiator() {
		
		String packageWithHealthchecks = "org.ensembl.healthcheck.testcase";
		String packageWithTestgroups   = "org.ensembl.healthcheck.testgroup";
			
		setupTab.testInstantiator = new TestInstantiatorDynamic(
				packageWithHealthchecks, 
				packageWithTestgroups
			);
		if (setupTab.allGroups!=null) {
			setupTab.testInstantiator.addDynamicGroups(Constants.ALL_TESTS_GROUP_NAME, setupTab.allGroups);
		}
	}
	
	protected void buildListOfTestsToBeExecutedPopupMenu() {
		
		if (setupTab.actionListener==null) {
			throw new NullPointerException("actionListener has not been built.");
		}
		if (setupTab==null) {
			throw new NullPointerException("setupTab has not been built.");
		}

		setupTab.listOfTestsToBeExecutedPopupMenu = GuiTestRunnerFrameComponentBuilder
			.createListOfTestsToBeExecutedPopupMenu(setupTab.actionListener);
	}

	protected void buildMysqlConnectionWidget() {
		
		setupTab.mysqlWidget = new MySqlConnectionWidget(
				setupTab.dbDetails, 
				setupTab.dbServerSelector, 
				setupTab.databaseTabbedPaneWithSearchBox.getDatabasePane()
		);
	}
	
	protected void buildComponentWiring() {

		if (setupTab==null) {
			throw new NullPointerException("setupTab has not been built.");
		}
		if (setupTab.tree==null) {
			throw new NullPointerException("setupTab.tree has not been built.");
		}
		if (setupTab.dbServerSelector==null) {
			throw new NullPointerException("setupTab.dbServerSelector has not been built.");
		}
		if (setupTab.secondaryDbServerSelector==null) {
			throw new NullPointerException("setupTab.secondaryDbServerSelector has not been built.");
		}

		setupTab.databaseTabbedPaneWithSearchBox.getDatabasePane().addActionListener(setupTab.mysqlWidget);
		setupTab.dbServerSelector.addActionListener(setupTab.mysqlWidget);
		
		JScrollPane treePane  = new JScrollPane(setupTab.tree);
		
		JSplitPane testSelectionWidgets = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT,
				treePane, 
				setupTab.testsPane
		);

		setupTab.setLayout(new BorderLayout());
		setupTab.add(setupTab.mysqlWidget, BorderLayout.SOUTH);
		setupTab.add(
				new JSplitPane(
						JSplitPane.VERTICAL_SPLIT, 
						setupTab.databaseTabbedPaneWithSearchBox,
						testSelectionWidgets
				),
				BorderLayout.CENTER
		);

		Box hbox = Box.createHorizontalBox();
		
		hbox.add(setupTab.dbServerSelector);
		hbox.add(setupTab.secondaryDbServerSelector);
		
		setupTab.add(hbox, BorderLayout.NORTH);
		
		// Create space around components so they don't look too crammed. 
		//
		Border defaultEmptyBorder = GuiTestRunnerFrameComponentBuilder.defaultEmptyBorder;

		Border noBorder = BorderFactory.createEmptyBorder(0, 0, 0, 0);
		
		setupTab.dbServerSelector          .setBorder(BorderFactory.createTitledBorder(noBorder, "Primary (where your database is):"));
		setupTab.secondaryDbServerSelector .setBorder(BorderFactory.createTitledBorder(noBorder, "Secondary (used by the ComparePreviousVersion* healthchecks):"));
		
		setupTab.databaseTabbedPaneWithSearchBox   .setBorder(BorderFactory.createTitledBorder(defaultEmptyBorder, "2. Select a database:"));

		hbox                 .setBorder(BorderFactory.createTitledBorder(defaultEmptyBorder, "1. Select database servers:"));
		testSelectionWidgets .setBorder(BorderFactory.createTitledBorder(defaultEmptyBorder, "3. Select tests to be run:"));
		treePane             .setBorder(BorderFactory.createTitledBorder("Drag tests to the panel on the right"));
		setupTab.testsPane   .setBorder(BorderFactory.createTitledBorder("Select tests and run from context menu"));
		
		/**
		 * Preferred size of the window.
		 *  
		 */
		int windowWidth  = Constants.INITIAL_APPLICATION_WINDOW_WIDTH;
		int windowHeight = Constants.INITIAL_APPLICATION_WINDOW_HEIGHT;

		setupTab.databaseTabbedPaneWithSearchBox.setMinimumSize(new Dimension(0,       windowHeight/4));
		setupTab.testsPane         .setMinimumSize(new Dimension(0,       windowHeight/4));
		treePane          .setMinimumSize(new Dimension(windowWidth/3, 0));

	}

	protected void buildListOfTestsToBeRunArea() {
		
		if (setupTab.testInstantiator==null) {
			throw new NullPointerException("testInstantiator has not been built.");
		}
		if (setupTab.listOfTestsToBeExecutedPopupMenu==null) {
			throw new NullPointerException("listOfTestsToBeExecutedPopupMenu has not been built.");
		}
		if (setupTab.actionListener==null) {
			throw new NullPointerException("actionListener has not been built.");
		}

		setupTab.listOfTestsToBeRun = GuiTestRunnerFrameComponentBuilder.createListOfTestsToBeRunArea(
				setupTab.testInstantiator, 
				setupTab.listOfTestsToBeExecutedPopupMenu,
				setupTab.actionListener
		);
		setupTab.rmSelectedTests  = GuiTestRunnerFrameComponentBuilder.createRemoveSelectedTestsButton(setupTab.actionListener);
		setupTab.runAllTests      = GuiTestRunnerFrameComponentBuilder.createRunAllTestsButton(setupTab.actionListener);
		setupTab.runSelectedTests = GuiTestRunnerFrameComponentBuilder.createRunSelectedTestsButton(setupTab.actionListener);

		Box buttonBox = Box.createHorizontalBox();
		
		buttonBox.add(setupTab.rmSelectedTests);
		buttonBox.add(Box.createHorizontalStrut(Constants.DEFAULT_HORIZONTAL_COMPONENT_SPACING));
		buttonBox.add(setupTab.runSelectedTests);
		buttonBox.add(Box.createHorizontalStrut(Constants.DEFAULT_HORIZONTAL_COMPONENT_SPACING));
		buttonBox.add(setupTab.runAllTests);

		setupTab.testsPane = new JPanel(new BorderLayout());
		
		setupTab.testsPane.add(buttonBox,                      BorderLayout.SOUTH);
		setupTab.testsPane.add(new JScrollPane(setupTab.listOfTestsToBeRun),  BorderLayout.CENTER);
	}
	
	protected void buildDbDetails() {
		
		if (setupTab==null) {
			throw new NullPointerException("setupTab has not been built.");
		}
		if (setupTab.dirsWithDbServerConfigs==null) {
			throw new NullPointerException("setupTab.dirsWithDbServerConfigs has not been built.");
		}
		
		// Set to false, because checking availability costs too much startup
		// time when there are many servers configured and opens too many 
		// connections.
		//
		boolean checkAvailabilityOfServers = false;
		
		if (checkAvailabilityOfServers) {		
			setupTab.dbDetails = GuiTestRunnerFrameUtils.grepForAvailableServers(setupTab.dbDetails);
		}
		setupTab.dbDetails = GuiTestRunnerFrameUtils.createDbDetailsConfigurations(
				setupTab.dirsWithDbServerConfigs
		);
	}
	
	protected void buildDbServerSelector() {

		if (setupTab==null) {
			throw new NullPointerException("setupTab has not been built.");
		}
		if (setupTab.dbDetails==null) {
			throw new NullPointerException("setupTab.dbDetails has not been built.");
		}
		if (setupTab.actionListener==null) {
			throw new NullPointerException("setupTab.actionListener has not been built.");
		}
		
		setupTab.dbServerSelector = GuiTestRunnerFrameComponentBuilder.createDbServerSelector(setupTab.dbDetails);
		setupTab.dbServerSelector.setActionCommand(Constants.DB_SERVER_CHANGED);
		setupTab.dbServerSelector.addActionListener(setupTab.actionListener);
		
		//
		// See, if defaultSecondaryServerName can be found in dbDetails. If so,
		// select this as the default secondary server.
		//
		int numServers = setupTab.dbDetails.size();
		int defaultSelectedServerIndex = 0;
		
		for (int index=0; index<numServers; index++) {
		
			String serverName = setupTab.dbDetails.get(index).getHost();
			if (serverName.contains(setupTab.defaultPrimaryServerName)) {
				defaultSelectedServerIndex = index;
			}
		}
		
		if (setupTab.dbServerSelector.getItemCount()>0) {
			setupTab.dbServerSelector.setSelectedIndex(defaultSelectedServerIndex);
			setupTab.primaryHostDetails = setupTab.dbDetails.get(defaultSelectedServerIndex);
		}
	}
	
	protected void buildSecondaryDbServerSelector() {

		if (setupTab==null) {
			throw new NullPointerException("setupTab has not been built.");
		}
		if (setupTab.dbDetails==null) {
			throw new NullPointerException("setupTab.dbDetails has not been built.");
		}
		if (setupTab.actionListener==null) {
			throw new NullPointerException("setupTab.actionListener has not been built.");
		}

		setupTab.secondaryDbServerSelector = GuiTestRunnerFrameComponentBuilder.createDbServerSelector(setupTab.dbDetails);
		setupTab.secondaryDbServerSelector.setActionCommand(Constants.SECONDARY_DB_SERVER_CHANGED);
		setupTab.secondaryDbServerSelector.addActionListener(setupTab.actionListener);
		
		//
		// See, if defaultSecondaryServerName can be found in dbDetails. If so,
		// select this as the default secondary server.
		//
		int numServers = setupTab.dbDetails.size();
		int defaultSelectedServerIndex = 0;
		
		for (int index=0; index<numServers; index++) {
		
			String serverName = setupTab.dbDetails.get(index).getHost();
			if (serverName.contains(setupTab.defaultSecondaryServerName)) {
				defaultSelectedServerIndex = index;
			}
		}
		
		if (setupTab.secondaryDbServerSelector.getItemCount()>0) {
			setupTab.secondaryDbServerSelector.setSelectedIndex(defaultSelectedServerIndex);
			setupTab.secondaryHostDetails = setupTab.dbDetails.get(defaultSelectedServerIndex);
		}
	}
	
	protected void buildDatabaseTabbedPaneWithSearchBox() {
		
		if (setupTab==null) {
			throw new NullPointerException("setupTab has not been built.");
		}
		if (setupTab.actionListener==null) {
			throw new NullPointerException("setupTab.actionListener has not been built.");
		}

		DBUtils.initialise(false);
		
		List<String> regexps = new ArrayList<String>();
		regexps.add(".*");

		DatabaseRegistry databaseRegistry = new DatabaseRegistry(regexps, null, null, false);

		DatabaseTabbedPane databaseTabbedPane = new DatabaseTabbedPane(databaseRegistry);
		databaseTabbedPane.addActionListener(setupTab.actionListener);

		setupTab.databaseTabbedPaneWithSearchBox = new DatabaseTabbedPaneWithSearchBox(databaseTabbedPane);
	}
	
	protected void buildTreeOfTestGroups() {

		if (setupTab==null) {
			throw new NullPointerException("guiTestRunnerFrame has not been built.");
		}
		
		// Functionality of the popup menu is not implemented yet,
		// so no popup menu for the tree
		//
		//final JPopupMenu popupMenuTree = GuiComponentBuilder
		//		.createTreeOfTestGroupsPopupMenu(actionListener);
		final JPopupMenu popupMenuTree = null;
		
		setupTab.tree = GuiTestRunnerFrameComponentBuilder.createTreeOfTestGroups(
			setupTab.testGroupList,
			popupMenuTree
		);
	}

	public SetupTab getResult() {
		return setupTab;
	}
}
