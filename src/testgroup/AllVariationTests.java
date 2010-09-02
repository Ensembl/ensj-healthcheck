package testgroup;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.variation.*;

/**
 * All tests from AllVariationTests, so probably the tests that should work 
 * on variation databases. 
 * 
 * @author michael
 *
 */
public class AllVariationTests extends GroupOfTests {
			
	public AllVariationTests() {
	
		addTest(
			CompareVariationSchema.class,
			EmptyVariationTables.class,
			FlankingUpDownSeq.class,
			ForeignKeyCoreId.class,
			IndividualType.class,
			Meta_coord.class,
			Meta.class,
			TranscriptVariation.class,
			VariationForeignKeys.class,
			VariationSet.class,
			VariationSynonym.class,
			VFCoordinates.class
		);
	}
}
