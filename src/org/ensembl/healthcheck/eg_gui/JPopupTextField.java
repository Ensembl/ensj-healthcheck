/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2018] EMBL-European Bioinformatics Institute
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

import javax.swing.JTextField;


/**
 * <p>
 * 	A TextField that has a popup menu for copy and paste operations.
 * </p>
 * 
 * @author michael
 *
 */
class JPopupTextField extends JTextField {

    public JPopupTextField() {
    	new CopyAndPastePopupBuilder().addPopupMenu(this);
    }

    public JPopupTextField(String defaultText) {
    	this();
        this.setText(defaultText);
    }
}
