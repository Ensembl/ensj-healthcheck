package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.generic.*;

import org.ensembl.healthcheck.testcase.generic.AnalysisTypes;
import org.ensembl.healthcheck.testcase.generic.ProductionBiotypes;
import org.ensembl.healthcheck.testcase.generic.TranscriptNames;

/**
 * Supergroup of tests for Ensembl Genomes (incorporates {@link EGCoreGeneModel}
 * , {@link EGCoreMeta}, {@link EGCoreAnnotation} and {@link EGCoreCompare})
 * 
 * @author dstaines
 * 
 */
public class EGCore extends GroupOfTests {

	public EGCore() {

		setDescription("Supergroup of tests for core databases from Ensembl Genomes.");

		addTest(EGCoreGeneModel.class, EGCoreMeta.class,
				EGCoreAnnotation.class, EGCoreCompare.class, EGCommon.class,
				EGCoreMulti.class,
				AnalysisTypes.class,
				// CheckDeclarations.class,
				// ProductionAnalysisLogicName.class,
				ProductionBiotypes.class,
				// ProductionMeta.class,
				TranscriptNames.class, ControlledCoreTables.class,
				AnalysisLogicName.class
		// SeqRegionsConsistentWithComparaMaster.class
		);
	}
}
