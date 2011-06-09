package org.ensembl.healthcheck.eg_gui;

import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.JList;
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

	//final protected Map<String,List<ReportLine>> report;
	
	final protected Map<Class<? extends EnsTestCase>,List<ReportLine>> report;

	final protected JList                       testList;
	final protected JScrollPane                 testListScrollPane;
	//final protected DefaultListModel listModel;
	final protected TestClassListModel          listModel;
	final protected ReportPanel                 reportPanel;
	final protected TestCaseColoredCellRenderer testCaseCellRenderer;
	
	public GuiReporterTab() {

		//report      = new HashMap<String,List<ReportLine>>();
		report      = new HashMap<Class<? extends EnsTestCase>,List<ReportLine>>();
		
		testList    = new JList();		
		//listModel   = new DefaultListModel();
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
							//report.get(listModel.get(testList.getSelectedIndex()))
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
		
		//final String currentKey = reportLine.getShortTestCaseName();
		final Class<? extends EnsTestCase> currentKey = reportLine.getTestCase().getClass();
		
		if (!report.containsKey(currentKey)) {
			
			//report.put(currentKey, new ArrayList<ReportLine>());
			report.put(currentKey, new ArrayList<ReportLine>());
			
			testCaseCellRenderer.setOutcome(currentKey, null);

			// This method will be called from a different thread. Therefore
			// updates to the components must be run through SwingUtilities.
			//
			SwingUtilities.invokeLater(
					new Runnable() {
						@Override public void run() {
							//listModel.addElement(currentKey);
							listModel.addTest(currentKey);
						}
					}
				);
		}
		report.get(currentKey).add(reportLine);
		
		if (reportLine.getLevel()==ReportLine.PROBLEM) {
			testCaseCellRenderer.setOutcome(currentKey, false);
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
	
	final protected JTextField level;
	final protected JTextField teamResponsible;
	final protected JTextField speciesName;
	final protected JPopupTextArea  message;
	//protected JEditorPane  message;
	
	final String copy_selected_text_action = "copy_selected_text_action";
	
	public ReportPanel() {
		
		Box singleLineInfo = Box.createVerticalBox();
		
		level           = new JTextField("Level");
		teamResponsible = new JTextField("Team Responsible");
		speciesName     = new JTextField("Species Name");
		message         = new JPopupTextArea ();
		
		singleLineInfo.add(level);
		singleLineInfo.add(Box.createVerticalStrut(Constants.DEFAULT_VERTICAL_COMPONENT_SPACING));
		singleLineInfo.add(teamResponsible);
		singleLineInfo.add(Box.createVerticalStrut(Constants.DEFAULT_VERTICAL_COMPONENT_SPACING));
		singleLineInfo.add(speciesName);
		singleLineInfo.add(Box.createVerticalStrut(Constants.DEFAULT_VERTICAL_COMPONENT_SPACING));
		
		setLayout(new BorderLayout());
		
		final JPopupMenu popup = new JPopupMenu();		
		
		message.add(GuiTestRunnerFrameComponentBuilder.makeMenuItem("Copy selected text", this, copy_selected_text_action));
		
		//this.add(popup);
		message.setComponentPopupMenu(popup);
		
		add(singleLineInfo,           BorderLayout.NORTH);
		add(new JScrollPane(message), BorderLayout.CENTER);
	}
	
	public void setData(List<ReportLine> report) {
		
		ReportLine firstReportLine = report.get(0);
		
		level.setText           ( firstReportLine.getLevelAsString()          );
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












