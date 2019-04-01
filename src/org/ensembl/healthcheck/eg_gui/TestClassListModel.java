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

import java.util.ArrayList;
import java.lang.IllegalArgumentException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractListModel;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.eg_gui.TestClassListItem;
import org.ensembl.healthcheck.testcase.EnsTestCase;

public class TestClassListModel extends AbstractListModel {

	protected GroupOfTests groupOfTests;

	public GroupOfTests getGroupOfTests() {
		return groupOfTests;
	}

	protected List<Class<? extends EnsTestCase>> testList;

	protected void GroupOfTestsChanged() {
		
		List<Class<? extends EnsTestCase>> testList = groupOfTests.getListOfTests();
		
		Collections.sort(testList, new Comparator() {
			@Override
			public int compare(Object arg0, Object arg1) {
				
				String name1 = ( (Class<? extends EnsTestCase>) arg0 ).getSimpleName();
				String name2 = ( (Class<? extends EnsTestCase>) arg1 ).getSimpleName();
				
				return name1.compareToIgnoreCase(name2);
			}
		});
		
		this.testList = testList;
		
		// I don't know, where the new element ended up and it seems pointless
		// to keep track of this, so I'll update the entire list.
		//
		fireContentsChanged(this, 0, testList.size()-1);
	}

	public void removeTest(EnsTestCase e) {	
		
		removeTest(e.getClass());
	}

	public void removeTest(Class<? extends EnsTestCase>... testClasses) {	
		
		for (Class<? extends EnsTestCase> e : testClasses) {
			groupOfTests.removeTest(e);
		}
		GroupOfTestsChanged();
	}

	public void addTest(EnsTestCase e) {	
		
		groupOfTests.addTest(e.getClass());
		GroupOfTestsChanged();
	}

	public void addTest(GroupOfTests groupOfTests) {
		
		groupOfTests.addTest(groupOfTests);
		GroupOfTestsChanged();
	}
	
	public void addTest(Class<?> e) {
		
		if (EnsTestCase.class.isAssignableFrom(e)) {
			groupOfTests.addTest((Class<EnsTestCase>) e);
			GroupOfTestsChanged();
			return;
		}
		if (GroupOfTests.class.isAssignableFrom(e)) {
			groupOfTests.addTest((Class<GroupOfTests>) e);
			GroupOfTestsChanged();
			return;
		}
		throw new IllegalArgumentException(e.getCanonicalName() + " is not allowed as an argument!");
	}
	
//	public void addElement(String testClassName) {
//		
//	}
	
	public TestClassListModel() {
		this(new GroupOfTests());
	}	

	public TestClassListModel(GroupOfTests groupOfTests) {

		this.groupOfTests = groupOfTests;
		GroupOfTestsChanged();
	}	
	
    public int getSize() { 
    	return testList.size(); 
    }
    
    public Object getElementAt(int index) { 
    	
    	return new TestClassListItem(testList.get(index));
    }	
}



