package org.ensembl.healthcheck.eg_gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.configuration.ConfigureHost;
import org.ensembl.healthcheck.configurationmanager.ConfigurationFactory;
import org.ensembl.healthcheck.configurationmanager.ConfigurationFactory.ConfigurationType;
import org.ensembl.healthcheck.eg_gui.AdminTab;
import org.ensembl.healthcheck.eg_gui.Constants;
import org.ensembl.healthcheck.eg_gui.DatabaseTabbedPane;
import org.ensembl.healthcheck.eg_gui.GuiReporterTab;
import org.ensembl.healthcheck.eg_gui.TestInstantiatorDynamic;
import org.ensembl.healthcheck.eg_gui.TestProgressDialog;
import org.ensembl.healthcheck.testcase.EnsTestCase;
import org.ensembl.healthcheck.testcase.PerlScriptConfig;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * <p>
 * 	The main window of the healthcheck GUI.
 * </p>
 * 
 * @author michael
 *
 */
public class GuiTestRunnerFrame extends JFrame implements ActionListener {

	/** The logger to use for this class */
	protected static Logger logger = Logger.getLogger("HealthCheckLogger");
	
	/**
	 * Preferred size of the window.
	 *  
	 */
	protected int windowWidth  = Constants.INITIAL_APPLICATION_WINDOW_WIDTH;
	protected int windowHeight = Constants.INITIAL_APPLICATION_WINDOW_HEIGHT;
	
	/**
	 * Title of the Window
	 * 
	 */
	String windowTitle = "Healthchecks";

	/**
	 * Directories in which configuration files for database servers will be 
	 * searched for.
	 * 
	 */
	final String[] dirsWithDbServerConfigs = new String[] {

		// ~ does not work in java to reference the home directory:
		//
		// "~/.ensj",
		//
		System.getProperty("user.home") + "/.ensj"
	};

	protected DatabaseTabbedPane databaseTabbedPane;
	protected final JList        listOfTestsToBeRun;
	protected final JButton      rmSelectedTests;
	protected final JButton      runAllTests;
	protected final JButton      runSelectedTests;
	protected final JTree        tree;
	protected final JComboBox    dbServerSelector;
	
	protected final List<ConfigureHost> dbDetails;

	protected TestProgressDialog testProgressDialog;
	
	// The tabs on the main window.
	//
	final protected JTabbedPane tab;
	final protected JPanel      tabSetup;
	      protected int         tabSetupTabIndex;
	      protected String      tabSetupName = "Setup";
	      protected JPanel      tabResults;
	      protected int         tabResultsTabIndex;
	      protected String      tabResultsName = "Results";
	      protected JPanel      tabResultsLegacy;
	      protected int         tabResultsLegacyTabIndex;
	      protected AdminTab    tabAdmin;
	      protected int         tabAdminTabIndex;

	      // Holds a reference to the gui reporter. It is a component of the
	      // JPanel tabResults.
	      //
	      protected GuiReporterTab currentGuiReporter;
	      
	      protected Thread currentGuiTestRunnerThread;
	      
	      protected GuiLogHandler guiLogHandler;
	


	protected void processWindowEvent(WindowEvent e) {
		
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {

			// If a healthcheck session is currently running, terminate this.
			// Perl based healthchecks don't automatically terminate when the
			// window closes, so this is done here explicitly.
			//
			if (currentGuiTestRunnerThread != null) {		
	    		currentGuiTestRunnerThread.interrupt();
	       	}
			
		}
		super.processWindowEvent(e);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

		String cmd = arg0.getActionCommand();

		// Not implemented yet
		if (cmd.equals(Constants.Add_to_tests_to_be_run)) {
			
		}
		
		if (cmd.equals(Constants.REMOVE_SELECTED_TESTS)) {

			GuiTestRunnerFrameActionPerformer.removeSelectedTests(listOfTestsToBeRun);
		}
		if (cmd.equals(Constants.DB_SERVER_CHANGED)) {

			GuiTestRunnerFrameActionPerformer.setupDatabasePane(
				databaseTabbedPane, 
				dbDetails.get(dbServerSelector.getSelectedIndex())
			);
		}
		if (cmd.equals(Constants.RUN_ALL_TESTS) || cmd.equals(Constants.RUN_SELECTED_TESTS)) {

			DatabaseRegistryEntry[] selectedDatabases 
				= databaseTabbedPane.getSelectedDatabases();

			if (selectedDatabases.length == 0) {
				JOptionPane.showMessageDialog(
					this, 
					"No databases selected!",
					"Error", 
					JOptionPane.ERROR_MESSAGE
				);
			} else {

				ReportManager.initialise();
				
				// If currentGuiReporter has been initialised once, it has 
				// also been added to the tabResults. It has to be removed
				// first, before a new one is created and put in its place.
				//
				if (currentGuiReporter!=null) {
					tabResults.remove(currentGuiReporter);
				}
				currentGuiReporter = new GuiReporterTab();
				
				guiLogHandler = new GuiLogHandler();				
				guiLogHandler.setReporter(currentGuiReporter);
				
				// Set the formatter for EnsTestcases to what the user 
				// configured them to look like. 
				//

				// Check, if a formatter was configured. If so, then use
				// this formatter, otherwise create a new one.
				//
				Handler[] configuredHandler = Logger.getLogger(
						EnsTestCase.class.getCanonicalName()
				).getHandlers();
				
				Formatter configuredFormatter;
				
				if (configuredHandler.length == 0) {
					configuredFormatter = new SimpleFormatter();
				} else {
					configuredFormatter = configuredHandler[0].getFormatter();
				}
				
				// Set the formatter.
				guiLogHandler.setFormatter(configuredFormatter);

				testProgressDialog = new TestProgressDialog("", 0, 100);
				
				tabResults.setLayout(new BorderLayout());
				tabResults.add(testProgressDialog, BorderLayout.SOUTH);
				tabResults.add(currentGuiReporter, BorderLayout.CENTER);
				
				tab.setEnabledAt(tabResultsTabIndex,       true);
				tab.setEnabledAt(tabResultsLegacyTabIndex, true);
				tab.setSelectedIndex(tabResultsTabIndex);
				
				tabResultsLegacy.setLayout(new BorderLayout());
				
				ReportManager.setReporter(currentGuiReporter);
				
				PerlScriptConfig psc = new PerlScriptConfig(
						tabAdmin.getPerl5Binary(),
						tabAdmin.getPerlOptions()
				);
				
				if (cmd.equals(Constants.RUN_SELECTED_TESTS)) {
					
					currentGuiTestRunnerThread = GuiTestRunnerFrameActionPerformer.runSelectedTests(
						listOfTestsToBeRun, 
						selectedDatabases,
						testProgressDialog,
						tabResultsLegacy,
						tabAdmin.getPerl5Lib(),
						psc,
						guiLogHandler
					);
				}
				if (cmd.equals(Constants.RUN_ALL_TESTS)) {
					
					currentGuiTestRunnerThread = GuiTestRunnerFrameActionPerformer.runAllTests(
						listOfTestsToBeRun, 
						selectedDatabases,
						testProgressDialog,
						tabResultsLegacy,
						tabAdmin.getPerl5Lib(),
						psc,
						guiLogHandler
					);
				}
			}
		}
	}
	
	/**
	 * <p>
	 * 	Creates a configuration object of type ConfigureHost from the File
	 * passed as a parameter and returns it.
	 * </p>
	 * 
	 * @param iniFile
	 * @return
	 */
	public static ConfigureHost getHostConfiguration(File iniFile) {

		List<File> propertyFileNames = new ArrayList<File>();
		
		propertyFileNames.add(iniFile);
		
		ConfigurationFactory<ConfigureHost> confFact = new ConfigurationFactory(
			ConfigureHost.class, 
			propertyFileNames
		);		
		ConfigureHost configuration = confFact.getConfiguration(ConfigurationType.Properties);

		return configuration;
	}

	/**
	 * <p>
	 * 	Iterates over all files in the directories dirWithDbServerConfigs. 
	 * Assumes they are inifiles with data to populate a ConfigureHost object.
	 * Creates a ConfigureHost object for every inifile and returns a 
	 * List<ConfigureHost>.
	 * </p>
	 * 
	 * @param dirWithDbServerConfigs
	 * @return
	 * @throws IOException 
	 */
	protected static List<ConfigureHost> createDbDetailsConfigurations(String... dirsWithDbServerConfigs) {
		
		List<ConfigureHost> dbDetails = new ArrayList<ConfigureHost>();
		
		for (String dirWithDbServerConfigs : dirsWithDbServerConfigs) {
		
			File currentDir = new File(dirWithDbServerConfigs);
			
			if (currentDir.exists() && currentDir.canRead() && currentDir.isDirectory()) {

				for (
					File f : currentDir.listFiles(

						// Only use ini files. 
						//
						new FilenameFilter() {
							public boolean accept(File arg0, String arg1) {								
								return arg1.endsWith(".ini");
							}
						}
					)
				) {
					ConfigureHost configuration = getHostConfiguration(f);			
					dbDetails.add(configuration);
				}
			} else {
				logger.info("Skipping " + currentDir);
			}
		}
		if (dbDetails.isEmpty()) {
			
			throw new RuntimeException(
				"Found no ini files with database server details. Please add at least one into the directories "
				+ Arrays.toString(dirsWithDbServerConfigs)
			);
		}
		return dbDetails;
	}
	
	public GuiTestRunnerFrame(
		List<GroupOfTests> testGroupList,
		TestInstantiatorDynamic testInstantiator
	) {

		final ActionListener defaultAL = this;
		
		final JPopupMenu popupMenuList = GuiTestRunnerFrameComponentBuilder
				.createListOfTestsToBeExecutedPopupMenu(defaultAL);

		// Functionality of the popup menu is not implemented yet,
		// so no popup menu for the tree
		//
		//final JPopupMenu popupMenuTree = GuiComponentBuilder
		//		.createTreeOfTestGroupsPopupMenu(defaultAL);
		final JPopupMenu popupMenuTree = null;

		listOfTestsToBeRun = GuiTestRunnerFrameComponentBuilder.createListOfTestsToBeRunArea(
				testInstantiator, 
				popupMenuList,
				defaultAL
		);
		tree = GuiTestRunnerFrameComponentBuilder.createTreeOfTestGroups(
			testGroupList,
			popupMenuTree
		);

		rmSelectedTests  = GuiTestRunnerFrameComponentBuilder.createRemoveSelectedTestsButton(defaultAL);
		runAllTests      = GuiTestRunnerFrameComponentBuilder.createRunAllTestsButton(defaultAL);
		runSelectedTests = GuiTestRunnerFrameComponentBuilder.createRunSelectedTestsButton(defaultAL);

		tabSetup = new JPanel();
		
		dbDetails = createDbDetailsConfigurations(dirsWithDbServerConfigs);

		dbServerSelector = GuiTestRunnerFrameComponentBuilder.createDbServerSelector(dbDetails);		
		dbServerSelector.setActionCommand(Constants.DB_SERVER_CHANGED);
		dbServerSelector.addActionListener(this);
		
		DBUtils.setHostConfiguration(dbDetails.get(0));

		List<String> regexps = new ArrayList<String>();
		regexps.add(".*");

		DatabaseRegistry databaseRegistry = new DatabaseRegistry(regexps, null,
				null, false);
		if (databaseRegistry.getEntryCount() == 0) {
			logger.warning("Warning: no databases found!");
		}

		databaseTabbedPane = new DatabaseTabbedPane(databaseRegistry);
		
		this.setTitle(windowTitle);
		
		tab = new JTabbedPane();

		init();
	}

	/**
	 * <p>
	 * Plugs the individual components of the GUI to the JFrame. This is where
	 * the components are arranged.
	 * </p>
	 * 
	 * @param databaseTabbedPane
	 * @param tree
	 * @param testsToBeRun
	 * @param buttonPanel
	 * 
	 */
	protected void addComponentsToLayout(
			DatabaseTabbedPane databaseTabbedPane, 
			JTree tree, 
			JList testsToBeRun,
			JPanel buttonPanel, 
			JComboBox dbServerSelector
	) {
		
		JScrollPane treePane  = new JScrollPane(tree); 
		JPanel      testsPane = new JPanel(new BorderLayout());
		
		Box buttonBox = Box.createHorizontalBox();
		
		buttonBox.add(rmSelectedTests);	
		buttonBox.add(Box.createHorizontalStrut(Constants.DEFAULT_HORIZONTAL_COMPONENT_SPACING));
		buttonBox.add(runSelectedTests);
		buttonBox.add(Box.createHorizontalStrut(Constants.DEFAULT_HORIZONTAL_COMPONENT_SPACING));
		buttonBox.add(runAllTests);
		
		testsPane.add(buttonBox,                      BorderLayout.SOUTH);
		testsPane.add(new JScrollPane(testsToBeRun),  BorderLayout.CENTER);
		
		JSplitPane testSelectionWidgets = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT,
				treePane, 
				testsPane
		);
		
		tabSetup.setLayout(new BorderLayout());
		
		tabSetup.add(
				new JSplitPane(
						JSplitPane.VERTICAL_SPLIT, 
						databaseTabbedPane,
						testSelectionWidgets
				),
				BorderLayout.CENTER
		);

		tabSetup.add(dbServerSelector, BorderLayout.NORTH);
		
		tabAdmin = new AdminTab();
		
		tabResults       = new JPanel();
		tabResultsLegacy = new JPanel();
		
		tab.add(tabSetupName,   tabSetup);
		tabSetupTabIndex = 0;
		
		tab.add(tabResultsName, tabResults);
		tabResultsTabIndex = 1;
		
		tab.add("Legacy", tabResultsLegacy);
		tabResultsLegacyTabIndex = 2;
		
		tab.add("Admin", tabAdmin);
		tabAdminTabIndex = 3;
		
		tab.setEnabledAt(tabResultsTabIndex,       false);
		tab.setEnabledAt(tabResultsLegacyTabIndex, false);
		
		this.getContentPane().add(tab);
		
		// Create space around components so they don't look too crammed. 
		//
		Border defaultEmptyBorder = GuiTestRunnerFrameComponentBuilder.defaultEmptyBorder;
		
		dbServerSelector     .setBorder(BorderFactory.createTitledBorder(defaultEmptyBorder, "1. Select a database server:"));
		databaseTabbedPane   .setBorder(BorderFactory.createTitledBorder(defaultEmptyBorder, "2. Select a database:"));
		testSelectionWidgets .setBorder(BorderFactory.createTitledBorder(defaultEmptyBorder, "3. Select tests to be run:"));
		treePane             .setBorder(BorderFactory.createTitledBorder("Drag tests to the panel on the right"));
		testsPane            .setBorder(BorderFactory.createTitledBorder("Select tests and run from context menu"));

		// The default dimensions look appalling. This looks slightly less
		// appalling.
		//
		databaseTabbedPane.setMinimumSize(new Dimension(0,       windowHeight/4));
		testsPane         .setMinimumSize(new Dimension(0,       windowHeight/4));
		treePane          .setMinimumSize(new Dimension(windowWidth/3, 0));
	}

	/**
	 * 
	 * <p>
	 * Creates the GUI components and arranges them on the JFrame (this).
	 * </p>
	 * 
	 * @param testGroupList
	 * @param testInstantiator
	 * 
	 */
	protected void init() {

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//databaseTabbedPane.setPreferredSize(new Dimension(500, 200));

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(2, 1));
		buttonPanel.add(rmSelectedTests);
		buttonPanel.add(runAllTests);

		addComponentsToLayout(
				databaseTabbedPane, 
				tree, 
				listOfTestsToBeRun,
				buttonPanel,
				dbServerSelector
		);

		this.setPreferredSize(new Dimension(windowWidth, windowHeight));
		this.pack();

		// The following stuff that positions the frame must come after the
		// this.pack() statement above otherwise it won't work as expected.
		
		// Center on screen
		//
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle frame = getBounds();
		setLocation(
				(screen.width  - frame.width)  / 2,
				(screen.height - frame.height) / 2
		);
	}

}
