package org.ensembl.healthcheck.eg_gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.border.Border;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class TestProgressDialog extends JPanel implements ActionListener {

    private JProgressBar progressBar;
    private JLabel       noteLabel;
    private JButton      cancelButton;
    
    /**
     * If this is set, then the thread will be stopped using the interrupt
     * method, when this window closes.
     */
    protected Thread Runner;

	@Override
	public void actionPerformed(ActionEvent arg0) {

		if (arg0.getSource() == cancelButton) {
		
			if (Runner != null) {
				Runner.interrupt();
				cancelButton.setEnabled(false);
				noteLabel.setText("Test is being cancelled.");
			}
		}
	}

    public Thread getRunner() {
		return Runner;
	}

	public void setRunner(Thread runner) {
		Runner = runner;
	}

	public TestProgressDialog(String note, int min, int max) {

		Border defaultEmptyBorder = GuiTestRunnerFrameComponentBuilder.defaultEmptyBorder;
		
		this.setBorder(BorderFactory.createTitledBorder(defaultEmptyBorder, "Progress"));		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        noteLabel    = new JLabel(note);
        progressBar  = new JProgressBar(min, max);
        cancelButton = new JButton("Cancel");
        
        cancelButton.addActionListener(this);

        this.add(noteLabel);
        this.add(Box.createVerticalStrut(Constants.DEFAULT_VERTICAL_COMPONENT_SPACING));
        this.add(progressBar);
        this.add(Box.createVerticalStrut(Constants.DEFAULT_VERTICAL_COMPONENT_SPACING));
        this.add(cancelButton);

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
