package org.ensembl.healthcheck.eg_gui;

public class SetupTabBuildDirector {
	
	public SetupTab construct(SetupTabBuilder builder) {
		
		builder.buildAllTestsList();
		builder.buildTestGroupList();
		builder.buildAllTests();
		builder.buildTestInstantiator();
		builder.buildListOfTestsToBeExecutedPopupMenu();
		builder.buildListOfTestsToBeRunArea();
		builder.buildTreeOfTestGroups();
		builder.buildDbDetails();
		builder.buildDbServerSelector();
		builder.buildDatabaseTabbedPaneWithSearchBox();
		builder.buildMysqlConnectionCmd();
		builder.buildSecondaryDbServerSelector();
		builder.buildComponentWiring();
		
		return builder.getResult();
	}
}
