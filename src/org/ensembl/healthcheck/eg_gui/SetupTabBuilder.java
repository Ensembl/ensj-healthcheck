/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2020] EMBL-European Bioinformatics Institute
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
				setupTab.dbPrimaryServerSelector, 
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
		if (setupTab.dbPrimaryServerSelector==null) {
			throw new NullPointerException("setupTab.dbServerSelector has not been built.");
		}
		if (setupTab.dbSecondaryServerSelector==null) {
			throw new NullPointerException("setupTab.secondaryDbServerSelector has not been built.");
		}

		setupTab.databaseTabbedPaneWithSearchBox.getDatabasePane().addActionListener(setupTab.mysqlWidget);
		setupTab.dbPrimaryServerSelector.addActionListener(setupTab.mysqlWidget);
		
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
		
		hbox.add(setupTab.dbPrimaryServerSelector);
		hbox.add(setupTab.dbSecondPrimaryServerSelector);		
		hbox.add(setupTab.dbSecondaryServerSelector);
		
		setupTab.add(hbox, BorderLayout.NORTH);
		
		// Create space around components so they don't look too crammed. 
		//
		Border defaultEmptyBorder = GuiTestRunnerFrameComponentBuilder.defaultEmptyBorder;

		Border noBorder = BorderFactory.createEmptyBorder(0, 0, 0, 0);
		
		setupTab.dbPrimaryServerSelector      .setBorder(BorderFactory.createTitledBorder(noBorder, "Primary (where your database is):"));
		setupTab.dbSecondPrimaryServerSelector.setBorder(BorderFactory.createTitledBorder(noBorder, "Pan db server (for master databases)"));
		setupTab.dbSecondaryServerSelector    .setBorder(BorderFactory.createTitledBorder(noBorder, "Secondary (for ComparePreviousVersion* tests):"));
		
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
	
	protected void buildDbServerSelector(
		CallByReferenceWorkaround callByReferenceWorkaround,
		String actionCommandChanged,
		ActionListener actionListener,
		String defaultServerName
	) {
		
		if (setupTab==null) {
			throw new NullPointerException("setupTab has not been built.");
		}
		if (setupTab.dbDetails==null) {
			throw new NullPointerException("setupTab.dbDetails has not been built.");
		}
		if (setupTab.actionListener==null) {
			throw new NullPointerException("setupTab.actionListener has not been built.");
		}		
				
		JComboBox dbServerSelector = GuiTestRunnerFrameComponentBuilder.createDbServerSelector(setupTab.dbDetails);
		dbServerSelector.setActionCommand(actionCommandChanged);
		dbServerSelector.addActionListener(actionListener);		
		
		//
		// See, if the default server name can be found in dbDetails. If so,
		// select this as the default server for this database selector.
		//
		int numServers = setupTab.dbDetails.size();
		int defaultSelectedServerIndex = 0;
		
		for (int index=0; index<numServers; index++) {
		
			String serverName = setupTab.dbDetails.get(index).getHost();
			if (serverName.contains(defaultServerName)) {
				defaultSelectedServerIndex = index;
			}
		}
		
		if (dbServerSelector.getItemCount()>0) {
			dbServerSelector.setSelectedIndex(defaultSelectedServerIndex);
			callByReferenceWorkaround.setConfigureHostAttribute(setupTab.dbDetails.get(defaultSelectedServerIndex));
		}
		callByReferenceWorkaround.setDbServerSelectorAttribute(dbServerSelector);
	}
	
	protected interface CallByReferenceWorkaround {
		public abstract void setDbServerSelectorAttribute(JComboBox jcb);
		public abstract void setConfigureHostAttribute(ConfigureHost ch);
	}
	
	protected void buildPrimaryDbServerSelector() {

		buildDbServerSelector(
			new CallByReferenceWorkaround() {
				@Override public void setDbServerSelectorAttribute(JComboBox jcb) {
					setupTab.dbPrimaryServerSelector = jcb;					
				}
				@Override
				public void setConfigureHostAttribute(ConfigureHost ch) {
					setupTab.primaryHostDetails = ch;					
				}				
			},
			Constants.DB_SERVER_CHANGED,
			setupTab.actionListener,
			setupTab.defaultPrimaryServerName
		);
	}
	
	protected void buildSecondaryDbServerSelector() {

		buildDbServerSelector(
			new CallByReferenceWorkaround() {
				@Override public void setDbServerSelectorAttribute(JComboBox jcb) {
					setupTab.dbSecondaryServerSelector = jcb;					
				}
				@Override
				public void setConfigureHostAttribute(ConfigureHost ch) {
					setupTab.secondaryHostDetails = ch;					
				}				
			},
			Constants.SECONDARY_DB_SERVER_CHANGED,
			setupTab.actionListener,
			setupTab.defaultSecondaryServerName
		);
	}
	
	protected void buildSecondDbServerSelector() {

		buildDbServerSelector(
			new CallByReferenceWorkaround() {
				@Override public void setDbServerSelectorAttribute(JComboBox jcb) {
					setupTab.dbSecondPrimaryServerSelector = jcb;					
				}
				@Override
				public void setConfigureHostAttribute(ConfigureHost ch) {
					setupTab.secondPrimaryHostDetails = ch;					
				}				
			},
			Constants.PAN_DB_SERVER_CHANGED,
			setupTab.actionListener,
			setupTab.defaultPanServerName
		);
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
