package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.variation.AlleleFrequencies;
import org.ensembl.healthcheck.testcase.variation.CompareVariationSchema;
import org.ensembl.healthcheck.testcase.variation.EmptyVariationTables;
import org.ensembl.healthcheck.testcase.variation.FlankingUpDownSeq;
import org.ensembl.healthcheck.testcase.variation.ForeignKeyCoreId;
import org.ensembl.healthcheck.testcase.variation.IndividualType;
import org.ensembl.healthcheck.testcase.variation.Meta;
import org.ensembl.healthcheck.testcase.variation.Meta_coord;
import org.ensembl.healthcheck.testcase.variation.StructuralVariation;
import org.ensembl.healthcheck.testcase.variation.TranscriptVariation;
import org.ensembl.healthcheck.testcase.variation.VFCoordinates;
import org.ensembl.healthcheck.testcase.variation.VariationForeignKeys;
import org.ensembl.healthcheck.testcase.variation.VariationSet;
import org.ensembl.healthcheck.testcase.variation.VariationSynonym;

/**
 * Group of tests for variation databases
 * 
 * @author dstaines
 * 
 */
public class EGVariation extends GroupOfTests {

	public EGVariation() {
		addTest(AlleleFrequencies.class, CompareVariationSchema.class,
				EmptyVariationTables.class, FlankingUpDownSeq.class,
				ForeignKeyCoreId.class, IndividualType.class, Meta_coord.class,
				Meta.class, StructuralVariation.class,
				TranscriptVariation.class, VariationForeignKeys.class,
				VariationSet.class, VFCoordinates.class);
	}
}
