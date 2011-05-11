package org.ensembl.healthcheck.eg_gui;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * <p>
 * 	Copied from http://www.java2s.com/Code/Java/Swing-Components/ToolTipTreeExample.htm
 * </p>
 * 
 * @author michael
 *
 */
public class TestNode extends DefaultMutableTreeNode {
	  private String toolTipText;

	  public TestNode(String str, String toolTipText) {
	    super(str);
	    this.toolTipText = toolTipText;
	  }

	  public String getToolTipText() {
	    return toolTipText;
	  }
}
