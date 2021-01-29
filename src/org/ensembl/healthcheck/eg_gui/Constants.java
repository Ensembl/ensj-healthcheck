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

import java.awt.Color;
import java.util.logging.Level;

public class Constants {

	/**
	 * If nothing has been set explicitly use fine logging when running the
	 * healthchecks in the gui.
	 *  
	 */
	protected static Level defaultLogLevel = Level.FINE;
	
	public static final String RUN_ALL_TESTS          = "RUN_ALL_TESTS";
	public static final String RUN_SELECTED_TESTS     = "RUN_SELECTED_TESTS";
	public static final String REMOVE_SELECTED_TESTS  = "REMOVE_SELECTED_TESTS";

	public static final String OPEN_MYSQL_CLI  = "OPEN_MYSQL_CLI";
	
	public static final String DB_SERVER_CHANGED           = "DB_SERVER_CHANGED";
	public static final String SECONDARY_DB_SERVER_CHANGED = "SECONDARY_DB_SERVER_CHANGED";
	public static final String PAN_DB_SERVER_CHANGED       = "PAN_DB_SERVER_CHANGED";
	
	public static final String Add_to_tests_to_be_run = "Add to tests to be run";
	public static final String Execute                = "Execute";
	
	public static final String checkoutPerlDependenciesButton = "checkoutPerlDependenciesButton";
	
	public static final int DEFAULT_BUTTON_WIDTH  = 50;
	public static final int DEFAULT_BUTTON_HEIGHT = 20;
	
	public static final String TREE_ROOT_NODE_NAME = "All Groups";
	
	public static final String ALL_TESTS_GROUP_NAME = "All Tests";
	
	public static final int INITIAL_APPLICATION_WINDOW_WIDTH  = 1200;
	public static final int INITIAL_APPLICATION_WINDOW_HEIGHT =  800;
	
	public static final int DEFAULT_HORIZONTAL_COMPONENT_SPACING = 10;
	public static final int DEFAULT_VERTICAL_COMPONENT_SPACING   = 10;
	
	public static final Color COLOR_SUCCESS = new Color(0, 192, 0);
	public static final Color COLOR_FAILURE = new Color(192, 0, 0);
	
	public static final String selectedDatabaseChanged = "selectedDatabaseChanged";
	
}
