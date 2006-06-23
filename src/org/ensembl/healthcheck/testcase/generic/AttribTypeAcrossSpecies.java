/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.generic;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;

/**
 * Check that the attrib_type table is the same in all necessary databases.
 */
public class AttribTypeAcrossSpecies extends MultiDatabaseTestCase {

    private DatabaseType[] types = {DatabaseType.CORE, DatabaseType.OTHERFEATURES, DatabaseType.VEGA};

    /**
     * Creates a new instance of AttribTypeTablesAcrossSpecies
     */
    public AttribTypeAcrossSpecies() {

        addToGroup("release");
        setDescription("Check that the attrib_type table contains the same information for all databases with the same species.");

    }

    /**
     * Make sure that the assembly tables are all the same.
     * 
     * @param dbr
     *          The database registry containing all the specified databases.
     * @return True if the assembly table is the same across all the species in
     *         the registry.
     */
    public boolean run(DatabaseRegistry dbr) {

        return checkTableAcrossSpecies("attrib_type", dbr, types, "attrib_type tables all the same", "attrib_type tables are different", "WHERE code NOT LIKE 'GeneNo%'");

    } // run

} // AttribTypeTablesAcrossSpecies
