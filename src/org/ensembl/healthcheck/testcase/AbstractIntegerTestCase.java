/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2021] EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * File: AbstractRowCountTestCase.java
 * Created by: dstaines
 * Created on: Mar 8, 2010
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.TemplateBuilder;

/**
 * Base class for testing number returned by a query against an exact expected
 * value
 * 
 * @author dstaines
 * 
 */
public abstract class AbstractIntegerTestCase extends AbstractTemplatedTestCase {

	public AbstractIntegerTestCase() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.healthcheck.testcase.eg_core.AbstractEgCoreTestCase#runTest
	 * (org.ensembl.healthcheck.DatabaseRegistryEntry)
	 */
	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		int count = getTemplate(dbre).queryForDefaultObject(getSql(),
				Integer.class);
		if(testValue(count)) {
			return true;
		} else {
			ReportManager.problem(this, dbre.getConnection(), getErrorMessage(count));
			return false;
		}
	}

	/**
	 * @return query that returns a number
	 */
	protected abstract String getSql();

	/**
	 * Method to test whether number meets criteria
	 * 
	 * @param value
	 *            number produced by SQL to test
	 * @return true if number meets criteria for success
	 */
	protected abstract boolean testValue(int value);
	
	protected abstract String getErrorMessage(int value);

}
