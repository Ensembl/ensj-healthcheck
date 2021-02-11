/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2021] EMBL-European Bioinformatics Institute
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
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import org.ensembl.healthcheck.testcase.EnsTestCase;

public class TestCaseColoredCellRenderer extends TestCaseCellRenderer {
	
	protected final Map<Class<? extends EnsTestCase>, Boolean>  colorForItems;

	public TestCaseColoredCellRenderer() {
		super();
		colorForItems  = new HashMap<Class<? extends EnsTestCase>, Boolean>();
	}
	
	public void setOutcome(Class<? extends EnsTestCase> item, Boolean success) {	
		
		colorForItems.put(item, success);
	}
	
	public JComponent getListCellRendererComponent(
	        JList list,
	        Object value,
	        int index,
	        boolean isSelected,
	        boolean cellHasFocus) {
		
		TestClassListItem entry = (TestClassListItem) value;
		
		JComponent label = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		
		Boolean success = colorForItems.get(entry.getTestClass());
		
		// What a test looks like when succeeded or failed, is configured here:
		//
		
		if (success==null) {
			label.setForeground(list.getForeground());
		} else {
			if (success==true) {
				label.setForeground(Constants.COLOR_SUCCESS);
			}		
			if (success==false) {
				label.setBackground(Constants.COLOR_FAILURE);
				label.setForeground(Color.WHITE);
			}		
		}
		
		// The look of a test when it is selected is set here:
		//
		
		Font f = label.getFont();
		
		if (isSelected) {
			label.setBorder(new BevelBorder(BevelBorder.LOWERED));
		} else {
			label.setBorder(new EmptyBorder(0, 0, 0, 0));
		}
		
		return label;		
	}


}
