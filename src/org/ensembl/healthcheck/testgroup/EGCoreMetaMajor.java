package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.eg_core.MetaForCompara;
import org.ensembl.healthcheck.testcase.eg_core.TranslationAttribType;
import org.ensembl.healthcheck.testcase.generic.AnalysisDescription;
import org.ensembl.healthcheck.testcase.generic.Biotypes;

public class EGCoreMetaMajor extends GroupOfTests {

	public EGCoreMetaMajor() {
		addTest(
				Biotypes.class,
				AnalysisDescription.class,	
				MetaForCompara.class,
				TranslationAttribType.class);	
	}

}
