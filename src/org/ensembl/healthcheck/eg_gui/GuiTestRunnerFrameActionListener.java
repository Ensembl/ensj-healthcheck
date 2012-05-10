package org.ensembl.healthcheck.eg_gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JOptionPane;

import org.ensembl.healthcheck.ConfigurableTestRunner;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.SystemPropertySetter;
import org.ensembl.healthcheck.configuration.ConfigurationUserParameters;
import org.ensembl.healthcheck.configuration.ConfigureHost;
import org.ensembl.healthcheck.configurationmanager.ConfigurationByProperties;
import org.ensembl.healthcheck.configurationmanager.ConfigurationFactory;
import org.ensembl.healthcheck.configurationmanager.ConfigurationFactory.ConfigurationType;
import org.ensembl.healthcheck.testcase.EnsTestCase;
import org.ensembl.healthcheck.testcase.PerlScriptConfig;
import org.ensembl.healthcheck.util.DBUtils;

public class GuiTestRunnerFrameActionListener implements ActionListener {
	
	protected final GuiTestRunnerFrame guiTestRunnerFrame;
	protected boolean active;
    // Holds a reference to the gui reporter. It is a component of the
    // JPanel tabResults.
    //
    protected GuiReporterTab currentGuiReporter;
    
    protected GuiLogHandler guiLogHandler;
    
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public GuiTestRunnerFrameActionListener(GuiTestRunnerFrame guiTestRunnerFrame) {
		
		this.guiTestRunnerFrame = guiTestRunnerFrame;
		this.active = true;
	}
	
	/**
	 * <p>
	 * 	Sets the primary and secondary host details in DBUtils.
	 * </p>
	 * 
	 * @param primaryHostDetails
	 * @param secondaryHostDetails
	 */
	protected void setPrimaryAndSecondaryAndSystemPropertiesHost(
			ConfigureHost primaryHostDetails,
			ConfigureHost secondaryHostDetails
		) {
		
		ConfigurationUserParameters combinedHostConfig = createConfigurationObject(primaryHostDetails, secondaryHostDetails);
		
		// And finally set the new configuration file in which the 
		// secondary host has been configured.
		//
		DBUtils.setHostConfiguration((ConfigureHost) combinedHostConfig);
		
		SystemPropertySetter systemPropertySetter = new SystemPropertySetter(combinedHostConfig);
		systemPropertySetter.setPropertiesForHealthchecks();
	}
	
	protected ConfigurationUserParameters createConfigurationObject(
			ConfigureHost primaryHostDetails,
			ConfigureHost secondaryHostDetails
		) {
		
		Properties secondaryHostProperties = new Properties();
		
		if (secondaryHostDetails!=null) {
			secondaryHostProperties.setProperty("secondary.host",     secondaryHostDetails.getHost());
			secondaryHostProperties.setProperty("secondary.port",     secondaryHostDetails.getPort());
			secondaryHostProperties.setProperty("secondary.user",     secondaryHostDetails.getUser());
			secondaryHostProperties.setProperty("secondary.password", secondaryHostDetails.getPassword());
			secondaryHostProperties.setProperty("secondary.driver",   secondaryHostDetails.getDriver());
		}
		
		ConfigureHost secondaryHostConfiguration = 
			(ConfigureHost) ConfigurationByProperties.newInstance(
				ConfigureHost.class, 
				secondaryHostProperties
			);
		
		List<File> propertyFileNames = new ArrayList<File>();
		propertyFileNames.add(new File(ConfigurableTestRunner.getDefaultPropertiesFile()));

		ConfigurationUserParameters ConfigurationByPropertyFiles =
			new ConfigurationFactory<ConfigurationUserParameters>(
				ConfigurationUserParameters.class,
				propertyFileNames
			).getConfiguration(ConfigurationType.Properties);

		
		ConfigurationFactory<ConfigurationUserParameters> confFact =
			new ConfigurationFactory<ConfigurationUserParameters>(
				ConfigurationUserParameters.class,
				primaryHostDetails,
				secondaryHostConfiguration,
				ConfigurationByPropertyFiles
			);
		
		ConfigurationUserParameters combinedHostConfig = confFact.getConfiguration(ConfigurationType.Cascading);
		
		return combinedHostConfig;
	}

	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		if (!active) {
			return;
		}
		String cmd = arg0.getActionCommand();

		if (cmd.equals(Constants.selectedDatabaseChanged)) {
			
			DatabaseRadioButton selectedDbRadioButton = (DatabaseRadioButton) arg0.getSource();
			String currentlySelectsDBName = selectedDbRadioButton.getText();
			guiTestRunnerFrame.setupTab.updateDbCmdLine(currentlySelectsDBName);

		}
		
		// Not implemented yet
		if (cmd.equals(Constants.Add_to_tests_to_be_run)) {
			
		}
		
		if (cmd.equals(Constants.REMOVE_SELECTED_TESTS)) {

			GuiTestRunnerFrameActionPerformer.removeSelectedTests(guiTestRunnerFrame.setupTab.listOfTestsToBeRun);
		}
		if (cmd.equals(Constants.DB_SERVER_CHANGED)) {

			GuiTestRunnerFrameActionPerformer.setupDatabasePane(
				guiTestRunnerFrame.setupTab.databaseTabbedPaneWithSearchBox.getDtp(), 
				guiTestRunnerFrame.setupTab.dbDetails.get(guiTestRunnerFrame.setupTab.dbServerSelector.getSelectedIndex())
			);
			guiTestRunnerFrame.setupTab.updateDbCmdLine();
		}
		if (cmd.equals(Constants.RUN_ALL_TESTS) || cmd.equals(Constants.RUN_SELECTED_TESTS)) {

			//
			// Check, if basic conditions have been met so that tests can be 
			// run.
			//
			if (
				(guiTestRunnerFrame.currentGuiTestRunnerThread != null) 
				&& (guiTestRunnerFrame.currentGuiTestRunnerThread.isAlive())
			) {
				
				JOptionPane.showMessageDialog(
						guiTestRunnerFrame, 
						"A session of healthchecks is currently running already. "
						+ "Please wait for it to terminate before starting another.", 
						"Error",
						JOptionPane.ERROR_MESSAGE
					);
					return;
			}
			
			if (
				cmd.equals(Constants.RUN_SELECTED_TESTS) 
				&& guiTestRunnerFrame.setupTab.listOfTestsToBeRun.getSelectedValues().length==0
			) {
				JOptionPane.showMessageDialog(
					guiTestRunnerFrame, 
					"You have not selected any tests!", 
					"Error",
					JOptionPane.ERROR_MESSAGE
				);
				return;
			}
			if (
				cmd.equals(Constants.RUN_ALL_TESTS) 
				&& guiTestRunnerFrame.setupTab.listOfTestsToBeRun.getModel().getSize()==0
			) {
				JOptionPane.showMessageDialog(
					guiTestRunnerFrame, 
					"No tests! Please drag tests from the tree in the left into the area above.", 
					"Error",
					JOptionPane.ERROR_MESSAGE
				);
				return;
			}
			
			DatabaseRegistryEntry[] selectedDatabases 
				= guiTestRunnerFrame.setupTab.databaseTabbedPaneWithSearchBox.getDtp().getSelectedDatabases();

			if (selectedDatabases.length == 0) {
				JOptionPane.showMessageDialog(
						guiTestRunnerFrame, 
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
					guiTestRunnerFrame.resultTab.remove(currentGuiReporter);
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

				guiTestRunnerFrame.testProgressDialog = new TestProgressDialog("", 0, 100);
				
				guiTestRunnerFrame.resultTab.setLayout(new BorderLayout());
				guiTestRunnerFrame.resultTab.add(guiTestRunnerFrame.testProgressDialog, BorderLayout.SOUTH);
				guiTestRunnerFrame.resultTab.add(currentGuiReporter, BorderLayout.CENTER);
				
				guiTestRunnerFrame.tabbedPane.setEnabledAt(guiTestRunnerFrame.resultTabIndex,       true);
				guiTestRunnerFrame.tabbedPane.setEnabledAt(guiTestRunnerFrame.legacyResultTabIndex, true);
				guiTestRunnerFrame.tabbedPane.setSelectedIndex(guiTestRunnerFrame.resultTabIndex);
				
				guiTestRunnerFrame.legacyResultTab.setLayout(new BorderLayout());
				
				ReportManager.setReporter(currentGuiReporter);
				
				PerlScriptConfig psc = new PerlScriptConfig(
						guiTestRunnerFrame.adminTab.getPerl5Binary(),
						guiTestRunnerFrame.adminTab.getPerlOptions()
				);
				
				setPrimaryAndSecondaryAndSystemPropertiesHost(
					guiTestRunnerFrame.setupTab.dbDetails.get(guiTestRunnerFrame.setupTab.dbServerSelector.getSelectedIndex()),
					guiTestRunnerFrame.setupTab.dbDetails.get(guiTestRunnerFrame.setupTab.secondaryDbServerSelector.getSelectedIndex())
				);
				
				if (cmd.equals(Constants.RUN_SELECTED_TESTS)) {
					
					guiTestRunnerFrame.currentGuiTestRunnerThread = GuiTestRunnerFrameActionPerformer.runSelectedTests(
							guiTestRunnerFrame.setupTab.listOfTestsToBeRun, 
						selectedDatabases,
						guiTestRunnerFrame.testProgressDialog,
						guiTestRunnerFrame.legacyResultTab,
						guiTestRunnerFrame.adminTab.getPerl5Lib(),
						psc,
						guiLogHandler
					);
				}
				if (cmd.equals(Constants.RUN_ALL_TESTS)) {
					guiTestRunnerFrame.currentGuiTestRunnerThread = GuiTestRunnerFrameActionPerformer.runAllTests(
							guiTestRunnerFrame.setupTab.listOfTestsToBeRun, 
						selectedDatabases,
						guiTestRunnerFrame.testProgressDialog,
						guiTestRunnerFrame.legacyResultTab,
						guiTestRunnerFrame.adminTab.getPerl5Lib(),
						psc,
						guiLogHandler
					);
				}
			}
		}
	}
}
