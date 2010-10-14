package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionAnalysisDescriptions;
import org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionBase;
import org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionBiotypes;
import org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionExonCoords;
import org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionGOXrefs;
import org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionRepeatTypes;
import org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionSynonyms;
import org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionTableRows;
import org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionXrefs;

/**
 * Set of tests to compare a core database with the previous release
 * 
 * @author dstaines
 * 
 */
public class EGCoreCompare extends GroupOfTests {

	public EGCoreCompare() {
		addTest(ComparePreviousVersionAnalysisDescriptions.class,
				ComparePreviousVersionBase.class,
				ComparePreviousVersionBiotypes.class,
				ComparePreviousVersionExonCoords.class,
				ComparePreviousVersionGOXrefs.class,
				ComparePreviousVersionRepeatTypes.class,
				ComparePreviousVersionSynonyms.class,
				ComparePreviousVersionTableRows.class,
				ComparePreviousVersionXrefs.class);
	}

}
