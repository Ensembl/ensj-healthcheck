package testgroup;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.compara.*;

/**
 * These are all tests that exist for compara databases.
 * 
 * @author michael
 *
 */
public class AllComparaTests extends GroupOfTests {
	
	public AllComparaTests() {

		addTest(
			CheckConservationScore.class,
			ForeignKeyGenomicAlignId.class,
			CheckFlatProteinTrees.class,
			ForeignKeyHomologyId.class,
			CheckGenomeDB.class,
			ForeignKeyMemberId.class,
			CheckGenomicAlignGenomeDBs.class,
			ForeignKeyMethodLinkId.class,
			CheckHomology.class,
			ForeignKeyMethodLinkSpeciesSetIdGenomicAlignBlock.class,
			CheckSequenceTable.class,
			ForeignKeyMethodLinkSpeciesSetId.class,
			CheckSpeciesSetTag.class,
			ForeignKeySequenceId.class,
			CheckSynteny.class,
			ForeignKeySyntenyRegionId.class,
			CheckTaxon.class,
			ForeignKeyTaxonId.class,
			CheckTopLevelDnaFrag.class,
			Meta.class,
			DuplicateGenomeDb.class,
			MultipleGenomicAlignBlockIds.class,
			ForeignKeyDnafragId.class,
			ForeignKeyFamilyId.class,
			SingleDBCollations.class,
			ForeignKeyGenomeDbId.class,
			SpeciesNameConsistency.class,
			ForeignKeyGenomicAlignBlockId.class
		);
	}
}
