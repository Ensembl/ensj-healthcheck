/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
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

/**
 * Base class for testing number returned by a query
 * 
 * @author dstaines
 * 
 */
public abstract class AbstractRowCountTestCase extends AbstractIntegerTestCase {

	public AbstractRowCountTestCase() {
		super();
	}

	/**
	 * @return number that the rowcount should return
	 */
	protected abstract int getExpectedCount();

	@Override
	protected boolean testValue(int value) {
		return getExpectedCount() == value;
	}

}
