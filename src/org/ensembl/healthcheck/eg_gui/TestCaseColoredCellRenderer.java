package org.ensembl.healthcheck.eg_gui;

import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JList;

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
	
	public Component getListCellRendererComponent(
	        JList list,
	        Object value,
	        int index,
	        boolean isSelected,
	        boolean cellHasFocus) {
		
		TestClassListItem entry = (TestClassListItem) value;
		
		Component label = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		
		Boolean success = colorForItems.get(entry.getTestClass());
		
		//Color color = colorForItems.get(entry.getTestClass());
		
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
		return label;		
	}


}
