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
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

import com.sun.tools.javac.util.Name;

/**
 * Check that certain seq_region_attribs are present.
 */
public class SeqRegionAttribsPresent extends SingleDatabaseTestCase {

	private String[] attribCodes = { "GeneNo_knwCod" };

	/**
	 * Create a new SeqRegionAttribsPresent healthcheck.
	 */
	public SeqRegionAttribsPresent() {

		addToGroup("release");
		setDescription("Check that certain seq_region_attribs are present on each chromosome");
		setEffect("Webiste gene counts will be wrong");
		setFix("Check and re-run seq_region_attribs.pl script");
		
	}

	/**
	 * This only really applies to core databases
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.ESTGENE);
		removeAppliesToType(DatabaseType.VEGA);
		removeAppliesToType(DatabaseType.CDNA);

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

		List<String> topLevelChrNames = getTopLevelChromosomeNames(con); // Note we only check chromosome names - won't work for species that don't have chromosomes

		for (String chr : topLevelChrNames) {

			if (chr.matches("^Un.*") || chr.matches("^NT_.*") ||chr.matches(".*_random")) {
				continue;
			}

			for (int i = 0; i < attribCodes.length; i++) {

				String code = attribCodes[i];
				int count = getRowCount(con, "SELECT COUNT(*) FROM seq_region sr, attrib_type at, seq_region_attrib sra WHERE sr.seq_region_id=sra.seq_region_id AND sra.attrib_type_id=at.attrib_type_id AND at.code='" + code + "' AND sr.name='" + chr + "'");

				if (count == 0) {

					ReportManager.problem(this, con, "No seq_region_attribs of code " + code + "on toplevel seq region " + chr + " - there should be some. ");
					result = false;

				} else {

					ReportManager.correct(this, con, count + " seq_region_attrib entries for " + code);

				}
			}
		}

		return result;

	} // run

	// -----------------------------------------------------------------

} // SeqRegionAttribsPresent
