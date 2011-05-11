package org.ensembl.healthcheck.eg_gui.dragAndDrop;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

public class TestsTransferHandler extends TransferHandler {
	
    public boolean importData(JComponent comp, Transferable t) {

    	// Make sure we have the right starting points.
      if (!(comp instanceof JList)) {
        return false;
      }
      
      return true;
    }
    
    // We support only file lists on FSTrees.
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
    	
    	//System.out.println("comp: " + comp);
    	//System.out.println("transferFlavors: " + transferFlavors);
    	
      if (comp instanceof JList) {
    	  
//        for (int i = 0; i < transferFlavors.length; i++) {
//          if (!transferFlavors[i].equals(DataFlavor.javaFileListFlavor)) {
//            return false;
//          }
//        }
        return true;
      }
      return false;
    }
}
