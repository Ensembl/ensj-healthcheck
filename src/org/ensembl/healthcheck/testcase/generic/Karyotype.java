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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.SqlTemplate;


/**
 * Check if any chromosomes that have different lengths in karyotype & seq_region tables.
 */
public class Karyotype extends SingleDatabaseTestCase {

	/**
	 * Create a new Karyotype test case.
	 */
	public Karyotype() {

		addToGroup("post_genebuild");
		addToGroup("release");
		addToGroup("post-compara-handover");
		
		setDescription("Check that karyotype and seq_region tables agree");
		setTeamResponsible(Team.CORE);

	}

	/**
	 * This only applies to core and Vega databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.ESTGENE);
		removeAppliesToType(DatabaseType.VEGA);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if the test passed.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

                result &= karyotypeExists(dbre);

		// don't check for empty karyotype table - this is done in EmptyTables
		// meta_coord check also done in MetaCoord

		// The seq_region.length and karyotype.length should always be the
		// same.
		// The SQL returns failures

		String karsql = "SELECT sr.name, max(kar.seq_region_end), sr.length " + "FROM seq_region sr, karyotype kar " + "WHERE sr.seq_region_id=kar.seq_region_id " + "GROUP BY kar.seq_region_id "
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
					ReportManager.problem(this, con, "Chromosome " + chrName + " is " + bp + "bp " + prob + " in the karyotype table than " + "in the seq_region table");
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


        protected boolean karyotypeExists(DatabaseRegistryEntry dbre) {
                Connection con = dbre.getConnection();
                SqlTemplate t = DBUtils.getSqlTemplate(dbre);
                boolean result = true;
                String sqlCS = "SELECT count(*) FROM coord_system WHERE name = 'chromosome'";
                String sqlAttrib = "SELECT count(*) FROM seq_region_attrib sa, attrib_type at WHERE at.attrib_type_id = sa.attrib_type_id AND code = 'karyotype_rank'";
                String sqlMT = "SELECT count(*) FROM seq_region_attrib sa, attrib_type at, seq_region s WHERE at.attrib_type_id = sa.attrib_type_id AND code = 'karyotype_rank' AND s.name = 'MT'";
                int karyotype = t.queryForDefaultObject(sqlCS, Integer.class);
                if (karyotype > 0) {
                        int attrib = t.queryForDefaultObject(sqlAttrib, Integer.class);
                        if (attrib == 0) {
                                result = false;
                                ReportManager.problem(this, con, "Chromosome entry exists but no karyotype attrib is present");
                        }
                        int mt = t.queryForDefaultObject(sqlMT, Integer.class);
                        if (attrib == 0) {
                                result = false;
                                ReportManager.problem(this, con, "Species has a karyotype but MT is not part of it");
                        }
                } 
        return result;
        }

} // Karyotype
