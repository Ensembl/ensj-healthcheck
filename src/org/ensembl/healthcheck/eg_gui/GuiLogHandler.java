package org.ensembl.healthcheck.eg_gui;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.ensembl.healthcheck.ReportLine;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Reporter;
import org.ensembl.healthcheck.testcase.EnsTestCase;

import com.mysql.jdbc.Connection;

/**
 * <p>
 * 	A handler for the logging system. If added to a logger, it can forward
 * any logging requests made to a reporter.
 * </p>
 * 
 * @author michael
 *
 */
public class GuiLogHandler extends Handler {
	
	/**
	 * Messages sent to the logger will be associated with this testcase.
	 */
	protected EnsTestCase ensTestCase;
	
	/**
	 * Messages sent to the logger will be forwarded to this reporter. 
	 */
	protected Reporter reporter;
	
	public EnsTestCase getEnsTestCase() {
		return ensTestCase;
	}

	public void setEnsTestCase(EnsTestCase ensTestCase) {
		this.ensTestCase = ensTestCase;
	}

	public Reporter getReporter() {
		return reporter;
	}

	public void setReporter(Reporter reporter) {
		this.reporter = reporter;
	}
	
	public void publish(LogRecord logRecord) {
    	  
		reporter.message(
			new ReportLine(
				ensTestCase, 
				"", 
				ReportLine.LOG_MESSAGE,
				this.getFormatter().format(logRecord),
				ensTestCase.getTeamResponsible(), 
				ensTestCase.getSecondTeamResponsible()
			)
		);
	}

	@Override public void close() throws SecurityException {}
	@Override public void flush() {}

}
