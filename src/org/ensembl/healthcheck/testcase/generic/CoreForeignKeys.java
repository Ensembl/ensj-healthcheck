/*
 Copyright (C) 2004 EBI, GRL
 
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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key
 * realtionships.
 */

public class CoreForeignKeys extends SingleDatabaseTestCase {

    /**
     * Create an OrphanTestCase that applies to a specific set of databases.
     */
    public CoreForeignKeys() {

        addToGroup("post_genebuild");
        addToGroup("release");
        setDescription("Check for broken foreign-key relationships.");
    }

    /**
     * Look for broken foreign key realtionships.
     * @param dbre
     *          The database to use.
     * @return true Ff all foreign key relationships are valid.
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

        Connection con = dbre.getConnection();

        // ----------------------------

	result &= checkForOrphans(con, "exon", "exon_id", "exon_transcript", "exon_id", false);
	
	result &= checkForOrphans(con, "transcript", "transcript_id", "exon_transcript", "transcript_id", false);
	
	result &= checkForOrphans(con, "gene", "gene_id", "transcript", "gene_id", false);
	
	result &= checkForOrphans(con, "object_xref", "xref_id", "xref", "xref_id", true);
	
	result &= checkForOrphans(con, "xref", "external_db_id", "external_db", "external_db_id", true);
	
	result &= checkForOrphans(con, "dna", "seq_region_id", "seq_region", "seq_region_id", true);
	
	result &= checkForOrphans(con, "seq_region", "coord_system_id", "coord_system", "coord_system_id", true);
	
	result &= checkForOrphans(con, "assembly", "cmp_seq_region_id", "seq_region", "seq_region_id", true);
	
	result &= checkForOrphans(con, "marker_feature", "marker_id", "marker", "marker_id", true);
	
	result &= checkForOrphans(con, "seq_region_attrib", "seq_region_id", "seq_region", "seq_region_id", true);
	
	result &= checkForOrphans(con, "seq_region_attrib", "attrib_type_id", "attrib_type", "attrib_type_id", true);
	
	result &= checkForOrphans(con, "misc_feature_misc_set", "misc_feature_id", "misc_feature", "misc_feature_id",
				  true);
	
	result &= checkForOrphans(con, "misc_feature_misc_set", "misc_set_id", "misc_set", "misc_set_id", true);
	
	result &= checkForOrphans(con, "misc_feature", "misc_feature_id", "misc_attrib", "misc_feature_id", true);
	
	result &= checkForOrphans(con, "misc_attrib", "attrib_type_id", "attrib_type", "attrib_type_id", true);
	
	result &= checkForOrphans(con, "assembly_exception", "seq_region_id", "seq_region", "seq_region_id", true);
	
	result &= checkForOrphans(con, "assembly_exception", "exc_seq_region_id", "seq_region", "seq_region_id", true);
	
	result &= checkForOrphans(con, "protein_feature", "translation_id", "translation", "translation_id", true);
	
	result &= checkForOrphans(con, "marker_synonym", "marker_id", "marker", "marker_id", true);
	
	result &= checkForOrphans(con, "translation_attrib", "translation_id", "translation", "translation_id", true);
	
	result &= checkForOrphans(con, "transcript_attrib", "transcript_id", "transcript", "transcript_id", true);

	result &= checkForOrphans(con, "regulatory_feature", "regulatory_feature_id", "regulatory_feature_object", "regulatory_feature_id", true);

	// ----------------------------
	// Check regulatory features point to existing objects
         String[] rfTypes = {"Gene", "Transcript", "Translation"};
         for (int i = 0; i < rfTypes.length; i++) {
             result &= checkRegulatoryFeatureKeys(con, rfTypes[i]);
         }

         // ----------------------------
         // Check stable IDs all correspond to an existing object
         String[] stableIDtypes = {"gene", "transcript", "translation", "exon"};
         for (int i = 0; i < stableIDtypes.length; i++) {
             result &= checkStableIDKeys(con, stableIDtypes[i]);
         }

         // ----------------------------
         // Ensure that feature tables reference existing seq_regions

         String[] featTabs = {"exon", "repeat_feature", "simple_feature", "dna_align_feature", "protein_align_feature",
                 "marker_feature", "prediction_transcript", "prediction_exon", "gene", "qtl_feature", "transcript",
                 "karyotype"};

         for (int i = 0; i < featTabs.length; i++) {
             String featTab = featTabs[i];
             result &= checkForOrphans(con, featTab, "seq_region_id", "seq_region", "seq_region_id", true);
         }

        return result;

    }

    // -------------------------------------------------------------------------
    private boolean checkStableIDKeys(Connection con, String type) {

        if (tableHasRows(con, type + "_stable_id")) { 

	    return checkForOrphans(con, type, type + "_id", type + "_stable_id", type + "_id", false); 

	}

        return true;

    } // checkStableIDKeys

    // -------------------------------------------------------------------------
    private boolean checkRegulatoryFeatureKeys(Connection con, String type) {

	String table = type.toLowerCase();

	int rows = getRowCount(con, "SELECT COUNT(*) FROM regulatory_feature_object rfo LEFT JOIN " + table + " ON rfo.ensembl_object_id=" + table + "." + table + "_id WHERE rfo.ensembl_object_type=\'" + type + "\' AND rfo.ensembl_object_id IS NULL");

	if (rows > 0) {

	    ReportManager.problem(this, con, rows + " in regulatory_feature refer to non-existent " + table + "s");
	    return false;

	} else {

	    ReportManager.correct(this, con, "All rows in regulatory_feature refer to valid " + table + "s");
	    return true;
	}


    } // checkRegulatoryFeatureKeys

    // -------------------------------------------------------------------------

} // CoreForeignKeys
