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


package org.ensembl.healthcheck.testcase;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.TestResult;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.DatabaseConnectionIterator;

/**
 * Checks that the archive tables are up to date.
 */
public class CheckArchiveTestCase extends EnsTestCase {
   


	public CheckArchiveTestCase() {
		addToGroup("check_archive");
		setDescription("Checks the archive tables are up to date.");
		}

	public TestResult run() {
	
// select old_stable_id, old_version as "Updated Gene missing from gene_archive"
//  from stable_id_event LEFT JOIN gene_archive on old_stable_id=gene_stable_id
//  where old_stable_id like "%G%" and gene_stable_id is NULL 
//  and new_stable_id=old_stable_id and old_version!=new_version;

// select old_stable_id, old_version as "Updated Transcript missing from gene_archive"
//  from stable_id_event LEFT JOIN gene_archive on old_stable_id=transcript_stable_id
//  where old_stable_id like "%T%" and transcript_stable_id is NULL 
//  and new_stable_id=old_stable_id and old_version!=new_version;

// select old_stable_id, old_version as "Updated Translation missing from gene_archive"
//  from stable_id_event LEFT JOIN gene_archive on old_stable_id=translation_stable_id
//  where old_stable_id like "%P%" and translation_stable_id is NULL; 
//  and new_stable_id=old_stable_id and old_version!=new_version;




// select old_stable_id as "Deleted Translation missing from peptide_archive"
//  from stable_id_event LEFT JOIN peptide_archive on old_stable_id=translation_stable_id
//  where old_stable_id like "%P%" and new_stable_id is NULL and translation_stable_id is NULL;

// select old_stable_id, old_version as "Updated Translation missing from peptide_archive"
//  from stable_id_event LEFT JOIN peptide_archive on old_stable_id=translation_stable_id
//  where old_stable_id like "%P%" and new_stable_id=old_stable_id and old_version!=new_version
//  and translation_stable_id is NULL ;






// select * from peptide_archive limit 3;
// select * from gene_archive limit 3;


// select pa.translation_stable_id as "ERROR: these translations are in
// peptide_archive but not gene_archive", pa.translation_version from
// peptide_archive pa LEFT JOIN gene_archive ga ON
// ga.translation_stable_id=pa.translation_stable_id AND
// ga.translation_version=pa.translation_version WHERE
// ga.translation_stable_id is NULL;


// select ts.stable_id as "ERROR: this translation is in peptide but
// hasn't changed", ts.version from translation_stable_id ts,
// peptide_archive pa where ts.stable_id=pa.translation_stable_id AND
// ts.version= pa.translation_version;
		
		boolean result = true;
    
		DatabaseConnectionIterator it = getDatabaseConnectionIterator();
    
			 while (it.hasNext()) {
			 	
					Connection con = (Connection)it.next();
					
					boolean nullResult = checkNoNullStrings( con);
					boolean archiveIntegrityResult = checkArchiveIntegrity( con );
					boolean translationDiffResult = checkChangesInArchive( con, "translation");
					boolean transcriptDiffResult = checkChangesInArchive( con, "transcript");
					boolean geneDiffResult = checkChangesInArchive( con, "gene");
		
					result = result && nullResult && archiveIntegrityResult 
											&& translationDiffResult && transcriptDiffResult
											&& geneDiffResult;
			 }
			 
		return new TestResult(getShortTestName(), result);
	}

	
	

	/**
	 * @param con
	 * @param string
	 * @return
	 */
	private boolean checkChangesInArchive(Connection con, String string) {

	boolean result = true;
	
	result = result && checkDeletedInArchive( con, "gene", "G" );
	result = result && checkDeletedInArchive( con, "transcript", "T" );
	result = result && checkDeletedInArchive( con, "translation", "P" );
	
	return result;

	}



	/**
	 * Checks that all the deleted _type_s are included in the gene_archive.
	 * @param con connection on which to execute queries
	 * @param type type of item deleted
	 * @param filter substring to use use to filter relevant stableIDs, 
	 * will be used in SQL as "%FILTER%".
	 * @return whether the test succeeded.
	 */
	private boolean checkDeletedInArchive(Connection con, String type, String filter){	

		boolean result = true;
		
		String sql = "select old_stable_id from stable_id_event LEFT JOIN gene_archive on old_stable_id="
									+ type + "_stable_id where old_stable_id like \"%" + filter + "%\""
									+ " and new_stable_id is NULL and " + type + "_stable_id is NULL;";
		String[] rows = getColumnValues( con, sql);
		if ( rows.length>0 ) {
			StringBuffer msg = new StringBuffer();
			msg.append( rows.length + " deleted " + type + "s not in gene_archive ");
			for (int i = 0; i < rows.length && rows.length<10; i++) {
				msg.append( rows[i] ).append( "\n" ) ;
			}
			
			ReportManager.problem(this, con, "Deleted.");
			result = false;
		}
		
		return result;
	}



	/**
	 * @param con
	 * @return
	 */
	private boolean checkArchiveIntegrity(Connection con) {
		// TODO Auto-generated method stub
		return false;
	}



	/**
	 * Check no "NULL" or "null" strings in stable_id_event.new_stable_id or stable_id_event.oldable_id.
	 * @param con
	 * @return
	 */
	private boolean checkNoNullStrings(Connection con) {
	
		boolean result = true;
	
		String num = getRowColumnValue( con, "select count(*) from stable_id_event sie where	new_stable_id=\"NULL\";");
		if ( Integer.parseInt(num)>0 ) {
			ReportManager.problem(this, con, "stable_id_event.new_stable_id contains \"NULL\" string instead of NULL value.");
			result = false;
		}

		num = getRowColumnValue( con, "select count(*) from stable_id_event sie where	new_stable_id=\"null\";");
		if ( Integer.parseInt(num)>0 ) {
					ReportManager.problem(this, con, "stable_id_event.new_stable_id contains \"null\" string instead of NULL value.");
					result = false;
		}

		num = getRowColumnValue( con, "select count(*) from stable_id_event sie where	old_stable_id=\"NULL\";");
		if ( Integer.parseInt(num)>0 ) {
			ReportManager.problem(this, con, "stable_id_event.old_stable_id contains \"NULL\" string instead of NULL value.");
			result = false;
		}

		num = getRowColumnValue( con, "select count(*) from stable_id_event sie where	old_stable_id=\"null\";");
		if ( Integer.parseInt(num)>0 ) {
			ReportManager.problem(this, con, "stable_id_event.old_stable_id contains \"null\" string instead of NULL value.");
			result = false;
		}

		// todo: add this auto-fix code?

		//		#update stable_id_event set old_stable_id=NULL where
		//		#old_stable_id="NULL"

		//		#update stable_id_event set new_stable_id=NULL where
		//		#new_stable_id="NULL"		// TODO Auto-generated method stub
		
		
		return result;
	}





	/**
	 * Runs this TestCase via org.ensembl.healthcheck.TextTestRunner. Uses
	 * default if no parameters provided.
	 * @param args command line parameters to TextTestRunner.
	 */
	public static void main(String[] args) {
		if ( args.length==0 )
			args = "-d danio_rerio_core_15_2 -config database.properties.ecs2dforward check_archive".split(" ");
		org.ensembl.healthcheck.TextTestRunner.main( args );
	}

}
