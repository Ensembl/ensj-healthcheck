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
