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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;


public class TestCaseCellRenderer implements ListCellRenderer {

	/**
	 * <p>
	 * 	A cache so a JLabel does not have to be created every time a list item
	 * is displayed.
	 * </p>
	 */
	protected final Map<TestClassListItem, JLabel> labelsForItems;
	
	public TestCaseCellRenderer() {
		super();
		labelsForItems = new HashMap<TestClassListItem, JLabel>();
	}
	
	public JComponent getListCellRendererComponent(
	        JList list,
	        Object value,
	        int index,
	        boolean isSelected,
	        boolean cellHasFocus)
	    {
			JLabel label;
			TestClassListItem entry = (TestClassListItem) value;
		
			if (labelsForItems.containsKey(entry)) {
				
				label = labelsForItems.get(entry);
				
			} else {			
				label = new JLabel();
				
				label.setText(entry.toString());
				
				// Must be set to true, otherwise the setBackground calls later
				// will have no effect.
				//
				label.setOpaque(true);
				labelsForItems.put(entry, label);
			}
			
	        if (isSelected) {
	        	
	        	label.setBackground(list.getSelectionBackground());
	        	label.setForeground(list.getSelectionForeground());
	        	
	        } else {
	        	
	        	label.setBackground(list.getBackground());
	        	label.setForeground(list.getForeground());
	        }
	        return label;
	    }
}
