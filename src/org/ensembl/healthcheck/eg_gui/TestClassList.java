package org.ensembl.healthcheck.eg_gui;

import java.awt.event.MouseEvent;

import javax.swing.JList;

import org.ensembl.healthcheck.testcase.EnsTestCase;

/**
 * 
 * <p>
 * 	A JList for lists of tests.
 * </p>
 * 
 * @author mnuhn
 *
 */
public class TestClassList extends JList {

	/**
	 * Determines which kind of tooltip help will be shown.
	 */
	protected TestClassListToolTipType testClassListToolTipType;
	
	public TestClassListToolTipType getTestClassListToolTipType() {
		return testClassListToolTipType;
	}

	public void setTestClassListToolTipType(
			TestClassListToolTipType testClassListToolTipType) {
		this.testClassListToolTipType = testClassListToolTipType;
	}

	public static enum TestClassListToolTipType {
		CLASS, DESCRIPTION
	};
	
	public TestClassList(TestClassListToolTipType testClassListToolTipType) {
		this();
		setTestClassListToolTipType(testClassListToolTipType);
	}
	
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
        	
        	if (testClassListToolTipType==TestClassListToolTipType.DESCRIPTION) {

        		String tooltiptext = "";
					
				EnsTestCase etc = null;
				tooltiptext = "Couldn't get a description for this test.";
				
				try {
					etc         = item.newInstance();
					tooltiptext = etc.getDescription();

				} catch (InstantiationException e1) {
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
				}

				return tooltiptext;
        	}

        	if (testClassListToolTipType==TestClassListToolTipType.CLASS) {
        		return item.getCanonicalName();
        	}
        }
        return "";
      }
}
