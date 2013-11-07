/**
 * EnaProvider
 * 
 * @author dstaines
 * @author $Author$
 * @version $Revision$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import java.util.List;
import java.util.regex.Pattern;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * Test to make sure assembly.accession is set and available
 * 
 * @author dstaines
 * 
 */
public class AssemblyAccession extends AbstractEgCoreTestCase {

	private final static String META_VAL = "select meta_value from meta where meta_key=? and species_id=?";
	private final static Pattern ASS_PAT = Pattern.compile("GCA_[0-9]+\\.[0-9]+");

	public AssemblyAccession() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase#runTest(org
	 * .ensembl.healthcheck.DatabaseRegistryEntry)
	 */
	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean result = true;
		SqlTemplate temp = getSqlTemplate(dbre);
		for (int speciesId : dbre.getSpeciesIds()) {
			List<String> assAccs = temp.queryForDefaultObjectList(META_VAL,
					String.class, "assembly.accession", speciesId);
			if (assAccs.size() == 0) {
				ReportManager.problem(this, dbre.getConnection(),
						"No assembly.accession entry found for species "
								+ speciesId);
				result = false;
			} else if (assAccs.size() > 1) {
				ReportManager.problem(this, dbre.getConnection(),
						"Multiple assembly.accession entries found for species "
								+ speciesId);
				result = false;
			} else {
				String assAcc = assAccs.get(0);
				if (!ASS_PAT.matcher(assAcc).matches()) {
					ReportManager.problem(this, dbre.getConnection(),
							"assembly.accession " + assAcc + " for species "
									+ speciesId
									+ " does not match expected pattern "
									+ ASS_PAT.toString());
					result = false;
				}
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase#getEgDescription
	 * ()
	 */
	@Override
	protected String getEgDescription() {
		return "Test to check that assembly.accession is set";
	}

}
