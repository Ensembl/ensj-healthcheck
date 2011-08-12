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
	
	public GuiReportPanelData(ReportLine reportLine) {
		
		testName    = reportLine.getTestCase().getName();
		description = reportLine.getTestCase().getDescription();
		speciesName = reportLine.getSpeciesName();
		
		if (reportLine.getTeamResponsible()!=null) {			
			teamResponsible = reportLine.getTeamResponsible().name();			
		} else {			
			teamResponsible = "No team set.";
		}
		message = new StringBuffer(reportLine.getMessage());
	}
	
	public final void addReportLine(ReportLine reportLine) {
		
		message.append("\n" + reportLine.getMessage());
	}
}

