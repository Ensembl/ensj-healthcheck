/*
 Copyright (C) 2003 EBI, GRL
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.Utils;
import org.ensembl.healthcheck.util.SqlTemplate;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;


/**
 * Check that certain seq_regions that have known, protein_coding genes have the GeneNo_knwCod attribute associated with them.
 */
public class SeqRegionAttribsPresent extends SingleDatabaseTestCase {

	/**
	 * Create a new SeqRegionAttribsPresent healthcheck.
	 */
	public SeqRegionAttribsPresent() {

		addToGroup("release");
		addToGroup("post-compara-handover");
		
		setDescription("Check that certain seq_regions that have known, protein_coding genes have the GeneNo_knwCod attribute associated with them.");
		setEffect("Website gene counts will be wrong");
		setFix("Re-run ensembl/misc-scripts/density_feature/seq_region_stats.pl script");
		setTeamResponsible(Team.RELEASE_COORDINATOR);
	}

	/**
	 * This only really applies to core databases
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.ESTGENE);
		removeAppliesToType(DatabaseType.VEGA);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.RNASEQ);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database to check.
	 * @return true if the test passes.
	 */
	public boolean run(final DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		String code;
		if (dbre.getType() == DatabaseType.SANGER_VEGA) {
			code = "'KnwnPCCount'";
		} else {
			code = "'GeneNo_knwCod'";
		}

                SqlTemplate t = DBUtils.getSqlTemplate(dbre);
                String sql = "select distinct g.seq_region_id from gene g where g.biotype = 'protein_coding' and g.status = 'KNOWN' and g.seq_region_id not in (select distinct g.seq_region_id from gene g, seq_region_attrib sa, attrib_type at where g.seq_region_id = sa.seq_region_id and sa.attrib_type_id = at.attrib_type_id and at.code in ('LRG', 'non_ref'))" ;
                List<String> toplevel = t.queryForDefaultObjectList(sql, String.class);

                sql = "select distinct g.seq_region_id from gene g, seq_region_attrib sa, attrib_type at where g.seq_region_id = sa.seq_region_id and sa.attrib_type_id = at.attrib_type_id and code = " + code ;
                List<String> known = t.queryForDefaultObjectList(sql, String.class);

                Set<String> missing = new HashSet<String>(toplevel);
                missing.removeAll(known);

		if (missing.isEmpty()) {
                        ReportManager.correct(this, con, "All seq_regions with known, protein_coding genes have a GeneNo_knwCod attribute associated with them");
                } else {
                        for(CharSequence name: missing) {
                                String msg = String.format("Seq_region '%s' with known, protein_coding genes does not have the GeneNo_knwCod attribute associated", name);
                        	ReportManager.problem(this, con, msg);
        			result = false;
	        	}
                }
		return result;

	} // run

	// -----------------------------------------------------------------

} // SeqRegionAttribsPresent
