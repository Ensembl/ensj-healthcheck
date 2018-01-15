/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2018] EMBL-European Bioinformatics Institute
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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.compara.AbstractComparaTestCase;
import org.ensembl.healthcheck.util.DBUtils;


/**
 * Check that the Compara pipelines have generated stable IDs
 */

public class CheckComparaStableIDs extends AbstractComparaTestCase {

	public CheckComparaStableIDs() {
		setDescription("Check that gene-trees and families have stable IDs");
		setTeamResponsible(Team.COMPARA);
	}

	public boolean run(final DatabaseRegistryEntry comparaDbre) {
		boolean result = true;
		result &= checkCountIsZero(comparaDbre.getConnection(), "family", "stable_id IS NULL");
		result &= checkCountIsZero(comparaDbre.getConnection(), "gene_tree_root", "member_type = 'protein' AND tree_type = 'tree' AND clusterset_id='default' AND stable_id IS NULL");
		return result;
	}

} // CheckComparaStableIDs
