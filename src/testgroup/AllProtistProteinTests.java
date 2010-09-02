package testgroup;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.compara.*;

/**
 * These are the tests that should work on the protist *_hom_* databases.
 * 
 * @author michael
 *
 */
public class AllProtistProteinTests extends AllComparaHomology {
	
	public AllProtistProteinTests() {

		removeTest(
			org.ensembl.healthcheck.testcase.compara.ForeignKeyMethodLinkSpeciesSetId.class
		);
	}
}
