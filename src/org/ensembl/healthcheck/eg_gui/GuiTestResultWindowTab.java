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
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.ensembl.healthcheck.ReportLine;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.eg_gui.GuiTestResultWindowTab;
import org.ensembl.healthcheck.eg_gui.JLabelTreeCellRenderer;
import org.ensembl.healthcheck.eg_gui.ResultNode;
import org.ensembl.healthcheck.eg_gui.ResultTreePanel;

/**
 * <p>
 * 	This is mostly the same as org.ensembl.healthcheck.gui.GuiTestResultWindow
 * with some minor changes so it can appear in the eg_gui for people who want 
 * to view the test results in the format to which they are used.
 * </p>
 * 
 * Display the results of a test run.
 */
public class GuiTestResultWindowTab extends JPanel {

    private static final String OUTPUT_FILE = "GuiTestRunner.txt";

    /**
     * Create a new GuiTestResultWindow.
     * @param outputLevelAsString
     * @param outputLevel
     */
    public GuiTestResultWindowTab(String outputLevelAsString, int outputLevel) {

        //super("Healthcheck Results");

        //Container contentPane = getContentPane();
        this.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.RED);
        ResultTreePanel resultTreePanel = new ResultTreePanel(outputLevelAsString, outputLevel);
        topPanel.add(resultTreePanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(Color.WHITE);

        //JButton saveButton = new JButton("Save", new ImageIcon(this.getClass().getResource("save.gif")));
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                try {

                    PrintWriter pw = new PrintWriter(new FileOutputStream(OUTPUT_FILE));

                    pw.write("---- RESULTS BY TEST CASE ----\n");
                    Map map = ReportManager.getAllReportsByTestCase();
                    Set keys = map.keySet();
                    Iterator it = keys.iterator();
                    while (it.hasNext()) {
                        String key = (String) it.next();
                        System.out.println("\n" + key);
                        List lines = (List) map.get(key);
                        Iterator it2 = lines.iterator();
                        while (it2.hasNext()) {
                            ReportLine reportLine = (ReportLine) it2.next();
                            pw.write(" " + reportLine.getDatabaseName() + ": " + reportLine.getMessage() + "\n");
                        } // while it2
                    } // while it

                    pw.close();

                } catch (Exception ee) {
                    System.err.println("Error writing to " + OUTPUT_FILE);
                    ee.printStackTrace();
                }

                JOptionPane.showMessageDialog((Component) e.getSource(), "Results saved to " + OUTPUT_FILE);

            }
        });

//        JButton closeButton = new JButton("Close", new ImageIcon(this.getClass().getResource("close.gif")));
//        closeButton.addActionListener(new ActionListener() {
//
//            public void actionPerformed(ActionEvent e) {
//
//                dispose();
//
//            }
//        });

        bottomPanel.add(saveButton);
//        bottomPanel.add(closeButton);

        this.add(topPanel, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);

        //pack();

        // Centre on screen
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle frame = getBounds();
        setLocation((screen.width - frame.width) / 2, (screen.height - frame.height) / 2);

    }

    // -------------------------------------------------------------------------
    /**
     * Command-line entry point.
     * 
     * @param args The command-line arguments.
     */
    public static void main(String[] args) {

        GuiTestResultWindowTab gtrw = new GuiTestResultWindowTab("", 0);
//gtrw.pack();
        gtrw.setVisible(true);

    }

    // -------------------------------------------------------------------------

} // GuiTestResultWindow

//-------------------------------------------------------------------------
/**
 * A class that creates a panel (in a JScrollPane) containing tests, and provides methods for
 * accessing the selected ones.
 */

class ResultTreePanel extends JScrollPane {

    private JTree tree;

    //public ResultTreePanel(GuiTestRunnerFrame gtrf) {
    public ResultTreePanel(String outputLevelAsString, int outputLevel) {
    	

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.GREEN);

        String title = "Test Results - minimum output level: " + outputLevelAsString.toLowerCase();

        DefaultMutableTreeNode top = new DefaultMutableTreeNode(new ResultNode(title, false, false, false, false));

        Map reportsByTest = ReportManager.getAllReportsByTestCase(outputLevel);
        Set tests = reportsByTest.keySet();
        Iterator it = tests.iterator();
        while (it.hasNext()) {
            String test = (String) it.next();
            ResultNode n1 = new ResultNode(test, true, false, false, ReportManager.allDatabasesPassed(test));
            DefaultMutableTreeNode testNode = new DefaultMutableTreeNode(n1);

            List reports = (ArrayList) reportsByTest.get(test);
            Iterator it2 = reports.iterator();
            String lastDB = "";
            while (it2.hasNext()) {

                ReportLine line = (ReportLine) it2.next();
                String database = line.getDatabaseName();

                if (!database.equals(lastDB)) {

                    ResultNode n2 = new ResultNode(database, false, true, false, ReportManager.databasePassed(test, database));
                    DefaultMutableTreeNode dbNode = new DefaultMutableTreeNode(n2);
                    testNode.add(dbNode);
                    String detail = htmlize(ReportManager.getReports(test, database));
                    ResultNode n3 = new ResultNode(detail, false, false, true, false);
                    DefaultMutableTreeNode detailNode = new DefaultMutableTreeNode(n3);
                    dbNode.add(detailNode);

                }
                lastDB = database;

            } // while it2

            top.add(testNode);

        }

        tree = new JTree(top);
        //tree.setRootVisible(false);
        ToolTipManager.sharedInstance().registerComponent(tree);
        tree.setCellRenderer(new JLabelTreeCellRenderer());
        tree.setRowHeight(0);

        panel.add(tree);

        setViewportView(panel);

        // make window as wide as it needs to be plus a bit of padding, fixed height
        setPreferredSize(new Dimension(getPreferredSize().width + 150, 500));

    }

    // -------------------------------------------------------------------------

    private String htmlize(List reports) {

        StringBuffer buf = new StringBuffer();
        buf.append("<html>");
        Iterator it = reports.iterator();
        while (it.hasNext()) {
            ReportLine line = (ReportLine) it.next();
            buf.append(getFontForReport(line));
            buf.append(line.getMessage());
            buf.append("</font>");
            buf.append("<br>");
        }
        buf.append("</html>");

        return buf.toString();

    }

    //---------------------------------------------------------------------

    private String getFontForReport(ReportLine line) {

        String s1 = "";

        switch (line.getLevel()) {
        case (ReportLine.PROBLEM):
            s1 = "<font color='red'>";
            break;
        case (ReportLine.WARNING):
            s1 = "<font color='black'>";
            break;
        case (ReportLine.INFO):
            s1 = "<font color='grey'>";
            break;
        case (ReportLine.CORRECT):
            s1 = "<font color='green'>";
            break;
        default:
            s1 = "<font color='black'>";
        }

        return s1;

    }

    // -------------------------------------------------------------------------

} // ResultTreePanel

//  -------------------------------------------------------------------------
/**
 * Custom cell renderer for a tree of JLabels.
 */

class JLabelTreeCellRenderer extends DefaultTreeCellRenderer {

//    private ImageIcon smallCross = new ImageIcon(this.getClass().getResource("small_cross.gif"));
//
//    private ImageIcon smallTick = new ImageIcon(this.getClass().getResource("small_tick.gif"));
//
//    private ImageIcon listPass = new ImageIcon(this.getClass().getResource("list_pass.gif"));
//
//    private ImageIcon listFail = new ImageIcon(this.getClass().getResource("list_fail.gif"));

    private Color green = new Color(0, 192, 0);

    private Color red = new Color(192, 0, 0);

    public JLabelTreeCellRenderer() {

    }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf,
            int row, boolean hasFocus) {

        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

        DefaultMutableTreeNode dmtNode = (DefaultMutableTreeNode) value;
        ResultNode node = (ResultNode) (dmtNode.getUserObject());

        String defaultFontName = getFont().getName();
        int defaultFontSize = getFont().getSize();

        // defaults
        setText(node.getText());
        setIcon(null);
        setForeground(Color.BLACK);
        setBackground(Color.WHITE);
        setFont(new Font(defaultFontName, Font.PLAIN, defaultFontSize));
        setToolTipText(null);

        // node is rendered differently depending on how its flags are set
        if (node.isTestName()) {

            setFont(new Font(defaultFontName, Font.BOLD, defaultFontSize));
            if (node.passed()) {
                setForeground(green);
                //setIcon(listPass);
            } else {
                setForeground(red);
                //setIcon(listFail);
            }
            int[] passesFails = ReportManager.countPassesAndFailsTest(node.getText());
            setToolTipText(passesFails[0] + " databases passed, " + passesFails[1] + " databases failed");

        } else if (node.isDatabaseName()) {

            if (node.passed()) {
                setForeground(green);
                //setIcon(smallTick);
            } else {
                setForeground(red);
                //setIcon(smallCross);
            }
            int[] passesFails = ReportManager.countPassesAndFailsDatabase(node.getText());
            setToolTipText(node.getText() + " passed a total of " + passesFails[0] + " tests and failed a total of "
                    + passesFails[1] + " tests");

        } else {

            // other nodes - e.g. root
            setFont(new Font(defaultFontName, Font.BOLD, defaultFontSize));
            int[] passesFails = ReportManager.countPassesAndFailsAll();
            setToolTipText("A total of " + passesFails[0] + " individual tests passed and " + passesFails[1] + " failed");

        }

        return this;
    }

}

// -------------------------------------------------------------------------
/**
 * Class to store information about a node of the result tree; a node may represent a test, a
 * database name, a database label, or nothing in particular.
 */

class ResultNode {

    private String text;

    private boolean isDatabaseName;

    private boolean isTestName;

    private boolean isDatabaseLabel;

    private boolean passed;

    public ResultNode(String text, boolean isTestName, boolean isDatabaseName, boolean isDatabaseLabel, boolean passed) {

        this.text = text;
        this.isTestName = isTestName;
        this.isDatabaseName = isDatabaseName;
        this.isDatabaseLabel = isDatabaseLabel;
        this.passed = passed;

    }

    public boolean passed() {

        return passed;
    }

    /**
     * @return Returns the isDatabaseLabel.
     */
    public boolean isDatabaseLabel() {

        return isDatabaseLabel;
    }

    /**
     * @return Returns the isDatabaseName.
     */
    public boolean isDatabaseName() {

        return isDatabaseName;
    }

    /**
     * @return Returns the isTestName.
     */
    public boolean isTestName() {

        return isTestName;
    }

    /**
     * @return Returns the text.
     */
    public String getText() {

        return text;

    }

    public String toString() {

        return text;

    }
}
// -------------------------------------------------------------------------
