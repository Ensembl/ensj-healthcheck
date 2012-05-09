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
		String jarFile = "lib/ensj-healthcheck.jar";
		
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


