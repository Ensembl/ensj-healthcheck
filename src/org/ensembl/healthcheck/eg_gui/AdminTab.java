/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2021] EMBL-European Bioinformatics Institute
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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.ensembl.healthcheck.eg_gui.AdminTab;
import org.ensembl.healthcheck.eg_gui.AdminTabActionListener;
import org.ensembl.healthcheck.eg_gui.Constants;
import org.ensembl.healthcheck.eg_gui.JPopupTextArea;
import org.ensembl.healthcheck.util.ProcessExec;

public class AdminTab extends JPanel {
	
	protected final JButton checkoutPerlDependenciesButton;
	
	protected final JTextField perl5libTextField;
	protected final JTextField perlOptions;
	protected final JTextField perlBinary;
	protected final JTextArea  output;
	
	/**
	 * <p>
	 * 	Creates the default value for the perl executable by looking for perl 
	 * on the path. Calls "which perl" on the shell to do this.
	 * </p>
	 * 
	 * @return perl executable path
	 */
	protected String createPerlBinaryDefaultValue() {
		
		StringBuffer out = new StringBuffer();
		StringBuffer err = new StringBuffer();
		
		String cmd = "which perl";
		
		try {			
			ProcessExec.exec(cmd, out, err);
			
		} catch (IOException e) {
			
			JOptionPane.showMessageDialog(
				this, 
				"Couldn't execute " + cmd, 
				"Sad error", 
				JOptionPane.ERROR_MESSAGE
			);
		}		
		String path = out.toString();
		
		if (path.length()==0) {
			JOptionPane.showMessageDialog(
					this, 
					"Couldn't find perl on the path! This means perl based healthchecks probably won't run.", 
					"No perl interpreter error", 
					JOptionPane.ERROR_MESSAGE
				);
		}
		
		String error = err.toString();
		
		if (error.length()>0) {
			JOptionPane.showMessageDialog(
					this, 
					"A problem occurred while trying to determine the location of your perl interpreter:\n" + error, 
					"Error", 
					JOptionPane.ERROR_MESSAGE
				);
		}
		return out.toString();
	}
	
	// This value is used, if the PERL5LIB environment variable has not been
	// set.
	//
	protected final String perl5libDefaultValue = "perlcode/ensembl/modules/:perl/:perlcode/bioperl/:perlcode/ensembl-variation/modules";
	
	/**
	 * <p>
	 * 	The default value for perl5lib is what is in the PERL5LIB environment
	 * variable. If this is not set, the default string from the constant
	 * perl5libDefaultValue is used.
	 * </p>
	 * 
	 * @return default value
	 */
	protected String getperl5LibDefaultValue() {
		
		String defaultValue;

		// If the user set PERL5LIB, then prepend it to the libraries of the 
		// healthchecks
		//
		if (System.getenv().containsKey("PERL5LIB")) {
			defaultValue = System.getenv().get("PERL5LIB") + ":" + perl5libDefaultValue;
		} else {
			defaultValue = perl5libDefaultValue;
		}
		
		return defaultValue;
	}
	
	public String getPerl5Lib() {
		return perl5libTextField.getText();
	}
	
	public String getPerlOptions() {
		return perlOptions.getText();
	}
	
	public String getPerl5Binary() {
		return perlBinary.getText();
	}
	
	public AdminTab() {
		
		setLayout(new BorderLayout());
		
		perl5libTextField = new JTextField (getperl5LibDefaultValue());
		
		checkoutPerlDependenciesButton = new JButton("Checkout perl dependencies");
		
		checkoutPerlDependenciesButton.setActionCommand(Constants.checkoutPerlDependenciesButton);
		
		Box settingsForm = Box.createVerticalBox();
		
		settingsForm.setBorder(
			BorderFactory.createTitledBorder(
				GuiTestRunnerFrameComponentBuilder.defaultEmptyBorder,
				"Settings"
			)
		);
		settingsForm.add(GuiTestRunnerFrameComponentBuilder.createLeftJustifiedText("PERL5LIB"));
		
		settingsForm.add(perl5libTextField);		
		
		perlBinary = new JTextField();
		
		perlBinary.setText(createPerlBinaryDefaultValue());
		
		settingsForm.add(GuiTestRunnerFrameComponentBuilder.createLeftJustifiedText("Perl binary"));
		settingsForm.add(perlBinary);
		
		JButton fileChooserButton = new JButton("Choose perl interpreter..."); 
		
		settingsForm.add(GuiTestRunnerFrameComponentBuilder.createLeftJustifiedComponent(fileChooserButton));

		fileChooserButton.addActionListener(			
			new ActionListener() {		
				public void actionPerformed(ActionEvent ae) {
					JFileChooser chooser = new JFileChooser();
					chooser. setMultiSelectionEnabled(false);
					int option = chooser.showOpenDialog(AdminTab.this);
					if (option == JFileChooser.APPROVE_OPTION) {
						File f = chooser.getSelectedFile();
						perlBinary.setText(f.getAbsolutePath());
					} else {
						//perlBinary.setText("Cancelled");
					}
				}
			}
		);
	
		perlOptions = new JTextField();
		
		settingsForm.add(GuiTestRunnerFrameComponentBuilder.createLeftJustifiedText("Perl options"));
		settingsForm.add(perlOptions);
		
		settingsForm.add(Box.createVerticalStrut(Constants.DEFAULT_VERTICAL_COMPONENT_SPACING));
		
		settingsForm.add(GuiTestRunnerFrameComponentBuilder.createLeftJustifiedComponent(checkoutPerlDependenciesButton));		
		
		Box form = Box.createVerticalBox();
		
		form.setAlignmentX(LEFT_ALIGNMENT);
		
		form.add(settingsForm);
		
		output = new JPopupTextArea();
		
		output.setFont(
			new Font(
				"Courier", 
				Font.PLAIN, 
				output.getFont().getSize())
		);
		
		checkoutPerlDependenciesButton.addActionListener(new AdminTabActionListener(output));
		
		JPanel outputComponent = new JPanel();
		
		outputComponent.setLayout(new BorderLayout());
		
		outputComponent.setBorder(
			BorderFactory.createTitledBorder(
				GuiTestRunnerFrameComponentBuilder.defaultEmptyBorder, 
				"Console"
			)
		);
		
		outputComponent.add(new JScrollPane(output), BorderLayout.CENTER);
		
		add(form,            BorderLayout.NORTH);
		add(outputComponent, BorderLayout.CENTER);
	}
}
