/**
 * File: EgMetaTest.java
 * Created by: dstaines
 * Created on: May 26, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.SqlTemplate;
import org.ensembl.healthcheck.util.TestCaseUtils;
import org.ensembl.healthcheck.util.ConnectionBasedSqlTemplateImpl.SqlTemplateUncheckedException;

/**
 * Check to see if sample IDs have been added
 * @author dstaines
 * 
 */
public class SampleSetting extends AbstractEgCoreTestCase {

	private static final String GENE_ID = "SELECT gene_id FROM gene_stable_id WHERE stable_id=?";

	private static final String TRANSCRIPT_ID = "SELECT transcript_id FROM transcript_stable_id WHERE stable_id=?";

	private final static String META_QUERY = "select meta_key,meta_value from meta where species_id=? and meta_key like 'sample.%'";

	private static final String LOC_LEN = "select length from seq_region sr join coord_system cs using (coord_system_id) where cs.species_id=? and sr.name=?";

	private static Pattern locationPattern = Pattern
			.compile("([^:]+):(\\d+)-(\\d+)");

	private final String[] expectedKeys = { "sample.gene_text",
			"sample.gene_param", "sample.transcript_text",
			"sample.transcript_param", "sample.location_text",
			"sample.location_param", "sample.search_text" };

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;

		SqlTemplate template = getTemplate(dbre);

		for (int speciesId : dbre.getSpeciesIds()) {
			// 1. get map of samples
			Map<String, String> sampleKeys = template.queryForMap(META_QUERY,
					TestCaseUtils.singleValueMapper, speciesId);
			// 2. check expected keys
			for (String key : expectedKeys) {
				String val = sampleKeys.get(key);
				if (StringUtils.isEmpty(val)) {
					passes = false;
					ReportManager.problem(this, dbre.getConnection(), key
							+ " not found for species " + speciesId);
				} else {
					ReportManager
							.correct(this, dbre.getConnection(), key
									+ " set to " + val + " for species ID "
									+ speciesId);
				}
			}
			// 3. check that params exist
			// 3a. gene_param vs gene_stable_id
			String geneText = sampleKeys.get("sample.gene_param");
			if (!StringUtils.isEmpty(geneText)) {
				try {
					int geneId = template.queryForDefaultObject(GENE_ID,
							Integer.class, geneText);
					if (geneId == 0) {
						passes = false;
						ReportManager
								.problem(this, dbre.getConnection(),
										"Sample gene with ID " + geneText
												+ " not found for species "
												+ speciesId);
					}
				} catch (SqlTemplateUncheckedException e) {
					passes = false;
					ReportManager.problem(this, dbre.getConnection(),
							"Sample gene with ID " + geneText
									+ " not found for species " + speciesId);
				}
			}
			// 3b. transcript_param
			String transcriptText = sampleKeys.get("sample.transcript_param");
			if (!StringUtils.isEmpty(transcriptText)) {
				try {
					int transcriptId = template.queryForDefaultObject(
							TRANSCRIPT_ID, Integer.class, transcriptText);
					if (transcriptId == 0) {
						passes = false;
						ReportManager
								.problem(this, dbre.getConnection(),
										"Sample transcript with ID "
												+ transcriptText
												+ " not found for species "
												+ speciesId);
					}
				} catch (SqlTemplateUncheckedException e) {
					passes = false;
					ReportManager.problem(this, dbre.getConnection(),
							"Sample transcript with ID " + transcriptText
									+ " not found for species " + speciesId);
				}
			}
			// 3c. location_param
			String locationText = sampleKeys.get("sample.location_param");
			if (!StringUtils.isEmpty(locationText)) {
				Matcher m = locationPattern.matcher(locationText);
				if (m.matches()) {
					String loc = m.group(1);
					int start = Integer.parseInt(m.group(2));
					int end = Integer.parseInt(m.group(3));
					try {
						int length = template.queryForDefaultObject(LOC_LEN,
								Integer.class, speciesId, loc);
						if (start > length || end > length || start > end) {
							passes = false;
							ReportManager.problem(this, dbre.getConnection(),
									"Sample location " + start + "-" + end
											+ " not valid for location " + loc
											+ " of length " + length
											+ " for species " + speciesId);
						}
					} catch (SqlTemplateUncheckedException e) {
						passes = false;
						ReportManager
								.problem(this, dbre.getConnection(),
										"Seq region named " + loc
												+ " not found for species "
												+ speciesId);
					}
				} else {
					passes = false;
					ReportManager.problem(this, dbre.getConnection(),
							"Location text " + locationText
									+ " not in expected form for " + speciesId);
				}
			}
		}
		return passes;
	}

}
