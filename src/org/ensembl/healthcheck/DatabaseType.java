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

/*
 * Copyright (C) 2003 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.ensembl.healthcheck;

import org.ensembl.healthcheck.util.Utils;

/**
 * Typesafe "enum" to store information about the type of a database. Declared final since it only has private constructors.
 */
public final class DatabaseType {

    /** A core database */
    public static final DatabaseType CORE = new DatabaseType("core");

    /** A Compara database */
    public static final DatabaseType COMPARA = new DatabaseType("compara");

    /** A Mart database */
    public static final DatabaseType MART = new DatabaseType("mart");

    /** A variation database */
    public static final DatabaseType VARIATION = new DatabaseType("variation");

    /** A GO database */
    public static final DatabaseType GO = new DatabaseType("go");

    /** An xref database */
    public static final DatabaseType XREF = new DatabaseType("xref");

    /** An cDNA database */
    public static final DatabaseType CDNA = new DatabaseType("cdna");

    /** A help database */
    public static final DatabaseType HELP = new DatabaseType("HELP");

    /** An otherfeatures database */
    public static final DatabaseType OTHERFEATURES = new DatabaseType("otherfeatures");

    /** A taxonomy database */
    public static final DatabaseType NCBI_TAXONOMY = new DatabaseType("ncbi_taxonomy");

    /** An ensembl_website database */
    public static final DatabaseType ENSEMBL_WEBSITE = new DatabaseType("ensembl_website");

    /** A healthcheck database */
    public static final DatabaseType HEALTHCHECK = new DatabaseType("healthcheck");

    /** A functional genomics database */
    public static final DatabaseType FUNCGEN = new DatabaseType("funcgen");

    /** A production database */
    public static final DatabaseType PRODUCTION = new DatabaseType("production");

    /** An rnaseq database */
    public static final DatabaseType RNASEQ = new DatabaseType("rnaseq");

    /** A database whose type has not been determined */
    public static final DatabaseType UNKNOWN = new DatabaseType("unknown");

    private final String name;

    private DatabaseType(final String name) {

        this.name = name;
    }

    /**
     * @return a String representation of this DatabaseType object.
     */
    public String toString() {

        return this.name;
    }

    /**
     * @return a String representation of this DatabaseType object.
     */
    public String getName() {

        return this.name;
    }

    // -----------------------------------------------------------------
    /**
     * Resolve an alias to a DatabaseType object.
     * 
     * @param alias
     *          The alias (e.g. core).
     * @return The DatabaseType object corresponding to alias, or DatabaseType.UNKNOWN if it cannot be resolved.
     */
    public static DatabaseType resolveAlias(final String alias) {

        String lcAlias = alias.toLowerCase();

        // --------------------------------------
        // EG: treat eg_core as core dbs as well
        if (in(lcAlias, "core") || in(lcAlias, "eg_core") || in(lcAlias, "ancestral")) {

            return CORE;

        }

        // --------------------------------------

        if (in(lcAlias, "compara") || in(lcAlias, "eg_compara")) {

            return COMPARA;

        }

        // --------------------------------------

        if (in(lcAlias, "mart")) {

            return MART;

        }

        // --------------------------------------

        if (in(lcAlias, "variation")) {

            return VARIATION;

        }


        // --------------------------------------

        if (in(lcAlias, "go")) {

            return GO;

        }

        // --------------------------------------

        if (in(lcAlias, "xref")) {

            return XREF;

        }

        // --------------------------------------

        if (in(lcAlias, "cdna")) {

            return CDNA;

        }


        // --------------------------------------

        if (in(lcAlias, "help")) {

            return HELP;

        }

        // --------------------------------------

        if (in(lcAlias, "ensembl_website")) {

            return ENSEMBL_WEBSITE;

        }

        // --------------------------------------

        if (in(lcAlias, "ncbi_taxonomy")) {

            return NCBI_TAXONOMY;

        }

        // --------------------------------------

        if (in(lcAlias, "healthcheck")) {

            return HEALTHCHECK;

        }

        // --------------------------------------

        if (in(lcAlias, "funcgen") || in(lcAlias, "eg_funcgen")) {

            return FUNCGEN;

        }

        // --------------------------------------

        if (in(lcAlias, "ensembl_production")) {

            return PRODUCTION;

        }

        // --------------------------------------

        if (in(lcAlias, "rnaseq")) {

            return RNASEQ;

        }


        // --------------------------------------
        // treat ensembl genomes collection databases as core

        if (in(lcAlias, "collection")) {

            return CORE;

        }

        // --------------------------------------

        // default case
        return UNKNOWN;

    } // resolveAlias

    // -----------------------------------------------------------------

    /**
     * Return true if alias appears somewhere in comma-separated list.
     */
    private static boolean in(final String alias, final String list) {

        return (list.indexOf(alias) > -1);

    }

    // -------------------------------------------------------------------------
    /**
     * Check if a DatabaseType is core.
     * 
     * @return true if database is core.
     */
    public boolean isGeneric() {

        String[] genericTypes = {"core", "cdna", "otherfeatures", "rnaseq", "presite"};

        return Utils.stringInArray(name, genericTypes, false);

    }

    // -----------------------------------------------------------------

} // DatabaseType
