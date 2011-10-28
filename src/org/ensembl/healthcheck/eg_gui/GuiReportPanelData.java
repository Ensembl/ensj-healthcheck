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
		
		// I have no idea, why there are two carriage returns after a line in  
		// the test report. It just makes no sense, they should not be there in
		// the first place.
		//
		return message.toString().replaceAll("\n\n", "\n");
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

