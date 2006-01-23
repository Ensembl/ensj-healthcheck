/*
 * Copyright (C) 2003 EBI, GRL
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
package org.ensembl.healthcheck;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Store info about a species. Implemented as a typesafe enum
 * 
 * @see <a
 *      href="http://java.sun.com/developer/Books/shiftintojava/page1.html">Java
 *      Typesafe Enums </a>
 */

public final class Species {

	/** Specific type of species */
	public static final Species HOMO_SAPIENS = new Species("homo_sapiens");

	/** Specific type of species */
	public static final Species ANOPHELES_GAMBIAE = new Species(
			"anopheles_gambiae");

	/** Specific type of species */
	public static final Species CAENORHABDITIS_ELEGANS = new Species(
			"caenorhabditis_elegans");

	/** Specific type of species */
	public static final Species CAENORHABDITIS_BRIGGSAE = new Species(
			"caenorhabditis_briggsae");

	/** Specific type of species */
	public static final Species CIONA_INTESTINALIS = new Species(
			"ciona_intestinalis");

	/** Specific type of species */
	public static final Species DANIO_RERIO = new Species("danio_rerio");

	/** Specific type of species */
	public static final Species DROSOPHILA_MELANOGASTER = new Species(
			"drosophila_melanogaster");

	/** Specific type of species */
	public static final Species FUGU_RUBRIPES = new Species("fugu_rubripes");

	/** Specific type of species */
	public static final Species MUS_MUSCULUS = new Species("mus_musculus");

	/** Specific type of species */
	public static final Species RATTUS_NORVEGICUS = new Species(
			"rattus_norvegicus");

	/** Specific type of species */
	public static final Species PAN_TROGLODYTES = new Species("pan_troglodytes");

	/** Specific type of species */
	public static final Species GALLUS_GALLUS = new Species("gallus_gallus");

	/** Specific type of species */
	public static final Species TETRAODON_NIGROVIRIDIS = new Species(
			"tetraodon_nigroviridis");

	/** Specific type of species */
	public static final Species APIS_MELLIFERA = new Species("apis_mellifera");

	/** Specific type of species */
	public static final Species BOS_TAURUS = new Species("bos_taurus");

	/** Specific type of species */
	public static final Species CANIS_FAMILIARIS = new Species(
			"canis_familiaris");

	/** Specific type of species */
	public static final Species XENOPUS_TROPICALIS = new Species(
			"xenopus_tropicalis");

	/** Specific type of species */
	public static final Species MONODELPHIS_DOMESTICA = new Species(
			"monodelphis_domestica");

	/** Specific type of species */
	public static final Species SACCHAROMYCES_CEREVISIAE = new Species(
			"saccharomyces_cerevisiae");

	/** Specific type of species */
	public static final Species MACACA_MULATTA = new Species("macaca_mulatta");

	/** Specific type of species */
	public static final Species LOXODONTA_AFRICANA = new Species("loxodonta_africana");

	public static final Species DASYPUS_NOVEMCINCTUS = new Species("dasypus_novemcinctus");

    /** Non-ensembl database */
    public static final Species SYSTEM = new Species("system");

    /** Help database */
    public static final Species HELP = new Species("help");

	/** Unknown species */
	public static final Species UNKNOWN = new Species("unknown");

	private final String name;

	private static Logger logger = Logger.getLogger("HealthCheckLogger");

	private Species(String name) {

		this.name = name;
	}

	/**
	 * @return The string representation of this species.
	 */
	public String toString() {

		return this.name;
	}

	// Taxonomy IDs - see ensembl-compara/sql/taxon.txt
	private static Map taxonIDToSpecies = new HashMap();

	private static Map speciesToTaxonID = new HashMap();

	private static Map assemblyPrefixToSpecies = new HashMap();

	private static Map speciesToAssemblyPrefix = new HashMap();

	static {

		taxonIDToSpecies.put("9606", HOMO_SAPIENS);
		taxonIDToSpecies.put("10090", MUS_MUSCULUS);
		taxonIDToSpecies.put("10116", RATTUS_NORVEGICUS);
		taxonIDToSpecies.put("31033", FUGU_RUBRIPES);
		taxonIDToSpecies.put("7165", ANOPHELES_GAMBIAE);
		taxonIDToSpecies.put("7227", DROSOPHILA_MELANOGASTER);
		taxonIDToSpecies.put("6239", CAENORHABDITIS_ELEGANS);
		taxonIDToSpecies.put("6238", CAENORHABDITIS_BRIGGSAE);
		taxonIDToSpecies.put("7719", CIONA_INTESTINALIS);
		taxonIDToSpecies.put("7955", DANIO_RERIO);
		taxonIDToSpecies.put("9598", PAN_TROGLODYTES);
		taxonIDToSpecies.put("9031", GALLUS_GALLUS);
		taxonIDToSpecies.put("99883", TETRAODON_NIGROVIRIDIS);
		taxonIDToSpecies.put("7460", APIS_MELLIFERA);
		taxonIDToSpecies.put("9913", BOS_TAURUS);
		taxonIDToSpecies.put("9615", CANIS_FAMILIARIS);
		taxonIDToSpecies.put("8364", XENOPUS_TROPICALIS);
		taxonIDToSpecies.put("13616", MONODELPHIS_DOMESTICA);
		taxonIDToSpecies.put("4932", SACCHAROMYCES_CEREVISIAE);
		taxonIDToSpecies.put("9544", MACACA_MULATTA);
		taxonIDToSpecies.put("9785", LOXODONTA_AFRICANA);
		taxonIDToSpecies.put("9361", DASYPUS_NOVEMCINCTUS);
		
		// and the other way around
		Iterator it = taxonIDToSpecies.keySet().iterator();
		while (it.hasNext()) {
			String taxonID = (String) it.next();
			Species species = (Species) taxonIDToSpecies.get(taxonID);
			speciesToTaxonID.put(species, taxonID);
		}

		assemblyPrefixToSpecies.put("RGSC", RATTUS_NORVEGICUS);
		assemblyPrefixToSpecies.put("BDGP", DROSOPHILA_MELANOGASTER);
		assemblyPrefixToSpecies.put("ZFISH", DANIO_RERIO);
		assemblyPrefixToSpecies.put("FUGU", FUGU_RUBRIPES);
		assemblyPrefixToSpecies.put("AgamP", ANOPHELES_GAMBIAE);
		assemblyPrefixToSpecies.put("CEL", CAENORHABDITIS_ELEGANS);
		assemblyPrefixToSpecies.put("CBR", CAENORHABDITIS_BRIGGSAE);
		assemblyPrefixToSpecies.put("CIN", CIONA_INTESTINALIS);
		assemblyPrefixToSpecies.put("NCBI", HOMO_SAPIENS);
		assemblyPrefixToSpecies.put("NCBIM", MUS_MUSCULUS);
		assemblyPrefixToSpecies.put("TETRAODON", TETRAODON_NIGROVIRIDIS);
		assemblyPrefixToSpecies.put("AMEL", APIS_MELLIFERA);
		assemblyPrefixToSpecies.put("CHIMP", PAN_TROGLODYTES);
		assemblyPrefixToSpecies.put("WASHUC", GALLUS_GALLUS);
		assemblyPrefixToSpecies.put("BTAU", BOS_TAURUS);
		assemblyPrefixToSpecies.put("BROADD", CANIS_FAMILIARIS);
		assemblyPrefixToSpecies.put("JGI", XENOPUS_TROPICALIS);
		assemblyPrefixToSpecies.put("BROADO", MONODELPHIS_DOMESTICA);
		assemblyPrefixToSpecies.put("SGD", SACCHAROMYCES_CEREVISIAE);
		assemblyPrefixToSpecies.put("MM", MACACA_MULATTA);
		assemblyPrefixToSpecies.put("BROADE", LOXODONTA_AFRICANA);
		assemblyPrefixToSpecies.put("ARMA", DASYPUS_NOVEMCINCTUS );
		
		// and the other way around
		it = assemblyPrefixToSpecies.keySet().iterator();
		while (it.hasNext()) {
			String ap = (String) it.next();
			Species species = (Species) assemblyPrefixToSpecies.get(ap);
			speciesToAssemblyPrefix.put(species, ap);
		}
	}

	// -----------------------------------------------------------------
	/**
	 * Resolve an alias to a Species object.
	 * 
	 * @param speciesAlias
	 *            The alias (e.g. human, homosapiens, hsapiens).
	 * @return The species object corresponding to alias, or Species.UNKNOWN if
	 *         it cannot be resolved.
	 */
	public static Species resolveAlias(String speciesAlias) {

		String alias = speciesAlias.toLowerCase();

		// --------------------------------------

		if (in(alias, "human,hsapiens,homosapiens,homo_sapiens")) {

			return HOMO_SAPIENS;

		}

		// -------------------------------------

		if (in(alias,
				"mosquito,anopheles,agambiae,anophelesgambiae,anopheles_gambiae")) {

			return ANOPHELES_GAMBIAE;

		}

		// --------------------------------------

		if (in(alias,
				"elegans,celegans,caenorhabditiselegans,caenorhabditis_elegans")) {

			return CAENORHABDITIS_ELEGANS;

		}

		// --------------------------------------

		if (in(alias,
				"briggsae,cbriggsae,caenorhabditisbriggsae,caenorhabditis_briggsae")) {

			return CAENORHABDITIS_BRIGGSAE;

		}

		// --------------------------------------

		if (in(alias, "zebrafish,danio,drerio,daniorerio,danio_rerio")) {

			return DANIO_RERIO;

		}

		// --------------------------------------
		if (in(alias, "pufferfish,fugu,frubripes,fugurubripes,fugu_rubripes")) {

			return FUGU_RUBRIPES;

		}

		// --------------------------------------

		if (in(alias,
				"drosophila,dmelongaster,drosophilamelanogaster,drosophila_melanogaster")) {

			return DROSOPHILA_MELANOGASTER;

		}

		// --------------------------------------

		if (in(alias, "mouse,mmusculus,musmusculus,mus_musculus")) {

			return MUS_MUSCULUS;

		}

		// --------------------------------------

		if (in(alias, "rat,rnovegicus,rattusnorvegicus,rattus_norvegicus")) {

			return RATTUS_NORVEGICUS;

		}

		// --------------------------------------

		if (in(alias,
				"chimp,chimpanzee,ptroglodytes,pantroglodytes,pan_troglodytes")) {

			return PAN_TROGLODYTES;

		}

		// --------------------------------------

		if (in(alias, "chicken,chick,ggallus,gallusgallus,gallus_gallus")) {

			return GALLUS_GALLUS;

		}

		// --------------------------------------

		if (in(alias,
				"tetraodon,tnigroviridis,tetraodonnigroviridis,tetraodon_nigroviridis")) {

			return TETRAODON_NIGROVIRIDIS;

		}

		// --------------------------------------

		if (in(alias,
				"honeybee,honey_bee,apis,amellifera,apismellifera,apis_mellifera")) {

			return APIS_MELLIFERA;

		}

		// --------------------------------------

		if (in(alias, "cow,btaurus,bostaurus,bos_taurus")) {

			return BOS_TAURUS;

		}

		// --------------------------------------

		if (in(alias, "dog,doggy,cfamiliaris,canisfamiliaris,canis_familiaris")) {

			return CANIS_FAMILIARIS;

		}

		// --------------------------------------

		if (in(alias,
				"pipid,pipidfrog,xenopus,xtropicalis,xenopustropicalis,xenopus_tropicalis")) {

			return XENOPUS_TROPICALIS;

		}

		// --------------------------------------

		if (in(alias,
				"opossum,monodelphis,mdomestica,mdomesticus,monodelphisdomestica,monodelphisdomesticus,monodelphis_domestica,monodelphis_domesticus")) {

			return MONODELPHIS_DOMESTICA;

		}

		// --------------------------------------

		if (in(
				alias,
				"yeast,saccharomyces,scerevisiae,saccharomycescerevisiae,saccharomyces_cerevisiae")) {

			return SACCHAROMYCES_CEREVISIAE;

		}

		// --------------------------------------

		if (in(alias, "cionaintestinalis,ciona_intestinalis,ciona_int")) {

			return CIONA_INTESTINALIS;

		}

		//		 --------------------------------------

		if (in(alias, "macacamulatta,macaca_mulatta,rhesusmacaque,rhesus_macaque,macaque")) {

			return MACACA_MULATTA;

		}
		if (in(alias, "elephant,loxodonta_africana,nelly,loxodonta,african_elephant,african_savannah_elephant,african_bush_elephant")) {

			return LOXODONTA_AFRICANA;
                }
		if (in(alias, "armadillo,dasypus_novemcinctus,daisy,dasypus,nine_banded_armadillo,nine-banded_armadillo,texas_armadillo")) {

			return DASYPUS_NOVEMCINCTUS;
                }

		// --------------------------------------

		// default
		// logger.warning("Cannot resolve species alias " + alias + " to a
		// species - returning Species.UNKNOWN");

		return Species.UNKNOWN;

	}

	// -----------------------------------------------------------------
	/**
	 * Get the taxonomy ID associated with a particular species.
	 * 
	 * @param s
	 *            The species to look up.
	 * @return The taxonomy ID associated with s, or "" if none is found.
	 */
	public static String getTaxonomyID(Species s) {

		String result = "";
		if (speciesToTaxonID.containsKey(s)) {
			result = (String) speciesToTaxonID.get(s);
		} else {
			logger.warning("Cannot get taxonomy ID for species " + s);
		}

		return result;

	}

	// -----------------------------------------------------------------
	/**
	 * Get the species associated with a particular taxonomy ID.
	 * 
	 * @param t
	 *            The taxonomy ID to look up.
	 * @return The species associated with t, or Species.UNKNOWN if none is
	 *         found.
	 */
	public static Species getSpeciesFromTaxonomyID(String t) {

		Species result = UNKNOWN;

		if (taxonIDToSpecies.containsKey(t)) {
			result = (Species) taxonIDToSpecies.get(t);
		} else {
			logger.warning("Cannot get species for taxonomy ID " + t
					+ " returning Species.UNKNOWN");
		}

		return result;

	}

	// -----------------------------------------------------------------
	/**
	 * Return true if alias appears somewhere in a string.
	 */
	private static boolean in(String alias, String list) {

		return (list.indexOf(alias) > -1);

	}

	// -------------------------------------------------------------------------
	/**
	 * Return a Species object corresponding to a particular assembly prefix.
	 * 
	 * @param prefix
	 *            The assembly prefix.
	 * @return The Species corresponding to prefix, or Species.UNKNOWN.
	 */
	public static Species getSpeciesForAssemblyPrefix(String prefix) {

		Species result = Species.UNKNOWN;

		if (assemblyPrefixToSpecies.containsKey(prefix)) {
			result = (Species) assemblyPrefixToSpecies.get(prefix);
		} else {
			result = Species.UNKNOWN;
		}

		return result;

	}

	// -------------------------------------------------------------------------
	/**
	 * Get the assembly prefix for a species.
	 * 
	 * @param s
	 *            The species.
	 * @return The assembly prefix for s.
	 */
	public static String getAssemblyPrefixForSpecies(Species s) {

		return (String) speciesToAssemblyPrefix.get(s);

	}

	// -----------------------------------------------------------------

}
