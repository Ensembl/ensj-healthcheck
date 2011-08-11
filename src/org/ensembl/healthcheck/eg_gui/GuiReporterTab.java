package org.ensembl.healthcheck.eg_gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
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
	
	final protected Map<Class<? extends EnsTestCase>,List<ReportLine>> report;

	final protected TestClassList               testList;
	final protected JScrollPane                 testListScrollPane;
	final protected TestClassListModel          listModel;
	final protected ReportPanel                 reportPanel;
	final protected TestCaseColoredCellRenderer testCaseCellRenderer;
	
	public void selectDefaultListItem() {
		
		if (testList.isSelectionEmpty()) {
			selectLastListItem();
		}
	}
	
	public void selectLastListItem() {
		
		if (listModel.getSize()>0) {
			testList.setSelectedIndex(listModel.getSize()-1);
		}
	}
	
	public GuiReporterTab() {

		report      = new HashMap<Class<? extends EnsTestCase>,List<ReportLine>>();
		testList    = new TestClassList(TestClassList.TestClassListToolTipType.CLASS);		
		listModel   = new TestClassListModel();
		reportPanel = new ReportPanel();
		
		testList.setModel(listModel);
		
		testCaseCellRenderer = new TestCaseColoredCellRenderer();
		
		testList.setCellRenderer(testCaseCellRenderer);
		
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
				reportPanel
			), BorderLayout.CENTER
		);
		
		testList.addListSelectionListener(
			new ListSelectionListener() {

				@Override
				public void valueChanged(ListSelectionEvent arg0) {
					
					if (!arg0.getValueIsAdjusting()) {
						reportPanel.setData(
							report.get(
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
		
		if (!report.containsKey(currentKey)) {
			
			report.put(currentKey, new ArrayList<ReportLine>());
			
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
							if (testList.isSelectionEmpty()) {
								selectDefaultListItem();
							}
						}
					}
				);
		}
		
		// Save the new reportline in the report hashmap
		//
		report.get(currentKey).add(reportLine);
		
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
	
									report.get(
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
	
	protected Component createLabel(String text) {
		
		Box bb = Box.createHorizontalBox();
		
		
		JLabel t = new JLabel(text, JLabel.LEFT);
		t.setAlignmentX(LEFT_ALIGNMENT);

		bb.add(t);
		bb.add(Box.createHorizontalGlue());

		return bb;	
	}
	
	protected Component createVerticalSpacing() {
		return Box.createVerticalStrut(Constants.DEFAULT_VERTICAL_COMPONENT_SPACING);
	}
	
	public ReportPanel() {
		
		Box singleLineInfo = Box.createVerticalBox();
		
		testName        = new JPopupTextField("Name");
		description     = new JPopupTextArea(3, 0);
		teamResponsible = new JPopupTextField("Team Responsible");
		speciesName     = new JPopupTextField("Species Name");
		message         = new JPopupTextArea ();
		
		description.setLineWrap(true);
		
		singleLineInfo.add(createLabel("Test class:"));
		singleLineInfo.add(testName);
		singleLineInfo.add(createVerticalSpacing());
		
		singleLineInfo.add(createLabel("Description:"));
		singleLineInfo.add(description);
		singleLineInfo.add(createVerticalSpacing());
		
		singleLineInfo.add(createLabel("Team responsible:"));
		singleLineInfo.add(teamResponsible);
		singleLineInfo.add(createVerticalSpacing());
		
		singleLineInfo.add(createLabel("Species name:"));
		singleLineInfo.add(speciesName);
		singleLineInfo.add(createVerticalSpacing());
		
		setLayout(new BorderLayout());
		
		final JPopupMenu popup = new JPopupMenu();		
		
		message.add(GuiTestRunnerFrameComponentBuilder.makeMenuItem("Copy selected text", this, copy_selected_text_action));
		
		message.setComponentPopupMenu(popup);
		
		singleLineInfo.add(createLabel("Output from test:"));
		
		add(singleLineInfo,           BorderLayout.NORTH);
		
		add(new JScrollPane(message), BorderLayout.CENTER);
	}
	
	public void setData(List<ReportLine> report) {
		
		// It is possible that there is no report for a test, when it has not
		// created a message yet.
		//
		if (report.size()>0) {
		
			ReportLine firstReportLine = report.get(0);
			
			testName.setText(firstReportLine.getTestCase().getName());
			description.setText(firstReportLine.getTestCase().getDescription());
			
			//level.setText           ( firstReportLine.getLevelAsString()          );
			speciesName.setText     ( firstReportLine.getSpeciesName()            );
			
			if (firstReportLine.getTeamResponsible()!=null) {			
				teamResponsible.setText ( firstReportLine.getTeamResponsible().name() );			
			} else {			
				teamResponsible.setText ( "No team set." );			
			}
			
			StringBuffer messageText = new StringBuffer(); 
			for (ReportLine currentReportLine : report) {
			
				messageText.append(currentReportLine.getMessage());
				messageText.append("\n");
			}
			message.setText( messageText.toString()  );
			
		} else {
			
			message.setText( "Nothing has been reported yet."  );
			
		}
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

