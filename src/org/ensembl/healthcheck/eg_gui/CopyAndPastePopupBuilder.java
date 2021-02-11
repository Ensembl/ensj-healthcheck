/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2021] EMBL-European Bioinformatics Institute
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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

/**
 * 
 * <p>
 * 	Copied most of this from from http://www.objectdefinitions.com/odblog/2007/jtextarea-with-popup-menu/
 * </p>
 * 
 * @author michael
 *
 */
public class CopyAndPastePopupBuilder {

    private HashMap actions;
	
    public void addPopupMenu(JTextComponent c)
    {
        createActionTable(c);

        JPopupMenu menu = new JPopupMenu();
        menu.add(getActionByName(DefaultEditorKit.copyAction, "Copy"));
        menu.add(getActionByName(DefaultEditorKit.cutAction, "Cut"));
        menu.add(getActionByName(DefaultEditorKit.pasteAction, "Paste"));
        menu.add(new JSeparator());
        menu.add(getActionByName(DefaultEditorKit.selectAllAction, "Select All"));
        c.add(menu);

        c.addMouseListener(
           new PopupTriggerMouseListener(
                   menu,
                   c
           )
        );

        //no need to hold the references in the map,
        // we have used the ones we need.
        actions.clear();
    }

    private Action getActionByName(String name, String description) {
        Action a = (Action)(actions.get(name));
        
        if (a == null) {
        	
        	throw new NullPointerException(
        		"Can't find action with name " + name + "\n" + "Possible actions are:" + actions.keySet()
        	);
        }
        
        // Error in the copy and pasted code. Original:
        //
        // a.putValue(Action.NAME, description);
        //
        // corrected:
        //
        a.putValue(name, description);
        
        //System.out.println("The action is " + Action.NAME + " " + name);
        
        return a;
    }


    private void createActionTable(JTextComponent c) {
        actions = new HashMap();
        Action[] actionsArray = c.getActions();
        for (int i = 0; i < actionsArray.length; i++) {
            Action a = actionsArray[i];
            actions.put(a.getValue(Action.NAME), a);
        }
    }

    public static class PopupTriggerMouseListener extends MouseAdapter
    {
        private JPopupMenu popup;
        private JComponent component;

        public PopupTriggerMouseListener(JPopupMenu popup, JComponent component)
        {
            this.popup = popup;
            this.component = component;
        }

        private void showMenuIfPopupTrigger(MouseEvent e) {
        	
        	// Using e.isPopupTrigger will cause the popup menu to open only
        	// after the second right click. This makes the popup menu appear
        	// lazy. The reason is that after the first right click only a
        	// mouseReleased event is fired. The second right click fires all
        	// three mousePressed, mouseClicked and mouseReleased events as 
        	// expected.
        	//
        	if (e.getButton()==MouseEvent.BUTTON3)
            //if (e.isPopupTrigger())
            {
               popup.show(component, e.getX() + 3, e.getY() + 3);
            }
        }

        //according to the javadocs on isPopupTrigger, checking for popup trigger on mousePressed and mouseReleased
        //should be all  that is required
        public void mouseClicked(MouseEvent e) {}
        public void mousePressed(MouseEvent e) {}

        public void mouseReleased(MouseEvent e) {
            showMenuIfPopupTrigger(e);
        }
    }
}
