package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.eg_core.DeprecatedEgMeta;
import org.ensembl.healthcheck.testcase.eg_core.DuplicateMetaKeys;
import org.ensembl.healthcheck.testcase.eg_core.GeneBuildStartDate;
import org.ensembl.healthcheck.testcase.eg_core.GeneGC;
import org.ensembl.healthcheck.testcase.eg_core.RequiredEgMeta;
import org.ensembl.healthcheck.testcase.eg_core.SampleSetting;
import org.ensembl.healthcheck.testcase.generic.MetaCoord;

public class EGCoreMetaCritical extends GroupOfTests {

	public EGCoreMetaCritical() {
		addTest(
				RequiredEgMeta.class, 
				DeprecatedEgMeta.class,
				GeneBuildStartDate.class,
				DuplicateMetaKeys.class, 
				GeneGC.class, 
				MetaCoord.class,
				SampleSetting.class);
	}
}
