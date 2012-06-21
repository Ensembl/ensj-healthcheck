package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.eg_core.DuplicateTaxonomyIds;
import org.ensembl.healthcheck.testcase.eg_core.EnaProvider;
import org.ensembl.healthcheck.testcase.eg_core.MetaForCompara;
import org.ensembl.healthcheck.testcase.eg_core.PermittedEgMeta;
import org.ensembl.healthcheck.testcase.eg_core.TranslationAttribType;
import org.ensembl.healthcheck.testcase.generic.AnalysisDescription;

public class EGCoreMetaMajor extends GroupOfTests {

	public EGCoreMetaMajor() {
		addTest(
			AnalysisDescription.class,	
			MetaForCompara.class,
			TranslationAttribType.class,
			DuplicateTaxonomyIds.class,
			PermittedEgMeta.class, EnaProvider.class);
	}
}
