/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
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
