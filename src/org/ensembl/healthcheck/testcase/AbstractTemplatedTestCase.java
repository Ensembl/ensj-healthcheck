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
 * File: SampleMetaTestCase.java
 * Created by: dstaines
 * Created on: May 1, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * Base class for healthchecks that checks for uncaught exceptions and also
 * always adds correct if test passes. Also provides helper for getting {@link SqlTemplate}
 * 
 * @author dstaines
 * 
 */
public abstract class AbstractTemplatedTestCase extends SingleDatabaseTestCase {

	public AbstractTemplatedTestCase() {
		super();
	}

	protected SqlTemplate getTemplate(DatabaseRegistryEntry dbre) {
	  return DBUtils.getSqlTemplate(dbre);
	}

	protected abstract boolean runTest(DatabaseRegistryEntry dbre);

	public boolean run(DatabaseRegistryEntry dbre) {
		return runTest(dbre);
	}

}
