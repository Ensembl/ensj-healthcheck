package org.ensembl.healthcheck.testcase.funcgen;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
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
		setTeamResponsible(Team.FUNCGEN);

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

		//Nested join now accounts for focus_max_length
		int fmaxLength = 2000;  // Should add this as attribute to FuncgenSingleDatabaseTestCase?

		int regAMFs = getRowCount
			( con, "SELECT COUNT(distinct amf.motif_feature_id) from regulatory_attribute ra JOIN " +
			  "(associated_motif_feature amf JOIN annotated_feature af on af.annotated_feature_id=amf.annotated_feature_id " +
			  "AND (af.seq_region_end - af.seq_region_start +1) <= " + fmaxLength + ") " + 
			  "on amf.annotated_feature_id=ra.attribute_feature_id " + 
			  "where ra.attribute_feature_table='annotated'"
			 );


		
		if(regMFs != regAMFs){
			ReportManager.problem
				( this,  con, "The number of motif features associated to regulatory features (" + regMFs +
				  ") does not correspond to the number of motif features within its associated annotated features (" +regAMFs + ")\n" +
				  "USEFUL SQL:\tSELECT distinct(amf.motif_feature_id) from regulatory_attribute ra " +
				  "JOIN (associated_motif_feature amf JOIN annotated_feature af on af.annotated_feature_id=amf.annotated_feature_id " +
				  "AND (af.seq_region_end - af.seq_region_start +1) <= " + fmaxLength +
				  "LEFT JOIN regulatory_attribute ra1 on amf.motif_feature_id=ra1.attribute_feature_id AND attribute_feature_table='motif') " + 
				  "ON amf.annotated_feature_id=ra.attribute_feature_id " +
				  "where ra.attribute_feature_table='annotated' and ra1.attribute_feature_id is NULL"
				  );	
			//This SQL takes ~ 30-60 mins
			result = false;
		}
		
		
		return result;
	}
	

}
