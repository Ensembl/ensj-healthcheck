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
