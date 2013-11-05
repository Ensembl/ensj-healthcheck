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
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.SqlTemplate;
import org.apache.commons.collections.ListUtils;
import org.ensembl.healthcheck.util.CollectionUtils;



/**
 * Check if any chromosomes that have different lengths in karyotype & seq_region tables.
 */
public class Karyotype extends SingleDatabaseTestCase {

	/**
	 * Create a new Karyotype test case.
	 */
	public Karyotype() {

		addToGroup("post_genebuild");
		addToGroup("post-compara-handover");
                addToGroup("pre-compara-handover");
		
		setDescription("Check that karyotype and seq_region tables agree");
		setTeamResponsible(Team.GENEBUILD);

	}

	/**
	 * This only applies to core and Vega databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.ESTGENE);
		removeAppliesToType(DatabaseType.VEGA);
                removeAppliesToType(DatabaseType.OTHERFEATURES);
                removeAppliesToType(DatabaseType.CDNA);
                removeAppliesToType(DatabaseType.RNASEQ);

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
                Species species = dbre.getSpecies();
                Set<Species> karyotypeSpecies =
                      CollectionUtils.createLinkedHashSet(Species.DROSOPHILA_MELANOGASTER,
                                                Species.HOMO_SAPIENS,
                                                Species.MUS_MUSCULUS,
                                                Species.RATTUS_NORVEGICUS);

                result &= karyotypeExists(dbre);

		// don't check for empty karyotype table - this is done in EmptyTables
		// meta_coord check also done in MetaCoord

		// The seq_region.length and karyotype.length should always be the
		// same.
		// The SQL returns failures

                 if(karyotypeSpecies.contains(species) == false) {
                         return true;
                 }

                 String[] seqRegionNames = DBUtils.getColumnValues(con,
                                "SELECT s.name FROM seq_region s, coord_system cs WHERE s.coord_system_id=cs.coord_system_id AND cs.name='chromosome' AND cs.attrib='default_version' AND s.name NOT LIKE 'LRG%' AND s.name != 'MT'");

                 String[] patches = DBUtils.getColumnValues(con, "SELECT sr.name FROM seq_region sr, assembly_exception ae WHERE sr.seq_region_id=ae.seq_region_id AND ae.exc_type IN ('PATCH_NOVEL', 'PATCH_FIX', 'HAP')");
                 List<String> patchList = Arrays.asList(patches);
                 List<String> nonPatchSeqRegions = ListUtils.removeAll(Arrays.asList(seqRegionNames), patchList);

		int count = 0;
		try {
			PreparedStatement stmt = con.prepareStatement("SELECT sr.name, MAX(kar.seq_region_end), sr.length FROM seq_region sr, karyotype kar WHERE sr.seq_region_id=kar.seq_region_id AND sr.name = ? GROUP BY kar.seq_region_id");

                        for (String seqRegion : seqRegionNames) {
                                stmt.setString(1, seqRegion);
        			ResultSet rs = stmt.executeQuery();
                                boolean hasKaryotype = false;
                                while (rs.next() && count < 50) {
                                        hasKaryotype = true;
                                        if (patchList.contains(seqRegion)) { continue; }
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
                                        if (bp > 0) {
        		        		result = false;
        					ReportManager.problem(this, con, "Chromosome " + chrName + " is " + bp + "bp " + prob + " in the karyotype table than " + "in the seq_region table");
                                        }
                                }
                                if (!hasKaryotype) {
                                        result = false;
                                        ReportManager.problem(this, con, "Chromosome " + seqRegion + " has no karyotype data");
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
                String sqlMT = "SELECT count(*) FROM seq_region_attrib sa, attrib_type at, seq_region s WHERE s.seq_region_id = sa.seq_region_id AND at.attrib_type_id = sa.attrib_type_id AND code = 'karyotype_rank' AND s.name IN ('MT', 'Mito', 'dmel_mitochondrion_genome', 'MtDNA')";
                int karyotype = t.queryForDefaultObject(sqlCS, Integer.class);
                if (karyotype > 0) {
                        int attrib = t.queryForDefaultObject(sqlAttrib, Integer.class);
                        if (attrib == 0) {
                                result = false;
                                ReportManager.problem(this, con, "Chromosome entry exists but no karyotype attrib is present");
                        }
                        int mt = t.queryForDefaultObject(sqlMT, Integer.class);
                        if (mt == 0 && dbre.getType() != DatabaseType.SANGER_VEGA) {
                                result = false;
                                ReportManager.problem(this, con, "Species has chromosomes but neither MT nor Mito nor dmel_mitochondrion_genome nor MtDNA");
                        }
                } 
        return result;
        }

} // Karyotype
