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

package org.ensembl.healthcheck.configuration;

import org.ensembl.healthcheck.testcase.funcgen.CompareFuncgenSchema;
import org.ensembl.healthcheck.testcase.generic.CompareSchema;
import org.ensembl.healthcheck.testcase.variation.CompareVariationSchema;

import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * 
 * Interface for the parameters used by tests comparing schemas.
 * 
 * @author michael
 * 
 */
public interface ConfigureCompareSchema {

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.generic.CompareSchema
	// org.ensembl.healthcheck.testcase.funcgen.CompareFuncgenSchema
	//
	@Option(longName = CompareSchema.MASTER_SCHEMA, description = "Parameter used only in "
			+ "org.ensembl.healthcheck.testcase.generic.CompareSchema, "
			+ "and org.ensembl.healthcheck.testcase.funcgen.CompareFuncgenSchema")
	String getMasterSchema();

	boolean isMasterSchema();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.variation.CompareVariationSchema
	//
	@Option(longName = CompareVariationSchema.MASTER_VARIATION_SCHEMA, description = "Parameter used only in "
			+ "master.variation_schema")
	String getMasterVariationSchema();

	boolean isMasterVariationSchema();

	// Used in:
	//
	// org.ensembl.healthcheck.testcase.funcgen.CompareFuncgenSchema
	//
	@Option(longName = CompareFuncgenSchema.MASTER_FUNCGEN_SCHEMA, description = "Parameter used in "
			+ "org.ensembl.healthcheck.testcase.funcgen.CompareFuncgenSchema")
	String getMasterFuncgenSchema();

	boolean isMasterFuncgenSchema();

}
