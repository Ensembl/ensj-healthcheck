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

package org.ensembl.healthcheck.eg_gui.dragAndDrop;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.swing.JList;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.TestInstantiator;
import org.ensembl.healthcheck.eg_gui.TestClassListModel;
import org.ensembl.healthcheck.eg_gui.TestInstantiatorDynamic;
import org.ensembl.healthcheck.testcase.EnsTestCase;

/**
 * <p>
 * 	Class that handles drop events to the list of tests to be run.
 * </p>
 * 
 * @author michael
 *
 */
public class ListOfTestsToBeRunDropListener implements DropTargetListener {
	
	final protected JList DragAndDropDestination;
	final protected TestInstantiatorDynamic testInstantiator;
	
	public ListOfTestsToBeRunDropListener(
		JList DragAndDropDestination,
		TestInstantiatorDynamic testInstantiator
	) {
		
		super();
		this.DragAndDropDestination = DragAndDropDestination;
		this.testInstantiator       = testInstantiator;
	}
	
    public void dragEnter         (DropTargetDragEvent dtde) {}
    public void dragOver          (DropTargetDragEvent dtde) {}
    public void dropActionChanged (DropTargetDragEvent dtde) {}
    public void dragExit          (DropTargetEvent     dte)  {}

    /**
     * <p>
     * 	Adds a test to the list. The itemToAdd can be a String with the name
     * of the GroupOfTests or a String with the SimpleName of the testclass or
     * the testclass or the class of the GroupOfTests. 
     * </p>
     * <p>
     * 	If itemToAdd has a different type than the ones mentioned above, a 
     * RuntimeException is thrown.
     * </p>
     * 
     * @param itemsToAdd
     * 
     */
    protected void addTestToList(Object... itemsToAdd) {
    	
    	TestClassListModel currentListModel = (TestClassListModel) DragAndDropDestination.getModel();
    	
    	boolean testWasAdded = false;
    	
    	for (Object itemToAdd : itemsToAdd) {
    		
	    	if (itemToAdd instanceof String) {
	    		
	    		String currentItem = (String) itemToAdd;
	    		
	    		if (testInstantiator.isDynamic(currentItem)) {
	    			
	    			System.out.print(currentItem);
	    			
	    			currentListModel.addTest(
	    				testInstantiator.instanceByName(
	    					currentItem, GroupOfTests.class
	    				)
	    			);
	    			testWasAdded = true;
	    		} else {
		    		Class<?> itemToAddClass = testInstantiator.forName((String) itemToAdd);
		    		currentListModel.addTest(itemToAddClass);
		    		testWasAdded = true;
	    		}
	    	}
	
	    	if (
	    			// Make sure it is a Class object, before casting it in one of 
	    			// the next two tests
	    			//
	    			(itemToAdd instanceof Class) 
	    			&& (
	    				// Test, if itemToAdd is a class which is a testcase or a
	    				// GroupOfTests.
	    				//
	    				(EnsTestCase.class.isAssignableFrom((Class) itemToAdd))
	    			    || 
	    			    (GroupOfTests.class.isAssignableFrom((Class) itemToAdd))
	    			)
	    	) {
	    		currentListModel.addTest((Class) itemToAdd);
	    		testWasAdded = true;
	    		//DragAndDropDestination.revalidate();
	    	}
    	}

    	if (testWasAdded) {

    		// If something was added, repaint the list.

	        // The next two lines should not be done like this. It should be 
	        // possible to just run this to make the changes in the JList take 
	        // effect. Unfortunately this does not happen.
	        // 
	        //DragAndDropDestination.repaint();
	
	        TestClassListModel newListModel = new TestClassListModel(currentListModel.getGroupOfTests());        
	        DragAndDropDestination.setModel(newListModel);
	        
    	} else {
    		
    		// If nothing was added, an exception is thrown.
    		//
    		throw new RuntimeException("Couldn't add any of the objects "+ itemsToAdd.toString() +" to the list of tests to be run!");
    		
    	}
    }
    
    public void drop(DropTargetDropEvent dtde) {
    	
      try {
        // Get the dropped object and try to figure out what it is.
        Transferable tr = dtde.getTransferable( );
        DataFlavor[] flavors = tr.getTransferDataFlavors( );
        for (int i = 0; i < flavors.length; i++) {

          // Check that the type is correct.
          //
          if (flavors[i].isFlavorTextType()) {

            dtde.acceptDrop(DnDConstants.ACTION_COPY);
            String name = (String) tr.getTransferData(flavors[i]);
            addTestToList(name);
            dtde.dropComplete(true);
            return;
          }
        }
        dtde.rejectDrop( );
      } 
      catch (  UnsupportedFlavorException e) { dtde.rejectDrop(); } 
      catch (InvalidDnDOperationException e) { dtde.rejectDrop(); } 
      catch (IOException e) {
    	  throw new RuntimeException(e);
      } 
    }
}
