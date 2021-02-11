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

import java.util.logging.Logger;
import javax.swing.JFrame;
import org.ensembl.healthcheck.TestRunner;

public class GuiStarter {
	
	protected static Logger logger = Logger.getLogger("HealthCheckLogger");

	public void run() {
		
		// Set configuration file to use for default values.
		//
		TestRunner.setPropertiesFile("database.defaults.properties");

		// The jar file in which tests and testgroups will be searched.
		//
		String jarFile = "target/dist/ensj-healthcheck.jar";
		
		GuiTestRunnerFrameBuilder builder = new GuiTestRunnerFrameBuilder(jarFile);
		
		JFrame frame = new GuiTestRunnerFrameBuildDirector().construct(builder);
		
		frame.setVisible(true);
	}

	public static void main(String argv[]) {
		
		GuiStarter t = new GuiStarter();
		t.run();
	}
}

class GuiTestRunnerFrameBuildDirector {
	
	public JFrame construct(GuiTestRunnerFrameBuilder builder) {
		
		builder.buildEmptyGuiTestRunnerFrame();
		builder.buildActionListener();
		builder.buildSetupTab();
		builder.buildOtherTabs();
		builder.buildFinalise();
		
		return builder.getResult();
	}
}


