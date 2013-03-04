package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.eg_core.ExonBoundary;
import org.ensembl.healthcheck.testcase.eg_core.EgProteinFeatureTranslation;
import org.ensembl.healthcheck.testcase.eg_core.DuplicateProteinFeature;
import org.ensembl.healthcheck.testcase.eg_core.InappropriateTranslation;
import org.ensembl.healthcheck.testcase.eg_core.SeqRegionNaming;
import org.ensembl.healthcheck.testcase.generic.DuplicateExons;
import org.ensembl.healthcheck.testcase.generic.DuplicateGenes;
import org.ensembl.healthcheck.testcase.generic.GeneCoordSystem;
import org.ensembl.healthcheck.testcase.generic.Pseudogene;
import org.ensembl.healthcheck.testcase.generic.SpeciesID;

/**
 * Group of tests that should be run to assess gene models for Ensembl Genomes.
 * Failures/warnings are acceptable with explanations.
 * 
 * @author dstaines
 * 
 */
public class EGCoreGeneModelMajor extends GroupOfTests {

	public EGCoreGeneModelMajor() {
		addTest(
				DuplicateExons.class, 
				DuplicateGenes.class, 
				ExonBoundary.class,
				GeneCoordSystem.class, 
				Pseudogene.class, 
				SpeciesID.class,
				EgProteinFeatureTranslation.class,
                DuplicateProteinFeature.class,
				InappropriateTranslation.class,
				SeqRegionNaming.class);
	}
}
