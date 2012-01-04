/**
 * File: EgMeta.java
 * Created by: dstaines
 * Created on: Mar 2, 2010
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import java.util.Map;
import java.util.Map.Entry;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.TestCaseUtils;

/**
 * @author dstaines
 * 
 */
public class DeprecatedEgMeta extends AbstractEgMeta {

	/**
	 * @param metaKeys
	 */
	public DeprecatedEgMeta() {
		super(
				TestCaseUtils
						.resourceToStringList("/org/ensembl/healthcheck/testcase/eg_core/deprecated_meta_keys.txt"));
	}

	@Override
	protected boolean testKeys(DatabaseRegistryEntry dbre, int speciesId,
			Map<String, Boolean> metaKeyOut) {
		boolean passes = true;
		for (Entry<String, Boolean> e : metaKeyOut.entrySet()) {
			if (e.getValue()) {
				passes = false;
				ReportManager.problem(this, dbre.getConnection(),
						"Meta table for " + speciesId
								+ " contains a value for the deprecated key " + e.getKey());
			}
		}
		return passes;
	}

}

