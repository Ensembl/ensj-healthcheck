/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2018] EMBL-European Bioinformatics Institute
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


package org.ensembl.healthcheck.testcase.variation;

import java.sql.Connection;
import java.util.List;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.RowMapper;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * Checks database entries do not contain unsupported characters.
 */
public class CheckChar extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of CheckChar
	 */
	public CheckChar() {

		addToGroup("variation-release");
		setDescription("Check that imported names/descriptions contains only supported characters");
		setTeamResponsible(Team.VARIATION);

	}

	/**
	 * Check only for Phenotype table currently
	 * 
	 * @param dbre
	 *          The database to check.
	 * @return True if the test passed
	 */
  public boolean run(final DatabaseRegistryEntry dbre) {
    boolean result = true;

    Connection con = dbre.getConnection();

    try {
      /*
      * Will extract a list of phenotype.descriptions and check for unsupported char & short names
      */
      boolean char_ok   = true;



      List<String> data = getSqlTemplate(con).queryForDefaultObjectList("select description from phenotype where description is not null", String.class);

      for (int i = 0; i < data.size(); i++) {

        String input = data.get(i);

        // check for unusually short descriptions
        if(input.length() < 4){
          result = false;
          ReportManager.problem(this, con, "phenotype: " + input + " is suspiciously short");
        }

        // check for characters which will be interpreted a new lines
        if( input.contains("\n") ){
          result = false;
          ReportManager.problem(this, con, "phenotype: " + input + " contains a newline ");
        }

        // check for phenotype descriptions suggesting no phenotype
        boolean name_ok = checkNonTerms(input);
        if(name_ok == false){
          result = false;
          ReportManager.problem(this, con, "phenotype: " + input + " is not useful");
        }

        // check for unsupported individual character
        char_ok = checkUnsupportedChar(input);
        if(char_ok == false){
          result = false;
          String unsupportedChar = getUnsupportedChar(input);
          ReportManager.problem(this, con, "phenotype: \""+ input +"\" has suspect start or unsupported characters: \"" + unsupportedChar  + "\"");
        }

      }					    

    } catch (Exception e) {
      ReportManager.problem(this, con, "HealthCheck generated an exception: " + e.getMessage());
      result = false;
    }
    if (result) {
      // if there were no problems, just inform for the interface to pick the HC
      ReportManager.correct(this, con, "CheckChar healthcheck passed without any problem");
    }
    return result;
  } 

	// --------------------------------------------------------------

  public boolean checkUnsupportedChar( String input) {

    boolean is_ok = true;
    int len = input.length();
    
    for (int i =0; i< len; i++){
      char test_value= input.charAt(i);

      //get ascii code				
      int ascii_val = (int) test_value;	   

      // check code in supported range
      if(ascii_val < 32 || ascii_val  > 126 || ascii_val == 60 || ascii_val == 62 ){
        is_ok = false;
      }

      // also check first character makes sense
      if(i == 0 && ( ascii_val < 48 || 
        (ascii_val  > 57 && ascii_val < 65) ||
        (ascii_val  > 90 && ascii_val < 97) ||
        ascii_val  > 122)){
        is_ok = false;
      }
    }
    return is_ok;
  }

  public String getUnsupportedChar( String input) {

    boolean is_ok = true;
    int len = input.length();
    String unsupportedChar = "";
    for (int i =0; i< len; i++){
      char test_value= input.charAt(i);

      //get ascii code				
      int ascii_val = (int) test_value;	   

      // check code in supported range
      if(ascii_val < 32 || ascii_val  > 126 || ascii_val == 60 || ascii_val == 62 ){
        is_ok = false;
      }

      // also check first character makes sense
      if(i == 0 && ( ascii_val < 48 || 
        (ascii_val  > 57 && ascii_val < 65) ||
        (ascii_val  > 90 && ascii_val < 97) ||
        ascii_val  > 122)){
        is_ok = false;
      }
      if (!is_ok) {
        unsupportedChar = "" + input.charAt(i);
      }
    }
    return unsupportedChar;
  }

	public boolean checkNonTerms( String input) {

		boolean is_ok = true;
		String[] junk = {"None", "Not provided", "not specified", "Not in OMIM", "Variant of unknown significance", "not_provided", "?","." };

		int len = junk.length;
			    
		for (int i =0; i< len; i++){

		    if( input.equalsIgnoreCase( junk[i] ) ){
			is_ok  = false;
		    }
		}
		
		return is_ok;
	}


   

	// --------------------------------------
}
