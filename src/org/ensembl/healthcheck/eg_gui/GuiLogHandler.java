/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
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

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.ensembl.healthcheck.ReportLine;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Reporter;
import org.ensembl.healthcheck.testcase.EnsTestCase;

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
