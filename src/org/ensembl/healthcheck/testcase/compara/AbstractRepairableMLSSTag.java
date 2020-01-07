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

package org.ensembl.healthcheck.testcase.compara;

import org.ensembl.healthcheck.testcase.compara.AbstractRepairableComparaTestCase;

public abstract class AbstractRepairableMLSSTag extends AbstractRepairableComparaTestCase {

	protected abstract String getTagToCheck();

	protected String getTableName() {
		return "method_link_species_set_tag";
	}
	protected String getAddQuery(String key, String value) {
		return "INSERT INTO method_link_species_set_tag VALUES (\"" + key + "\", \"" + getTagToCheck() + "\", " + value + ");";
	}
	protected String getUpdateQuery(String key, String value) {
		return "UPDATE method_link_species_set_tag SET value = " + value + " WHERE tag = \"" + getTagToCheck() + "\" AND method_link_species_set_id = \"" + key + "\";";
	}
	protected String getRemoveQuery(String key) {
		return "DELETE FROM method_link_species_set_tag WHERE method_link_species_set_id = \"" + key + "\" AND tag = \"" + getTagToCheck() + "\";";
	}

}
