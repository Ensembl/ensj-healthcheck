/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportLine;
import org.ensembl.healthcheck.Reporter;
import org.ensembl.healthcheck.eg_gui.Constants;
import org.ensembl.healthcheck.eg_gui.JPopupTextArea;
import org.ensembl.healthcheck.eg_gui.ReportPanel;
import org.ensembl.healthcheck.testcase.EnsTestCase;

public class GuiReporterTab extends JPanel implements Reporter {
	
	final protected TestClassList               testList;
	final protected JScrollPane                 testListScrollPane;
	final protected TestClassListModel          listModel;
	final protected ReportPanel                 reportPanel;
	final protected TestCaseColoredCellRenderer testCaseCellRenderer;
	
	protected boolean userClickedOnTestList = false;
	
	final protected Map<Class<? extends EnsTestCase>,GuiReportPanelData> reportData;
	
	/**
	 * <p>
	 * 	While the testrunner is running, the test selected will be 
	 * automatically updated to be the last one in the list. That way
	 * the user can monitor what is going on while the tests are running.
	 * </p>
	 * 
	 * <p>
	 * 	This behaviour is deactivated, if the user has selected a test from
	 * the list or when he has given the message field focus.
	 * </p>
	 * 
	 * @return whether or not it is ok to scroll to the last test of the list automatically.
	 */
	protected boolean autoScrollTestListOk() {
		return !userClickedOnTestList && !reportPanel.messageFieldHasFocus();
	}
	
	public void selectDefaultListItem() {
		
		if (autoScrollTestListOk()) {
			selectLastListItem();
		}
	}
	
	/**
	 * Selects the last item in the list and scrolls to that position.
	 */
	public void selectLastListItem() {
		
		if (listModel.getSize()>0) {
			
			int indexOfLastComponentInList =listModel.getSize()-1; 
			
			testList.setSelectedIndex(indexOfLastComponentInList);
			testList.ensureIndexIsVisible(indexOfLastComponentInList);
		}
	}
	
	public GuiReporterTab() {

		this.setBorder(GuiTestRunnerFrameComponentBuilder.defaultEmptyBorder);
		
		reportData  = new HashMap<Class<? extends EnsTestCase>,GuiReportPanelData>();
		
		testList    = new TestClassList(TestClassList.TestClassListToolTipType.CLASS);		
		listModel   = new TestClassListModel();
		reportPanel = new ReportPanel();
		
		testList.setModel(listModel);
		
		testCaseCellRenderer = new TestCaseColoredCellRenderer();
		
		testList.setCellRenderer(testCaseCellRenderer);
		
		testList.addMouseListener(new MouseListener() {

			// We want to know, when as soon as the user clicks somewhere on 
			// the list so we can stop selecting the last item in the list.
			//
			@Override public void mouseClicked(MouseEvent arg0) {
				userClickedOnTestList = true;
			}

			@Override public void mouseEntered (MouseEvent arg0) {}
			@Override public void mouseExited  (MouseEvent arg0) {}
			@Override public void mousePressed (MouseEvent arg0) {}
			@Override public void mouseReleased(MouseEvent arg0) {}
			
		});
		
		// Setting the preferred size causes the scrollbars to not be adapted 
		// to the list changing size when items are added later on, so 
		// commented out.
		//
		//	testList.setPreferredSize(
		//		new Dimension(
		//			Constants.INITIAL_APPLICATION_WINDOW_WIDTH  / 3, 
		//			Constants.INITIAL_APPLICATION_WINDOW_HEIGHT / 3 * 2
		//		)
		//	);
		
		setLayout(new BorderLayout());
		
		testListScrollPane = new JScrollPane(testList);
		
		add(
			new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT, 
				testListScrollPane, 
				//new JScrollPane(reportPanel)
				reportPanel
			), BorderLayout.CENTER
		);
		
		testList.addListSelectionListener(
			new ListSelectionListener() {

				@Override
				public void valueChanged(ListSelectionEvent arg0) {
					
					if (!arg0.getValueIsAdjusting()) {
						reportPanel.setData(
							reportData.get(
								( (TestClassListItem) listModel.getElementAt(testList.getSelectedIndex()) ).getTestClass()
							)
						);
					}
				}
			}
		);
	}
	
	@Override
	public void message(final ReportLine reportLine) {
		
		final Class<? extends EnsTestCase> currentKey = reportLine.getTestCase().getClass();
		
		if (!reportData.containsKey(currentKey)) {
			
			reportData.put(currentKey, new GuiReportPanelData(reportLine));
			testCaseCellRenderer.setOutcome(currentKey, null);

			// This method will be called from a different thread. Therefore
			// updates to the components must be run through SwingUtilities.
			//
			SwingUtilities.invokeLater(
					new Runnable() {
						@Override public void run() {

							listModel.addTest(currentKey);

							// If nothing has been selected, then select 
							// something so the user is not staring at an
							// empty report.
							//
							//if (testList.isSelectionEmpty()) {
							if (!userClickedOnTestList) {
								selectDefaultListItem();
							}
						}
					}
				);
		} else {
		
			reportData.get(currentKey).addReportLine(reportLine);
		}
		
		// If anything was reported as a problem, the outcome is false.
		//
		if (reportLine.getLevel()==ReportLine.PROBLEM) {
			testCaseCellRenderer.setOutcome(currentKey, false);
		}

		// If a testcase has been selected, then display the new data in the 
		// GUI so the user can see the new line in real time.
		//
		if (!testList.isSelectionEmpty()) {

			SwingUtilities.invokeLater(
					new Runnable() {
						@Override public void run() {
							
							reportPanel.setData(
									
									reportData.get(
											( (TestClassListItem) listModel.getElementAt(testList.getSelectedIndex()) ).getTestClass()
									)
								);
						}
					}
			);
		}
	}

	@Override
	public void startTestCase(EnsTestCase testCase, DatabaseRegistryEntry dbre) {}

	@Override
	public void finishTestCase(
			final EnsTestCase testCase, 
			final boolean result, 
			DatabaseRegistryEntry dbre
	) {
		
		// This method will be called from a different thread. Therefore
		// updates to the components must be run through SwingUtilities.
		//
		SwingUtilities.invokeLater(
				new Runnable() {
					@Override public void run() {
						testCaseCellRenderer.setOutcome(testCase.getClass(), result);
					}
				}
			);
	}
}

class ReportPanel extends JPanel implements ActionListener {
	
	final protected JTextField testName;
	final protected JPopupTextArea description;
	final protected JTextField teamResponsible;
	final protected JTextField speciesName;
	final protected JPopupTextArea  message;
	
	final String copy_selected_text_action = "copy_selected_text_action";

	protected boolean updateInProgress;
	
	protected Component createVerticalSpacing() {
		return Box.createVerticalStrut(Constants.DEFAULT_VERTICAL_COMPONENT_SPACING);
	}
	
	private boolean messageFieldHasFocus = false;
	
	public boolean messageFieldHasFocus() {
		return messageFieldHasFocus;
	}

	public ReportPanel() {
		
		this.setBorder(GuiTestRunnerFrameComponentBuilder.defaultEmptyBorder);
		
		Box singleLineInfo = Box.createVerticalBox();
		
		testName        = new JPopupTextField("Name");
		description     = new JPopupTextArea(3, 0);
		teamResponsible = new JPopupTextField("Team Responsible");
		speciesName     = new JPopupTextField("Species Name");
		message         = new JPopupTextArea ();
		
		message.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				messageFieldHasFocus = true;				
			}

			@Override
			public void focusLost(FocusEvent e) {
				messageFieldHasFocus = false;				
			}			
		});
		
		description.setLineWrap(true);
		description.setWrapStyleWord(true);
		
		GuiTestRunnerFrameComponentBuilder g = null;
		
		singleLineInfo.add(g.createLeftJustifiedText("Test class:"));
		singleLineInfo.add(testName);
		singleLineInfo.add(createVerticalSpacing());
		
		singleLineInfo.add(g.createLeftJustifiedText("Description:"));
		singleLineInfo.add(description);
		singleLineInfo.add(createVerticalSpacing());
		
		singleLineInfo.add(g.createLeftJustifiedText("Team responsible:"));
		singleLineInfo.add(teamResponsible);
		singleLineInfo.add(createVerticalSpacing());
		
		singleLineInfo.add(g.createLeftJustifiedText("Species name:"));
		singleLineInfo.add(speciesName);
		singleLineInfo.add(createVerticalSpacing());
		
		setLayout(new BorderLayout());
		
		final JPopupMenu popup = new JPopupMenu();		
		
		message.add(GuiTestRunnerFrameComponentBuilder.makeMenuItem("Copy selected text", this, copy_selected_text_action));
		message.setComponentPopupMenu(popup);
		
		Font currentFont = message.getFont();
		Font newFont = new Font(
			"Courier",
			currentFont.getStyle(),
			currentFont.getSize()
		);
		
		message.setFont(newFont);
		//message.setLineWrap(true);
		//message.setWrapStyleWord(true);
		
		singleLineInfo.add(g.createLeftJustifiedText("Output from test:"));
		
		add(singleLineInfo,           BorderLayout.NORTH);
		add(new JScrollPane(message), BorderLayout.CENTER);
		
		// This has to be set to something small otherwise we will get problems when
		// wrapping this in a JSplitPane:
		//
		// http://docs.oracle.com/javase/6/docs/api/javax/swing/JSplitPane.html
		//
		// "When the user is resizing the Components the minimum size of the 
		// Components is used to determine the maximum/minimum position the 
		// Components can be set to. If the minimum size of the two 
		// components is greater than the size of the split pane the divider 
		// will not allow you to resize it."
		//
		this.setMinimumSize(new Dimension(200,300));
		
		updateInProgress = false;
		
	}
	
	public synchronized void setData(final GuiReportPanelData reportData) {
		
		/*
		 * Updating the reporter tab may be slow especially when setting the
		 * message to a long string.
		 * 
		 * Therefore the update is put into a new Thread to keep the gui 
		 * responsive.
		 * 
		 * Additional updates are prevented in the meantime. In the worst case
		 * this could lead to an StackOverflorError, if the method gets 
		 * repeatedly called before it can complete.
		 * 
		 */
		if (updateInProgress) {
			return;
		}
		
		Thread t = new Thread() {
			
			public void run() {
				
				testName        .setText (reportData.getTestName());
				description     .setText (reportData.getDescription());
				speciesName     .setText (reportData.getSpeciesName());
				teamResponsible .setText (reportData.getTeamResponsible());
				
				String msg = reportData.getMessage();
				
				message.setText (msg);
				
				if (!messageFieldHasFocus) {
					message.setCaretPosition(msg.length());
				}
				
				updateInProgress = false;				
			}
		};
		t.setName("Updating data in reporter tab.");
		updateInProgress = true;
		t.start();
		
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {

		if (arg0.paramString().equals(copy_selected_text_action)) {
			
			String selection = message.getSelectedText();
			StringSelection data = new StringSelection(selection);
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(data, data);
		}
	}
}

