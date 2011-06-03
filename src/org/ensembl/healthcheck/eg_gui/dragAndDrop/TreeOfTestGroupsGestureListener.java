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