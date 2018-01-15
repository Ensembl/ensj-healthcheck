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


package org.ensembl.healthcheck.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.ensembl.healthcheck.Species;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SpeciesTest {

    // -----------------------------------------------------------------

    @Test
    public void testToString() {
        assertEquals("homo_sapiens", Species.HOMO_SAPIENS.toString(), "toString method not returning expected result");

    }

    // -----------------------------------------------------------------

    @Test
    public void testEquals() {

      Assert.assertEquals(Species.HOMO_SAPIENS, Species.HOMO_SAPIENS, "equals method not working");
      Assert.assertTrue(Species.TAKIFUGU_RUBRIPES != Species.CAENORHABDITIS_BRIGGSAE, "species that are the not the same are coming out equal");

    }

    // -----------------------------------------------------------------

    @Test
    public void testAlias() {

        // not an exhaustive list ...
        assertEquals(Species.resolveAlias("human"), Species.HOMO_SAPIENS);
        assertEquals(Species.resolveAlias("rat"), Species.RATTUS_NORVEGICUS);
        assertEquals(Species.resolveAlias("mus_musculus"), Species.MUS_MUSCULUS);
        assertEquals(Species.resolveAlias("tetraodon_nigroviridis"), Species.TETRAODON_NIGROVIRIDIS);
        assertEquals(Species.resolveAlias("apis_mellifera"), Species.APIS_MELLIFERA);
        assertEquals(Species.resolveAlias("ciona_intestinalis"), Species.CIONA_INTESTINALIS);
        
        assertEquals(Species.resolveAlias("littlegreenman"), Species.UNKNOWN);

    }

    // -----------------------------------------------------------------

    @Test
    public void testTaxonomyIDs() {

        assertEquals(Species.getTaxonomyID(Species.HOMO_SAPIENS), "9606");
        assertEquals(Species.getTaxonomyID(Species.DANIO_RERIO), "7955");
        assertEquals(Species.getTaxonomyID(Species.TETRAODON_NIGROVIRIDIS), "99883");
        assertEquals(Species.getTaxonomyID(Species.CIONA_INTESTINALIS), "7719");
        assertEquals(Species.getTaxonomyID(Species.APIS_MELLIFERA), "7460");

        assertEquals("0", Species.getTaxonomyID(Species.UNKNOWN), "Checking unknown returns a taxon of 0");

    }

    // -----------------------------------------------------------------
    
    @Test
    public void testSimilarNames() {
        
        assertTrue(!(Species.resolveAlias("hedgehog").equals(Species.ECHINOPS_TELFAIRI)));
        assertTrue(Species.resolveAlias("hedgehog").equals(Species.ERINACEUS_EUROPAEUS));
        
    }
    
 // -----------------------------------------------------------------
    
    @Test
    public void testAssemblyPrefixes() {
        
        assertEquals(Species.getSpeciesForAssemblyPrefix("GRCh"), Species.HOMO_SAPIENS);
        
    }
    
    // -----------------------------------------------------------------

}
