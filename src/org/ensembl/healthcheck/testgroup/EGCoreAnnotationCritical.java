package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.eg_core.DbDisplayNameUniProt;
import org.ensembl.healthcheck.testcase.eg_core.DisplayXrefId;
import org.ensembl.healthcheck.testcase.eg_core.GeneDescriptionSource;
import org.ensembl.healthcheck.testcase.eg_core.GeneDescriptionUniProtSource;
import org.ensembl.healthcheck.testcase.eg_core.OntologyLevel;
import org.ensembl.healthcheck.testcase.eg_core.SharedDisplayXref;
import org.ensembl.healthcheck.testcase.eg_core.UniprotGeneNameObjectXref;
import org.ensembl.healthcheck.testcase.generic.DescriptionNewlines;
import org.ensembl.healthcheck.testcase.generic.DisplayLabels;
import org.ensembl.healthcheck.testcase.generic.DisplayXref;
import org.ensembl.healthcheck.testcase.generic.GeneDescriptions;
import org.ensembl.healthcheck.testcase.generic.TranscriptsSameName;

public class EGCoreAnnotationCritical extends GroupOfTests {

	public EGCoreAnnotationCritical() {
		addTest(DescriptionNewlines.class, DisplayLabels.class,
				DisplayXref.class, DisplayXrefId.class, GeneDescriptions.class,
				GeneDescriptionSource.class, SharedDisplayXref.class,
				TranscriptsSameName.class, OntologyLevel.class,
				UniprotGeneNameObjectXref.class,
				GeneDescriptionUniProtSource.class, DbDisplayNameUniProt.class);
	}
}
