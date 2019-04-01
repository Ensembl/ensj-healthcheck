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
import java.util.Arrays;
import java.util.List;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class DatabaseTabbedPaneWithSearchBox extends JPanel implements CaretListener, ChangeListener {

	private DatabaseTabbedPane databasePane;
	private JTextField searchField;
	
	public DatabaseTabbedPaneWithSearchBox(DatabaseTabbedPane databaseTabbedPane) {

		databasePane = databaseTabbedPane;
		searchField = new JTextField();
		
		this.setLayout(new BorderLayout());
		
		Box searchForm = Box.createHorizontalBox();
		
		searchForm.add(new JLabel("Filter by:"));
		searchForm.add(Box.createHorizontalGlue());
		searchForm.add(searchField);
		
		this.add(searchForm, BorderLayout.NORTH);
		this.add(databasePane,        BorderLayout.CENTER);
		
		new CopyAndPastePopupBuilder().addPopupMenu(searchField);
		searchField.addCaretListener(this);
		databasePane.addChangeListener(this);
	}

	public DatabaseTabbedPane getDatabasePane() {
		return databasePane;
	}

	public void setDtp(DatabaseTabbedPane dtp) {
		this.databasePane = dtp;
	}

	public JTextField getSearchField() {
		return searchField;
	}

	public void setSearchField(JTextField searchField) {
		this.searchField = searchField;
	}

	protected void applySearchtermFilter() {
		
		String searchText = searchField.getText();
		String[] searchItems = searchText.split(" ");
		List<String> SearchTerm = Arrays.asList(searchItems);
		
		databasePane.applySearchtermFilter(SearchTerm);
	}
	
	@Override
	public void caretUpdate(CaretEvent arg0) {
		applySearchtermFilter();
	}

	@Override
	public void stateChanged(ChangeEvent evt) {
		// This method is called whenever the selected tab changes

		DatabaseTabbedPane pane = (DatabaseTabbedPane) evt.getSource();
		// Get current tab
		int sel = pane.getSelectedIndex();
		
		
		if (sel!=-1) {
			applySearchtermFilter();
		}
	}
}
