package org.ensembl.healthcheck.eg_gui;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;

import java.awt.event.WindowEvent;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.ensembl.healthcheck.eg_gui.AdminTab;
import org.ensembl.healthcheck.eg_gui.Constants;
import org.ensembl.healthcheck.eg_gui.TestProgressDialog;

/**
 * <p>
 * 	The main window of the healthcheck GUI.
 * </p>
 * 
 * @author michael
 *
 */
public class GuiTestRunnerFrame extends JFrame {

	protected static Logger logger = Logger.getLogger("HealthCheckLogger");	
	protected String windowTitle = "Healthchecks";
	protected TestProgressDialog testProgressDialog;
	
	// The tabbed pane holding all the tabs of the gui.
	//
	protected JTabbedPane tabbedPane;

	// The tabs on the main window.
	//
	protected SetupTab    setupTab;
	protected int         setupTabIndex;
	protected String      setupTabName = "Setup";
	protected JPanel      resultTab;
	protected int         resultTabIndex;
	protected String      resultTabName = "Results";
	protected JPanel      legacyResultTab;
	protected int         legacyResultTabIndex;
	protected AdminTab    adminTab;
	protected int         adminTabIndex;

	protected Thread currentGuiTestRunnerThread;

	protected void processWindowEvent(WindowEvent e) {
		
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {

			// If a healthcheck session is currently running, terminate this.
			// Perl based healthchecks don't automatically terminate when the
			// window closes, so this is done here explicitly.
			//
			if (currentGuiTestRunnerThread != null) {
				currentGuiTestRunnerThread.interrupt();
			}
		}
		super.processWindowEvent(e);
	}

	public GuiTestRunnerFrame() {

		setTitle(windowTitle);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setPreferredSize(
			new Dimension(
				Constants.INITIAL_APPLICATION_WINDOW_WIDTH, 
				Constants.INITIAL_APPLICATION_WINDOW_HEIGHT
			)
		);
		pack();

		// The following stuff that positions the frame must come after the
		// this.pack() statement above otherwise it won't work as expected.
		
		// Center on screen
		//
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle frame = getBounds();
		setLocation(
				(screen.width  - frame.width)  / 2,
				(screen.height - frame.height) / 2
		);	
	}
}
