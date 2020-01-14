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
