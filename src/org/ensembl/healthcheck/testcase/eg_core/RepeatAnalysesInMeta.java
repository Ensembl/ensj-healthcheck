package org.ensembl.healthcheck.testcase.eg_core;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * @author mnuhn
 * 
 * <p>
 * 	Checks that repeat.analysis keys have been set in the meta table.
 * </p>
 * <p>
 * 	This test will fail, if
 * </p>
 * <ul>
 *     <li>
 *         Any of the analyses in getRepeatAnalysesThatShouldBeInMetaIfRun 
 *         has been run, but not been declared in meta for all the species in
 *         the database or
 *     </li>
 *     <li>
 *         No repeat.analyses entries have been set in the meta table for a 
 *         species. 
 *     </li>
 * </ul>
 * 
 */
public class RepeatAnalysesInMeta extends AbstractEgCoreTestCase {

	@Override
	protected String getEgDescription() {
		return "Make sure repeat.analysis has been set, if repeat analyses are present in the analysis table";
	}

	/**
	 * <p>
	 * 	The list of repeat analyses that we expect to see in meta, if they
	 * have been run.
	 * </p>
	 */
	protected List<String> getRepeatAnalysesThatShouldBeInMetaIfRun() {
		
		List<String> repeatAnalysesThatShouldBeInMetaIfRun = new ArrayList<String>();
		
		repeatAnalysesThatShouldBeInMetaIfRun.add("dust");
		repeatAnalysesThatShouldBeInMetaIfRun.add("repeatmask");
		repeatAnalysesThatShouldBeInMetaIfRun.add("trf");	
		
		return repeatAnalysesThatShouldBeInMetaIfRun;
	}
	
	
	protected DatabaseRegistryEntry dbre;
	protected SqlTemplate           sqlTemplate;
	protected List<Integer>         repeatAnalysisIds;
	protected List<String>          repeatLogicNames;
	protected List<Integer>         speciesIds;
	
	/**
	 * <p>
	 * 	Initialise attributes of this object. Ideally this would be done in 
	 * the constructor, but the DatabaseRegistryEntry is needed for this and
	 * the dbre is only available when the test is run, so this must be called
	 * first.
	 * </p>
	 * 
	 * @param dbre
	 */
	protected void init(DatabaseRegistryEntry dbre) {
		
		this.sqlTemplate = getTemplate(dbre);		
		this.dbre = dbre;
		
		this.repeatAnalysisIds = sqlTemplate.queryForDefaultObjectList(
				"select distinct analysis_id from repeat_feature;", Integer.class
		);
		
		if (repeatAnalysisIds.size()>0) {
			this.repeatLogicNames = sqlTemplate.queryForDefaultObjectList(
					"select logic_name from analysis where analysis_id in ("+StringUtils.join(repeatAnalysisIds, ',')+");", String.class
			);
		} else {
			this.repeatLogicNames = new ArrayList<String>();
		}
		
		this.speciesIds = sqlTemplate.queryForDefaultObjectList(
				"select distinct species_id from meta where species_id is not null;", Integer.class
		);

	}

	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		
		init(dbre);
		
		boolean passes = true;		
		
		if (repeatAnalysisIds.size()==0) {
			// Having no repeat features is also a bug, but this shall be
			// tested elsewhere.
			//
			logger.info("No repeat features found, skipping.");
			return passes;
		}
		
		logger.info("The following repeat logic names are in the analysis table: " + repeatLogicNames.toString());
		
		for (int speciesId : speciesIds) {			
			passes = checkMetaEntriesForSpecies(speciesId);
			
			if (!passes) {
				return passes;
			}
		}
		
		return passes;
	}

	/**
	 * <p>
	 * 	For a given species id, checks that the meta table has the necessary
	 * repeat.analysis entries for all the repeatLogicNames in the meta table.
	 * </p>
	 * 
	 * @param speciesId
	 * @return
	 */
	private boolean checkMetaEntriesForSpecies(int speciesId) {
		
		boolean currentSpeciesPasses = true;
		
		List<String> repeatAnalysesThatShouldBeInMetaIfRun = getRepeatAnalysesThatShouldBeInMetaIfRun();
		
		boolean noValidRepeatAnalysisInMetaFoundForThisSpecies = true;
		
		for (String repeatLogicName : repeatLogicNames) {
			
			List<Integer> meta_id = sqlTemplate.queryForDefaultObjectList(
					
					// meta_value is a binary field, so not case 
					// insensitive by default. We want case insensitivity
					// here. Wrong casing should be checked elsewhere, if
					// need be.
					//
					"select meta_id from meta where lower(meta_value)=lower('"+repeatLogicName+"') and meta_key=\"repeat.analysis\" and species_id="+speciesId+";",
					Integer.class
			);
			
			boolean foundOneEntryForCurrentSpecies = meta_id.size()==1;
			
			if (foundOneEntryForCurrentSpecies) {
				
				logger.info(
					"ok: " + repeatLogicName + " declared in meta for species " + speciesId
				);
				noValidRepeatAnalysisInMetaFoundForThisSpecies = false;
				
			} else {
				
				boolean missingAnalysisIsAProblem = repeatAnalysesThatShouldBeInMetaIfRun.contains(repeatLogicName);					
				boolean foundNoEntryForCurrentSpecies = meta_id.size() == 0;
				
				if (foundNoEntryForCurrentSpecies && missingAnalysisIsAProblem) {						
					currentSpeciesPasses = false;						
					ReportManager.problem(
						this, 
						dbre.getConnection(), 
						"not ok: " + repeatLogicName + " is a logic name for repeats, but has not been declared in meta for species " + speciesId
						+ "\n\nFix this by running\n\n"
						+ "insert into meta (species_id, meta_key, meta_value) values ("+speciesId+", \"repeat.analysis\", \""+repeatLogicName+"\");"
						+ "\n\non your database."
					);						
				}
				
				boolean foundMultipleEntriesForCurrentSpecies = meta_id.size()>1;
				
				if (foundMultipleEntriesForCurrentSpecies) {
					currentSpeciesPasses = false;						
					ReportManager.problem(
						this, 
						dbre.getConnection(), 
						"not ok: " + repeatLogicName + " is present multiple times in meta. Meta ids: " + StringUtils.join(meta_id, ',')
					);						
				}
			}
		}
		
		if (noValidRepeatAnalysisInMetaFoundForThisSpecies) {

			currentSpeciesPasses = false;			
			ReportManager.problem(
					this, 
					dbre.getConnection(), 
					"not ok: No valid 'repeat.analysis' key found in meta!"
			);
		}
		return currentSpeciesPasses;
	}
}
