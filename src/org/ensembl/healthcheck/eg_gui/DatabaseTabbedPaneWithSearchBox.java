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

	private DatabaseTabbedPane dtp;
	private JTextField searchField;
	
	public DatabaseTabbedPaneWithSearchBox(DatabaseTabbedPane databaseTabbedPane) {

		dtp         = databaseTabbedPane;
		searchField = new JTextField();
		
		this.setLayout(new BorderLayout());
		
		Box searchForm = Box.createHorizontalBox();
		
		searchForm.add(new JLabel("Filter by:"));
		searchForm.add(Box.createHorizontalGlue());
		searchForm.add(searchField);
		
		this.add(searchForm, BorderLayout.NORTH);
		this.add(dtp,        BorderLayout.CENTER);
		
		new CopyAndPastePopupBuilder().addPopupMenu(searchField);
		searchField.addCaretListener(this);
		dtp.addChangeListener(this);
	}

	public DatabaseTabbedPane getDtp() {
		return dtp;
	}

	public void setDtp(DatabaseTabbedPane dtp) {
		this.dtp = dtp;
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
		
		dtp.applySearchtermFilter(SearchTerm);
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
