package testgroup;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.compara.*;

/**
 * These are the tests that register themselves as compara_homology.
 * 
 * @author michael
 *
 */
public class AllComparaHomology extends GroupOfTests {
	
	public AllComparaHomology() {

		addTest(
				DuplicateGenomeDb.class,
				CheckHomology.class,
				CheckFlatProteinTrees.class,
				CheckSequenceTable.class,
				CheckSpeciesSetTag.class,
				ForeignKeyFamilyId.class,
				ForeignKeyGenomeDbId.class,
				ForeignKeyHomologyId.class,
				ForeignKeyMemberId.class,
				ForeignKeyMethodLinkId.class,
				ForeignKeyMethodLinkSpeciesSetId.class,
				ForeignKeySequenceId.class,
				ForeignKeyTaxonId.class,
				Meta.class,
				SingleDBCollations.class,
				SpeciesNameConsistency.class
		);
	}
}
