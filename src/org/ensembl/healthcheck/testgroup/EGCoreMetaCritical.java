package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.eg_core.AliasAndNaming;
import org.ensembl.healthcheck.testcase.eg_core.DuplicateMetaKeys;
import org.ensembl.healthcheck.testcase.eg_core.EgMeta;
import org.ensembl.healthcheck.testcase.eg_core.GeneGC;
import org.ensembl.healthcheck.testcase.eg_core.GenesDisplayable;
import org.ensembl.healthcheck.testcase.eg_core.SampleSetting;
import org.ensembl.healthcheck.testcase.generic.MetaCoord;

public class EGCoreMetaCritical extends GroupOfTests {

	public EGCoreMetaCritical() {
		addTest(
		//AliasAndNaming.class,	
		EgMeta.class,
		DuplicateMetaKeys.class,	
		GenesDisplayable.class,	
		GeneGC.class,	
		MetaCoord.class,	
		SampleSetting.class);	
	}

}
