package testgroup;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.compara.*;

public class DemoGroup extends GroupOfTests {

	public DemoGroup() {

		addTest(
			CheckConservationScore.class,
			ForeignKeyGenomicAlignId.class,
			CheckFlatProteinTrees.class,
			ForeignKeyHomologyId.class
		);
		
		this.addTest(new AllComparaTests());
	}
}
