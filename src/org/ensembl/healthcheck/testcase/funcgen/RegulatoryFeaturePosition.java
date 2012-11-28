package org.ensembl.healthcheck.testcase.funcgen;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

public class RegulatoryFeaturePosition extends SingleDatabaseTestCase {

	
	/**
	 * Create a new instance
	 */
	public RegulatoryFeaturePosition() {
		addToGroup("post_regulatorybuild");
		addToGroup("funcgen");//do we need this group and the funcgen-release group?
		addToGroup("funcgen-release");
		
		//setHintLongRunning(true); // should be relatively fast
		
		setTeamResponsible(Team.FUNCGEN);


		setDescription("Checks if all motifs from annotated features are associated to their respective regulatory features.");
		setPriority(Priority.AMBER);
		//	setEffect("Regulatory Features will seem to miss some motif features.");
		//setFix("Re-project motif features or fix manually.");
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


		Connection efgCon = dbre.getConnection();

		String schemaBuild = dbre.getSchemaVersion() + "_" + dbre.getGeneBuildVersion();
		String coreDBName = dbre.getSpecies() + "_core_" + schemaBuild;
		DatabaseRegistryEntry coreDbre = getDatabaseRegistryEntryByPattern(coreDBName);
		
		if (coreDbre == null){
			ReportManager.problem(this, efgCon, "Could not access default core DB:\t" + coreDBName);
			return false;	
		}		
				

	
	

		//Get existing distinct seq_region_id and their limits from the core DB
		//This needs to be restricted to the schemaBuild otherwise we may be using the wrong seq_region
		//No need for schemaBuild restriction here due to reg_feat join
		//Could still have problems if we have archived sets on older assembly. Unlikely to happen
		//Need to get name here so we can safely do sr join in feature count
		String sql = "select sr.core_seq_region_id, sr.name, sr.seq_region_id from seq_region sr, regulatory_feature rf where rf.seq_region_id=sr.seq_region_id group by sr.name";
		HashMap<String, String> coreSeqRegionIDName    = new HashMap<String, String>(); 
		HashMap<String, String> nameFuncgenSeqRegionID = new HashMap<String, String>(); 
		
 
		try {
			ResultSet rs = efgCon.createStatement().executeQuery(sql);

			while (rs.next()){
				coreSeqRegionIDName.put(rs.getString(1), rs.getString(2));
				nameFuncgenSeqRegionID.put(rs.getString(2), rs.getString(3));
			}
		}
		catch (SQLException se) {	
			se.printStackTrace();
			return false;
		}
		
					Connection coreCon = coreDbre.getConnection();
		HashMap<String, String> seqRegionLen      = new HashMap<String, String>(); 
			
		for (Iterator<String> iter = coreSeqRegionIDName.keySet().iterator(); iter.hasNext();) {
			String coreSrID = (String) iter.next();
			seqRegionLen.put(coreSrID, DBUtils.getRowColumnValue(coreCon, "select length from seq_region where seq_region_id=" + coreSrID) );
		}
		
		// Check if reg feat start/ends are beyond seq_region start/ends
		Iterator<String> it = seqRegionLen.keySet().iterator();
	
		while(it.hasNext()){
			String coreRegionId    = it.next();
			String srName          = coreSeqRegionIDName.get(coreRegionId);
			String funcgenRegionID = nameFuncgenSeqRegionID.get(srName);
			String srLength        = seqRegionLen.get(coreRegionId);
			//Using efg sr_id removes need for use of schema_build and sr join

			sql = "select count(regulatory_feature_id) from regulatory_feature " + 
				"WHERE seq_region_id=" + funcgenRegionID + 
				" AND  ((seq_region_start <= 0) OR ( (seq_region_start - bound_start_length) <= 0) " +
				"OR ( (seq_region_end + bound_end_length) > " + srLength + ") OR (seq_region_end > " + srLength + "))";
			//< 0 should never happen as it is an insigned field!
   
			Integer featCount = DBUtils.getRowCount(efgCon, sql); 	
			//This is already being 'caught' higher in the stack, but no exit
			//but still shows as 'PASSED' as result is true by default!
			//featCount is -1 not null if sql failed

			if(featCount == -1){
				ReportManager.problem(this, efgCon, "SQL Failed:\t" + sql);
				return false;
			}
			
			if(featCount > 0){
				//String updateSQL = "UPDATE regulatory_feature set bound_seq_region_end=" + srLength +  
				//	" WHERE seq_region_id=" + funcgenRegionID + 
				//	" AND bound_seq_region_end > " + srLength + ";\n" +
				//	"UPDATE regulatory_feature set seq_region_end=" + srLength +  
				//	" WHERE seq_region_id=" + funcgenRegionID + 
				//	" AND seq_region_end > " + srLength + ";\n";


				String deleteSQL = "\nDELETE ra, rf from regulatory_feature rf join regulatory_attribute ra using (regulatory_feature_id) WHERE seq_region_id=" + funcgenRegionID + 
					" AND  ((seq_region_start <= 0) OR ( (seq_region_start - bound_start_length)  <= 0) " +
					"OR ( (seq_region_end + bound_end_length)  > " + srLength + ") OR (seq_region_end > " + srLength + "))";

					String usefulSQL = sql + deleteSQL;

				//Omit start <0 for now as it should never happen
			
				//Will executing the fix screw the API as the underlying feats have not been patched?
				//Should we also test for start>end?
				//Probably don't need to do this? Just fix in pipeline?

				ReportManager.problem(this, efgCon, "SeqRegion " + srName + " has " + featCount + 
									  " features exceeding seq_region bounds\nUSEFUL SQL:\n" + usefulSQL);
				result =  false;
			}
		}
		
		return result;
	}
	

}
