package testgroup;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.compara.*;

/**
 * Compara tests that won't work on bacterial compara databases. 
 *
 * <ul>
 * 	<li>
 * SpeciesNameConsistency: Checks consistency of a species name with the name
 * in the taxonomy. Not every strain of bacteria is in the taxonomy, therefore
 * this is not applicable.
 * 	</li>
 * </ul> 
 *
 * @author michael
 *
 */
public class ExpectedToFailOnEGComparaBacteria extends GroupOfTests {
	
	public ExpectedToFailOnEGComparaBacteria() {

		addTest(
				CheckGenomicAlignGenomeDBs.class,
				SpeciesNameConsistency.class,
				ForeignKeyMemberId.class,
				CheckSpeciesSetTag.class,
				ForeignKeyMethodLinkSpeciesSetIdGenomicAlignBlock.class,
				CheckTaxon.class
		);
	}
}
