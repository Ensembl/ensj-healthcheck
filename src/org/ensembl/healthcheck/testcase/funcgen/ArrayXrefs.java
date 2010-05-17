package org.ensembl.healthcheck.testcase.funcgen;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check Array xrefs: - that each chromosome has at least 1 Probe/Set xref
 * 
 * Assumptions: Array Probe/ProbeSet xrefs and transcripts are both in the default chromosome coordinate system.
 * 
 */
public class ArrayXrefs extends SingleDatabaseTestCase {

	// if a database has more than this number of seq_regions in the chromosome coordinate system, it's ignored
	private static final int MAX_CHROMOSOMES = 75;

	/**
	 * Creates a new instance of OligoXrefs
	 */
	public ArrayXrefs() {

		addToGroup("post_genebuild");
		addToGroup("release");
		addToGroup("funcgen");
		addToGroup("funcgen-release");

		setDescription("Check Array probe2transcript xrefs");
		setHintLongRunning(true);

	}

	public void types() {
		removeAppliesToType(DatabaseType.CORE);
		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.VEGA);
	}

	/**
	 * Check all chromosomes have xrefs for each DISPLAYABLE array.
	 * 
	 * Get a list of chromosomes, then check the number of xrefs associated with each one. Fail is any chromosome has 0 xrefs. 
	 * Is this even possible now we have transcripts in a separate DB? 
	 * This is really essential for new arrays, as we can't rely on ComparePrevious
	 * Could do this via cross DB query only if on same server
	 *
	 * @param dbre
	 *          The database to use.
	 * @return true if the test passed.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;
		Connection efgCon = dbre.getConnection();

	
		// Check if there are any DISPLAYABLE Arrays - if so there should be  Xrefs
		// Checks EPXRESSION and CGH arrays only
		
		
//		Integer displayableArrays = getRowCount(efgCon, "SELECT COUNT(*) FROM array a, status s, status_name sn where sn.name='DISPLAYABLE' and " +
//														"sn.status_name_id=s.status_name_id and s.table_name='array' and s.table_id=a.array_id");
	
		Integer expressionArrays = getRowCount(efgCon, "SELECT COUNT(*) FROM array a where (format='EXPRESSION' OR format='CGH')");
		
		if ( expressionArrays == 0) { //Assume we should always have EXPRESSION arrays
			ReportManager.problem(this, efgCon, DBUtils.getShortDatabaseName(efgCon) + 
								  " has no EXPRESSION Arrays, not checking for probe2transcript xrefs");
			return false;
		}
		
		
		
		//	if ( displayableArrays < expressionArrays ){
		//		ReportManager.problem(this, efgCon, "Database contains non-DISPLAYABLE EXPRESSION Arrays");
		//		result = false;
		//	}
			
	
		StringBuffer hiddenArrays = new StringBuffer();

		try {
			ResultSet rs = efgCon.createStatement().executeQuery("SELECT a.name, sn1.name from array a left join " + 
					"(SELECT s.table_id, sn.name from status s, status_name sn where sn.name='DISPLAYABLE' and " +
					"sn.status_name_id=s.status_name_id and s.table_name='array') sn1 on sn1.table_id=a.array_id " +
					"WHERE (a.format ='EXPRESSION' OR a.format='CGH')");
					
			while (rs.next()){
				String arrayStatus = rs.getString(2);
				String arrayName   = rs.getString(1); 
				
				if (arrayStatus == null)	hiddenArrays.append(arrayName + " ");
			}
		
			rs.close();
			
			if(hiddenArrays.length() != 0){
				result = false;
				ReportManager.problem(this, efgCon, "Database contains non-DISPLAYABLE EXPRESSION Arrays:\t" + hiddenArrays);			
			}
			
		} catch (SQLException se) {	
			se.printStackTrace();
			return false;
		}
	
				
		//Get the matching core dbre to do the cross DB join
		//Assume we have the standard name for the core DB and it is on the same host
		
		String schemaBuild = dbre.getSchemaVersion() + "_" + dbre.getGeneBuildVersion();
		String coreDBName = dbre.getSpecies() + "_core_" + schemaBuild;
		//Never get's loaded if we specify a pattern
		//DatabaseRegistryEntry coreDbre = dbre.getDatabaseRegistry().getByExactName(coreDBName);
		DatabaseRegistryEntry coreDbre = getDatabaseRegistryEntryByPattern(coreDBName);
		
		
		if (coreDbre == null){
			ReportManager.problem(this, efgCon, "Could not access default core DB:\t" + coreDBName);
			return false;	
		}
		
		//And test the hosts are the same
		//assume user/pass will be able to access DBs on the same server
		//do not test DatabaseServer object, it may be a different
		if (! coreDbre.getDatabaseServer().getDatabaseURL().equals(dbre.getDatabaseServer().getDatabaseURL())){
			ReportManager.problem(this, efgCon, "Unable to perform chromosome xref counts as efg and core DB are not on the same DatabaseServer:\t" +
								  "core " + coreDbre.getDatabaseServer().getDatabaseURL() + "\tefg " + dbre.getDatabaseServer().getDatabaseURL());
			return false;	
		}
		

		// find all chromosomes in default assembly coordinate system
		// should really be parameterized
		Map<String, String> srID2name    = new HashMap<String, String>();
		Map<String, String> coreSrID2efg = new HashMap<String, String>();
			
		//Die if we don't see the current schema build and is the only one that is_current
		//Otherwise we cannot be sure that all seq_region records have been updated
		String[] currentSchemaBuilds = getColumnValues(efgCon,  "SELECT schema_build FROM coord_system where name='chromosome' and is_current=1");
		
		if ((currentSchemaBuilds.length != 1) ||
			(! currentSchemaBuilds[0].equals(schemaBuild))){
			
			ReportManager.problem(this, efgCon, "perform chromosome xref counts as efg and core DB are not on the same DatabaseServer:\t" +
								  "core " + coreDbre.getDatabaseServer().getDatabaseURL() + "\tefg " + dbre.getDatabaseServer().getDatabaseURL());
			return false;	
		}
		
		
		try {
			ResultSet rs = efgCon.createStatement().executeQuery("SELECT s.seq_region_id, s.name, s.core_seq_region_id FROM seq_region s, coord_system cs " +
					"WHERE cs.coord_system_id=s.coord_system_id AND cs.name='chromosome' and cs.attrib='default_version' and " +
							"cs.schema_build='" + schemaBuild + "' group by s.seq_region_id");
			

			//Do we even need this core_seq_region_id translation?
			//Just link via the sr.name!
	
			while (rs.next()){
				srID2name.put(rs.getString(1), rs.getString(2));
				coreSrID2efg.put(rs.getString(3), rs.getString(1));	
			}
			
			rs.close();

			if (srID2name.size() > MAX_CHROMOSOMES) {
				ReportManager.problem(this, efgCon, "Database has more than " + MAX_CHROMOSOMES + " seq_regions in 'chromosome' coordinate system (actually " + srID2name.size() + ") - test skipped");
				return false;
			}

			// Count the number of xrefs for each chr
			Map<String, String> coreSrIDcounts = new HashMap<String, String>();
			// (Optimisation: faster to use "in list" of external_db_ids than SQL
			// join.)
			StringBuffer inList   = new StringBuffer();
			String       edbName = dbre.getSpecies() + "_core_Transcript";

				//We really need to match the genebuild between the edb and the schema_build
			//otherwise we have out of date data?
			//Not enirely true, altho a genebuild increment should mean that the xrefs needs redoing
			//There should only be 1 edb ID, but we have duplicates at the moment, so handle this
			String[] exdbIDs = getColumnValues(efgCon, "select external_db_id from external_db where db_name='" + edbName 
											   + "' and db_release='" + schemaBuild + "'");

			for (int i = 0; i < exdbIDs.length; i++) {
				if (i > 0)
					inList.append(",");
				inList.append(exdbIDs[i]);
			}



			//We need to restrict this to the core_coord_system_ids for the specific DB
			//other wise we may get odd counts where core_coord_system_ids have changed between releases on the same assembly

			//Need to do this for Probe and ProbeSet arrays
	
			
			ResultSet xrefCounts = efgCon
					.createStatement()
					.executeQuery(
							"select t.seq_region_id, count(*) as count  from " + coreDBName + ".transcript t," + 
							coreDBName + ".transcript_stable_id ts," + " object_xref ox, xref x " +
							"where t.transcript_id=ts.transcript_id and ts.stable_id=x.dbprimary_acc " +
							"and ox.ensembl_object_type='ProbeSet' and ox.xref_id=x.xref_id and " + 
							"x.external_db_id in ("	+ inList + ") GROUP BY t.seq_region_id");
			
			while (xrefCounts.next())
				coreSrIDcounts.put(xrefCounts.getString(1), xrefCounts.getString(2));
			rs.close();

			
			//Need to check we actually have some results here	
			
			// check every chr has >0 xrefs.
			for (Iterator<String> iter = coreSrIDcounts.keySet().iterator(); iter.hasNext();) {
				String coreSrID = (String) iter.next();
				String efgSrID  = (String) coreSrID2efg.get(coreSrID);
				String name = (String) srID2name.get(efgSrID);
				//System.out.println("core " + coreSrID + " efg " + efgSrID + " name " + name);	
				
				//Skip nulls as these won't be chromosomes
				//But may have xrefs
				if ( name != null ){
					String label = name + " (seq_region_id=" + efgSrID + ")";
					long count = coreSrIDcounts.containsKey(coreSrID) ? Long.parseLong(coreSrIDcounts.get(coreSrID).toString()) : 0;
					
					//System.out.println(label + " " + count);
					
					if (count > 0) {
						ReportManager.correct(this, efgCon, "Chromosome " + label + " has " + coreSrIDcounts.get(coreSrID) + " associated array xrefs.");
					} else {
						ReportManager.problem(this, efgCon, "Chromosome " + label + " has no associated array xrefs.");
						result = false;
					}
				}	
			}

		} catch (SQLException se) {
			se.printStackTrace();
			result = false;
		}
		
		return result;

	} // run

} // ArrayXrefs
	