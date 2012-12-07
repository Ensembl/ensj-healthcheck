package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.compara.ForeignKeyDnafragId;
import org.ensembl.healthcheck.testcase.compara.ForeignKeyGenomeDbId;
import org.ensembl.healthcheck.testcase.compara.ForeignKeyGenomicAlignBlockId;
import org.ensembl.healthcheck.testcase.compara.ForeignKeyGenomicAlignId;
import org.ensembl.healthcheck.testcase.compara.ForeignKeyMethodLinkId;
import org.ensembl.healthcheck.testcase.compara.ForeignKeyTaxonId;
import org.ensembl.healthcheck.testcase.compara.MultipleGenomicAlignBlockIds;
import org.ensembl.healthcheck.testcase.eg_compara.EGCheckEmptyLocators;
import org.ensembl.healthcheck.testcase.eg_compara.EGCheckNoTreeStableIds;
import org.ensembl.healthcheck.testcase.eg_compara.EGCheckSynteny;
import org.ensembl.healthcheck.testcase.eg_compara.EGForeignKeyMethodLinkSpeciesSetId;

/**
 * Group of tests for EnsemblGenomes compara databases
 * 
 * @author dstaines
 * 
 */
public class EGCompara extends GroupOfTests {

	public EGCompara() {

		setDescription("Group of tests for EnsemblGenomes compara databases.");

		addTest(
				EGCommon.class, 
				EGComparaGeneTree.class, 
				EGCheckSynteny.class,
				EGForeignKeyMethodLinkSpeciesSetId.class,
				EGCheckNoTreeStableIds.class,
				ForeignKeyDnafragId.class, 
				ForeignKeyGenomeDbId.class,
				ForeignKeyGenomicAlignBlockId.class,
				ForeignKeyGenomicAlignId.class, 
				ForeignKeyMethodLinkId.class,
				ForeignKeyTaxonId.class, 
				EGCheckEmptyLocators.class,
				MultipleGenomicAlignBlockIds.class,
				ControlledComparaTables.class
		);
	}
}
