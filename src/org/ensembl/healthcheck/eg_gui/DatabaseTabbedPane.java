package org.ensembl.healthcheck.eg_gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
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

public class DatabaseTabbedPane extends JTabbedPane {

	/**
	 * 
	 * <p>
	 * 	Creates a map that maps a DatabaseRegistryEntry to the JRadioButton 
	 * that will represent for selection in the gui.
	 * </p>
	 * 
	 * @param databaseRegistry
	 * @return Map<DatabaseRegistryEntry, JRadioButton>
	 * 
	 */
	protected Map<DatabaseRegistryEntry, JRadioButton> createRadioButtonsForDb(DatabaseRegistryEntry[] allDbEntries) {
		
		Map<DatabaseRegistryEntry, JRadioButton> checkBoxMap = new HashMap<DatabaseRegistryEntry, JRadioButton>();
        
        for (DatabaseRegistryEntry currentEntry : allDbEntries) {
        	
        	DatabaseRadioButton dbcb = new DatabaseRadioButton(currentEntry, false);
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

    	init(databaseRegistry);
    }
    
    public void init(DatabaseRegistry databaseRegistry) {
    	
    	this.removeAll();
    	
    	Map<DatabaseRegistryEntry, JRadioButton> checkBoxMap = createRadioButtonsForDb(databaseRegistry.getAll());

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
        	List<JRadioButton> checkBoxesTabForCurrentType = new ArrayList<JRadioButton>();
        	
	        for (DatabaseRegistryEntry currentEntry : dbsOnServerWithThisType) {
	            checkBoxesTabForCurrentType.add(checkBoxMap.get(currentEntry));
	        }
    	
            addTab(
            	currentDbType.toString(), 
            	new DatabaseListPanel(checkBoxesTabForCurrentType, myOneAndOnlyButtonGroup)
            );
        }
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

/**
 * A JCheckBox that stores a reference to a DatabaseRegistryEntry.
 */

class DatabaseRadioButton extends JRadioButton {

    private DatabaseRegistryEntry database;

    public DatabaseRadioButton(DatabaseRegistryEntry database, boolean selected) {
        super(database.getName(), selected);
        this.database = database;
        //this.addChangeListener(l);
        //setBackground(Color.WHITE);
    }

    public DatabaseRegistryEntry getDatabase() {
        return database;
    }

    public void setDatabase(DatabaseRegistryEntry database) {
        this.database = database;
    }
}

/**
 * A class that creates a panel (in a JScrollPane) containing a list of DatabseCheckBoxes, and
 * provides methods for accessing the selected ones.
 */

class DatabaseListPanel extends JScrollPane {

    private List<JRadioButton> dbSelectionRadioButtons;
    
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
    public DatabaseListPanel(List<JRadioButton> dbSelectionRadioButtons, ButtonGroup buttonGroup) {

        this.dbSelectionRadioButtons = dbSelectionRadioButtons;
        this.buttonGroup             = buttonGroup;

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JPanel allDatabasesPanel = new JPanel();
        allDatabasesPanel.setLayout(new BoxLayout(allDatabasesPanel, BoxLayout.Y_AXIS));

        Iterator<JRadioButton> it = dbSelectionRadioButtons.iterator();
        
        while (it.hasNext()) {
        
        	JPanel radioButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            
            JRadioButton currentDatabaseRadioButton = it.next(); 
            
            radioButtonPanel.add(currentDatabaseRadioButton);
            buttonGroup     .add(currentDatabaseRadioButton);
            
            allDatabasesPanel.add(radioButtonPanel);
            
        }
        
        final DatabaseListPanel localDBLP = this;
        panel.add(allDatabasesPanel, BorderLayout.CENTER);
        setViewportView(panel);
    	
    }
    
    /**
     * <p>
     * 	Default Constructor.
     * </p>
     * 
     * @param dbSelectionRadioButtons
     * 
     */
    public DatabaseListPanel(List<JRadioButton> dbSelectionRadioButtons) {

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
