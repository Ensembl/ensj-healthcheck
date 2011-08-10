package org.ensembl.healthcheck.eg_gui;

import javax.swing.tree.DefaultMutableTreeNode;

import org.ensembl.healthcheck.GroupOfTests;

public class GroupNode extends DefaultMutableTreeNode {
	  
	final private GroupOfTests groupOfTests;
	
	public GroupNode(GroupOfTests groupOfTests) {
		super(groupOfTests.getName());
		this.groupOfTests = groupOfTests;
	}
	
	public String getToolTipText() {
		
		if (groupOfTests.getDescription().isEmpty()) {
			return groupOfTests.getName();
		} else {
			return groupOfTests.getDescription();
		}
	}
}
