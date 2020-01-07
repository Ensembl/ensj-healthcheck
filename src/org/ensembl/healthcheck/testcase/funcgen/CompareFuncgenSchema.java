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

/*
 * Copyright (C) 2011 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.funcgen;

import static org.ensembl.healthcheck.DatabaseType.FUNCGEN;
import static org.ensembl.healthcheck.testcase.generic.SchemaComparer.TestTypes.AVG_ROW_LENGTH;
import static org.ensembl.healthcheck.testcase.generic.SchemaComparer.TestTypes.CHARSET;
import static org.ensembl.healthcheck.testcase.generic.SchemaComparer.TestTypes.CHECK_UNEQUAL;
import static org.ensembl.healthcheck.testcase.generic.SchemaComparer.TestTypes.ENGINE;
import static org.ensembl.healthcheck.testcase.generic.SchemaComparer.TestTypes.IGNORE_AUTOINCREMENT_OPTION;
import static org.ensembl.healthcheck.testcase.generic.SchemaComparer.TestTypes.MAX_ROWS;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.testcase.generic.AbstractCompareSchema;
import org.ensembl.healthcheck.testcase.generic.SchemaComparer;

/**
 * Extension of the compare schema functionality customised to run only on
 * funcgen databases
 */
public class CompareFuncgenSchema extends AbstractCompareSchema {

	public static final String MASTER_FUNCGEN_SCHEMA = "master.funcgen_schema";

	@Override
	public void types() {
		addAppliesToType(FUNCGEN);
	}

	@Override
	protected SchemaComparer getComparer(DatabaseRegistryEntry dbre) {
		SchemaComparer comparer = new SchemaComparer();
		comparer.addTestTypes(IGNORE_AUTOINCREMENT_OPTION, CHARSET, ENGINE, MAX_ROWS, AVG_ROW_LENGTH, CHECK_UNEQUAL);
		// add extra tables that we expect may be present but can ignore
		comparer.addIgnoreTables("MTMP_probestuff_helper");
		return comparer;
	}

	@Override
	protected String getMasterSchemaKey() {
		return MASTER_FUNCGEN_SCHEMA;
	}

}
