package org.ensembl.healthcheck.testcase.eg_core;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;

public class RepeatAnalysesInMeta extends AbstractEgCoreTestCase {

	@Override
	protected String getEgDescription() {
		return "Make sure repeat.analysis has been set, if repeat analyses are present in the analysis table";
	}

	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		
		boolean passes = true;
		
		List<Integer> repeatAnalysisIds = getTemplate(dbre).queryForDefaultObjectList(
				"select distinct analysis_id from repeat_feature;", Integer.class
		);
		
		if (repeatAnalysisIds.size()==0) {
			// Having no repeat features is also a bug, but this shall be
			// tested elsewhere.
			//
			logger.info("No repeat features found, skipping.");
			return true;
		}
		
		List<String> repeatLogicNames = getTemplate(dbre).queryForDefaultObjectList(
				"select logic_name from analysis where analysis_id in ("+StringUtils.join(repeatAnalysisIds, ',')+");", String.class
		);
		
		ReportManager.correct(this, dbre.getConnection(), repeatLogicNames.toString());

		List<Integer> speciesIds = getTemplate(dbre).queryForDefaultObjectList(
				"select distinct species_id from meta where species_id is not null;", Integer.class
		);

		for (int speciesId : speciesIds) {
			for (String repeatLogicName : repeatLogicNames) {
				
				List<Integer> meta_id = getTemplate(dbre).queryForDefaultObjectList(
						
						// meta_value is a binary field, so not case 
						// insensitive by default. We want case insensitivity
						// here. Wrong casing should be checked elsewhere, if
						// need be.
						//
						"select meta_id from meta where lower(meta_value)=lower('"+repeatLogicName+"') and meta_key=\"repeat.analysis\" and species_id="+speciesId+";",
						Integer.class
				);
				
				boolean foundForSpecies = meta_id.size()==1;
				
				if (foundForSpecies) {					
					logger.info(
						"ok: " + repeatLogicName + " declared in meta for species " + speciesId
					);
				} else {
					
					passes = false;
					
					if (meta_id.size()==0) {
						
						ReportManager.problem(
							this, 
							dbre.getConnection(), 
							"not ok: " + repeatLogicName + " is a logic name for repeats, but has not been declared in meta for species " + speciesId
							+ "\n\nFix this by running\n\n"
							+ "insert into meta (species_id, meta_key, meta_value) values ("+speciesId+", \"repeat.analysis\", \""+repeatLogicName+"\");"
							+ "\n\non your database."
						);
						
					} else {

						ReportManager.problem(
								this, 
								dbre.getConnection(), 
								"not ok: " + repeatLogicName + " is present multiple times in meta. Meta ids: " + StringUtils.join(meta_id, ',')
						);
						
					}
				}
			}
		}
		
		return passes;
	}

}
