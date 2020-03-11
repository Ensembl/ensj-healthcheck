/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2020] EMBL-European Bioinformatics Institute
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

	/** An EST database */
	public static final DatabaseType EST = new DatabaseType("est");

	/** An ESTgene database */
	public static final DatabaseType ESTGENE = new DatabaseType("estgene");

	/** A Vega database - note this actually refers to the Ensembl Vega database */
	public static final DatabaseType VEGA = new DatabaseType("vega");

	/** A Sanger Vega database - note this is different from the Ensembl Vega database */
	public static final DatabaseType SANGER_VEGA = new DatabaseType("sangervega");

	/** A Compara database */
	public static final DatabaseType COMPARA = new DatabaseType("compara");

	/** A Mart database */
	public static final DatabaseType MART = new DatabaseType("mart");

	/** A variation database */
	public static final DatabaseType VARIATION = new DatabaseType("variation");

	/** A disease database */
	public static final DatabaseType DISEASE = new DatabaseType("disease");

	/** A haplotype database */
	public static final DatabaseType HAPLOTYPE = new DatabaseType("haplotype");

	/** A lite database */
	public static final DatabaseType LITE = new DatabaseType("lite");

	/** A GO database */
	public static final DatabaseType GO = new DatabaseType("go");

	/** An expression database */
	public static final DatabaseType EXPRESSION = new DatabaseType("expression");

	/** An xref database */
	public static final DatabaseType XREF = new DatabaseType("xref");

	/** An cDNA database */
	public static final DatabaseType CDNA = new DatabaseType("cdna");

	/** A sequence database */
	public static final DatabaseType SEQ = new DatabaseType("seq");

	/** A help database */
	public static final DatabaseType HELP = new DatabaseType("HELP");

	/** An otherfeatures database */
	public static final DatabaseType OTHERFEATURES = new DatabaseType("otherfeatures");

	/** A system database */
	public static final DatabaseType SYSTEM = new DatabaseType("system");

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

        /** A pre database */
        public static final DatabaseType PRE_SITE = new DatabaseType("presite");

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
		// needs to be before core and est since names
		// are of the form homo_sapiens_coreexpressionest_24_34e
		if (in(lcAlias, "expression")) {

			return EXPRESSION;

		}

		// --------------------------------------
		// EG: treat eg_core as core dbs as well
		if (in(lcAlias, "core") || in(lcAlias, "eg_core") || in(lcAlias, "ancestral")) {

			return CORE;

		}

		// --------------------------------------

		if (in(lcAlias, "est")) {

			return EST;

		}

		// --------------------------------------

		if (in(lcAlias, "estgene")) {

			return ESTGENE;

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

		if (in(lcAlias, "vega")) {

			return VEGA;

		}

		// --------------------------------------

		if (in(lcAlias, "variation")) {

			return VARIATION;

		}

		// --------------------------------------

		if (in(lcAlias, "disease")) {

			return DISEASE;

		}

		// --------------------------------------

		if (in(lcAlias, "haplotype")) {

			return HAPLOTYPE;

		}

		// --------------------------------------

		if (in(lcAlias, "lite")) {

			return LITE;

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

		if (in(lcAlias, "seq")) {

			return SEQ;

		}

		// --------------------------------------

		if (in(lcAlias, "help")) {

			return HELP;

		}

		// --------------------------------------

		if (in(lcAlias, "otherfeatures")) {

			return OTHERFEATURES;

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

                if (in(lcAlias, "presite")) {

                        return PRE_SITE;

                }

		// --------------------------------------
		// treat ensembl genomes collection databases as core

		if (in(lcAlias, "collection")) {

			return CORE;

		}

		// --------------------------------------
		// and sangervega (generally specified via -type)

		if (in(lcAlias, "sangervega")) {

			return SANGER_VEGA;

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
	 * Check if a DatabaseType is generic (core, est, estgene, vega).
	 * 
	 * @return true if database is core, est, estgene or vega etc.
	 */
	public boolean isGeneric() {

		String[] genericTypes = {"core", "est", "estgene", "vega", "cdna", "otherfeatures", "sangervega", "rnaseq", "presite"};

		return Utils.stringInArray(name, genericTypes, false);

	}

	// -----------------------------------------------------------------

} // DatabaseType
