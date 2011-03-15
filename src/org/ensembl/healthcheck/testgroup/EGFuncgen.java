package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.eg_funcgen.EGArrayXrefs;
import org.ensembl.healthcheck.testcase.funcgen.AnalysisDescription;
import org.ensembl.healthcheck.testcase.funcgen.CompareFuncgenSchema;
import org.ensembl.healthcheck.testcase.funcgen.ComparePreviousVersionArrayXrefs;
import org.ensembl.healthcheck.testcase.funcgen.FuncgenForeignKeys;
import org.ensembl.healthcheck.testcase.funcgen.FuncgenStableID;
import org.ensembl.healthcheck.testcase.funcgen.MetaCoord;
import org.ensembl.healthcheck.testcase.funcgen.RegulatoryFeatureTypes;
import org.ensembl.healthcheck.testcase.generic.BlankCoordSystemVersions;
import org.ensembl.healthcheck.testcase.generic.BlankEnums;
import org.ensembl.healthcheck.testcase.generic.BlankInfoType;
import org.ensembl.healthcheck.testcase.generic.BlanksInsteadOfNulls;
import org.ensembl.healthcheck.testcase.generic.ExternalDBDisplayName;
import org.ensembl.healthcheck.testcase.generic.NullStrings;

/**
 * Tests to run on an Ensembl Genomes funcgen database
 * 
 */
public class EGFuncgen extends GroupOfTests {

	public EGFuncgen() {

		addTest(BlankEnums.class, BlankInfoType.class, MetaCoord.class,
				ExternalDBDisplayName.class, EGArrayXrefs.class,
				FuncgenForeignKeys.class, BlankCoordSystemVersions.class,
				BlanksInsteadOfNulls.class, RegulatoryFeatureTypes.class,
				ComparePreviousVersionArrayXrefs.class,
				AnalysisDescription.class, FuncgenStableID.class,
				NullStrings.class, CompareFuncgenSchema.class);
		addTest(new EGCommon());

	}
}