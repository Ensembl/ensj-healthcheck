package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.eg_core.DuplicateTaxonomyIds;
import org.ensembl.healthcheck.testcase.eg_core.EnaProvider;
import org.ensembl.healthcheck.testcase.eg_core.GoTermCount;
import org.ensembl.healthcheck.testcase.eg_core.InterproHitCount;
import org.ensembl.healthcheck.testcase.eg_core.NoRepeatFeatures;
import org.ensembl.healthcheck.testcase.eg_core.RepeatAnalysesInMeta;
import org.ensembl.healthcheck.testcase.eg_core.SeqRegionsConsistentWithComparaMaster;
import org.ensembl.healthcheck.testcase.generic.Karyotype;
import org.ensembl.healthcheck.testcase.generic.ProductionAnalysisLogicName;
import org.ensembl.healthcheck.testcase.generic.StableID;
import org.ensembl.healthcheck.testcase.generic.XrefVersions;

/**
 * Supergroup of tests for Ensembl Bacteria - based on {@link EGCore} but
 * removing tests that are inappropriate for Ensembl Bacteria.
 * 
 * @author dstaines
 * 
 */
public class EBCore extends GroupOfTests {

	public EBCore() {

		setDescription("Supergroup of tests for core databases from Ensembl Bacteria.");

		addTest(EGCore.class);
		removeTest(DuplicateTaxonomyIds.class, EnaProvider.class,
				Karyotype.class, GoTermCount.class, InterproHitCount.class,
				NoRepeatFeatures.class, ProductionAnalysisLogicName.class,
				RepeatAnalysesInMeta.class, StableID.class, XrefVersions.class,
				SeqRegionsConsistentWithComparaMaster.class);
	}
}
