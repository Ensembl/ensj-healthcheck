/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.generic;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;

/**
 * Check that the seq region table is the same in all necessary databases.
 */
public class SeqRegionAcrossSpecies extends MultiDatabaseTestCase {

    private DatabaseType[] types = {DatabaseType.CORE, DatabaseType.CDNA, DatabaseType.VEGA, DatabaseType.OTHERFEATURES};

    /**
     * Creates a new instance of SeqRegionAcrossSpecies
     */
    public SeqRegionAcrossSpecies() {

        addToGroup("release");
        setDescription("Check that the seq_region table is the same across all generic DBs; if not it will cause problems on the website.");

    }

    /**
     * Make sure that the seq_region tables are all the same.
     * 
     * @param dbr The database registry containing all the specified databases.
     * @return True if the seq_region_attrib table is the same across all the species in the
     *         registry.
     */
    public boolean run(DatabaseRegistry dbr) {

        return checkTableAcrossSpecies("seq_region", dbr, types, "All seq_region tables are the same", "seq_region tables are different");

    } // run

} // SeqRegioAcrossSpecies

