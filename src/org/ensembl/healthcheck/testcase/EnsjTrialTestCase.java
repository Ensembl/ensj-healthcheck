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

import java.net.*;
import java.util.*;

import org.ensembl.healthcheck.*;
import org.ensembl.healthcheck.util.*;

// ensj-core imports
import org.ensembl.*;
import org.ensembl.util.*;
import org.ensembl.driver.*;
import org.ensembl.datamodel.*;
import org.ensembl.driver.plugin.standard.*;

/**
 * Test to see if we can use ensj-core in a test case.
 */
public class EnsjTrialTestCase extends EnsTestCase {
  
  /**
   * Creates a new instance of EnsjTrialTestCase
   */
  public EnsjTrialTestCase() {
    addToGroup("ensj");
  }
  
  /**
   * description
   */
  public TestResult run() {
    
    boolean result = true;
    
    // ensj needs some properties set; some are the same as in database.properties
    String propsFile = System.getProperty("user.dir") + System.getProperty("file.separator") + "database.properties";
    // @todo - use System. properties like elsewhere
    Properties testRunnerProps = Utils.readPropertiesFile(propsFile);
    Properties props = DBUtils.convertHealthcheckToEnsjProperties(testRunnerProps);
    
    // database *name* is not set at all in database.properties
    // we can use getAffectedDatabases() to do the loop
    // don't need DatabaseConnectionIterator since Ensj does its own connection handling
    String[] databases = getAffectedDatabases();
    
    for (int i = 0; i < databases.length; i++) {
      
      props.put("database" , databases[i]);
      
      //System.out.println(PropertiesUtil.toString(props));
      
      // create a Driver and initialise it with the properties
      try {
        
        LoggingManager.configure();
        Driver driver = new MySQLDriver();
        driver.initialise(props);
        
        // do a test
        GeneAdaptor geneAdaptor = driver.getGeneAdaptor();
        
        AssemblyLocation chromosomeOne = new AssemblyLocation();
        chromosomeOne.setChromosome("1");
        
        List genes = geneAdaptor.fetch(chromosomeOne);
        System.out.println("Got " + genes.size() + " genes");
        Iterator geneIterator = genes.iterator();
        
        while (geneIterator.hasNext()) {
          
          Gene gene = (Gene)geneIterator.next();
          logger.info("Working on " + gene.getAccessionID());
          List transcripts = gene.getTranscripts();
          Iterator it = transcripts.iterator();
          while (it.hasNext()) {
            Transcript transcript = (Transcript)it.next();
            Translation translation = transcript.getTranslation();
            String peptide = translation.getPeptide();
            if (peptide == null || peptide.length() == 0) {
              result = false;
              ReportManager.problem(this, databases[i], gene.getAccessionID() + " has transcript(s) that don't translate");
            } else {
              ReportManager.correct(this, databases[i], gene.getAccessionID() + " all transcripts translate");
            }
            
          } // while transcripts
          
        } // while genes
        
      } catch (Exception e) {
        e.printStackTrace();
      }
      
    } // for databases
    
    return new TestResult(getShortTestName(), result);
    
  } // run
  
} // EnsjTrialTestCase
