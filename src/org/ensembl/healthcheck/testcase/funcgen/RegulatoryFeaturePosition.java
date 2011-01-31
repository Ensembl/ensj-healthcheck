package org.ensembl.healthcheck.testcase.funcgen;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

public class RegulatoryFeaturePosition extends SingleDatabaseTestCase {

	
	/**
	 * Create a new instance of StableID.
	 */
	public RegulatoryFeaturePosition() {
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


		Connection efgCon = dbre.getConnection();

		String schemaBuild = dbre.getSchemaVersion() + "_" + dbre.getGeneBuildVersion();
		String coreDBName = dbre.getSpecies() + "_core_" + schemaBuild;
		DatabaseRegistryEntry coreDbre = getDatabaseRegistryEntryByPattern(coreDBName);
		
		if (coreDbre == null){
			ReportManager.problem(this, efgCon, "Could not access default core DB:\t" + coreDBName);
			return false;	
		}		
				
		//Get existing distinct seq_region_id and their limits from the core DB
		String sql = "select distinct sr.core_seq_region_id from seq_region sr, regulatory_feature rf where rf.seq_region_id=sr.seq_region_id";
		String[] coreSeqRegionId = getColumnValues(efgCon,sql); 
		Connection coreCon = coreDbre.getConnection();
		
		HashMap<String, String> seqRegionLen = new HashMap<String, String>(); 
		for (int i = 0; i < coreSeqRegionId.length; i++){
			seqRegionLen.put(coreSeqRegionId[i], getRowColumnValue(coreCon, "select length from seq_region where seq_region_id="+coreSeqRegionId[i]));
		}
		
		//Now check, for each core seq region, if boundaries are passed
		Iterator<String> it = seqRegionLen.keySet().iterator();
		while(it.hasNext()){
			String coreRegionId = it.next();
			String kh = "select rf.regulatory_feature_id from regulatory_feature rf, seq_region sr where "+
			" sr.seq_region_id=rf.seq_region_id and sr.core_seq_region_id="+coreRegionId+" and "+
			" ((rf.seq_region_start <= 0) or (rf.seq_region_end > "+seqRegionLen.get(coreRegionId)+"))";
			if(getRowCount(efgCon,kh) > 0){
				ReportManager.problem(this, efgCon, "There are regulatory features that go over the limit of the genomic region where they are");
				return false;
			}
		}
		
		return result;
	}
	

}
