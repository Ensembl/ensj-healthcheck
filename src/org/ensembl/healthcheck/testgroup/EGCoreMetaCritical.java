package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.eg_core.DuplicateMetaKeys;
import org.ensembl.healthcheck.testcase.eg_core.EgMeta;
import org.ensembl.healthcheck.testcase.eg_core.GeneBuildStartDate;
import org.ensembl.healthcheck.testcase.eg_core.GeneGC;
import org.ensembl.healthcheck.testcase.eg_core.SampleSetting;
import org.ensembl.healthcheck.testcase.generic.MetaCoord;

public class EGCoreMetaCritical extends GroupOfTests {

	public EGCoreMetaCritical() {
		addTest(
				EgMeta.class, 
				GeneBuildStartDate.class,
				DuplicateMetaKeys.class, 
				GeneGC.class, 
				MetaCoord.class,
				SampleSetting.class);
	}
}
