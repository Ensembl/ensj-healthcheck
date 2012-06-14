/**
 * EnaProvider
 * 
 * @author dstaines
 * @author $Author$
 * @version $Revision$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * Test to make sure we're not using ENA as the provider where we could be using
 * something better and more accurate
 * 
 * @author dstaines
 * 
 */
public class InterproHitCount extends AbstractEgCoreTestCase {
	private final static String GENE_COUNT_SQL = "select count(distinct(gene.gene_id)) from gene "
			+ "join seq_region using (seq_region_id) " + "join coord_system using (coord_system_id) "
			+ "where gene.biotype='protein_coding' and species_id=?";
	private final static String IPR_COUNT_SQL = "select count(distinct(gene.gene_id)) from gene "
			+ "join seq_region using (seq_region_id) " + "join coord_system using (coord_system_id) "
			+ "join transcript using (gene_id) " + "join translation using (transcript_id) "
			+ "join protein_feature using (translation_id) " + "join interpro on (id=hit_name) "
			+ "where gene.biotype='protein_coding' and species_id=?";
	private final static double THRESHOLD = 0.5;
	public InterproHitCount() {
		super();
		setDescription("Test to check that at least " + THRESHOLD * 100
				+ "% of protein coding genes have at least one interpro hit");
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase#runTest(org
	 * .ensembl.healthcheck.DatabaseRegistryEntry)
	 */
	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean result = true;
		SqlTemplate temp = getSqlTemplate(dbre);
		// count number genes
		for (int speciesId : dbre.getSpeciesIds()) {
			int geneN = temp.queryForDefaultObject(GENE_COUNT_SQL,
					Integer.class, speciesId);
			// count number of genes with at least 1 interpro hit
			int iprN = temp.queryForDefaultObject(IPR_COUNT_SQL, Integer.class,
					speciesId);
			double ratio = (double)iprN / geneN;
			if (ratio < THRESHOLD) {
				ReportManager.problem(this, dbre.getConnection(), iprN
						+ " protein_coding genes of a total of " + geneN
						+ " for species " + speciesId
						+ " have at least one interpro domain -"
						+ " this is less than the permitted threshold of "
						+ THRESHOLD * 100 + "%");
				result = false;
			} else {
				ReportManager.info(this, dbre.getConnection(), iprN
						+ " protein_coding genes of a total of " + geneN
						+ " for species " + speciesId
						+ " have at least one interpro domain");
			}
		}
		return result;
	}
}
