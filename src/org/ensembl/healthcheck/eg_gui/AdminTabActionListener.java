/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2018] EMBL-European Bioinformatics Institute
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.ensembl.healthcheck.eg_gui.Constants;
import org.ensembl.healthcheck.util.ProcessExec;

/**
 * Runs commands selected in the admin tab.
 * 
 * @author michael
 *
 */
public class AdminTabActionListener implements ActionListener {
	
	/**
	 * JTextArea to which output is written. 
	 */
	protected final JTextArea output;
	
	public AdminTabActionListener(JTextArea output) {
		
		this.output = output;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		if (arg0.getActionCommand() == Constants.checkoutPerlDependenciesButton) {
			
			runShellCmd("ant checkoutPerlDependencies");
		}
	}
	
	protected void runShellCmd(final String cmd) {

		// Clear text from a previous run.
		//
		output.setText(null);
		
		// Wrap in thread, otherwise GUI will be unresponsive while 
		// dependencies are being checked out.
		//
		Thread t = new Thread() {

			OutputGobbler out = new OutputGobbler(output);
			
			public void run() {
				try {
					ProcessExec.execShell(cmd, out, out);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		};
		t.start();		
	}
}

class OutputGobbler implements Appendable {
	
	protected final JTextArea output;
	
	public OutputGobbler(JTextArea output) {
		
		this.output = output;
	}

	@Override
	public Appendable append(final CharSequence csq) throws IOException {
		
		// Using invokeLater so JTextArea will scroll while text is appended.
		// Suggested here:
		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4201999
		//
		SwingUtilities.invokeLater(
			new Runnable() {
				public void run() {
					output.append(csq.toString());
				}
			}
		);
		
		return this;
	}

	@Override
	public Appendable append(final char c) throws IOException {

		// Using invokeLater so JTextArea will scroll while text is appended.
		// Suggested here:
		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4201999
		//
		SwingUtilities.invokeLater(
			new Runnable() {
				public void run() {
					output.append("" + c);
				}
			}
		);
		return this;
	}

	@Override
	public Appendable append(CharSequence csq, int start, int end)
			throws IOException {
		
		throw new NoSuchMethodError("This method should not be needed at the moment.");
	}	
}

