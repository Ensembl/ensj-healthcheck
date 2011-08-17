package org.ensembl.healthcheck.eg_gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
	
	protected boolean userClickedOnList = false;
	
	final protected Map<Class<? extends EnsTestCase>,GuiReportPanelData> reportData;
	
	public void selectDefaultListItem() {
		
		if (!userClickedOnList) {
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
				userClickedOnList = true;
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
							if (!userClickedOnList) {
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

	
	protected Component createVerticalSpacing() {
		return Box.createVerticalStrut(Constants.DEFAULT_VERTICAL_COMPONENT_SPACING);
	}
	
	public ReportPanel() {
		
		this.setBorder(GuiTestRunnerFrameComponentBuilder.defaultEmptyBorder);
		
		Box singleLineInfo = Box.createVerticalBox();
		
		testName        = new JPopupTextField("Name");
		description     = new JPopupTextArea(3, 0);
		teamResponsible = new JPopupTextField("Team Responsible");
		speciesName     = new JPopupTextField("Species Name");
		message         = new JPopupTextArea ();
		
		description.setLineWrap(true);
		
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
		
		singleLineInfo.add(g.createLeftJustifiedText("Output from test:"));
		
		add(singleLineInfo,           BorderLayout.NORTH);
		add(new JScrollPane(message), BorderLayout.CENTER);
	}
	
	public void setData(GuiReportPanelData reportData) {
		
		testName        .setText (reportData.getTestName());
		description     .setText (reportData.getDescription());
		speciesName     .setText (reportData.getSpeciesName());
		teamResponsible .setText (reportData.getTeamResponsible());
		message         .setText (reportData.getMessage());
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

