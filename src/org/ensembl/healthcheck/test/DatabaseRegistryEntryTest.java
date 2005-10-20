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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Species;

import junit.framework.TestCase;

/**
 * @author glenn
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class DatabaseRegistryEntryTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(DatabaseRegistryEntryTest.class);
    }

    /**
     * Constructor for DatabaseRegistryEntryTest.
     * 
     * @param arg0
     */
    public DatabaseRegistryEntryTest(String arg0) {
        super(arg0);
    }

    // -----------------------------------------------------------------

    public void testSetSpeciesAndTypeFromName() {

        // all of these should resolve
        String[] names = {"homo_sapiens_core_20_34", "human_core_20"};

        for (int i = 0; i < names.length; i++) {
            DatabaseRegistryEntry dbre = new DatabaseRegistryEntry(names[i], null, null, false);
            assertTrue(dbre.getSpecies() != Species.UNKNOWN);
            assertTrue(dbre.getType() != DatabaseType.UNKNOWN);
        }
    }

    // -----------------------------------------------------------------

}
