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

import java.awt.Component;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.TestInstantiator;
import org.ensembl.healthcheck.configuration.ConfigureHost;
import org.ensembl.healthcheck.eg_gui.dragAndDrop.ListOfTestsToBeRunDropListener;
import org.ensembl.healthcheck.eg_gui.dragAndDrop.TestsTransferHandler;
import org.ensembl.healthcheck.eg_gui.dragAndDrop.TreeOfTestGroupsGestureListener;
import org.ensembl.healthcheck.testcase.EnsTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * 
 * <p>
 * 	Collection of static methods for building the GUI components. This class
 * is stateless. Holds code that would clutter up GuiGroupTestRunnerFrame 
 * otherwise. 
 * </p>
 * 
 * @author michael
 *
 */
public class GuiTestRunnerFrameComponentBuilder {
	
	public static Border defaultEmptyBorder = BorderFactory.createEmptyBorder(12, 4, 12, 4);
	
	public static Component createLeftJustifiedComponent(Component c) {
		
		Box box = Box.createHorizontalBox();
		box.add(c);
		box.add(Box.createHorizontalGlue());

		return box;	
	}
	public static Component createLeftJustifiedText(String text) {
		
		JLabel t = new JLabel(text, JLabel.LEFT);
		t.setAlignmentX(JLabel.LEFT_ALIGNMENT);

		return createLeftJustifiedComponent(t);	
	}
	
	public static JComboBox createDbServerSelector(List<ConfigureHost> dbDetails) {
		
		List<String> dbServerComboBoxDisplayName = new ArrayList<String>();
		
		// Sort items in list by hostname then by user.
		//
		Collections.sort(dbDetails, new Comparator<ConfigureHost>() {
			@Override
			public int compare(ConfigureHost o1, ConfigureHost o2) {
				
				int comparisonResult = o1.getHost().compareTo(o2.getHost());
				
				if (comparisonResult!=0) {
					return comparisonResult;
				}
				
				return 
					( o1.getUser().compareTo(o2.getUser()) )
				;
			}
			
		});
		
		for (ConfigureHost currentDbServer : dbDetails) {
			
			if (currentDbServer.getHost().equals("127.0.0.1")) {

				// If the connection is local, it is probably being port 
				// forwarded. Add port number so the user knows which one
				// it is.
				//
				dbServerComboBoxDisplayName.add(
						currentDbServer.getHost() + ":" + currentDbServer.getPort() 
						+ " as "
						+ currentDbServer.getUser()
				);

			} else {
			
				dbServerComboBoxDisplayName.add(
					currentDbServer.getHost() 
					+ " as "
					+ currentDbServer.getUser()
				);
			}
		}
		
		JComboBox dbServerSelector = new JComboBox(dbServerComboBoxDisplayName.toArray());
		
		return dbServerSelector;
	}
	
	/**
	 * 
	 * <p>
	 * 	Shamelessly copied this useful method from page 605 of Learning Java
	 * from O'Reilly.
	 * </p>
	 * 
	 * @param label
	 * @return menu item for label/action
	 * 
	 */
	public static JMenuItem makeMenuItem(String label, ActionListener a, String actionCommand) {
		
		JMenuItem item = new JMenuItem(label);
		item.addActionListener(a);
		item.setActionCommand(actionCommand);
		return item;
	}
	
	public static JPopupMenu createTreeOfTestGroupsPopupMenu(ActionListener a) {
		
		final JPopupMenu popup = new JPopupMenu();		
		
		popup.add(makeMenuItem("Add to tests to be run", a, Constants.Add_to_tests_to_be_run));
		
		return popup;
	}
	
	public static JPopupMenu createListOfTestsToBeExecutedPopupMenu(ActionListener a) {
		
		final JPopupMenu popup = new JPopupMenu();		

		popup.add(makeMenuItem("Remove selected tests",  a, Constants.REMOVE_SELECTED_TESTS));
		popup.add(makeMenuItem("Run selected tests",     a, Constants.RUN_SELECTED_TESTS));
		popup.add(makeMenuItem("Run all tests",          a, Constants.RUN_ALL_TESTS));

		return popup;
	}
	
    /**
     * 
     * <p>
     * 	Builds the JTree with the testgroups from which the user can select 
     * the tests to be run.
     * </p>
     * 
     * @param testGroupList
     * @return JTree
     * 
     */
    public static JTree createTreeOfTestGroups(List<GroupOfTests> testGroupList, JPopupMenu popup) {
    	
		JTree tree = new JTree(
				TreeModelFromListOfGroupsBuilder.GroupOfTestsToTreeModel(testGroupList)
		) {
			public String getToolTipText(MouseEvent evt) {
				
				if (getRowForLocation(evt.getX(), evt.getY()) == -1)
					return null;
				TreePath curPath = getPathForLocation(evt.getX(), evt.getY());
				
				if (curPath.getLastPathComponent() instanceof TestNode) {
					return ((TestNode) curPath.getLastPathComponent()).getToolTipText();
				}
				if (curPath.getLastPathComponent() instanceof GroupNode) {
					return ((GroupNode) curPath.getLastPathComponent()).getToolTipText();
				}
				return "No tooltip text";
			}
		};
		
		tree
			.getSelectionModel()
			.setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION
			);
		if (popup!=null) {
			tree.setComponentPopupMenu(popup);
		}
		
		// Must be set or the getToolTipText method will never be called.
		//
		tree.setToolTipText("");

	    DragSource ds = new DragSource();
	    DragGestureRecognizer dgr = ds.createDefaultDragGestureRecognizer(
	    		tree, 
	    		DnDConstants.ACTION_COPY, 
	    		new TreeOfTestGroupsGestureListener(
	    			tree, 
	    			ds
	    		)
	    );
	    
		return tree;
    }
    
    /**
     * <p>
     * 	Creates the area in which the tests that will be run are listed.  
     * </p>
     * 
     * @param testInstantiator
     * @return area for list
     * 
     */
    public static JList createListOfTestsToBeRunArea(
    		TestInstantiatorDynamic testInstantiator, 
    		JPopupMenu popup,
    		final ActionListener al
    ) {
    	
    	JList listOfTestsToBeRun = new TestClassList(TestClassList.TestClassListToolTipType.DESCRIPTION);
    	
    	listOfTestsToBeRun.setToolTipText("");
    	    
   	    listOfTestsToBeRun.setTransferHandler(new TestsTransferHandler());
   	    listOfTestsToBeRun.setComponentPopupMenu(popup);
   	    //
   	    // Prevents the user from selecting multiple intervals. Removing 
   	    // multiple intervals from the list does not work properly in 
   	    // ActionExecution.removeSelectedTests and I don't see why, so I do 
   	    // not allow selecting like that it here.
   	    //
   	    listOfTestsToBeRun.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
   	    
   	    listOfTestsToBeRun.setCellRenderer(new TestCaseCellRenderer());
   	    
		listOfTestsToBeRun.addKeyListener(
				
				new KeyListener() {

					@Override public void keyPressed(KeyEvent arg0) {

						// Backspace or delete button can be used to remove 
						// selected tests.
						//
						boolean removeTestsKeyPressed
							=  (arg0.getKeyCode()==KeyEvent.VK_DELETE) 
							|| (arg0.getKeyCode()==KeyEvent.VK_BACK_SPACE);
						
						if (removeTestsKeyPressed) {
							
							al.actionPerformed(
								new ActionEvent(
									this, 
									0, 
									Constants.REMOVE_SELECTED_TESTS
								)
							);
						}
					}

					@Override public void keyReleased(KeyEvent arg0) {}
					@Override public void keyTyped   (KeyEvent arg0) {}
				}
			);
    	    
   	    DropTarget dt = new DropTarget(
   	    	listOfTestsToBeRun,
    		new ListOfTestsToBeRunDropListener(
    			listOfTestsToBeRun, 
    			testInstantiator
	    	)
    	);
   	    
    	return listOfTestsToBeRun;
    }
    
	/**
	 * <p>
	 * 	Creates the button used to delete tests from the list of tests to be 
	 * run.
	 * </p>
	 * 
	 * @param buttonActionListener
	 * @return button
	 * 
	 */
    public static JButton createRemoveSelectedTestsButton(ActionListener buttonActionListener) {
		
	    JButton removeSelectedTestsButton = new JButton("Remove selected tests");
	    
	    removeSelectedTestsButton.setMnemonic(KeyEvent.VK_R);
		
	    removeSelectedTestsButton.setActionCommand(Constants.REMOVE_SELECTED_TESTS);
	    removeSelectedTestsButton.addActionListener(buttonActionListener);
	    removeSelectedTestsButton.setSize(
	    		Constants.DEFAULT_BUTTON_WIDTH, 
	    		Constants.DEFAULT_BUTTON_HEIGHT
	    );
	    return removeSelectedTestsButton;
	}
    
	/**
	 * <p>
	 * 	Creates the button used to run all tests from the list of tests to be 
	 * run.
	 * </p>
	 * 
	 * @param buttonActionListener
	 * @return button
	 * 
	 */
    public static JButton createRunAllTestsButton(ActionListener buttonActionListener) {
		
	    JButton button = new JButton("Run all tests");

	    button.setActionCommand(Constants.RUN_ALL_TESTS);
	    button.addActionListener(buttonActionListener);
	    button.setMnemonic(KeyEvent.VK_A);
	    button.setSize(
	    		Constants.DEFAULT_BUTTON_WIDTH, 
	    		Constants.DEFAULT_BUTTON_HEIGHT
	    );
	    return button;
	}

    public static JButton createRunSelectedTestsButton(ActionListener buttonActionListener) {
		
	    JButton button = new JButton("Run selected tests");

	    button.setActionCommand(Constants.RUN_SELECTED_TESTS);
	    button.addActionListener(buttonActionListener);
	    button.setMnemonic(KeyEvent.VK_S);
	    button.setSize(
	    		Constants.DEFAULT_BUTTON_WIDTH, 
	    		Constants.DEFAULT_BUTTON_HEIGHT
	    );
	    return button;
	}
    
    /**
     * 
     * <p>
     * Dummy ActionListener that only prints command names to STDOUT.
     * </p>
     * 
     * @return listener instance
     * 
     */
    public static ActionListener createDummyActionListener() {
    	return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.out.println("Actioncommand received: " + arg0.getActionCommand());
			}
    	};
    }
}
