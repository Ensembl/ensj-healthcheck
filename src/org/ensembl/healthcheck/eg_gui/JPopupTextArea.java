package org.ensembl.healthcheck.eg_gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.text.DefaultEditorKit;

/**
 * <p>
 * 	A TextArea that has a popup menu for copy and paste operations.
 * </p>
 * 
 * @author michael
 *
 */
class JPopupTextArea extends JTextArea {

	protected void addPopupMenu() {
		new CopyAndPastePopupBuilder().addPopupMenu(this);
	}
	
	public JPopupTextArea(int rows, int columns) {
    	super(rows, columns);
    	addPopupMenu();
    }

    public JPopupTextArea() {
    	super();
    	addPopupMenu();
    }
}
