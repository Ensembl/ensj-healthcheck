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
package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check Oligometrix xrefs: - that each chromosome has at least 1 Oligo xref
 *
 * Assumptions: oligo xrefs and transcripts are both in the default
 * chromosome coordinate system. 
 */
public class OligoXrefs extends SingleDatabaseTestCase {

    // if a database has more than this number of seq_regions in the chromosome coordinate system, it's ignored
    private static final int MAX_CHROMOSOMES = 75;

    /**
     * Creates a new instance of OligoXrefs
     */
    public OligoXrefs() {

        addToGroup("post_genebuild");
        addToGroup("release");
        addToGroup("core_xrefs");
        setDescription("Check oligo xrefs");
        setHintLongRunning(true);

    }

    /**
     * Check all chromosomes have oligo xrefs.
     * 
     * Get a list of chromosomes, then check the number of Oligo xrefs
     * associated with each one.  Fail is any chromosome has 0 oligo xrefs.
     *
     * @param dbre The database to use.
     * @return true if the test pased.
     *  
     */
  public boolean run(DatabaseRegistryEntry dbre) {

    boolean result = true;
    Connection con = dbre.getConnection();
    
    try {

      // Check if there are any Oligo features - if so there should be Oligo Xrefs
      if (getRowCount(con, "SELECT COUNT(*) FROM oligo_array") == 0) {
        logger.info(DBUtils.getShortDatabaseName(con) + " has no Oligo features, not checking for Oligo xrefs");
        return true;
      }


      // find all chromosomes in default assembly coordinate system
      Map srID2name = new HashMap();
      ResultSet rs = con.createStatement().executeQuery("SELECT seq_region_id, s.name FROM seq_region s, coord_system c WHERE c.coord_system_id=s.coord_system_id AND c.name='chromosome' and attrib='default_version '");
      while(rs.next()) 
        srID2name.put(rs.getString(1),rs.getString(2));
      rs.close();
      if (srID2name.size() > MAX_CHROMOSOMES) {
          ReportManager.problem(this, con, "Database has more than " + MAX_CHROMOSOMES + " seq_regions in 'chromosome' coordinate system (actually " + srID2name.size() + ") - test skipped");
        return false;
      }        

        
      // Count the number of oligo xrefs for each chr
      Map srID2count = new HashMap();
      // (Optimisation: faster to use "in list" of external_db_ids than SQL
      // join.)
      StringBuffer inList = new StringBuffer();
      String[] exdbIDs = getColumnValues(con, "select external_db_id from external_db where db_name LIKE \'AFFY%\'");
      for(int i=0; i<exdbIDs.length;i++) {
        if (i>0)
          inList.append(",");
        inList.append(exdbIDs[i]);            
      }
      rs = con.createStatement().executeQuery("select seq_region_id, count(*) as count  from transcript t, object_xref ox, xref x where t.transcript_id=ox.ensembl_id and ensembl_object_type='Transcript' and ox.xref_id=x.xref_id and x.external_db_id in ("+inList+") GROUP BY seq_region_id");
      while (rs.next()) 
        srID2count.put(rs.getString("seq_region_id"), rs.getString("count")); 
      rs.close();


      // check every chr has >0 oligo xrefs.
      for (Iterator iter = srID2name.keySet().iterator(); iter.hasNext(); ) {
        String srID = (String)iter.next();
        String name = (String)srID2name.get(srID);
        String label = name +"( seq_region_id="+srID+")";
        long count = srID2count.containsKey(srID) ? Long.parseLong(srID2count.get(srID).toString()) : 0;
        if ( count >0) {
          ReportManager.correct(this, con, "Chromosome " + label + " has " + srID2count.get(srID) + " associated Oligo xrefs.");
        } else {
          ReportManager.problem(this, con, "Chromosome " + label + " has no associated Oligo xrefs.");
          result = false;
        } 

      }
      
    } catch (SQLException se) {
      se.printStackTrace();
      result = false;
    }
    return result;
    
  } // run

} // OligoXrefs

