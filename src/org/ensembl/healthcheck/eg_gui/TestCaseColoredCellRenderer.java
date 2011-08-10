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
