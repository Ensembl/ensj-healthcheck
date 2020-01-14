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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.eg_gui.DatabaseListPanel;
import org.ensembl.healthcheck.eg_gui.DatabaseRadioButton;
import org.ensembl.healthcheck.eg_gui.DatabaseTypeGUIComparator;
import org.ensembl.healthcheck.eg_gui.TabChangeListener;

/**
 * <p>
 * A class that extends JTabbedPane and provides a method for getting all the selected databases.
 * Also highlights currently-selected tab.
 * </p>
 * 
 */

public class DatabaseTabbedPane extends JTabbedPane implements ActionListener {

	protected DatabaseRegistry databaseRegistry;
	protected final List<ActionListener> actionListener;

	/**
	 * Updated automatically to always hold the currently selected DbButton.
	 */
	protected DatabaseRadioButton selectedDbButton;

	public DatabaseRadioButton getSelectedDbButton() {
		return selectedDbButton;
	}

	public void addActionListener(ActionListener l) {
		actionListener.add(l);
	}

	protected void fireActionEvent(ActionEvent actionEvent) {
		for(ActionListener currentActionListener : actionListener) {
			currentActionListener.actionPerformed(actionEvent);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {

		String cmd = arg0.getActionCommand();
		
		if (cmd.equals(Constants.selectedDatabaseChanged)) {			
			selectedDbButton = (DatabaseRadioButton) arg0.getSource();
			
			ActionEvent actionEvent = new ActionEvent(
				this, 
				arg0.getID() +1, 
				Constants.selectedDatabaseChanged
			);
			fireActionEvent(actionEvent);
		}
	}

	/**
	 * 
	 * <p>
	 * 	Creates a map that maps a DatabaseRegistryEntry to the JRadioButton 
	 * that will represent for selection in the gui.
	 * </p>
	 * 
	 * @param allDbEntries
	 * @return Map<DatabaseRegistryEntry, JRadioButton>
	 * 
	 */
	protected Map<DatabaseRegistryEntry, DatabaseRadioButton> createRadioButtonsForDb(DatabaseRegistryEntry[] allDbEntries) {
		
		Map<DatabaseRegistryEntry, DatabaseRadioButton> checkBoxMap = new HashMap<DatabaseRegistryEntry, DatabaseRadioButton>();
        
        for (DatabaseRegistryEntry currentEntry : allDbEntries) {
        	
        	DatabaseRadioButton dbcb = new DatabaseRadioButton(currentEntry, false);
        	dbcb.addActionListener(this);
        	dbcb.setActionCommand(Constants.selectedDatabaseChanged);
        	
            checkBoxMap.put(currentEntry, dbcb);
        }
        return checkBoxMap;
	}
	
	/**
	 * 
	 * <p>
	 * 	Searches through an array of allDbEntries and creates a list of 
	 * DatabaseRegistryEntry with the specified filterForType. 
	 * </p>
	 * 
	 * @param allDbEntries
	 * @param filterForType
	 * @return
	 * 
	 */
	private List<DatabaseRegistryEntry> filterForDbsOfType(DatabaseRegistryEntry[] allDbEntries, DatabaseType filterForType) {
		
    	List<DatabaseRegistryEntry> dbRegistryEntryWithOfType = new ArrayList<DatabaseRegistryEntry>();

        for (DatabaseRegistryEntry currentEntry : allDbEntries) {
        	
            if (currentEntry.getType() == filterForType) {
                dbRegistryEntryWithOfType.add(currentEntry);
            }
        }
		return dbRegistryEntryWithOfType;
	}
	
    /**
     * 
     * <p>
     * 	Creates the database tabbed pane from which the user can select the 
     * database on which he wants to run his tests.
     * </p>
     * 
     * @param databaseRegistry
     * 
     */
    public DatabaseTabbedPane(DatabaseRegistry databaseRegistry) {

    	this.databaseRegistry = databaseRegistry;
    	actionListener = new LinkedList<ActionListener>();
    	init();
    }
    
    public DatabaseTabbedPane(
    	DatabaseRegistry databaseRegistry,
    	ActionListener radioActionListener
    	) {

    	this(databaseRegistry);
    	this.addActionListener(radioActionListener);
    }

    public void setMessage(String caption, String message) {

    	this.removeAll();
		addTab(caption, new JLabel(message));
    }


    public void init(DatabaseRegistry databaseRegistry) {
    	this.databaseRegistry = databaseRegistry;
    	init();
    }

    public synchronized void init() {
    	
    	this.removeAll();
    	
    	selectedDbButton = null;
    	
    	DatabaseRegistryEntry[] allDbEntries = databaseRegistry.getAll();
    	
    	if (allDbEntries.length==0) {
    		addTab(
    			"Problem",
    			new JLabel("No databases available.")
    		);
    	}
    	
    	Map<DatabaseRegistryEntry, DatabaseRadioButton> checkBoxMap = createRadioButtonsForDb(allDbEntries);

        DatabaseType[] types = databaseRegistry.getTypes();
        Arrays.sort(types, new DatabaseTypeGUIComparator());
        
        // One Buttongroup for all radio buttons across all the database
        // tabs.
        //
        ButtonGroup myOneAndOnlyButtonGroup = new ButtonGroup();

        for (DatabaseType currentDbType : types) {

        	// Create a list of databases on the server with the currentDbType.
        	//
        	List<DatabaseRegistryEntry> dbsOnServerWithThisType 
    		= filterForDbsOfType(
    			databaseRegistry.getAll(), 
    			currentDbType
    		);

        	// Create Radiobuttons for them for the user to select.
        	//
        	List<DatabaseRadioButton> checkBoxesTabForCurrentType = new ArrayList<DatabaseRadioButton>();
        	
	        for (DatabaseRegistryEntry currentEntry : dbsOnServerWithThisType) {
	            checkBoxesTabForCurrentType.add(checkBoxMap.get(currentEntry));
	        }
    	
            addTab(
            	currentDbType.toString(), 
            	new DatabaseListPanel(checkBoxesTabForCurrentType, myOneAndOnlyButtonGroup)
            );
        }
        addChangeListener(new TabChangeListener());
        if (getTabCount()>0) {
        	setEnabledAt(0, true);
        }
    }


    public void applySearchtermFilter(List<String> SearchTerm) {
    	
    	int index = this.getSelectedIndex();

    	// Returns a JLabel when the JTabbedPane is not fully initialised yet.
    	//
    	Object careful = this.getComponentAt(index);
    	
    	if (!(careful instanceof DatabaseListPanel)) {
    		return;
    	}
    	
    	DatabaseListPanel p = (DatabaseListPanel) careful;
    	if (p!=null) {
    		p.applySearchtermFilter(SearchTerm);
    	}
    }

    // -------------------------------------------------------------------------

    public DatabaseRegistryEntry[] getSelectedDatabases() {

    	return new DatabaseRegistryEntry[] { getSelectedDbButton().getDatabase() };
    	
//        List result = new ArrayList();
//
//        // get all the selected databases for each tab in turn
//        for (int i = 0; i < getTabCount(); i++) {
//
//            DatabaseListPanel dblp = (DatabaseListPanel) getComponentAt(i);
//            DatabaseRegistryEntry[] panelSelected = dblp.getSelected();
//            for (int j = 0; j < panelSelected.length; j++) {
//                result.add(panelSelected[j]);
//            }
//        }
//
//        return (DatabaseRegistryEntry[]) result.toArray(new DatabaseRegistryEntry[result.size()]);

    } // getSelectedDatabases

    // -------------------------------------------------------------------------

} // DatabaseTabbedPane

/**
 * <p>
 * A JRadioButton with a reference to a DatabaseRegistryEntry.
 * </p>
 * 
 */

class DatabaseRadioButton extends JRadioButton {

	private DatabaseRegistryEntry database;

	public DatabaseRadioButton(DatabaseRegistryEntry database, boolean selected) {
		super(database.getName(), selected);
		this.database = database;
	}

	public DatabaseRegistryEntry getDatabase() {
		return database;
	}

	public void setDatabase(DatabaseRegistryEntry database) {
		this.database = database;
	}
}

/**
 * 
 * <p>
 * 	A class that creates a panel (in a JScrollPane) containing a list of 
 * DatabaseRadioButton, and provides methods for accessing the selected ones.
 * </p>
 * 
 */

class DatabaseListPanel extends JScrollPane {

	private List<DatabaseRadioButton> dbSelectionRadioButtons;
	private final ButtonGroup buttonGroup;

	/**
	 * 
	 * <p>
	 * 	This constructor allows the user to set a ButtonGroup explicitly. That
	 * way the same ButtonGroup can be used
	 * </p>
	 * 
	 * @param dbSelectionRadioButtons
	 * @param buttonGroup
	 * 
	 */
	public DatabaseListPanel(List<DatabaseRadioButton> dbSelectionRadioButtons, ButtonGroup buttonGroup) {

		this.dbSelectionRadioButtons = dbSelectionRadioButtons;
		this.buttonGroup             = buttonGroup;
	
		Iterator<DatabaseRadioButton> it = dbSelectionRadioButtons.iterator();
		
		JPanel radioButtonPanel = new JPanel();
		radioButtonPanel.setLayout(new BoxLayout(radioButtonPanel, BoxLayout.Y_AXIS));
		
		while (it.hasNext()) {
		
			DatabaseRadioButton currentDatabaseRadioButton = it.next();
	
			radioButtonPanel.add(currentDatabaseRadioButton);
			buttonGroup     .add(currentDatabaseRadioButton);
		}
		setViewportView(radioButtonPanel);
	}
	
	public void applySearchtermFilter(List<String> SearchTerm) {
		
		Iterator<DatabaseRadioButton> it = dbSelectionRadioButtons.iterator();
		
		while (it.hasNext()) {
			
			DatabaseRadioButton currentDatabaseRadioButton = it.next();
			boolean allSearchTermsPresent = true;
			
			for (String currentSearchTerm : SearchTerm) {
				
				boolean searchTermNotFound = currentDatabaseRadioButton.getDatabase().getName().indexOf(currentSearchTerm)==-1;

				if (searchTermNotFound) {
					allSearchTermsPresent = false;
					break;
				}
			}
			currentDatabaseRadioButton.setVisible(allSearchTermsPresent);
		}
	}
	
    
    /**
     * <p>
     * 	Default Constructor.
     * </p>
     * 
     * @param dbSelectionRadioButtons
     * 
     */
    public DatabaseListPanel(List<DatabaseRadioButton> dbSelectionRadioButtons) {

    	this(dbSelectionRadioButtons, new ButtonGroup());
    }

    // -------------------------------------------------------------------------
    /**
     * Get the databases that are selected on this panel.
     * 
     * @return the selected databases.
     */
    public DatabaseRegistryEntry[] getSelected() {

        List selected = new ArrayList();

        Iterator it = dbSelectionRadioButtons.iterator();
        while (it.hasNext()) {
        	DatabaseRadioButton dbcb = (DatabaseRadioButton) it.next();
            if (dbcb.isSelected()) {
                selected.add(dbcb.getDatabase());
            }
        }
        return (DatabaseRegistryEntry[]) selected.toArray(new DatabaseRegistryEntry[selected.size()]);
    }
}

/**
 * <p>
 * 	Fiddle things so that generic database types are moved to the front.
 * </p>
 */
class DatabaseTypeGUIComparator implements Comparator {

    public int compare(Object o1, Object o2) {

        if (!(o1 instanceof DatabaseType) || !(o2 instanceof DatabaseType)) {
            throw new RuntimeException("Arguments to DatabaseTypeGUIComparator must be DatabaseType!");
        }

        DatabaseType t1 = (DatabaseType) o1;
        DatabaseType t2 = (DatabaseType) o2;

        if (t1.isGeneric() && !t2.isGeneric()) {
            return -1;
        } else if (!t1.isGeneric() && t2.isGeneric()) {
            return 1;
        } else {
            return t1.toString().compareTo(t2.toString());
        }
    }
}

/**
 * Highlight the currently-selected tab of a JTabbedPane.
 */
class TabChangeListener implements ChangeListener {
    public void stateChanged(ChangeEvent e) {}
}
