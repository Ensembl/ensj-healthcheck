package org.ensembl.healthcheck.testcase.funcgen;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

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


		int regMFs = DBUtils.getRowCount(con, "SELECT COUNT(distinct attribute_feature_id) from regulatory_attribute where attribute_feature_table='motif'");

		//Nested join now accounts for focus_max_length
		int fmaxLength = 2000;  // Should add this as attribute to FuncgenSingleDatabaseTestCase?
    
    //This first test needs tweaking to take account of out of bounds?
    //Don't we already do that with fmaxLength

		int regAMFs = DBUtils.getRowCount
			( con, "select count(distinct amf.motif_feature_id) from " + 
			  "associated_motif_feature amf, annotated_feature af, regulatory_attribute ra " + 
			  "where af.annotated_feature_id=amf.annotated_feature_id and " + 
			  "ra.attribute_feature_id=af.annotated_feature_id and " + 
			  "ra.attribute_feature_table='annotated' and " + 
			  "(af.seq_region_end - af.seq_region_start +1) <= " + fmaxLength);

		
		if(regMFs != regAMFs){
        // This incorporates mfs for afs < 2000bp where the af has been integrated into the rf, but the mf hasn't for some reason?

        ReportManager.problem
				( this,  con, "The number of motif features associated to regulatory features (" + regMFs +
				  ") does not correspond to the number of motif features within its associated annotated features (" 
				  + regAMFs + ") which are less than " + fmaxLength + " bp\n" +
				  "USEFUL SQL:\nALTER table regulatory_attribute add index `attribute_id_type`(attribute_feature_table, attribute_feature_id);\n" +
				  "insert ignore into regulatory_attribute select ra.regulatory_feature_id, amf.motif_feature_id, 'motif' from " + 
				  "annotated_feature af, regulatory_attribute ra, associated_motif_feature amf left join " + 
				  "regulatory_attribute ra1 on (amf.motif_feature_id=ra1.attribute_feature_id and ra1.attribute_feature_table='motif') " + 
				  "where af.annotated_feature_id=amf.annotated_feature_id and ra.attribute_feature_id=af.annotated_feature_id and " + 
				  "ra.attribute_feature_table='annotated' and (af.seq_region_end - af.seq_region_start +1) <= " + 
				  fmaxLength + " and ra1.attribute_feature_id is NULL;\nALTER table regulatory_attribute drop index `attribute_id_type`;"
				  );
			//This now takes seconds with the new temporary index
			
			result = false;
		}


    //The above only catches MFs that have been missed or when we have deleted an af with but not an associated mf that were both supporting an rf

    
    //Need to add this one
    //Out of core region query.
		//select mf.motif_feature_id, mf.seq_region_start, mf.seq_region_end, rf.seq_region_start, rf.seq_region_end, rf.regulatory_feature_id from feature_set fs, regulatory_feature rf, regulatory_attribute ra, motif_feature mf where fs.name!='RegulatoryFeatures:MultiCell' and fs.feature_set_id=rf.feature_set_id and rf.regulatory_feature_id=ra.regulatory_feature_id and ra.attribute_feature_table='motif' and ra.attribute_feature_id=mf.motif_feature_id and (mf.seq_region_start<rf.seq_region_start OR mf.seq_region_end>rf.seq_region_end) 
    

		int outOfBoundMFs = DBUtils.getRowCount 
        (con, 
         "SELECT count(mf.motif_feature_id) FROM feature_set fs, regulatory_feature rf, regulatory_attribute ra, motif_feature mf " + 
         "WHERE fs.feature_set_id=rf.feature_set_id and rf.regulatory_feature_id=ra.regulatory_feature_id and ra.attribute_feature_table='motif'" + 
         " and ra.attribute_feature_id=mf.motif_feature_id and (mf.seq_region_start<rf.seq_region_start OR mf.seq_region_end>rf.seq_region_end)");

    if(outOfBoundMFs != 0){
        
        ReportManager.problem
            ( this,  con,
              "Found " + outOfBoundMFs + " MotifFeatures which lie outside the core region. USEFUL SQL:\n" +
              "SELECT mf.motif_feature_id, mf.seq_region_start, mf.seq_region_end, rf.regulatory_feature_id, rf.seq_region_start, rf.seq_region_end, concat(rf.stable_id, 11 0) " +
              "FROM feature_set fs, regulatory_feature rf, regulatory_attribute ra, motif_feature mf " + 
              "WHERE fs.feature_set_id=rf.feature_set_id and rf.regulatory_feature_id=ra.regulatory_feature_id and ra.attribute_feature_table='motif'" + 
              " and ra.attribute_feature_id=mf.motif_feature_id and (mf.seq_region_start<rf.seq_region_start OR mf.seq_region_end>rf.seq_region_end)");
              
              result = false;
    }
  
		return result;
	}
}
