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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.RowMapper;
import org.ensembl.healthcheck.util.SqlTemplate;


/**
 * Check that certain seq_regions that have known, protein_coding genes 
 * have the coding_cnt attribute associated with them. Also ensure
 * that _rna_edit attributes represent substitutions
 */
public class SeqRegionAttribsPresent extends SingleDatabaseTestCase {

	/**
	 * Create a new SeqRegionAttribsPresent healthcheck.
	 */
	public SeqRegionAttribsPresent() {

		setDescription("Check that certain seq_regions that have protein_coding genes have the coding_cnt attribute associated with them. Also ensure that _rna_edit attributes represent substitutions");
		setEffect("Website gene counts will be wrong and API will fail to load");
		setFix("Re-run density generation pipeline or edit the database to remove the offending attribute");
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
		result &= checkCodingCountAttributes(dbre);
		result &= checkRnaEditAttributes(dbre);
		return result;
	} // run
	
	private boolean checkCodingCountAttributes(final DatabaseRegistryEntry dbre) {
	  boolean result = true;

    Connection con = dbre.getConnection();
    String code = (dbre.getType() == DatabaseType.SANGER_VEGA) ? "KnwnPCCount" : "coding_cnt";

    SqlTemplate t = DBUtils.getSqlTemplate(dbre);
    String sql = "select distinct g.seq_region_id from gene g where g.biotype = ? and g.seq_region_id not in (select distinct g.seq_region_id from gene g, seq_region_attrib sa, attrib_type at where g.seq_region_id = sa.seq_region_id and sa.attrib_type_id = at.attrib_type_id and at.code in (?,?))" ;
    List<String> toplevel = t.queryForDefaultObjectList(sql, String.class, "protein_coding", "LRG", "non_ref");

    sql = "select distinct g.seq_region_id from gene g, seq_region_attrib sa, attrib_type at where g.seq_region_id = sa.seq_region_id and sa.attrib_type_id = at.attrib_type_id and code =? ";
    List<String> known = t.queryForDefaultObjectList(sql, String.class, code);

    Set<String> missing = new HashSet<String>(toplevel);
    missing.removeAll(known);

    if (missing.isEmpty()) {
      ReportManager.correct(this, con, "All seq_regions with protein_coding genes have a coding_cnt attribute associated with them");
    } else {
      String msg = String.format("%s regions with protein_coding genes do not have the coding_cnt attribute associated", missing.size());
      ReportManager.problem(this, con, msg);
      result = false;
    }
    
    return result;
	}
	
	/**
	 * Check that any _rna_edit attribute represents a substitution rather
	 * than an insertion or deletion
	 */
	private boolean checkRnaEditAttributes(final DatabaseRegistryEntry dbre) {
    boolean ok = true;
    
    RowMapper<Attrib> mapper = new RowMapper<Attrib>() {
      public Attrib mapRow(ResultSet rs, int row) throws SQLException {
        return new Attrib(rs.getString(1), rs.getLong(2), rs.getString(3));
      }
    };
    
    String sql = "select sr.name, sr.seq_region_id, sra.value "
        + "from seq_region sr join seq_region_attrib sra using (seq_region_id) "
        + "join attrib_type at using (attrib_type_id) where at.code =?";

    List<Attrib> attributes = getSqlTemplate(dbre).queryForList(sql, mapper, "_rna_edit");
    
    for (Attrib a : attributes) {
      if (!a.isOk()) {
        ReportManager.warning(this, dbre.getConnection(), a.toString());
        ok = false;
      }
    }
    if (!ok) {
      ReportManager.problem(this, dbre.getConnection(),
        "Detected sequence regions with incorrectly formatted _rna_edit attributes. Check warnings");
    }
    
    return ok;
  }

  /**
   * Only to be used in this class
   */
  private static class Attrib {
    private final String seqRegionName;
    private final Long seqRegionId;
    private final String attribute;

    private final Integer editStart;
    private final Integer editEnd;
    private final String editString;

    public Attrib(final String seqRegionName, final Long seqRegionId,
        final String attribute) {
      this.seqRegionName = seqRegionName;
      this.seqRegionId = seqRegionId;
      this.attribute = attribute;
      Scanner sc = new Scanner(attribute).useDelimiter("\\s+");
      editStart = sc.nextInt();
      editEnd = sc.nextInt();
      editString = sc.next();
      sc.close();
    }

    public boolean isOk() {
      return editLength() == editStringLength();
    }

    public int editLength() {
      return ((editEnd - editStart) + 1);
    }

    public int editStringLength() {
      return (editString != null) ? editString.length() : 0;
    }

    public String editType() {
      int editStringLength = editStringLength();
      return (editStringLength == 0) ? "a deletion" : "an insertion";
    }

    public String toString() {
      return "Sequence Region " + seqRegionName + " (" + seqRegionId
          + ") has an incorrectly formatted _rna_edit attribute '" + attribute
          + "'. Edit length was " + editLength()
          + " but insert string length was " + editStringLength()
          + ". Mostly likely " + editType();
    }
  }

	// -----------------------------------------------------------------

} // SeqRegionAttribsPresent
