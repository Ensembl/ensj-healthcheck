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

import org.ensembl.healthcheck.Species;

import junit.framework.TestCase;

/**
 * Junit test case for Species
 */
public class SpeciesTest extends TestCase {

    public SpeciesTest(String arg) {

        super(arg);

    }

    // -----------------------------------------------------------------

    public void testToString() {

        Species s = Species.HOMO_SAPIENS;
        assertEquals("toString method not returning expected result", s.toString(), "homo_sapiens");

    }

    // -----------------------------------------------------------------

    public void testEquals() {

        assertEquals("equals method not working", Species.HOMO_SAPIENS, Species.HOMO_SAPIENS);
        assertTrue("compare with == not working", Species.DANIO_RERIO == Species.DANIO_RERIO);
        assertTrue("species that are the not the same are coming out equal",
                Species.FUGU_RUBRIPES != Species.CAENORHABDITIS_BRIGGSAE);

    }

    // -----------------------------------------------------------------

    public void testAlias() {

        // not an exhaustive list ...
        assertEquals(Species.resolveAlias("human"), Species.HOMO_SAPIENS);
        assertEquals(Species.resolveAlias("rat"), Species.RATTUS_NORVEGICUS);
        assertEquals(Species.resolveAlias("mus_musculus"), Species.MUS_MUSCULUS);

        assertEquals(Species.resolveAlias("littlegreenman"), Species.UNKNOWN);

    }

    // -----------------------------------------------------------------

    public void testTaxonomyIDs() {

        assertEquals(Species.getTaxonomyID(Species.HOMO_SAPIENS), "9606");
        assertEquals(Species.getTaxonomyID(Species.DANIO_RERIO), "7955");

        assertEquals(Species.getSpeciesFromTaxonomyID("10090"), Species.MUS_MUSCULUS);
        assertEquals(Species.getSpeciesFromTaxonomyID("10116"), Species.RATTUS_NORVEGICUS);

        assertEquals(Species.getTaxonomyID(Species.UNKNOWN), "");
        assertEquals(Species.getSpeciesFromTaxonomyID("-1"), Species.UNKNOWN);

    }

    // -----------------------------------------------------------------

}
