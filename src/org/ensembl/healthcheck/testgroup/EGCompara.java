package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.compara.CheckConservationScore;
import org.ensembl.healthcheck.testcase.compara.CheckFlatProteinTrees;
import org.ensembl.healthcheck.testcase.compara.CheckGenomeDB;
import org.ensembl.healthcheck.testcase.compara.CheckGenomicAlignGenomeDBs;
import org.ensembl.healthcheck.testcase.compara.CheckHomology;
import org.ensembl.healthcheck.testcase.compara.CheckSequenceTable;
import org.ensembl.healthcheck.testcase.compara.CheckSpeciesSetTag;
import org.ensembl.healthcheck.testcase.compara.CheckSynteny;
import org.ensembl.healthcheck.testcase.compara.CheckTaxon;
import org.ensembl.healthcheck.testcase.compara.CheckTopLevelDnaFrag;
import org.ensembl.healthcheck.testcase.compara.DuplicateGenomeDb;
import org.ensembl.healthcheck.testcase.compara.ForeignKeyDnafragId;
import org.ensembl.healthcheck.testcase.compara.ForeignKeyFamilyId;
import org.ensembl.healthcheck.testcase.compara.ForeignKeyGenomeDbId;
import org.ensembl.healthcheck.testcase.compara.ForeignKeyGenomicAlignBlockId;
import org.ensembl.healthcheck.testcase.compara.ForeignKeyGenomicAlignId;
import org.ensembl.healthcheck.testcase.compara.ForeignKeyHomologyId;
import org.ensembl.healthcheck.testcase.compara.ForeignKeyMemberId;
import org.ensembl.healthcheck.testcase.compara.ForeignKeyMethodLinkId;
import org.ensembl.healthcheck.testcase.compara.ForeignKeyMethodLinkSpeciesSetId;
import org.ensembl.healthcheck.testcase.compara.ForeignKeyMethodLinkSpeciesSetIdGenomicAlignBlock;
import org.ensembl.healthcheck.testcase.compara.ForeignKeySequenceId;
import org.ensembl.healthcheck.testcase.compara.ForeignKeySyntenyRegionId;
import org.ensembl.healthcheck.testcase.compara.ForeignKeyTaxonId;
import org.ensembl.healthcheck.testcase.compara.Meta;
import org.ensembl.healthcheck.testcase.compara.MultipleGenomicAlignBlockIds;
import org.ensembl.healthcheck.testcase.compara.SingleDBCollations;
import org.ensembl.healthcheck.testcase.compara.SpeciesNameConsistency;

/**
 * Group of tests for EnsemblGenomes compara databases 
 * 
 * @author dstaines
 * 
 */
public class EGCompara extends GroupOfTests {

	public EGCompara() {
		addTest(
				
			EGCommon.class,
				
			CheckConservationScore.class, 
			CheckFlatProteinTrees.class,
			CheckGenomeDB.class, 
			CheckGenomicAlignGenomeDBs.class,
			CheckHomology.class, 
			CheckSequenceTable.class,
			CheckSpeciesSetTag.class, 
			CheckSynteny.class, 
			CheckTaxon.class,
			CheckTopLevelDnaFrag.class, 
			DuplicateGenomeDb.class,
			ForeignKeyDnafragId.class, 
			ForeignKeyFamilyId.class,
			ForeignKeyGenomeDbId.class,
			ForeignKeyGenomicAlignBlockId.class,
			ForeignKeyGenomicAlignId.class, 
			ForeignKeyHomologyId.class,
			ForeignKeyMemberId.class, 
			ForeignKeyMethodLinkId.class,
			ForeignKeyMethodLinkSpeciesSetId.class,
			ForeignKeyMethodLinkSpeciesSetIdGenomicAlignBlock.class,
			ForeignKeySequenceId.class, 
			ForeignKeySyntenyRegionId.class,
			ForeignKeyTaxonId.class, 
			Meta.class,
			MultipleGenomicAlignBlockIds.class, 
			SingleDBCollations.class,
			SpeciesNameConsistency.class
		);
	}
}
