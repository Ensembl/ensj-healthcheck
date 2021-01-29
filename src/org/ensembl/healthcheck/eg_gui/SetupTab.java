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

import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JTree;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.configuration.ConfigureHost;
import org.ensembl.healthcheck.testcase.EnsTestCase;

public class SetupTab extends JPanel {
	
	protected ActionListener actionListener;
	
	protected JPopupMenu listOfTestsToBeExecutedPopupMenu;
	protected TestInstantiatorDynamic testInstantiator;
	protected List<GroupOfTests> testGroupList;
	protected List<Class<EnsTestCase>> allTestsList;
	protected GroupOfTests allGroups;
	protected String jarFile;
	protected ConfigureHost primaryHostDetails;
	protected ConfigureHost secondPrimaryHostDetails;
	protected ConfigureHost secondaryHostDetails;
	
	protected List<ConfigureHost> dbDetails;

	/**
	 * Name of the server that will be set as default, if there is a 
	 * configuration for this.
	 */
	String defaultPrimaryServerName   = "staging-1";
	String defaultSecondaryServerName = "mysql.ebi.ac.uk";
	String defaultPanServerName       = "pan-prod";

	/**
	 * Directories in which configuration files for database servers will be 
	 * searched for.
	 * 
	 */
	String[] dirsWithDbServerConfigs = new String[] {

			// ~ does not work in java to reference the home directory:
			//
			// "~/.ensj",
			//
			System.getProperty("user.home") + "/.ensj"
	};

	// The gui widgets of this tab
	//
	protected JComboBox dbPrimaryServerSelector;
	protected JComboBox dbSecondPrimaryServerSelector;
	protected JComboBox dbSecondaryServerSelector;
	
	protected DatabaseTabbedPaneWithSearchBox databaseTabbedPaneWithSearchBox;

	protected JTree tree;
	protected JPanel testsPane;
	protected JList listOfTestsToBeRun;
	protected JButton rmSelectedTests;
	protected JButton runAllTests;
	protected JButton runSelectedTests;
	
	protected MySqlConnectionWidget mysqlWidget;

}
