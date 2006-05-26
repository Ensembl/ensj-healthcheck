/*
 Copyright (C) 2004 Wellcome Trust Sanger Centre
 
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
import java.sql.ResultSet;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check gene type names and analysis logic names.
 */
public class GeneType extends SingleDatabaseTestCase {

	/*
	 * Valid gene-types are listed in whitelist_type[]; valid analysis (logic
	 * names) are listed in whitelist_analysis[]; the valid combinations of
	 * gene-types/analisis names are definded for each database-type in
	 * whitelist[][][] by their index position in the previous arrays. If the
	 * index of a certain gene-type is listed in the array of a certain analysis
	 * (which is identified by its positional index) in the array of a certain
	 * database (which again is identified by its positional index), it is valid.
	 */

	/*
	 * Define Database-Types
	 */
	private String[] database_types = { "core", // 0
			"otherfeatures", // 1
			"estgene", // 2
			"cdna"  // 3
	};

	/*
	 * Define valid Analysis-Types
	 */
	private String[] whitelist_analysis = { 
                        "ensembl", // 0
			"wormbase", // 1
			"flybase", // 2
			"est_exonerate", // 3
			"estgene", // 4
			"ncRNA", // 5
			"CYT", // 6
			"HOX", // 7
			"GSTEN", // 8
			"cDNA_update", // 9
			"singapore_est", //10 
			"singapore_protein" //11 
	};

	/*
	 * Define valid Gene-Types
	 */
	private String[] whitelist_type = { "ensembl", // 0
			"pseudogene", // 1
			"bacterial_contaminant", // 2
			"snoRNA", // 3
			"snRNA", // 4
			"scRNA", // 5
			"rRNA", // 6
			"misc_RNA", // 7
			"miRNA", // 8
			"estgene", // 9
			"gene", // 10
			"Mt-tRNA-pseudogene", // 11
			"misc_RNA-pseudogene", // 12
			"snRNA-pseudogene", // 13
			"miRNA-pseudogene", // 14
			"tRNA-pseudogene", // 15
			"snoRNA-pseudogene", // 16
			"est", // 17
			"scRNA-pseudogene", // 18
			"rRNA-pseudogene", // 19
			"wormbase", // 20
			"Genoscope_predicted", // 21
			"Genoscope_annotated", // 22
			"Mt-rRNA", // 23
			"Mt-tRNA", // 24
			"protein_coding", // 25
			"tRNA", // 26
			"cDNA_update", // 27

	};

	/*
	 * Define valid Analysis/Gene combinations for the different Database types
	 * syntax: {dbtype_index}{analyis-index}{type-index} example:
	 * whitelist[0][1][10] would be ["core" database]["wormbase" analysis]["gene"
	 * gene type]
	 */
	private int[][][] whitelist = { 
                                         // Block for allowed analysis in CORE-databases 
                                        {       
                                          { 0, 1, 2, 25 },   // 0 - analysis 'ensembl' --> allowed gene_types : ensembl(0),pseudogene(1),protein_coding(25) ...
			                  { 1, 10, 20, 25 }, // 1 - wormbase-analsyis" allowed types in CORE : pseudogene,gene,wormbase,protein_coding
			                  { 1, 3, 4, 6, 7, 10, 25, 26 }, // 2 - allowed types for 'flybase'  
                    		   	  {}, // 3 - est_exonerate   
		                	  {}, // 4 - estgeene 
			                  { 3, 4, 5, 6, 7, 8, 11, 12, 13, 14, 15, 16, 18, 19, 23, 24 }, { 22 }, // 5-Allowed ncRNA genetypes
			                  { 22 }, // 6 - Allowed gene types for CYT 
			                  { 21 }, // 7 - Allowed gene types for HOX
			                  {} , // 8 - Allowed gene types for GSTEN
			                  {} , // 9 - Allowed gene types for cDNA-update 
			                  {} , // 10 - Allowed gene-types for singapore_est in CORE 
			                  {}  // 11 -Allowed gene-types for singapore_est in CORE
                                       }, 
                                         // Block for allowed analysis OTHERFEATURE--databases 
                                       { 
                                         {},     // 0 ensembl
			                 {},     // 1 wormbase 
			                 {},     // 2 flybase 
			                 { 17 }, // 3 est_exonerate 
		                   	 {},     // 4 estgene 
                                         {},     // 5 ncRNA 
                                         {},     // 6  
                                         {},     // 7  
                                         {},     // 8  
                                         {},     // 9  
			                 { 25 }, // 10 Allowed gene-types analysis 'singapore_est' in OTHERFEATUERS db 
			                 { 1,25 } // 11 Allowed gene-types for 'singapore_protein' in OTHERFEATURES db 
                                      }, 
                                      { 
                                         // Block for allowed analysis/genetype-pairs in ESTGENE-db 
                                         {},     // 0 ensembl
			                 {},     // 1 wormbase 
			                 {},     // 2 flybase 
			                 {},     // 3 est_exonerate 
                                         { 9 },  // 4 est_exonerate  
                                         {}, 
                                         {}, 
                                         {}, 
                                         {}, 
                                         {}, 
			                 {} , // Allowed gene-types analysis 'singapore_est' in ESTGENE-db 
			                 {}  // Allowed gene-types for 'singapore_protein' in ESTGENE-db 
                                      }, 
                                      { 
                                         // Block for CDNA_UPDATE dbs 
                                         {}, 
			                 {}, 
                                         {}, 
                                         {}, 
                                         {}, 
                                         {}, 
                                         {}, 
                                         {}, 
                                         { 27 } ,  // cDNA_update is allowed for analyis cDNA_update in CDNA_UPDATE db's 
			                 {} , // Allowed gene-types analysis 'singapore_est' in cDNA-UPDATE dbs 
			                 {},  // Allowed gene-types for 'singapore_protein' in cDNA-UPDATE db's 
                                      },
                                  };

	/**
	 * Create a new testcase.
	 */
	public GeneType() {

		addToGroup("post_genebuild");
		addToGroup("release");
		setDescription("Checks for valid gene type names and analysis logic names " + "and their combination in the different dbs.");

	}

	/**
	 * This test does not apply to Vega databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.VEGA);

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
		int i;

		Connection con = dbre.getConnection();

		// check for existance of gene & analysis tables
		if ((checkTableExists(con, "gene") == false) || (checkTableExists(con, "analysis") == false)) {
			ReportManager.problem(this, con, " not applicable.");
			return true;
		}

		DatabaseType curr_dbtype = dbre.getType();
		String curr_dbtype_name = curr_dbtype.toString();
		int curr_dbtype_int = -1;
		for (i = 0; i < database_types.length; i++) {
			if (curr_dbtype_name.equals(database_types[i])) {
				curr_dbtype_int = i;
				break;
			}
		}
		if (curr_dbtype_int < 0) {
			ReportManager.problem(this, con, " invalid db type (" + curr_dbtype_name + ").");
			return false;
		}

		try {

			String sql = "SELECT g.biotype, a.logic_name FROM gene g, analysis a "
					+ "WHERE g.analysis_id=a.analysis_id GROUP BY g.biotype, a.logic_name;";

			Statement stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
			stmt.setFetchSize(1000);
			ResultSet rs = stmt.executeQuery(sql);

			String genetype;
			String analysisname;
			int analysisid = -1;
			int typeid = -1;
			boolean combination;

			while (rs.next()) {

				analysisid = -1;
				typeid = -1;
				combination = false;

				// load the vars
				genetype = rs.getString(1);
				analysisname = rs.getString(2);

				// check analysis logic name
				for (i = 0; i < whitelist_analysis.length; i++) {
					if (analysisname.equals(whitelist_analysis[i])) {
						logger.fine(analysisname + " accepted as analysis logic name.");
						analysisid = i;
					}
				}

				// check gene type
				for (i = 0; i < whitelist_type.length; i++) {
					if (genetype.equals(whitelist_type[i])) {
						logger.fine(genetype + " accepted as gene type name.");
						typeid = i;
					}
				}

				if ((analysisid >= 0) && (typeid >= 0)) {

					// check combination
					for (i = 0; i < whitelist[curr_dbtype_int][analysisid].length; i++) {
						if (whitelist[curr_dbtype_int][analysisid][i] == typeid) {
							combination = true;
						}
					}
					if (combination == true) {
						logger.fine("combination accepted.");
					} else {
						ReportManager.problem(this, con, genetype + " is not a valid gene type name for " + analysisname + ".");
						result = false;
					}

				} else {
					if ((analysisid < 0) && (typeid < 0)) {
						ReportManager.problem(this, con, "gene type " + genetype + " and analysis " + analysisname + " are invalid names.");
						result = false;
					} else {
						if (analysisid < 0) {
							ReportManager.problem(this, con, analysisname + " (" + genetype + ") is not a valid analysis logic name.");
							result = false;
						} else {
							ReportManager.problem(this, con, genetype + " is not a valid gene type name.");
							result = false;
						}
					}
				}

			} // while rs

			rs.close();
			stmt.close();

		} catch (Exception e) {
			result = false;
			e.printStackTrace();
		}
		return result;

	} // run

} // GeneType
