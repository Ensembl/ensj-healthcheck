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

package org.ensembl.healthcheck.eg_gui.dragAndDrop;

import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.ensembl.healthcheck.eg_gui.Constants;
import org.ensembl.healthcheck.eg_gui.dragAndDrop.TreeToListDragSourceListener;

/**
 * <p>
 * 	Listener for drag and drop event on the tree with the test groups.
 * </p>
 * 
 * @author michael
 *
 */
public class TreeOfTestGroupsGestureListener implements DragGestureListener {
	
	final protected JTree      tree;
	final protected DragSource ds;
	
	public TreeOfTestGroupsGestureListener(JTree tree, DragSource ds) {
		super();
		this.tree = tree;
		this.ds   = ds;
	}

	public void dragGestureRecognized(DragGestureEvent dge) {
  	  
	  TreePath currentSelection = tree.getSelectionPath();
 
	  String nodeName = currentSelection.getLastPathComponent().toString();
	  
	  // Since we are working with constants, this is actually one of the 
	  // rare cases where we can compare strings using the == operator.
	  //
	  //if (!nodeName.equals(Constants.TREE_ROOT_NODE_NAME)) {
	  //
	  if (nodeName == Constants.TREE_ROOT_NODE_NAME) {
		  
		  return;
	  }

	  ds.startDrag(
		 dge, 
		 DragSource.DefaultCopyDrop, 
		 new StringSelection(nodeName), 
		 new TreeToListDragSourceListener()
	  );
	}
}
