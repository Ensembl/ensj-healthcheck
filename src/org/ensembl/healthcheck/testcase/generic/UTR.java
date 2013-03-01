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

package org.ensembl.healthcheck.testcase.generic;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.DecimalFormat;


import org.ensembl.healthcheck.AssemblyNameInfo;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.SqlTemplate;
import org.ensembl.healthcheck.util.Utils;


/**
 * Checks that changes between releases (assembly, repeatmasking or gene set) have been declared. 
 */


public class UTR extends SingleDatabaseTestCase {

        public UTR() {

                addToGroup("post_genebuild");
                addToGroup("release");
                addToGroup("compara-ancestral");
                addToGroup("pre-compara-handover");
                addToGroup("post-compara-handover");

                setTeamResponsible(Team.GENEBUILD);
                setDescription("Check that coding transcripts have UTR attached");
        }

        /**
         * This test applies only to core dbs
         */
        public void types() {
                removeAppliesToType(DatabaseType.SANGER_VEGA);
                removeAppliesToType(DatabaseType.VEGA);
                removeAppliesToType(DatabaseType.CDNA);
                removeAppliesToType(DatabaseType.OTHERFEATURES);
                removeAppliesToType(DatabaseType.RNASEQ);
        }


        /**
         * Look for UTRs
         * 
         * @param dbre
         *          The database to check.
         * @return True if the test passed.
         */

        public boolean run(final DatabaseRegistryEntry dbre) {

                boolean result = true;

                Connection con = dbre.getConnection();

                result &= countUTR(dbre);

                return result;
        }

  private boolean countUTR(DatabaseRegistryEntry dbre) {

    boolean result = true;

    Connection con = dbre.getConnection();
    DecimalFormat twoDForm = new DecimalFormat("#.##");

    String utr_sql = "SELECT count(distinct(gene_id)) FROM exon e, exon_transcript et, transcript t WHERE e.exon_id=et.exon_id AND et.transcript_id=t.transcript_id AND t.biotype = 'protein_coding' AND phase = -1";
    int utrTranscript = DBUtils.getRowCount(con, utr_sql);
    String coding_sql = "SELECT count(distinct(gene_id)) FROM transcript WHERE biotype = 'protein_coding'";
    int codingTranscript = DBUtils.getRowCount(con, coding_sql);

    double percentage = (( (double) utrTranscript / (double) codingTranscript) * 100);
    float comp = Float.valueOf(twoDForm.format(percentage));

    if (comp < 50) {
      result = false;
      ReportManager.info(this, con, "Only " + comp + " % coding transcripts have UTRs");
    }

    return result;
  }

}





