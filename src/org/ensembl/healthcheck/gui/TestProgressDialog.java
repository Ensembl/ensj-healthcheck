package org.ensembl.healthcheck.gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class TestProgressDialog extends JDialog {

    private JProgressBar progressBar;

    private JLabel messageLabel;

    private JLabel noteLabel;
    
    /**
     * If this is set, then the thread will be stopped using the interrupt
     * method, when this window closes.
     */
    protected Thread Runner;

    protected void processWindowEvent(WindowEvent e) {
    	
    	// Interrupt the runner, when this window is closing.
    	//
        if(e.getID() == WindowEvent.WINDOW_CLOSING) {

        	if (Runner != null) {

        		Runner.interrupt();
        	}
        }
        super.processWindowEvent(e);
    }
    
    public Thread getRunner() {
		return Runner;
	}

	public void setRunner(Thread runner) {
		Runner = runner;
	}

	public TestProgressDialog(String message, String note, int min, int max) {

        setTitle("Healthcheck progress");

        setSize(new Dimension(300, 100));
        setBackground(Color.WHITE);

        JPanel progressPanel = new JPanel();
        progressPanel.setBackground(Color.WHITE);
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
        messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Dialog", Font.BOLD, 12));
        messageLabel.setBackground(Color.WHITE);
        noteLabel = new JLabel(note);
        noteLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        noteLabel.setBackground(Color.WHITE);

        progressBar = new JProgressBar(min, max);
        progressBar.setBackground(Color.WHITE);

        progressPanel.add(messageLabel);
        progressPanel.add(noteLabel);
        progressPanel.add(progressBar);

        Container contentPane = getContentPane();
        contentPane.setBackground(Color.WHITE);
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.add(progressPanel);

        //pack();

    }

	public void reset() {
		
		progressBar.setValue(progressBar.getMinimum());
		noteLabel.setText("");
	}
	
    public void setMaximum(int max) {

        progressBar.setMaximum(max);

    }

    public int getMaximum() {

        return progressBar.getMaximum();

    }

    public void setProgress(int p) {

        progressBar.setValue(p);

    }

    public void setNote(String s) {

        noteLabel.setText(s);

    }

    public void update(String s, int p) {

        setNote(s);
        setProgress(p);

    }
}
