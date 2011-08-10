package org.ensembl.healthcheck.eg_gui;

import java.awt.event.MouseEvent;

import javax.swing.JList;

import org.ensembl.healthcheck.testcase.EnsTestCase;

public class TestClassList extends JList {

	public TestClassList() {
		super(new TestClassListModel());
	}
	
	public String getToolTipText(MouseEvent e) {

        int index = locationToIndex(e.getPoint());

        if (-1 < index) {
      	  
      	  Class<? extends EnsTestCase> item = (Class<? extends EnsTestCase>) 
      	  	(
      	  		(TestClassListItem) getModel().getElementAt(index)
      	  	).getTestClass();

			EnsTestCase etc = null;
			String description = "Couldn't get a description for this test.";
			  
			try {
				etc = item.newInstance();
				description = etc.getDescription(); 
			} catch (InstantiationException e1) {
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
			}
      	  
			return item.getCanonicalName() + ": " + description;

        } else {

      	  return null;

        }
      }
}
