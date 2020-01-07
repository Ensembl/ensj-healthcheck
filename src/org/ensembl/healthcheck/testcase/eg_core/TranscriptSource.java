/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2020] EMBL-European Bioinformatics Institute
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
 * File: DisplayXrefIdTest.java
 * Created by: dstaines
 * Created on: May 27, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.testcase.AbstractRowCountTestCase;

/**
 * Test to check if we have used source=ensembl in any
 * 
 * @author dstaines
 * 
 */
public class TranscriptSource extends AbstractRowCountTestCase {

	private final static String QUERY = "select count(*) from transcript where source='Ensembl'";

	@Override
	protected int getExpectedCount() {
		return 0;
	}

	@Override
	protected String getSql() {
		return QUERY;
	}

	@Override
	protected String getErrorMessage(int value) {
		return "Found "
				+ value
				+ " transcripts with source 'Ensembl' which should be replaced with an "
				+ "appropriate GOA compatible name for the original source";
	}

}
