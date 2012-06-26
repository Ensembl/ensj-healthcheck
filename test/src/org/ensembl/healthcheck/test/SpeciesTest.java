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
        
        assertEquals(Species.getSpeciesFromTaxonomyID(10090), Species.MUS_MUSCULUS);
        assertEquals(Species.getSpeciesFromTaxonomyID(10116), Species.RATTUS_NORVEGICUS);

        assertEquals("0", Species.getTaxonomyID(Species.UNKNOWN), "Checking unknown returns a taxon of 0");
        assertEquals(Species.getSpeciesFromTaxonomyID(-1), Species.UNKNOWN);

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
