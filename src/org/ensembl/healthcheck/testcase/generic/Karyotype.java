/*
 * Created on 09-Mar-2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check if any chromosomes that have different lengths in karyotype &
 * seq_region tables.
 */
public class Karyotype extends SingleDatabaseTestCase {

	/**
	 * Create a new Karyotype test case.
	 */
	public Karyotype() {

		addToGroup("post_genebuild");
		addToGroup("release");
		setDescription("Check that karyotype and seq_region tables agree");

	}

	/**
	 * This only applies to core and Vega databases.
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
	 *          The database to use.
	 * @return true if the test pased.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		// This test should fail if the karyotype table is empty
		if (!tableHasRows(con, "karyotype")) {

			// certain species are allowed to have empty karyotype tables
			Species species = dbre.getSpecies();
			if (species == Species.CAENORHABDITIS_BRIGGSAE || species == Species.CAENORHABDITIS_ELEGANS || species == Species.DANIO_RERIO
					|| species == Species.TAKIFUGU_RUBRIPES || species == Species.XENOPUS_TROPICALIS || species == Species.APIS_MELLIFERA
					|| species == Species.PAN_TROGLODYTES || species == Species.SACCHAROMYCES_CEREVISIAE
					|| species == Species.CANIS_FAMILIARIS || species == Species.BOS_TAURUS || species == Species.CIONA_INTESTINALIS
					|| species == Species.TETRAODON_NIGROVIRIDIS || species == Species.GALLUS_GALLUS
					|| species == Species.DASYPUS_NOVEMCINCTUS || species == Species.LOXODONTA_AFRICANA
					|| species == Species.ECHINOPS_TELFAIRI || species == Species.ORNITHORHYCHUS_ANATINUS
					|| species == Species.ORYCTOLAGUS_CUNICULUS || species == Species.ORYZIAS_LATIPES
					|| species == Species.AEDES_AEGYPTI || species == Species.CIONA_SAVIGNYI
					|| species == Species.GASTEROSTEUS_ACULEATUS || species == Species.MACACA_MULATTA
					|| species == Species.MONODELPHIS_DOMESTICA) {

				ReportManager.correct(this, con, "Karyotype table is empty, but this is allowed for " + species.toString());
				return true;

			} else { // if it's not one of those species, it's a problem

				ReportManager.problem(this, con, "Karyotype table is empty");
				return false;

			}

		}

		// check for entry for karyotype in meta_coord; if this is missing,
		// the karyotype won't be displayed on the web, even if the karyotype
		// table is OK
		int rows = getRowCount(con, "SELECT COUNT(*) FROM meta_coord WHERE table_name='karyotype'");
		if (rows == 0) {

			ReportManager.problem(this, con, "No entry for karyotype in meta_coord; no karyotype will be displayed on the website");
			result = false;

		} else {

			ReportManager.correct(this, con, "meta_coord has an entry for karyotype");

		}

		// The seq_region.length and karyotype.length should always be the
		// same.
		// The SQL returns failures

		String karsql = "SELECT sr.name, max(kar.seq_region_end), sr.length " + "FROM seq_region sr, karyotype kar "
				+ "WHERE sr.seq_region_id=kar.seq_region_id " + "GROUP BY kar.seq_region_id "
				+ "HAVING sr.length <> MAX(kar.seq_region_end)";
		int count = 0;
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(karsql);
			if (rs != null) {
				while (rs.next() && count < 50) {
					count++;
					String chrName = rs.getString(1);
					int karLen = rs.getInt(2);
					int chrLen = rs.getInt(3);
					String prob = "";
					int bp = 0;
					if (karLen > chrLen) {
						bp = karLen - chrLen;
						prob = "longer";
					} else {
						bp = chrLen - karLen;
						prob = "shorter";
					}
					result = false;
					ReportManager.problem(this, con, "Chromosome " + chrName + " is " + bp + "bp " + prob + " in the karyotype table than "
							+ "in the seq_region table");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (count == 0) {
			ReportManager.correct(this, con, "Chromosome lengths are the same" + " in karyotype and seq_region tables");
		}

		return result;

	} // run

} // Karyotype
