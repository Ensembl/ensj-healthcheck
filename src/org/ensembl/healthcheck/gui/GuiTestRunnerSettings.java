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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import org.ensembl.healthcheck.ReportLine;

/**
 * Settings Dialog for the GuiTestRunner.
 */
public class GuiTestRunnerSettings extends JDialog {

    private GuiTestRunner guiTestRunner;

    /**
     * Creates new form GuiTestRunnerSettings
     * 
     * @param parent
     *          The parent frame.
     * @param gtr
     *          The GuiTestRunner to use.
     * @param modal
     *          Whether this dialog is modal or not.
     */
    public GuiTestRunnerSettings(GuiTestRunnerFrame parent, GuiTestRunner gtr, boolean modal) {

        super(parent, modal);
        this.guiTestRunner = gtr;
        initComponents();
    }

    /**
     * Initialise the components making up the frame.
     */
    private void initComponents() {

        JPanel topPanel = new JPanel();
        JPanel centrePanel = new JPanel();
        JPanel threadsPanel = new JPanel();
        JLabel outputLabel = new JLabel();
        JComboBox outputComboBox = new JComboBox();
        JPanel bottomPanel = new JPanel();
        JButton applyButton = new JButton();
        JButton cancelButton = new JButton();

        setTitle("EnsEMBL HealthCheck Settings");
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent evt) {

                setVisible(false);
                dispose();
            }
        });

        setBackground(Color.WHITE);
        topPanel.setBackground(Color.WHITE);
        bottomPanel.setBackground(Color.WHITE);
        getContentPane().add(topPanel, BorderLayout.NORTH);

        centrePanel.setLayout(new BoxLayout(centrePanel, BoxLayout.Y_AXIS));
        centrePanel.setBackground(Color.WHITE);
        threadsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        threadsPanel.setBackground(Color.WHITE);
        threadsPanel.setBorder(new EtchedBorder());

        outputLabel.setFont(new Font("Dialog", 0, 12));
        outputLabel.setText("  Output level: ");
        threadsPanel.add(outputLabel);

        outputComboBox.setFont(new Font("Dialog", 0, 12));
        outputComboBox.setModel(new DefaultComboBoxModel(new String[] {"All", "Problems only", "Correct results",
                "Summary", "Info", "None"}));
        threadsPanel.add(outputComboBox);

        centrePanel.add(threadsPanel);

        getContentPane().add(centrePanel, BorderLayout.CENTER);

        bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        applyButton.setText("Apply");
        applyButton.setToolTipText("Apply the current settings and return");
        bottomPanel.add(applyButton);

        cancelButton.setText("Cancel");
        cancelButton.setToolTipText("Return without applying settings");
        bottomPanel.add(cancelButton);

        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        final GuiTestRunner localGTR = guiTestRunner;
        final JComboBox localOutputComboBox = outputComboBox;

        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                closeDialog();
            }
        });

        applyButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                HashMap levels = new HashMap();
                levels.put("All", new Integer(ReportLine.ALL));
                levels.put("Problems only", new Integer(ReportLine.PROBLEM));
                levels.put("Correct results", new Integer(ReportLine.CORRECT));
                levels.put("Warnings", new Integer(ReportLine.WARNING));
                levels.put("Info", new Integer(ReportLine.INFO));
                levels.put("None", new Integer(ReportLine.NONE));

                String selection = localOutputComboBox.getSelectedItem().toString();
                int level = ((Integer) levels.get(selection)).intValue();

                localGTR.setOutputLevel(level);

                closeDialog();
            }
        });

        pack();

    }

    // -------------------------------------------------------------------------

    private void closeDialog() {

        setVisible(false);
        dispose();
    }

    // -------------------------------------------------------------------------

}
