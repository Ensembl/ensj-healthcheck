package org.ensembl.healthcheck.eg_gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class GuiTestRunnerFrameBuilder {
	
	protected final String jarFile;
	protected GuiTestRunnerFrameActionListener actionListener;
	
	protected GuiTestRunnerFrame guiTestRunnerFrame;

	public GuiTestRunnerFrame getResult() {
		return guiTestRunnerFrame;
	}

	public GuiTestRunnerFrameBuilder(String jarFile) {
		
		this.jarFile = jarFile;
	}

	
	protected void buildEmptyGuiTestRunnerFrame() {
		
		guiTestRunnerFrame = new GuiTestRunnerFrame();
	}

	protected void buildActionListener() {
		
		if (guiTestRunnerFrame==null) {
			throw new NullPointerException("guiTestRunnerFrame has not been built.");
		}
		
		actionListener = new GuiTestRunnerFrameActionListener(guiTestRunnerFrame);
		
		// Prevent actions from being triggered while things are being set up.
		// Will cause lots of NullPointerExceptions otherwise, because some 
		// components might not have been created yet.
		//
		// Will be set to true at the end in buildFinalise()
		//
		actionListener.setActive(false);
	}
	
	protected void buildSetupTab() {
		
		SetupTabBuilder setupTabBuilder = new SetupTabBuilder(
			actionListener, 
			jarFile
		);
		guiTestRunnerFrame.setupTab = new SetupTabBuildDirector().construct(setupTabBuilder);
	}

	protected void buildOtherTabs() {
		
		guiTestRunnerFrame.adminTab = new AdminTab();
		
		guiTestRunnerFrame.resultTab       = new JPanel();
		guiTestRunnerFrame.legacyResultTab = new JPanel();
		
		guiTestRunnerFrame.tabbedPane = new JTabbedPane();
		
		guiTestRunnerFrame.tabbedPane.add(guiTestRunnerFrame.setupTabName,   guiTestRunnerFrame.setupTab);
		guiTestRunnerFrame.setupTabIndex = 0;
		
		guiTestRunnerFrame.tabbedPane.add(guiTestRunnerFrame.resultTabName, guiTestRunnerFrame.resultTab);
		guiTestRunnerFrame.resultTabIndex = 1;
		
		guiTestRunnerFrame.tabbedPane.add("Legacy", guiTestRunnerFrame.legacyResultTab);
		guiTestRunnerFrame.legacyResultTabIndex = 2;
		
		guiTestRunnerFrame.tabbedPane.add("Admin", guiTestRunnerFrame.adminTab);
		guiTestRunnerFrame.adminTabIndex = 3;
		
		guiTestRunnerFrame.tabbedPane.setEnabledAt(guiTestRunnerFrame.resultTabIndex,       false);
		guiTestRunnerFrame.tabbedPane.setEnabledAt(guiTestRunnerFrame.legacyResultTabIndex, false);
		
		guiTestRunnerFrame.getContentPane().add(guiTestRunnerFrame.tabbedPane);
	}
	
	protected void buildFinalise() {
		
		actionListener.setActive(true);
		
		if (guiTestRunnerFrame.setupTab.dbServerSelector.getItemCount()>0) {

			guiTestRunnerFrame.setupTab.actionListener.actionPerformed(
				new ActionEvent(
					guiTestRunnerFrame.setupTab.actionListener, 
					0, 
					Constants.DB_SERVER_CHANGED
				)
			);
		}
	}
}
