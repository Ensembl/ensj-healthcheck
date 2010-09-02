package testgroup;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.eg_core.*;

/**
 * All tests from EGCore, so probably the tests that should work on core 
 * databases.
 * 
 * @author michael
 *
 */
public class AllEGCore extends GroupOfTests {
	
	public AllEGCore() {

		addTest(
				AliasAndNaming.class,
				AssemblyMapping.class,
				DisplayXrefId.class,
				DuplicateMetaKeys.class,
				DuplicateProteinId.class,
				DuplicateTopLevel.class,
				DuplicateXref.class,
				EgMeta.class,
				EgProteinFeatureTranslation.class,
				ExonBoundary.class,
				GeneDescription.class,
				GeneDescriptionNewline.class,
				GeneDescriptionSource.class,
				GeneGC.class,
				GenesDisplayable.class,
				IdentityXref.class,
				IgiXref.class,
				InterproFeature.class,
				LowerCaseAnalysisName.class,
				MetaForCompara.class,
				PeptideTranslationAttribs.class,
				ProteinCodingGene.class,
				ProteinTranslation.class,
				SampleSetting.class,
				SeqRegionName.class,
				SharedDisplayXref.class,
				SpeciesSqlName.class,
				StableId.class,
				SuggestedEgMeta.class,
				TranslationAttribType.class
		);
	}
}
