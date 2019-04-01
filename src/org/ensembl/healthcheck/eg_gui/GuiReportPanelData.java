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

import org.ensembl.healthcheck.ReportLine;

class GuiReportPanelData {

	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTeamResponsible() {
		return teamResponsible;
	}

	public void setTeamResponsible(String teamResponsible) {
		this.teamResponsible = teamResponsible;
	}

	public String getSpeciesName() {
		return speciesName;
	}

	public void setSpeciesName(String speciesName) {
		this.speciesName = speciesName;
	}

	public String getMessage() {
		return message.toString();
	}

	public void setMessage(StringBuffer message) {
		this.message = message;
	}

	String testName;
	String description;
	String teamResponsible;
	String speciesName;
	
	StringBuffer message;
	
	boolean messageEndsWithCR;
	
	public GuiReportPanelData(ReportLine reportLine) {
		
		testName    = reportLine.getTestCase().getName();
		description = reportLine.getTestCase().getDescription();
		speciesName = reportLine.getSpeciesName();
		
		if (reportLine.getTeamResponsible()!=null) {
			teamResponsible = reportLine.getTeamResponsible().name();
		} else {
			teamResponsible = "No team set.";
		}
		messageEndsWithCR = false;
		
		message = new StringBuffer();
		
		addReportLine(reportLine);
	}
	
	public final void addReportLine(ReportLine reportLine) {
		
		String currentMessage = reportLine.getMessage();
		
		if (!messageEndsWithCR) {
			message.append("\n");
		}

		messageEndsWithCR = currentMessage.endsWith("\n");

		message.append(currentMessage);
	}
}

