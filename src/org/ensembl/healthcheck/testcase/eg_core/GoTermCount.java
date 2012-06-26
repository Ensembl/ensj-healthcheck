/**
 * GoTermCount
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
 * Test to check that at least 50% of protein coding genes have at least one GO
 * term
 * 
 * @author dstaines
 * 
 */
public class GoTermCount extends AbstractEgCoreTestCase {

	private final static String GENE_COUNT_SQL = "select count(distinct(gene.gene_id)) from gene "
			+ "join seq_region using (seq_region_id) "
			+ "join coord_system using (coord_system_id) "
			+ "where gene.biotype='protein_coding' and species_id=?";

	private final static String GO_TRANSLATION_COUNT_SQL = "select count(distinct(gene.gene_id)) from gene "
			+ "join seq_region using (seq_region_id) "
			+ "join coord_system using (coord_system_id) "
			+ "join transcript using (gene_id) "
			+ "join translation using (transcript_id) "
			+ "join object_xref on (translation_id=ensembl_id AND ensembl_object_type='Translation') "
			+ "join xref using (xref_id) "
			+ "join external_db using (external_db_id) "
			+ "join ontology_xref using (object_xref_id) "
			+ "where gene.biotype='protein_coding' and external_db.db_name='GO' and species_id=?";

	private final static String GO_TRANSCRIPT_COUNT_SQL = "select count(distinct(gene.gene_id)) from gene "
			+ "join seq_region using (seq_region_id) "
			+ "join coord_system using (coord_system_id) "
			+ "join transcript using (gene_id) "
			+ "join object_xref on (transcript_id=ensembl_id AND ensembl_object_type='Transcript') "
			+ "join xref using (xref_id) "
			+ "join external_db using (external_db_id) "
			+ "join ontology_xref using (object_xref_id) "
			+ "where gene.biotype='protein_coding' and external_db.db_name='GO' and species_id=?";

	private final static String GO_GENE_COUNT_SQL = "select count(distinct(gene.gene_id)) from gene "
			+ "join seq_region using (seq_region_id) "
			+ "join coord_system using (coord_system_id) "
			+ "join object_xref on (gene_id=ensembl_id AND ensembl_object_type='Gene') "
			+ "join xref using (xref_id) "
			+ "join external_db using (external_db_id) "
			+ "join ontology_xref using (object_xref_id) "
			+ "where gene.biotype='protein_coding' and external_db.db_name='GO' and species_id=?";

	private final static double THRESHOLD = 0.4;

	public GoTermCount() {
		super();

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
			// count number of genes with at least 1 GO term (try gene,
			// transcript,
			// translation)
			int goN = temp.queryForDefaultObject(GO_TRANSLATION_COUNT_SQL,
					Integer.class, speciesId);
			if (goN == 0) {
				goN = temp.queryForDefaultObject(GO_TRANSCRIPT_COUNT_SQL,
						Integer.class, speciesId);
			}
			if (goN == 0) {
				goN = temp.queryForDefaultObject(GO_GENE_COUNT_SQL,
						Integer.class, speciesId);
			}
			double ratio = (double) goN / geneN;
			if (ratio < THRESHOLD) {
				ReportManager.problem(this, dbre.getConnection(), goN
						+ " protein_coding genes of a total of " + geneN
						+ " for species " + speciesId
						+ " have at least one GO term -"
						+ " this is less than the suggested threshold of "
						+ THRESHOLD * 100 + "%");
				result = false;
			} else {
				ReportManager.info(this, dbre.getConnection(), goN
						+ " protein_coding genes of a total of " + geneN
						+ " for species " + speciesId
						+ " have at least one GO term");
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.healthcheck.testcase.eg_core.AbstractEgCoreTestCase#
	 * getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Test to check that at least " + THRESHOLD * 100
				+ "% of protein coding genes have at least one GO term";
	}

}
