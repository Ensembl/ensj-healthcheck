/*
 Copyright (C) 2004 EBI, GRL
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.ensembl.healthcheck.gui;

import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.logging.*;

import org.ensembl.healthcheck.*;
import org.ensembl.healthcheck.util.*;
import org.ensembl.healthcheck.testcase.*;

/**
 * The main display frame for GuiTestRunner.
 */
public class GuiTestRunnerFrame extends javax.swing.JFrame implements CallbackTarget {

    GuiTestRunner guiTestRunner;

    private static final Dimension PANEL_SIZE = new Dimension(600, 650);

    private JLabel statusLabel;

    Map testButtonInfoWindows = new HashMap();

    // -------------------------------------------------------------------------
    /**
     * Creates new form GuiTestRunnerFrame
     * 
     * @param gtr The GuiTestRunner that is associated with this Frame.
     */
    public GuiTestRunnerFrame(GuiTestRunner gtr, TestRegistry testRegistry, DatabaseRegistry databaseRegistry) {

        initComponents(testRegistry, databaseRegistry);
        this.guiTestRunner = gtr;

    }

    // -------------------------------------------------------------------------
    /**
     * Initialise the GUI
     */
    private void initComponents(TestRegistry testRegistry, DatabaseRegistry databaseRegistry) {

        // ----------------------------
        // Frame

        setSize(PANEL_SIZE);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle("EnsEMBL HealthCheck");
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent evt) {

                System.exit(0);
            }
        });

        // ----------------------------
        // Top panel - title

        JPanel topPanel = new JPanel();
        JLabel titleLabel = new JLabel();
        topPanel.setBackground(new Color(255, 255, 255));
        titleLabel.setFont(new Font("SansSerif", 1, 18));
        titleLabel.setText("HealthCheck");
        titleLabel.setIcon(new ImageIcon(this.getClass().getResource("e-logo.gif")));
        topPanel.add(titleLabel);

        // ----------------------------
        // Centre panel - tests and databases
        JPanel centrePanel = new JPanel();
        centrePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        centrePanel.setBackground(Color.WHITE);

        // Panel to hold list of tests
        JPanel testPanel = new JPanel();
        testPanel.setLayout(new BoxLayout(testPanel, BoxLayout.Y_AXIS));
        testPanel.setBackground(new Color(255, 255, 255));
        testPanel.setBorder(new TitledBorder("Select tests"));
        final TestTabbedPane testTabbedPane = new TestTabbedPane(testRegistry);
        testPanel.add(testTabbedPane);

        // Panel to hold database selection tabs
        JPanel databasePanel = new JPanel();
        databasePanel.setBackground(new Color(255, 255, 255));
        databasePanel.setBorder(new TitledBorder("Select databases"));
        final DatabaseTabbedPane databaseTabbedPane = new DatabaseTabbedPane(databaseRegistry);
        databasePanel.add(databaseTabbedPane);

        centrePanel.add(testPanel);
        centrePanel.add(databasePanel);

        // -----------------------------
        // Bottom panel - buttons & status bar
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(Color.WHITE);

        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(new Color(153, 153, 255));
        statusLabel = new JLabel();
        statusLabel.setFont(new Font("Dialog", 0, 12));
        statusLabel.setText("Status ...");
        statusPanel.add(statusLabel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);

        JButton runButton = new JButton("Run");
        runButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                DatabaseRegistryEntry[] selectedDatabases = databaseTabbedPane.getSelectedDatabases();
                for (int i = 0; i < selectedDatabases.length; i++) {
                    System.out.println(selectedDatabases[i].getName());
                }
                // TODO - implement

            }
        });

        JButton settingsButton = new JButton("Settings ...");
        settingsButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                openSettingsDialog();
            }
        });

        JButton quitButton = new JButton("Quit");
        quitButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                ConnectionPool.closeAll();
                dispose();

            }
        });

        buttonPanel.add(runButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(50, 0)));
        buttonPanel.add(settingsButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(50, 0)));
        buttonPanel.add(quitButton);

        bottomPanel.add(buttonPanel);
        bottomPanel.add(statusPanel);

        // ----------------------------
        // Add basic panels to content pane
        Container contentPane = getContentPane();
        contentPane.add(topPanel, BorderLayout.NORTH);
        contentPane.add(centrePanel, BorderLayout.CENTER);
        contentPane.add(bottomPanel, BorderLayout.SOUTH);

        pack();

    }

    // -------------------------------------------------------------------------
    /**
     * Update the status bar.
     * 
     * @param s The status.
     */
    public void setStatus(String s) {

        statusLabel.setText(s);

    } // setStatus

    // -------------------------------------------------------------------------
    /**
     * Implementation of CallbackTarget; update the relevant part of the GUI when a message is
     * received from the logger.
     * 
     * @param logRecord The log record to display.
     */
    public void callback(LogRecord logRecord) {

        setStatus(logRecord.getMessage());

    } // callback

    // -------------------------------------------------------------------------
    /**
     * Open the window that allows the user to set various parameters.
     */
    private void openSettingsDialog() {

        Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
        Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);

        setCursor(waitCursor);
        GuiTestRunnerSettings gtrs = new GuiTestRunnerSettings(this, guiTestRunner, true);
        gtrs.show();
        setCursor(defaultCursor);

    } // openSettingsDialog

    // -------------------------------------------------------------------------
    /**
     * Append a log record to the info window.
     * 
     * @param logRecord The record to append.
     */
    private void updateTestInfoWindow(LogRecord logRecord) {

        // try and figure out which window to update
        String loggingClass = logRecord.getSourceClassName();

        TestInfoWindow infoWindow = (TestInfoWindow) testButtonInfoWindows.get(loggingClass);
        if (infoWindow != null) {
            infoWindow.append(logRecord.getMessage() + "\n");
        }

    } // updateTestRunnerWindow

    // -------------------------------------------------------------------------
    /**
     * Set the text of a particular info window.
     * 
     * @param testClassName The name of the class sending the info. Used to decide which window to
     *            update.
     * @param report The String to add.
     */
    public void setTestInfoWindowText(String testClassName, String report) {

        TestInfoWindow infoWindow = (TestInfoWindow) testButtonInfoWindows.get(testClassName);
        if (infoWindow != null) {
            infoWindow.setText(report);
        }

    } // setTestInfoWindowText

    // -------------------------------------------------------------------------
    /**
     * Set the text of a particular info window.
     * 
     * @param testClassName The name of the class sending the info. Used to decide which window to
     *            update.
     * @param lines A List of Strings to add to the window.
     */
    public void setTestInfoWindowText(String testClassName, List lines) {

        TestInfoWindow infoWindow = (TestInfoWindow) testButtonInfoWindows.get(testClassName);
        if (infoWindow != null) {
            Iterator it = lines.iterator();
            while (it.hasNext()) {
                ReportLine line = (ReportLine) it.next();
                infoWindow.append(line.getDatabaseName() + ": " + (String) line.getMessage() + "\n");
            }
        }

    } // setTestInfoWindowText

    // -------------------------------------------------------------------------
    /**
     * Return the output level as set in the parent GuiTestRunner.
     * 
     * @return The output level.
     */
    public int getOutputLevel() {

        return guiTestRunner.getOutputLevel();

    } // getOutputLevel

    // -------------------------------------------------------------------------

} // GuiTestRunnerFrame

// -------------------------------------------------------------------------

class TestInfoWindowOpener implements ActionListener {

    TestInfoWindow infoWindow;

    public TestInfoWindowOpener(TestInfoWindow infoWindow) {

        this.infoWindow = infoWindow;

    } // TestInfoWindowOpener

    public void actionPerformed(ActionEvent e) {

        infoWindow.setVisible(!infoWindow.isVisible()); // toggle

    }

} // TestInfoWindowOpener

// -------------------------------------------------------------------------
/**
 * A class that extends JTabbedPane and provides a method for getting all the selected databases.
 * Also highlights currently-selected tab.
 */

class DatabaseTabbedPane extends JTabbedPane {

    public DatabaseTabbedPane(DatabaseRegistry databaseRegistry) {

        setBackground(Color.WHITE);

        DatabaseRegistryEntry[] entries = databaseRegistry.getAll();
        Map checkBoxMap = new HashMap();
        for (int i = 0; i < entries.length; i++) {
            DatabaseCheckBox dbcb = new DatabaseCheckBox(entries[i], false);
            checkBoxMap.put(entries[i], dbcb);
        }

        DatabaseType[] types = databaseRegistry.getTypes();
        for (int i = 0; i < types.length; i++) {
            List checkBoxesForTab = new ArrayList();
            for (int j = 0; j < entries.length; j++) {
                if (entries[j].getType() == types[i]) {
                    checkBoxesForTab.add((DatabaseCheckBox) checkBoxMap.get(entries[j]));
                }
            }
            addTab(types[i].toString(), new DatabaseListPanel(checkBoxesForTab));
        }

        setBackgroundAt(0, Color.LIGHT_GRAY);

        addChangeListener(new TabChangeListener());

    }

    // -------------------------------------------------------------------------

    public DatabaseRegistryEntry[] getSelectedDatabases() {

        List result = new ArrayList();

        // get all the selected databases for each tab in turn
        for (int i = 0; i < getTabCount(); i++) {

            DatabaseListPanel dblp = (DatabaseListPanel) getComponentAt(i);
            DatabaseRegistryEntry[] panelSelected = dblp.getSelected();
            for (int j = 0; j < panelSelected.length; j++) {
                result.add(panelSelected[j]);
            }
        }

        return (DatabaseRegistryEntry[]) result.toArray(new DatabaseRegistryEntry[result.size()]);

    } // getSelectedDatabases

    // -------------------------------------------------------------------------

} // DatabaseTabbedPane

// -------------------------------------------------------------------------
/**
 * Highlight the currently-selected tab of a JTabbedPane.
 */

class TabChangeListener implements ChangeListener {

    public void stateChanged(ChangeEvent e) {

        JTabbedPane jtp = (JTabbedPane) e.getSource();
        int sel = jtp.getSelectedIndex();
        for (int i = 0; i < jtp.getTabCount(); i++) {
            jtp.setBackgroundAt(i, (i == sel ? Color.LIGHT_GRAY : Color.WHITE));
            // TODO - set font?
        }

    }

} // tabChangeListener

// -------------------------------------------------------------------------
/**
 * A class that creates a panel (in a JScrollPane) containing a list of DatabseCheckBoxes, and
 * provides methods for accessing the selected ones.
 */

class DatabaseListPanel extends JScrollPane {

    private List checkBoxes;

    public DatabaseListPanel(List checkBoxes) {

        this.checkBoxes = checkBoxes;

        setPreferredSize(new Dimension(300, 500));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);

        Iterator it = checkBoxes.iterator();
        while (it.hasNext()) {
            panel.add((JCheckBox) it.next());
        }

        setViewportView(panel);
    }

    // -------------------------------------------------------------------------

    public DatabaseRegistryEntry[] getSelected() {

        List selected = new ArrayList();

        Iterator it = checkBoxes.iterator();
        while (it.hasNext()) {
            DatabaseCheckBox dbcb = (DatabaseCheckBox) it.next();
            if (dbcb.isSelected()) {
                selected.add(dbcb.getDatabase());
            }
        }

        return (DatabaseRegistryEntry[]) selected.toArray(new DatabaseRegistryEntry[selected.size()]);

    } // getSelected

    // -------------------------------------------------------------------------

} // DatabaseListPanel

// -------------------------------------------------------------------------
/**
 * A JCheckBox that stores a reference to a DatabaseRegistryEntry.
 */

class DatabaseCheckBox extends JCheckBox {

    private DatabaseRegistryEntry database;

    public DatabaseCheckBox(DatabaseRegistryEntry database, boolean selected) {

        super(database.getName(), selected);
        this.database = database;
        setBackground(Color.WHITE);

    }

    /**
     * @return Returns the database.
     */
    public DatabaseRegistryEntry getDatabase() {

        return database;
    }

    /**
     * @param database The database to set.
     */
    public void setDatabase(DatabaseRegistryEntry database) {

        this.database = database;
    }

} // DatabaseCheckBox

//-------------------------------------------------------------------------
/**
 * A class that extends JTabbedPane to show test information. Also highlights currently-selected
 * tab.
 */

class TestTabbedPane extends JTabbedPane {

    public TestTabbedPane(TestRegistry testRegistry) {

        setBackground(Color.WHITE);

        DatabaseType[] types = testRegistry.getTypes();
        for (int i = 0; i < types.length; i++) {
            addTab(types[i].toString(), new TestListPanel(types[i], testRegistry));
        }

        setBackgroundAt(0, Color.LIGHT_GRAY);

        addChangeListener(new TabChangeListener());

    }

    // -------------------------------------------------------------------------

    public EnsTestCase[] getSelectedTests() {

        List result = new ArrayList();

        // get all the selected tests for each tab in turn
        for (int i = 0; i < getTabCount(); i++) {

            TestListPanel tlp = (TestListPanel) getComponentAt(i);
            EnsTestCase[] panelSelected = tlp.getSelected();
            for (int j = 0; j < panelSelected.length; j++) {
                result.add(panelSelected[j]);
            }
        }

        return (EnsTestCase[]) result.toArray(new EnsTestCase[result.size()]);

    } // getSelectedTests

    // -------------------------------------------------------------------------

} // TestTabbedPane

//-------------------------------------------------------------------------
/**
 * A class that creates a panel (in a JScrollPane) containing tests, and provides methods for
 * accessing the selected ones.
 */

class TestListPanel extends JScrollPane {

    public TestListPanel(DatabaseType type, TestRegistry testRegistry) {

        setPreferredSize(new Dimension(300, 500));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);

        TestDefaultMutableTreeNode top = new TestDefaultMutableTreeNode("Select tests:");

        String[] groups = testRegistry.getGroups(type);
        for (int i = 0; i < groups.length; i++) {
            TestDefaultMutableTreeNode groupNode = new TestDefaultMutableTreeNode(groups[i]);
            EnsTestCase[] testCasesInGroup = testRegistry.getTestsInGroup(groups[i], type);
            for (int j = 0; j < testCasesInGroup.length; j++) {
                groupNode.add(new TestDefaultMutableTreeNode(testCasesInGroup[j]));
            }
            top.add(groupNode);
        }

        JTree tree = new JTree(top);
        tree.setRowHeight(0);
        tree.setCellRenderer(new TestTreeCellRenderer());
        tree.addMouseListener(new TestTreeNodeSelectionListener(tree));
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

        panel.add(tree);

        setViewportView(panel);
    }

    // -------------------------------------------------------------------------

    public EnsTestCase[] getSelected() {

        List selected = new ArrayList();

        // TODO implement

        return (EnsTestCase[]) selected.toArray(new EnsTestCase[selected.size()]);

    } // getSelected

    // -------------------------------------------------------------------------

} // TestListPanel

// -------------------------------------------------------------------------

class TestTreeCellRenderer extends JComponent implements TreeCellRenderer {

    JLabel label;

    JCheckBox checkBox;

    public TestTreeCellRenderer() {

        label = new JLabel();
        label.setBackground(Color.WHITE);
        checkBox = new JCheckBox();
        checkBox.setBackground(Color.WHITE);
        setLayout(new FlowLayout(FlowLayout.LEFT));
        add(checkBox);
        add(label);

    }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf,
            int row, boolean hasFocus) {

        TestDefaultMutableTreeNode node = (TestDefaultMutableTreeNode) value;
        if (node != null) {

            checkBox.setSelected(node.isSelected());
            String defaultFontName = label.getFont().getName();
            int defaultFontSize = label.getFont().getSize();

            if (node.isGroup()) {
                label.setText(node.getGroupName());
                label.setFont(new Font(defaultFontName, Font.BOLD, defaultFontSize));
            } else {
                label.setText(node.getTest().getShortTestName());
                label.setFont(new Font(defaultFontName, Font.PLAIN, defaultFontSize));
            }

        }

        return this;
    }

    public Dimension getPreferredSize() {

        Dimension dim = new Dimension(550, 25);
        return dim;
    }

}

/*
 * Subclass of DefaultMutableTreeNode that tracks whether it's selected or not.
 */

class TestDefaultMutableTreeNode extends DefaultMutableTreeNode {

    private boolean selected;

    private EnsTestCase test;

    private String groupName;

    private boolean isGroup;

    public TestDefaultMutableTreeNode(Object o) {

        if (o instanceof String) {
            this.groupName = (String) o;
            isGroup = true;
        } else if (o instanceof EnsTestCase) {
            this.test = (EnsTestCase) o;
            isGroup = false;
        }

    }

    public boolean isSelected() {

        return selected;
    }

    public void setSelected(boolean selected) {

        this.selected = selected;
    }

    public void toggle() {

        setSelected(!isSelected());
    }

    public boolean isGroup() {

        return isGroup;

    }

    public String getGroupName() {

        return groupName;
    }

    public EnsTestCase getTest() {

        return test;
    }

}

// -------------------------------------------------------------------------
/*
 * Listener that changes whether or not a node is selected when it is clicked.
 */

class TestTreeNodeSelectionListener extends MouseAdapter {

    JTree tree;

    int controlWidth = 20;

    TestTreeNodeSelectionListener(JTree tree) {

        this.tree = tree;
        // TODO - get width of control icons from BasicLookAndFeel and set controlWidth appropriately
    }

    public void mouseClicked(MouseEvent me) {

        if (me.getSource().equals(tree) && me.getX() > controlWidth) { // try to avoid tree expansion
                                                             // controls

            // which node was clicked?
            TestDefaultMutableTreeNode node = (TestDefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node != null) {

                node.toggle();

                // select/deselect children
                if (!node.isLeaf()) {
                    Enumeration children = node.children();
                    while (children.hasMoreElements()) {
                        TestDefaultMutableTreeNode child = (TestDefaultMutableTreeNode) children.nextElement();
                        child.setSelected(node.isSelected());
                    }
                }

                // let the tree repaint itself
                tree.repaint();
            }
        }

    }

}

// -------------------------------------------------------------------------
