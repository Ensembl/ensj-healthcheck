package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.eg_core.DuplicateXref;
import org.ensembl.healthcheck.testcase.eg_core.IgiXref;
import org.ensembl.healthcheck.testcase.eg_core.UniProtKB_DisplayXrefIds;
import org.ensembl.healthcheck.testcase.generic.BlankCoordSystemVersions;
import org.ensembl.healthcheck.testcase.generic.BlankEnums;
import org.ensembl.healthcheck.testcase.generic.BlankInfoType;
import org.ensembl.healthcheck.testcase.generic.BlanksInsteadOfNulls;
import org.ensembl.healthcheck.testcase.generic.InterproDescriptions;
import org.ensembl.healthcheck.testcase.generic.IsCurrent;
import org.ensembl.healthcheck.testcase.generic.NullStrings;
import org.ensembl.healthcheck.testcase.generic.XrefCategories;
import org.ensembl.healthcheck.testcase.generic.XrefHTML;
import org.ensembl.healthcheck.testcase.generic.XrefIdentifiers;
import org.ensembl.healthcheck.testcase.generic.XrefLevels;
import org.ensembl.healthcheck.testcase.generic.XrefTypes;
import org.ensembl.healthcheck.testcase.generic.XrefVersions;

public class EGCoreAnnotationMajor extends GroupOfTests {

	public EGCoreAnnotationMajor() {
		addTest(BlankCoordSystemVersions.class, BlankEnums.class,
				BlankInfoType.class, BlanksInsteadOfNulls.class,
				DuplicateXref.class, IgiXref.class, InterproDescriptions.class,
				IsCurrent.class, NullStrings.class, XrefCategories.class,
				XrefHTML.class, XrefIdentifiers.class, XrefLevels.class,
				XrefTypes.class, XrefVersions.class, UniProtKB_DisplayXrefIds.class);
	}

}
