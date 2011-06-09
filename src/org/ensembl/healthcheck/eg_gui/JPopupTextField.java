package org.ensembl.healthcheck.eg_gui;

import javax.swing.JTextField;


/**
 * <p>
 * 	A TextField that has a popup menu for copy and paste operations.
 * </p>
 * 
 * @author michael
 *
 */
class JPopupTextField extends JTextField {

    public JPopupTextField() {
    	new CopyAndPastePopupBuilder().addPopupMenu(this);
    }

    public JPopupTextField(String defaultText) {
    	this();
        this.setText(defaultText);
    }
}
