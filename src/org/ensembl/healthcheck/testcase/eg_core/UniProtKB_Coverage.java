package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * test for minimal level of uniprot coverage to ensure all cases are checked
 * manually
 * 
 * @author dstaines
 * 
 */
public class UniProtKB_Coverage extends AbstractTemplatedTestCase {

	public UniProtKB_Coverage() {
		super();
		this.addToGroup(AbstractEgCoreTestCase.EG_GROUP);
		this.appliesToType(DatabaseType.CORE);
		this.setTeamResponsible(Team.ENSEMBL_GENOMES);
	}

	private final static double THRESHOLD = 90.0;

	private final static String QUERY_UNIPROT = "SELECT count(distinct(g.gene_id)) "
			+ "FROM gene g join transcript t using (gene_id) "
			+ "join translation tl using (transcript_id) "
			+ "join object_xref ox on (tl.transcript_id=ox.ensembl_id and ox.ensembl_object_type='Translation') "
			+ "join xref x using (xref_id) join external_db d using (external_db_id) "
			+ "WHERE g.biotype='protein_coding' "
			+ "AND d.db_name IN ('Uniprot/SPTREMBL','Uniprot/SPTREMBL_predicted','Uniprot/SWISSPROT','Uniprot/SWISSPROT_predicted')";

	private final static String QUERY_GENES = "select count(*) from gene where biotype='protein_coding'";

	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		SqlTemplate template = getSqlTemplate(dbre);
		int nProteinCoding = template.queryForDefaultObject(QUERY_GENES,
				Integer.class);
		if (nProteinCoding == 0) {
			ReportManager.problem(this, dbre.getConnection(),
					"No protein coding genes found!");
			return false;
		}
		int nUniProt = template.queryForDefaultObject(QUERY_UNIPROT,
				Integer.class);
		double ratio = (100.0 * nUniProt) / nProteinCoding;
		if (ratio < THRESHOLD) {
			ReportManager
					.problem(
							this,
							dbre.getConnection(),
							"Less than "
									+ THRESHOLD
									+ "% of protein_coding genes have a UniProtKB xref ("
									+ nUniProt
									+ "/"
									+ nProteinCoding
									+ "): this may be correct for some genomes so please check and annotate accordingly");
			return false;
		} else {
			return true;
		}
	}

}
