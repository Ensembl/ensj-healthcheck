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

/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.funcgen;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.generic.CoreForeignKeys;
import org.ensembl.healthcheck.Team;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key relationships.
 */

public class FuncgenForeignKeys extends CoreForeignKeys {

	/**
	 * Create an OrphanTestCase that applies to a specific set of databases.
	 */
	public FuncgenForeignKeys() {

		addToGroup("post_regbuild");
		addToGroup("funcgen-release");
		addToGroup("funcgen");
		setDescription("Check for broken foreign-key relationships.");
		setHintLongRunning(true);
		setTeamResponsible(Team.FUNCGEN);
		removeSecondTeamResponsible(); // Does not appear to be imported
	}

	/**
	 * Look for broken foreign key relationships.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if all foreign key relationships are valid.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

        Connection con = dbre.getConnection();

        try{

            result &= checkForOrphans(con, "alignment", "analysis_id", "analysis", "analysis_id", true);
//            result &= checkForOrphans(con, "alignment", "bam_file_id", "data_file", "data_file_id", true);
//            result &= checkForOrphans(con, "alignment", "bigwig_file_id", "data_file", "data_file_id", true);

            result &= checkForOrphans(con, "alignment_read_file", "alignment_id", "alignment", "alignment_id", true);
            result &= checkForOrphans(con, "alignment_read_file", "read_file_id", "read_file", "read_file_id", true);

            result &= checkForOrphans(con, "analysis_description", "analysis_id", "analysis", "analysis_id", true);

            result &= checkForOrphans(con, "array_chip", "array_id", "array", "array_id", true);

            result &= checkForOrphans(con, "associated_feature_type", "feature_type_id", "feature_type", "feature_type_id", true);

            try {
                ResultSet rs = con.createStatement().executeQuery("SELECT distinct(table_name) from associated_feature_type");

                while (rs.next()) {
                    String tableName = rs.getString(1);
                    result &= checkForOrphansWithConstraint(con, "associated_feature_type", "table_id", tableName, tableName + "_id", "table_name='" + tableName + "'");
                }
                rs.close();
            } catch (SQLException se) {
                se.printStackTrace();
                return false;
            }

            result &= checkForOrphans(con, "associated_motif_feature", "motif_feature_id", "motif_feature", "motif_feature_id", true);

            result &= checkForOrphans(con, "associated_xref", "object_xref_id", "object_xref", "object_xref_id", true);
            result &= checkForOrphans(con, "associated_xref", "xref_id", "xref", "xref_id", true);
            result &= checkForOrphans(con, "associated_xref", "associated_group_id", "associated_group", "associated_group_id", true);

            try {
                ResultSet rs = con.createStatement().executeQuery("SELECT distinct(table_name) from data_file where table_name != 'alignment'");

                while (rs.next()){
                    String tableName   = rs.getString(1);
                    result &= checkForOrphansWithConstraint(con, "data_file", "table_id", tableName, tableName + "_id", "table_name='" + tableName + "'");
                }
                rs.close();
            }
            catch (SQLException se) {
                se.printStackTrace();
                return false;
            }

            result &= checkForOrphans(con, "experiment", "experimental_group_id", "experimental_group", "experimental_group_id", true);
            result &= checkForOrphans(con, "experiment", "feature_type_id", "feature_type", "feature_type_id", true);
            result &= checkForOrphansWithConstraint(con, "experiment", "epigenome_id", "epigenome", "epigenome_id", "epigenome_id != 0");

            result &= checkForOrphans(con, "external_feature", "feature_set_id", "feature_set", "feature_set_id", true);
            result &= checkForOrphans(con, "external_feature", "feature_type_id", "feature_type", "feature_type_id", true);

            result &= checkForOrphans(con, "external_feature_file", "analysis_id", "analysis", "analysis_id", true);
            result &= checkForOrphansWithConstraint(con, "external_feature_file", "epigenome_id", "epigenome", "epigenome_id", "epigenome_id != 0");
            result &= checkForOrphansWithConstraint(con, "external_feature_file", "feature_type_id", "feature_type", "feature_type_id", "feature_type_id != 0");

            result &= checkForOrphans(con, "external_synonym", "xref_id", "xref", "xref_id", true);

            result &= checkForOrphans(con, "feature_set", "feature_type_id", "feature_type", "feature_type_id", true);
            result &= checkForOrphans(con, "feature_set", "analysis_id", "analysis", "analysis_id", true);

//            result &= checkForOrphans(con, "feature_type", "analysis_id", "analysis", "analysis_id", true);

            result &= checkForOrphans(con, "identity_xref", "object_xref_id", "object_xref", "object_xref_id", true);

            result &= checkForOrphans(con, "mirna_target_feature", "feature_type_id", "feature_type", "feature_type_id", true);
            result &= checkForOrphans(con, "mirna_target_feature", "feature_set_id", "feature_set", "feature_set_id", true);

            result &= checkForOrphans(con, "motif_feature", "binding_matrix_id", "binding_matrix", "binding_matrix_id", true);

            result &= checkForOrphans(con, "object_xref", "xref_id", "xref", "xref_id", true);
            try {
                ResultSet rs = con.createStatement().executeQuery("SELECT distinct(ensembl_object_type) from object_xref");

                while (rs.next()){
                    String objType   = rs.getString(1);
                    result &= checkKeysByEnsemblObjectType(con, "object_xref", objType);
                }

                rs.close();
            }
            catch (SQLException se) {
                se.printStackTrace();
                return false;
            }
//            result &= checkForOrphans(con, "object_xref", "analysis_id", "analysis", "analysis_id", true);

            result &= checkForOrphans(con, "ontology_xref", "object_xref_id", "object_xref", "object_xref_id", true);

            result &= checkForOrphans(con, "peak", "peak_calling_id", "peak_calling", "peak_calling_id", true);

//            result &= checkForOrphans(con, "probe", "probe_set_id", "probe_set", "probe_set_id", false);
            result &= checkForOrphansWithConstraint(con, "probe", "probe_set_id", "probe_set", "probe_set_id", "probe_set_id !=0");
            result &= checkForOrphans(con, "probe", "array_chip_id", "array_chip", "array_chip_id", false);
            result &= checkForOrphans(con, "probe", "probe_seq_id", "probe_seq", "probe_seq_id", false);

            result &= checkForOrphans(con, "probe_feature", "probe_id", "probe", "probe_id", true);
            result &= checkForOrphans(con, "probe_feature", "analysis_id", "analysis", "analysis_id", true);

            result &= checkForOrphans(con, "probe_feature_transcript", "probe_feature_id", "probe_feature", "probe_feature_id", true);

            result &= checkForOrphans(con, "probe_set", "array_chip_id", "array_chip", "array_chip_id", true);

            result &= checkForOrphans(con, "probe_set_transcript", "probe_set_id", "probe_set", "probe_set_id", true);

            result &= checkForOrphans(con, "read_file", "analysis_id", "analysis", "analysis_id", true);

            result &= checkForOrphans(con, "read_file_experimental_configuration", "read_file_id", "read_file", "read_file_id", true);
            result &= checkForOrphans(con, "read_file_experimental_configuration", "experiment_id", "experiment", "experiment_id", true);

            result &= checkForOrphans(con, "regulatory_activity", "regulatory_feature_id", "regulatory_feature", "regulatory_feature_id", true);
            result &= checkForOrphans(con, "regulatory_activity", "epigenome_id", "epigenome", "epigenome_id", true);

            result &= checkForOrphans(con, "regulatory_build", "feature_type_id", "feature_type", "feature_type_id", true);
            result &= checkForOrphans(con, "regulatory_build", "analysis_id", "analysis", "analysis_id", true);
            result &= checkForOrphans(con, "regulatory_build", "sample_regulatory_feature_id", "regulatory_feature", "regulatory_feature_id", true);

            result &= checkForOrphans(con, "regulatory_build_epigenome", "regulatory_build_id", "regulatory_build", "regulatory_build_id", true);
            result &= checkForOrphans(con, "regulatory_build_epigenome", "epigenome_id", "epigenome", "epigenome_id", true);

            result &= checkForOrphans(con, "regulatory_feature", "feature_type_id", "feature_type", "feature_type_id", true);
            result &= checkForOrphans(con, "regulatory_feature", "regulatory_build_id", "regulatory_build", "regulatory_build_id", true);

            result &= checkForOrphans(con, "segmentation_file", "regulatory_build_id", "regulatory_build", "regulatory_build_id", true);
            result &= checkForOrphans(con, "segmentation_file", "analysis_id", "analysis", "analysis_id", true);
            result &= checkForOrphans(con, "segmentation_file", "epigenome_id", "epigenome", "epigenome_id", true);

            result &= checkForOrphans(con, "unmapped_object", "analysis_id", "analysis", "analysis_id", true);
            result &= checkOptionalRelation(con, "unmapped_object", "external_db_id", "external_db", "external_db_id");
            result &= checkForOrphans(con, "unmapped_object", "unmapped_reason_id", "unmapped_reason", "unmapped_reason_id", true);

            result &= checkForOrphans(con, "xref", "external_db_id", "external_db", "external_db_id", true);//shouldn't this be false?
		}
		catch (Exception e) { //Catch all possible exceptions
            ReportManager.problem(this, con, "HealthCheck generated an " +
                    "exception:\n\t" + e.getMessage());
            result = false;
        }

		return result;
	}
} // FuncgenForeignKeys
