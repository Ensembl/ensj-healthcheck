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
package org.ensembl.healthcheck;

import java.util.logging.Logger;

/**
 * Store info about a species.
 * Implemented as a typesafe enum 
 * @see http://java.sun.com/developer/Books/shiftintojava/page1.html
 */
public class Species {

	private final String name;

	protected static Logger logger = Logger.getLogger("HealthCheckLogger");

	private Species(String name) {
		this.name = name;
	}

	public String toString() {
		return this.name;
	}

	public static final Species HOMO_SAPIENS = new Species("homo_sapiens");
	public static final Species ANOPHELES_GAMBIAE = new Species("anopheles_gambiae");
	public static final Species CAENORHABDITIS_ELEGANS = new Species("caenorhabditis_elegans");
	public static final Species CAENORHABDITIS_BRIGGSAE = new Species("caenorhabditis_briggsae");
	public static final Species DANIO_RERIO = new Species("danio_rerio");
	public static final Species DROSOPHILA_MELANOGASTER = new Species("drosophila_melanogaster");
	public static final Species FUGU_RUBRIPES = new Species("fugu_rubripes");
	public static final Species MUS_MUSCULUS = new Species("mus_musculus");
	public static final Species RATTUS_NORVEGICUS = new Species("rattus_norvegicus");

	// special type to indicate that this species is unknown
	public static final Species UNKNOWN = new Species("unknown");

	// -----------------------------------------------------------------
	/**
	 * Resolve an alias to a Species object.
	 * @param alias The alias (e.g. human, homosapiens, hsapiens).
	 * @return The species object corresponding to alias, or Species.UNKNOWN if it cannot be resolved.
	 */
	public static Species resolveAlias(String alias) {

		alias = alias.toLowerCase();

		// --------------------------------------

		if (in(alias, "human,hsapiens,homosapiens,homo_sapiens")) {

			return HOMO_SAPIENS;

		}

		//		--------------------------------------

		if (in(alias, "mosquito,anopheles,agambiae,anophelesgambiae,anopheles_gambiae")) {

			return ANOPHELES_GAMBIAE;

		}

		//		--------------------------------------

		if (in(alias, "elegans,celegans,caenorhabditiselegans,caenorhabditis_elegans")) {

			return CAENORHABDITIS_ELEGANS;

		}

		//		--------------------------------------

		if (in(alias, "briggsae,cbriggsae,caenorhabditisbriggsae,caenorhabditis_briggsae")) {

			return CAENORHABDITIS_BRIGGSAE;

		}

		//		--------------------------------------

		if (in(alias, "zebrafish,danio,drerio,daniorerio,danio_rerio")) {

			return DANIO_RERIO;

		}

		//		--------------------------------------
		if (in(alias, "pufferfish,fugu,frubripes,fugurubripes,fugu_rubripes")) {

			return FUGU_RUBRIPES;

		}

		//		--------------------------------------

		if (in(alias, "drosophila,dmelongaster,drosophilamelanogaster,drosophila_melanogaster")) {

			return DROSOPHILA_MELANOGASTER;

		}

		//		--------------------------------------

		if (in(alias, "mouse,mmusculus,musmusculus,mus_musculus")) {

			return MUS_MUSCULUS;

		}

		//		--------------------------------------

		if (in(alias, "rat,rnovegicus,rattusnorvegicus,rattus_norvegicus")) {

			return RATTUS_NORVEGICUS;

		}

		//		--------------------------------------

		// default
		logger.warning("Cannot resolve species alias " + alias + " to a species - returning Species.UNKNOWN");

		return Species.UNKNOWN;

	}

	// -----------------------------------------------------------------
	/**
	 * Return true if alias appears somewhere in comma-separated list.
	 */
	private static boolean in(String alias, String list) {

		return (list.indexOf(alias) > -1);

	}

}
