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
     * Valid gene-types are listed in whitelist_type[];
     * valid analysis (logic names) are listed in whitelist_analysis[];
     * the valid combinations of gene-types/analisis names are definded for each
     * database-type in whitelist[][][] by their index position in the previous arrays.
     * If the index of a certain gene-type is listed in the array of a certain analysis
     * (which is identified by it's positional index) in the array of a certain database
     * (which again is identified by it's positional index), it is valid.
    */

    /*
     * Define Database-Types
     */
    private String[] database_types = {"core",                      //0
				       "est",                       //1
				       "estgene"                    //2
    };

    /*
     * Define valid Analysis-Types
     */
    private String[] whitelist_analysis = {"ensembl",               //0
					   "wormbase",              //1
					   "flybase",               //2
					   "est_exonerate",         //3
					   "estgene",               //4
					   "ncRNA"                  //5
    };

    /*
     * Define valid Gene-Types
     */
    private String[] whitelist_type = {"ensembl",                   //0
				       "pseudogene",                //1
				       "bacterial_contaminant",     //2
				       "snoRNA",                    //3
				       "snRNA",                     //4
				       "scRNA",                     //5
				       "rRNA",                      //6
				       "misc_RNA",                  //7
				       "miRNA",                     //8
				       "estgene",                   //9
				       "gene",                      //10
				       "Mt-tRNA-pseudogene",        //11
				       "misc_RNA-pseudogene",       //12
				       "snRNA-pseudogene",          //13
				       "miRNA-pseudogene",          //14
				       "tRNA-pseudogene",           //15
				       "snoRNA-pseudogene",         //16
				       "est",                       //17
				       "scRNA-pseudogene",          //18
				       "rRNA-pseudogene",           //19
				       "wormbase"                   //20
    };


    /*
     * Define valid Analysis/Gene combinations
     * for the different Database types
     * syntax: {dbtype_index}{analyis-index}{type-index}
     * example: whitelist[0][2][1] would be 
     *      ["core" database]["wormbase" analysis]["gene" gene type]
     */
    private int[][][] whitelist =    {  {  {0,1,2},    //this block    //this array for "ensembl" analysis'
					   {1,10,20},  //for CORE      //this array for "wormbase" analysis'
					   {1,10},     //databases     //this array for "flybase" analysis'
					   {},         //
					   {},         //
					   {3,4,5,6,7,8,11,12,13,14,15,16,18,19}
                                        },
					{  {},         //this block
					   {},         //for EST
					   {},         //databases
					   {17}        // ...
					},
					{  {},
					   {},
					   {},
					   {},
					   {9}
					}
    };


    /**
     * Create a new testcase.
     */
    public GeneType() {

        addToGroup("post_genebuild");
        addToGroup("release");
        setDescription("Checks for valid gene type names and analysis logic names "
		       + "and their combination in the different dbs.");

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

	//check for existance of gene & analysis tables
	if( (checkTableExists(con,"gene")==false) || (checkTableExists(con,"analysis")==false) ){
	    ReportManager.problem(this, con, " not applicable.");
	    return true;
	}
		    
	DatabaseType curr_dbtype =  dbre.getType();
	String curr_dbtype_name = curr_dbtype.toString();
	int curr_dbtype_int=-1;
	for (i = 0; i < database_types.length; i++){
	    if( curr_dbtype_name.equals(database_types[i]) ){
		curr_dbtype_int = i;
		break;
	    }
	}
	if(curr_dbtype_int < 0){
	    ReportManager.problem(this, con, " invalid db type (" + curr_dbtype_name + ")."); 
	    return false;
	}
	
	try {	
	    
	    String sql = "SELECT g.type, a.logic_name FROM gene g, analysis a "
		+ "WHERE g.analysis_id=a.analysis_id GROUP BY g.type, a.logic_name;";

            Statement stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
                    java.sql.ResultSet.CONCUR_READ_ONLY);
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

		//check analysis logic name
		for (i = 0; i < whitelist_analysis.length; i++){
		    if( analysisname.equals(whitelist_analysis[i]) ){
			ReportManager.info(this, con, analysisname + " accepted as analysis logic name.");
			analysisid = i;
		    }
		}

		//check gene type
		for (i = 0; i < whitelist_type.length; i++){
		    if( genetype.equals(whitelist_type[i]) ){
			ReportManager.info(this, con, genetype + " accepted as gene type name.");
			typeid = i;
		    }
		}

		if( (analysisid >= 0) && (typeid >= 0) ){
			
		    //check combination
		    for(i = 0; i < whitelist[curr_dbtype_int][analysisid].length; i++){
			if(whitelist[curr_dbtype_int][analysisid][i] == typeid){
			    combination = true;
			}
		    }
		    if(combination == true){
			ReportManager.info(this, con, " combination accepted.");
		    }
		    else{
			ReportManager.problem(this, con, genetype + " is not a valid gene type name for "
					      + analysisname + ".");
			result = false;
		    }

		}
		else{
		    if( (analysisid < 0) && (typeid < 0) ){
			ReportManager.problem(this, con, " gene type " + genetype + " and analysis " 
					      + analysisname + " are invalid names.");
			result = false;
		    }
		    else{
			if(analysisid < 0){
			    ReportManager.problem(this, con, analysisname + " (" + genetype 
						  + ") is not a valid analysis logic name.");
			    result = false;
		        }
			else{
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
