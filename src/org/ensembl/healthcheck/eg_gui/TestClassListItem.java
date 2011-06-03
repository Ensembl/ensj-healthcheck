package org.ensembl.healthcheck.eg_gui;

import org.ensembl.healthcheck.testcase.EnsTestCase;

public class TestClassListItem {
	
	final protected Class<? extends EnsTestCase> testClass;
	
	public Class<? extends EnsTestCase> getTestClass() {
		return testClass;
	}
	public TestClassListItem(Class<? extends EnsTestCase> e) {
		
		this.testClass = e;
	}
	public String toString() {
		return testClass.getSimpleName();
	}
}
