/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.ensembl.healthcheck.testcase.variation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that allele frequencies add up to 1
 */
public class AlleleFrequencies extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of Check Allele Frequencies
	 */
	public AlleleFrequencies() {
		setHintLongRunning(true);
		setDescription("Check that the allele frequencies add up to 1");
		setTeamResponsible(Team.VARIATION);
	}

	/**
	 * Check that all allele/genotype frequencies add up to 1 for the same variation/subsnp and sample.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return Result.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {
		boolean result = true;

		Connection con = dbre.getConnection();
		String[] tables = new String[] { "population_genotype", "allele" };

		// Set this flag to true if we want to count ALL failed frequencies and not just break as soon as we've found one
		boolean countAll = false;
		// Tolerance for the deviation from 1.0
		float tol = 0.025f;
		// Get the results in batches (determined by the variation_id)
		int chunk = 250000;

		// long mainStart = System.currentTimeMillis();
		try {

			Statement stmt = con.createStatement();

			// Get variations with allele/genotype frequencies that don't add up to 1 for the same variation_id, subsnp_id and population_id
			for (int i = 0; i < tables.length; i++) {
				// long subStart = System.currentTimeMillis();

				// Get the maximum variation id
				String sql = "SELECT MAX(s.variation_id) FROM " + tables[i] + " s";
				sql = DBUtils.getRowColumnValue(con, sql);
				if (sql == null || sql.isEmpty()) {
					sql = "0";
				}
				int maxId = Integer.parseInt(sql);

				// The query to get the data
				sql = "SELECT s.variation_id, s.subsnp_id, s.population_id, s.frequency FROM " + tables[i]
						+ " s USE INDEX (variation_idx,subsnp_idx) WHERE s.variation_id BETWEEN VIDLOWER AND VIDUPPER ORDER BY s.variation_id, s.subsnp_id, s.population_id";
				int offset = 1;

				// Count the number of failed
				int failed = 0;
				// Keep the failed entries
				ArrayList failedEntries = new ArrayList();

				// Loop until we've reached the maximum variation_id
				while (offset <= maxId) {

					// long s = System.currentTimeMillis();

					// Replace the offsets in the SQL query
					ResultSet rs = stmt.executeQuery(sql.replaceFirst("VIDLOWER", String.valueOf(offset)).replaceFirst("VIDUPPER", String.valueOf(offset + chunk)));

					// long e = System.currentTimeMillis();
					// System.out.println("Got " + String.valueOf(chunk) + " variations in " + String.valueOf(((e-s)/1000)) +
					// " seconds. Offset is " + String.valueOf(offset));

					// Increase the offset with the chunk size and add 1
					offset += chunk + 1;

					int lastVid = 0;
					int lastSSid = 0;
					int lastSid = 0;
					int curVid;
					int curSSid;
					int curSid;
					float freq;
					float sum = 1.f;
					int count = 0;

					while (rs.next()) {

						// Get the variation_id, subsnp_id, population_id and frequency. If any of these are NULL, they will be returned as 0
						curVid = rs.getInt(1);
						curSSid = rs.getInt(2);
						curSid = rs.getInt(3);
						freq = rs.getFloat(4);

						// If any of the values was NULL, skip processing the row. For the frequency, we have to use the wasNull() function to
						// check this. The ids it is sufficient to check if they were 0
						if (curVid != 0 && curSSid != 0 && curSid != 0 && !rs.wasNull()) {

							// If any of the ids is different from the last one, stop summing and check the sum of the latest variation
							if (curVid != lastVid || curSSid != lastSSid || curSid != lastSid) {
								// See if the sum of the frequencies deviates from 1 more than what we tolerate. In that case, count it as a failed
								if (Math.abs(1.f - sum) > tol) {
									// Store the failed data in failedEntries
									failedEntries.add(new int[] { lastVid, lastSSid, lastSid, Math.round(1000 * sum) });
									failed++;
								}

								// Set the last ids to this one and reset the sum
								lastVid = curVid;
								lastSSid = curSSid;
								lastSid = curSid;
								sum = 0.f;
							}
							// Add the frequency to the sum
							sum += freq;
						}
						count++;
						
						// Break if we've encountered a failed frequency (unless flagged not to)
						if (failed > 0 && !countAll) {
							break;
						}
					}

					rs.close();

					// s = System.currentTimeMillis();
					// System.out.println("Processed " + String.valueOf(count) + " rows in " + String.valueOf(((s-e)/1000)) + " seconds");
				}
				if (failed == 0) {
					// Report that the current table is ok
					ReportManager.correct(this, con, "Frequencies in " + tables[i] + " all add up to 1");
				} else {
					// Get an example and print it
					int[] entry = (int[]) failedEntries.get(0);
					String example = "variation_id = " + String.valueOf(entry[0]) + ", subsnp_id = " + String.valueOf(entry[1]) + ", population_id = " + String.valueOf(entry[2]) + ", sum is "
							+ String.valueOf((0.001f * entry[3]));
					ReportManager.problem(this, con, "There are " + String.valueOf(failed) + " variations in " + tables[i] + " where the frequencies don't add up to 1 +/- " + String.valueOf(tol) + " (e.g. "
							+ example + ")");
					result = false;

					// Loop over the failed entries and print a list of variation_id, subsnp_id, population_id and summed frequency to stdout
					/*
					for (int j = 0; j < failedEntries.size(); j++) {
						entry = (int[]) failedEntries.get(j);
						System.out.println(String.valueOf(entry[0]) + "\t" + String.valueOf(entry[1]) + "\t" + String.valueOf(entry[2]) + "\t" + String.valueOf((0.001f * entry[3])));
					}
					*/
				}
				// long subEnd = System.currentTimeMillis();
				// System.out.println("Time for healthcheck on " + tables[i] + " (~" + String.valueOf(maxId) + " variations) was " +
				// String.valueOf(((subEnd-subStart)/1000)) + " seconds");
			}
			stmt.close();
		} catch (Exception e) {
			result = false;
			e.printStackTrace();
		}

		// long mainEnd = System.currentTimeMillis();
		// System.out.println("Total time for healthcheck was " + String.valueOf(((mainEnd-mainStart)/1000)) + " seconds");

		if (result) {
			ReportManager.correct(this, con, "Allele/Genotype frequency healthcheck passed without any problem");
		}
		return result;

	} // run

} // AlleleFrequencies

