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
		builder.buildPrimaryDbServerSelector();
		builder.buildSecondDbServerSelector();		
		builder.buildSecondaryDbServerSelector();
		builder.buildDatabaseTabbedPaneWithSearchBox();
		builder.buildMysqlConnectionWidget();
		builder.buildComponentWiring();
		
		return builder.getResult();
	}
}
