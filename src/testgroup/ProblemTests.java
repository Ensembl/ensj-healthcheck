package testgroup;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.compara.*;
import org.ensembl.healthcheck.testcase.eg_core.*;
import org.ensembl.healthcheck.testcase.variation.*;


/**
 * Group of healthchecks that cause problems for various reasons.
 * 
 * <ul>
 * 	<li>
 * 		DuplicateProteinId: Makes no sense
 * 	</li>
 * 	<li>
 * 		SuggestedEgMeta: Tests for something I have never heard of and then fails
 * 	</li>
 * 	<li>
 * 		MultipleGenomicAlignBlockIds: Really slow
 * 	</li>
 * 	<li>
 * 		Meta_coord: Looks for a table called structural_variation_feature 
 * which does not exist and writes this as an error message. The test passes 
 * anyway, so would not be a problem. Annoying for me though, so I'll put it 
 * here.
 * 	</li>
 * 	<li>
 * 		VariationForeignKeys: Asked Paul D.: Test does not take account 
 * contents of the failed_variation table. The test complains about rows in 
 * variation_feature for which there is no allele. It thinks this should not 
 * happen, however, if variation_features have an entry in failed_variation, 
 * they don't have to have an entry in the allele table.
 * 	</li>
 * 	<li>
 * 		EmptyVariationTables: Makes no sense
 * 	</li>
 * </ul> 
 * 
 * @author michael
 *
 */
public class ProblemTests extends GroupOfTests {
	
	public ProblemTests() {

		addTest(
				ProteinTranslation.class,
				// Bullocks healthcheck (Dan)
				DuplicateProteinId.class,
				// Tests for something I have never heard of and then fails
				SuggestedEgMeta.class,
				// Really slow
				MultipleGenomicAlignBlockIds.class,
				// These multidatabase tests don't work, because they use the 
				// static org.ensembl.healthcheck.Species class which uses
				// information hardcoded in the an enum in the file. The
				// information there is for Ensembl only.
				//
				CheckGenomeDB.class,
				CheckTopLevelDnaFrag.class,
				VFCoordinates.class,
				ForeignKeyCoreId.class,
				
				// Looks for a table called structural_variation_feature 
				// which does not exist and writes this as an error
				// message. The test passes anyway, so would not be a
				// problem. Annoying for me though, so I'll put it here.
				//
				Meta_coord.class,
				// Asked Paul D.: Test does not take account contents of the 
				// failed_variation table. 
				//
				// The test complains about rows in variation_feature for 
				// which there is no allele. It thinks this should not happen,
				// however, if variation_features have an entry in 
				// failed_variation, they don't have to have an entry in
				// the allele table.
				//
				VariationForeignKeys.class,
				// Asked Paul D.: Makes no sense in the first place.
				EmptyVariationTables.class
				
		);
	}
}
