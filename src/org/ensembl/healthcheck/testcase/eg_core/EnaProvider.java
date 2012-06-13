/**
 * EnaProvider
 * 
 * @author dstaines
 * @author $Author$
 * @version $Revision$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * Test to make sure we're not using ENA as the provider where we could be using something better and more accurate
 * @author dstaines
 *
 */
public class EnaProvider extends AbstractEgCoreTestCase {
	
	private final static String META_VAL = "select distinct(meta_value) from meta where meta_key=?";
	private final static String[] KEYS = {"provider.name","provider.url"};
	private final static String[] VALS = {"European Nucleotide Archive","http://www.ebi.ac.uk/ena/"};
	public EnaProvider() {
		super();
		setDescription("Test to make sure the default GenomeLoader providers are not used as there are likely to be better providers to use");
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase#runTest(org.ensembl.healthcheck.DatabaseRegistryEntry)
	 */
	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean result = true;
		SqlTemplate temp = getSqlTemplate(dbre);
		for(String key: KEYS) {
			for(String val: temp.queryForDefaultObjectList(META_VAL, String.class, key)) {
				for(String badVal: VALS) {
					if(val.contains(badVal)) {
						ReportManager.problem(this, dbre.getConnection(), "Meta key "+key+" set to GenomeLoader default "+val);
						result = false;
					}
				}
			}
		}
		return result;
	}

}
