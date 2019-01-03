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


package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * Healthcheck for the assembly_exception table.
 */

public class AssemblyExceptions extends SingleDatabaseTestCase {

	/**
	 * Check the assembly_exception table.
	 */



	public AssemblyExceptions() {
		setDescription("Check assembly_exception table");
		setTeamResponsible(Team.GENEBUILD);

	}

        public void types() {

                removeAppliesToType(DatabaseType.ESTGENE);
                removeAppliesToType(DatabaseType.CDNA);
                removeAppliesToType(DatabaseType.VEGA);
                removeAppliesToType(DatabaseType.SANGER_VEGA);
                removeAppliesToType(DatabaseType.OTHERFEATURES);
                removeAppliesToType(DatabaseType.RNASEQ);

        }

	/**
	 * Check the data in the assembly_exception table. Note referential integrity checks are done in CoreForeignKeys.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return Result.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

                result &= checkStartEnd(dbre);
                result &= seqMapping(dbre);
                result &= uniqueRegion(dbre);
                result &= checkExternalDB(dbre);

                return result;
        }

        private boolean checkExternalDB(DatabaseRegistryEntry dbre) {

                boolean result = false;

                SqlTemplate t = DBUtils.getSqlTemplate(dbre);
                Connection con = dbre.getConnection();
                String unique_sql = "SELECT distinct sr.name FROM seq_region sr, assembly_exception ax, external_db e, "
                       + " dna_align_feature daf, analysis a WHERE a.analysis_id = daf.analysis_id AND "
                       + " daf.seq_region_id = sr.seq_region_id AND ax.seq_region_id = sr.seq_region_id AND "
                       + " e.external_db_id = daf.external_db_id AND logic_name = 'alt_seq_mapping' AND "
                       + " exc_type not in ('PAR') AND e.db_name != 'GRC_primary_assembly'" ;
                List<String> unique_regions = t.queryForDefaultObjectList(unique_sql, String.class);

                if (unique_regions.isEmpty()) {
                     result = true;
                }

                for (String region: unique_regions) {
                     String msg = String.format("Assembly exception %s has a mapping which is not from 'GRC_primary_assembly'", region);
                     ReportManager.problem(this, dbre.getConnection(), msg);
                }

                return result;
        }

        private boolean uniqueRegion(DatabaseRegistryEntry dbre) {

                boolean result = false;

                SqlTemplate t = DBUtils.getSqlTemplate(dbre);
                Connection con = dbre.getConnection();
                String unique_sql = "SELECT distinct sr.name FROM seq_region sr, assembly_exception ax, seq_region sr2, " 
                       + " dna_align_feature daf, analysis a WHERE a.analysis_id = daf.analysis_id AND "
                       + " daf.seq_region_id = sr.seq_region_id AND ax.seq_region_id = sr.seq_region_id AND "
                       + " ax.exc_seq_region_id = sr2.seq_region_id AND logic_name = 'alt_seq_mapping' AND "
                       + " exc_type not in ('PAR') AND sr2.name != hit_name" ;
                List<String> unique_regions = t.queryForDefaultObjectList(unique_sql, String.class);

                if (unique_regions.isEmpty()) {
                     result = true;
                }

                for (String region: unique_regions) {
                     String msg = String.format("Assembly exception %s maps more than one reference region", region);
                     ReportManager.problem(this, dbre.getConnection(), msg);
                }

                return result;
        }

        private boolean seqMapping(DatabaseRegistryEntry dbre) {

                boolean result = false;

                SqlTemplate t = DBUtils.getSqlTemplate(dbre);
                Connection con = dbre.getConnection();
                String all_sql = "SELECT distinct sr.name FROM seq_region sr, assembly_exception ax where ax.seq_region_id = sr.seq_region_id and exc_type not in ('PAR')";
                List<String> all_exc = t.queryForDefaultObjectList(all_sql, String.class);

                String daf_sql = "SELECT distinct sr.name FROM seq_region sr, assembly_exception ax, dna_align_feature daf, analysis a "
                       + " WHERE sr.seq_region_id = ax.seq_region_id AND exc_type not in ('PAR') AND sr.seq_region_id = daf.seq_region_id "
                       + " AND daf.analysis_id = a.analysis_id AND a.logic_name = 'alt_seq_mapping'";
                List<String> daf_exc = t.queryForDefaultObjectList(daf_sql, String.class);

                Set<String> missing = new HashSet<String>(all_exc);
                missing.removeAll(daf_exc);

                if(missing.isEmpty()) {
                     result = true;
                }
                for(String name: missing) {
                     String msg = String.format("Assembly exception '%s' does not have results in dna_align_feature table for analysis alt_seq_mapping", name);
                     ReportManager.problem(this, dbre.getConnection(), msg);
                }

                return result;
        }


        private boolean checkStartEnd(DatabaseRegistryEntry dbre) {

                Connection con = dbre.getConnection();
                boolean result = true;

		// check that seq_region_end > seq_region_start
		int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM assembly_exception WHERE seq_region_start > seq_region_end");
		if (rows > 0) {
			result = false;
			ReportManager.problem(this, con, "assembly_exception has " + rows + " rows where seq_region_start > seq_region_end");
		}

		// check that exc_seq_region_start > exc_seq_region_end
		rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM assembly_exception WHERE exc_seq_region_start > exc_seq_region_end");
		if (rows > 0) {
			result = false;
			ReportManager.problem(this, con, "assembly_exception has " + rows + " rows where exc_seq_region_start > exc_seq_region_end");
		}

		// If the assembly_exception table contains an exception of type 'HAP' then
		// there should be at least one seq_region_attrib row of type 'non-reference'
		if (DBUtils.getRowCount(con, "SELECT COUNT(*) FROM assembly_exception WHERE exc_type='HAP'") > 0) {

			if (DBUtils.getRowCount(con, "SELECT COUNT(*) FROM seq_region_attrib sra, attrib_type at WHERE sra.attrib_type_id=at.attrib_type_id AND at.code='non_ref'") == 0) {
				result = false;
				ReportManager.problem(this, con, "assembly_exception contains at least one exception of type 'HAP' but there are no seq_region_attrib rows of type 'non-reference'");
			}

		}

		if (result) {
			ReportManager.correct(this, con, "assembly_exception start/end co-ordinates make sense");
		}

		return result;

	}

} // AssemblyException
