package org.ensembl.healthcheck.testcase.funcgen;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

public class RegulatoryMotifFeatures extends SingleDatabaseTestCase {

	
	/**
	 * Create a new instance of StableID.
	 */
	public RegulatoryMotifFeatures() {
		addToGroup("post_regulatorybuild");
		addToGroup("funcgen");//do we need this group and the funcgen-release group?
		addToGroup("funcgen-release");
		
		//setHintLongRunning(true); // should be relatively fast
		
		//Are there constants to identify teams?
		setTeamResponsible("funcgen");

		setDescription("Checks if all motifs from annotated features are associated to their respective regulatory features.");
		setPriority(Priority.AMBER);
		setEffect("Regulatory Features will seem to miss some motif features.");
		setFix("Re-project motif features or fix manually.");
	}
	
	
	/**
	 * This only applies to funcgen databases.
	 */
	public void types() {
		addAppliesToType(DatabaseType.FUNCGEN);
		
		//Do we really need these removes?
		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.CORE);
		removeAppliesToType(DatabaseType.VARIATION);
		removeAppliesToType(DatabaseType.COMPARA);

	}
	
	
	/**
	 * Run the test.
	 * We will check if all the motif features in a regulatory feature contain all
	 *  the motif features associated to the annotated features associated to the regulatory feature
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if the test passed.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		int regMFs = getRowCount(con, "SELECT COUNT(distinct attribute_feature_id) from regulatory_attribute where attribute_feature_table='motif'");
		int regAMFs = getRowCount(con, "SELECT COUNT(distinct motif_feature_id) from associated_motif_feature amf join regulatory_attribute ra on amf.annotated_feature_id=attribute_feature_id where ra.attribute_feature_table='annotated'");
		
		if(regMFs != regAMFs){
			ReportManager.problem(this, con, "The number of motif features associated to regulatory features ("+regMFs+
					") does not correspond to the number of motif features within its associated annotated features ("+regAMFs+")");	
			result = false;
		}
		
		
		return result;
	}
	

}
