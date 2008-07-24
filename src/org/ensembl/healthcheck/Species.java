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
	public static final Species ANOPHELES_GAMBIAE = new Species("anopheles_gambiae");

	/** Specific type of species */
	public static final Species CAENORHABDITIS_ELEGANS = new Species("caenorhabditis_elegans");

	/** Specific type of species */
	public static final Species CAENORHABDITIS_BRIGGSAE = new Species("caenorhabditis_briggsae");

	/** Specific type of species */
	public static final Species CIONA_INTESTINALIS = new Species("ciona_intestinalis");

	/** Specific type of species */
	public static final Species DANIO_RERIO = new Species("danio_rerio");

	/** Specific type of species */
	public static final Species DROSOPHILA_MELANOGASTER = new Species("drosophila_melanogaster");

	/** Specific type of species */
	public static final Species TAKIFUGU_RUBRIPES = new Species("takifugu_rubripes");

	/** Specific type of species */
	public static final Species MUS_MUSCULUS = new Species("mus_musculus");

	/** Specific type of species */
	public static final Species RATTUS_NORVEGICUS = new Species("rattus_norvegicus");

	/** Specific type of species */
	public static final Species PAN_TROGLODYTES = new Species("pan_troglodytes");

	/** Specific type of species */
	public static final Species GALLUS_GALLUS = new Species("gallus_gallus");

	/** Specific type of species */
	public static final Species TETRAODON_NIGROVIRIDIS = new Species("tetraodon_nigroviridis");

	/** Specific type of species */
	public static final Species APIS_MELLIFERA = new Species("apis_mellifera");

	/** Specific type of species */
	public static final Species BOS_TAURUS = new Species("bos_taurus");

	/** Specific type of species */
	public static final Species CANIS_FAMILIARIS = new Species("canis_familiaris");

	/** Specific type of species */
	public static final Species XENOPUS_TROPICALIS = new Species("xenopus_tropicalis");

	/** Specific type of species */
	public static final Species MONODELPHIS_DOMESTICA = new Species("monodelphis_domestica");

	/** Specific type of species */
	public static final Species SACCHAROMYCES_CEREVISIAE = new Species("saccharomyces_cerevisiae");

	/** Specific type of species */
	public static final Species MACACA_MULATTA = new Species("macaca_mulatta");

	/** Specific type of species */
	public static final Species LOXODONTA_AFRICANA = new Species("loxodonta_africana");

	/** Specific type of species */
	public static final Species DASYPUS_NOVEMCINCTUS = new Species("dasypus_novemcinctus");

	/** Specific type of species */
	public static final Species ORYZIAS_LATIPES = new Species("oryzias_latipes");

	/** Specific type of species */
	public static final Species SUS_SCROFA = new Species("sus_scrofa");

	/** Specific type of species */
	public static final Species ECHINOPS_TELFAIRI = new Species("echinops_telfairi");

	/** Specific type of species */
	public static final Species ORYCTOLAGUS_CUNICULUS = new Species("oryctolagus_cuniculus");

	/** Specific type of species */
	public static final Species CIONA_SAVIGNYI = new Species("ciona_savignyi");

	/** Specific type of species */
	public static final Species GASTEROSTEUS_ACULEATUS = new Species("gasterosteus_aculeatus");

	/** Specific type of species */
	public static final Species AEDES_AEGYPTI = new Species("aedes_aegypti");

	/** Specific type of species */
	public static final Species ORNITHORHYNCHUS_ANATINUS = new Species("ornithorhynchus_anatinus");
	
	/** Specific type of species */
	public static final Species FELIS_CATUS = new Species("felis_catus");

	/** Specific type of species */
	public static final Species OTOLEMUR_GARNETTII = new Species("otolemur_garnettii");

	/** Specific type of species */
	public static final Species MYOTIS_LUCIFUGUS = new Species("myotis_lucifugus");

	/** Specific type of species */
	public static final Species SOREX_ARANEUS = new Species("sorex_araneus");

	/** Specific type of species */
	public static final Species OCHOTONA_PRINCEPS = new Species("ochotona_princeps");

	/** Specific type of species */
	public static final Species ERINACEUS_EUROPAEUS = new Species("erinaceus_europaeus");

	/** Specific type of species */
	public static final Species CAVIA_PORCELLUS = new Species("cavia_porcellus");

	/** Specific type of species */
	public static final Species TUPAIA_BELANGERI = new Species("tupaia_belangeri");

	/** Specific type of species */
	public static final Species SPERMOPHILUS_TRIDECEMLINEATUS = new Species("spermophilus_tridecemlineatus");

	/** Specific type of species */
	public static final Species EQUUS_CABALLUS = new Species("equus_caballus");

	/** Specific type of species */
	public static final Species PETROMYZON_MARINUS = new Species("petromyzon_marinus");

	/** Specific type of species */
	public static final Species ANOLIS_CAROLINENSIS = new Species("anolis_carolinensis");

	/** Specific type of species */
	public static final Species MICROCEBUS_MURINUS = new Species("microcebus_murinus");

	/** Specific type of species */
	public static final Species CULEX_PIPIENS = new Species("culex_pipiens");

	/** Specific type of species */
	public static final Species PONGO_PYGMAEUS = new Species("pongo_pygmaeus");

	/** Non-ensembl database */
	public static final Species SYSTEM = new Species("system");

	/** Help database */
	public static final Species HELP = new Species("help");

	/** A taxonomy database */
	public static final Species NCBI_TAXONOMY = new Species("ncbi_taxonomy");

	/** An ensembl_website database */
	public static final Species ENSEMBL_WEBSITE = new Species("ensembl_website");

	/** An healthcheck database */
	public static final Species HEALTHCHECK = new Species("healthcheck");

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

	private static Map ensemblStableIDPrefix = new HashMap();
	private static Map vegaStableIDPrefix = new HashMap();
	
	static {

		taxonIDToSpecies.put("9606", HOMO_SAPIENS);
		taxonIDToSpecies.put("10090", MUS_MUSCULUS);
		taxonIDToSpecies.put("10116", RATTUS_NORVEGICUS);
		taxonIDToSpecies.put("31033", TAKIFUGU_RUBRIPES);
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
		taxonIDToSpecies.put("8090", ORYZIAS_LATIPES);
		taxonIDToSpecies.put("9371", ECHINOPS_TELFAIRI);
		taxonIDToSpecies.put("9986", ORYCTOLAGUS_CUNICULUS);
		taxonIDToSpecies.put("9823", SUS_SCROFA);
		taxonIDToSpecies.put("51511", CIONA_SAVIGNYI);
		taxonIDToSpecies.put("69293", GASTEROSTEUS_ACULEATUS);
		taxonIDToSpecies.put("7159", AEDES_AEGYPTI);
		taxonIDToSpecies.put("9685", FELIS_CATUS);
		taxonIDToSpecies.put("30611", OTOLEMUR_GARNETTII);
		taxonIDToSpecies.put("59463", MYOTIS_LUCIFUGUS);
		taxonIDToSpecies.put("42254", SOREX_ARANEUS);
		taxonIDToSpecies.put("9978", OCHOTONA_PRINCEPS);
		taxonIDToSpecies.put("9258", ORNITHORHYNCHUS_ANATINUS);
		taxonIDToSpecies.put("9365", ERINACEUS_EUROPAEUS);
		taxonIDToSpecies.put("10141", CAVIA_PORCELLUS);
		taxonIDToSpecies.put("37347", TUPAIA_BELANGERI);
		taxonIDToSpecies.put("43179", SPERMOPHILUS_TRIDECEMLINEATUS);
		taxonIDToSpecies.put("9796", EQUUS_CABALLUS);
		taxonIDToSpecies.put("7757", PETROMYZON_MARINUS);
		taxonIDToSpecies.put("28377", ANOLIS_CAROLINENSIS );
		taxonIDToSpecies.put("30608", MICROCEBUS_MURINUS );
		taxonIDToSpecies.put("7175", CULEX_PIPIENS );
		taxonIDToSpecies.put("9600",  PONGO_PYGMAEUS);
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
		assemblyPrefixToSpecies.put("FUGU", TAKIFUGU_RUBRIPES);
		assemblyPrefixToSpecies.put("AgamP", ANOPHELES_GAMBIAE);
		assemblyPrefixToSpecies.put("WS", CAENORHABDITIS_ELEGANS);
		assemblyPrefixToSpecies.put("CBR", CAENORHABDITIS_BRIGGSAE);
		assemblyPrefixToSpecies.put("JGI", CIONA_INTESTINALIS);
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
		assemblyPrefixToSpecies.put("ARMA", DASYPUS_NOVEMCINCTUS);
		assemblyPrefixToSpecies.put("MEDAKA", ORYZIAS_LATIPES);
		assemblyPrefixToSpecies.put("TENREC", ECHINOPS_TELFAIRI);
		assemblyPrefixToSpecies.put("RABBIT", ORYCTOLAGUS_CUNICULUS);
		assemblyPrefixToSpecies.put("PIG", SUS_SCROFA);
		assemblyPrefixToSpecies.put("CSAV", CIONA_SAVIGNYI);
		assemblyPrefixToSpecies.put("BROADS", GASTEROSTEUS_ACULEATUS);
		assemblyPrefixToSpecies.put("CAT", FELIS_CATUS);
		assemblyPrefixToSpecies.put("BUSHBABY", OTOLEMUR_GARNETTII);
		assemblyPrefixToSpecies.put("MICROBAT", MYOTIS_LUCIFUGUS);
		assemblyPrefixToSpecies.put("COMMON_SHREW", SOREX_ARANEUS);
		assemblyPrefixToSpecies.put("PIKA", OCHOTONA_PRINCEPS);
		assemblyPrefixToSpecies.put("OANA", ORNITHORHYNCHUS_ANATINUS);
		assemblyPrefixToSpecies.put("HEDGEHOG", ERINACEUS_EUROPAEUS);
		assemblyPrefixToSpecies.put("cavPor", CAVIA_PORCELLUS);
		assemblyPrefixToSpecies.put("TREESHREW", TUPAIA_BELANGERI);
		assemblyPrefixToSpecies.put("SQUIRREL", SPERMOPHILUS_TRIDECEMLINEATUS);
		assemblyPrefixToSpecies.put("EquCab", EQUUS_CABALLUS);
		assemblyPrefixToSpecies.put("PMAR", PETROMYZON_MARINUS);
		assemblyPrefixToSpecies.put("AnoCar", ANOLIS_CAROLINENSIS);
		assemblyPrefixToSpecies.put("micMur", MICROCEBUS_MURINUS);
		assemblyPrefixToSpecies.put("CpiJ", CULEX_PIPIENS);
		assemblyPrefixToSpecies.put("PPYG", PONGO_PYGMAEUS);
		// and the other way around
		it = assemblyPrefixToSpecies.keySet().iterator();
		while (it.hasNext()) {
			String ap = (String) it.next();
			Species species = (Species) assemblyPrefixToSpecies.get(ap);
			speciesToAssemblyPrefix.put(species, ap);
		}
		
		// stable ID prefixes - ensembl databases
		ensemblStableIDPrefix.put(AEDES_AEGYPTI, "IGNORE");
		ensemblStableIDPrefix.put(ANOPHELES_GAMBIAE, "IGNORE");
		ensemblStableIDPrefix.put(BOS_TAURUS, "ENSBTA");
		ensemblStableIDPrefix.put(CAENORHABDITIS_ELEGANS, "IGNORE");
		ensemblStableIDPrefix.put(CANIS_FAMILIARIS, "ENSCAF");
		ensemblStableIDPrefix.put(CAVIA_PORCELLUS, "ENSCPO");
		ensemblStableIDPrefix.put(CIONA_INTESTINALIS, "ENSCIN");
		ensemblStableIDPrefix.put(CIONA_SAVIGNYI, "ENSCSAV");
		ensemblStableIDPrefix.put(DANIO_RERIO, "ENSDAR");
		ensemblStableIDPrefix.put(DASYPUS_NOVEMCINCTUS, "ENSDNO");
		ensemblStableIDPrefix.put(DROSOPHILA_MELANOGASTER, "IGNORE");
		ensemblStableIDPrefix.put(ECHINOPS_TELFAIRI, "ENSETE");
		ensemblStableIDPrefix.put(ERINACEUS_EUROPAEUS, "ENSEEU");
		ensemblStableIDPrefix.put(EQUUS_CABALLUS, "ENSECA");
		ensemblStableIDPrefix.put(FELIS_CATUS, "ENSFCA");
		ensemblStableIDPrefix.put(GALLUS_GALLUS, "ENSGAL");
		ensemblStableIDPrefix.put(GASTEROSTEUS_ACULEATUS, "ENSGAC");
		ensemblStableIDPrefix.put(HOMO_SAPIENS, "ENS");
		ensemblStableIDPrefix.put(LOXODONTA_AFRICANA, "ENSLAF");
		ensemblStableIDPrefix.put(MACACA_MULATTA, "ENSMMU");
		ensemblStableIDPrefix.put(MONODELPHIS_DOMESTICA, "ENSMOD");
		ensemblStableIDPrefix.put(MUS_MUSCULUS, "ENSMUS");
		ensemblStableIDPrefix.put(MYOTIS_LUCIFUGUS, "ENSMLU");
		ensemblStableIDPrefix.put(OCHOTONA_PRINCEPS, "ENSOPR");
		ensemblStableIDPrefix.put(ORNITHORHYNCHUS_ANATINUS, "ENSOAN");
		ensemblStableIDPrefix.put(ORYCTOLAGUS_CUNICULUS, "ENSOCU");
		ensemblStableIDPrefix.put(ORYZIAS_LATIPES, "ENSORL");
		ensemblStableIDPrefix.put(OTOLEMUR_GARNETTII, "ENSOGA");
		ensemblStableIDPrefix.put(PAN_TROGLODYTES, "ENSPTR");
		ensemblStableIDPrefix.put(SOREX_ARANEUS, "ENSSAR");
		ensemblStableIDPrefix.put(RATTUS_NORVEGICUS, "ENSRNO");
		ensemblStableIDPrefix.put(SACCHAROMYCES_CEREVISIAE, "IGNORE");
		ensemblStableIDPrefix.put(TAKIFUGU_RUBRIPES, "ENSTRU");
		ensemblStableIDPrefix.put(TETRAODON_NIGROVIRIDIS, "IGNORE");
		ensemblStableIDPrefix.put(TUPAIA_BELANGERI, "ENSTBE");
		ensemblStableIDPrefix.put(XENOPUS_TROPICALIS, "ENSXET");
		ensemblStableIDPrefix.put(SPERMOPHILUS_TRIDECEMLINEATUS, "ENSSTO");
		ensemblStableIDPrefix.put(PETROMYZON_MARINUS, "ENSPMA");
		ensemblStableIDPrefix.put(ANOLIS_CAROLINENSIS, "ENSACA");
		ensemblStableIDPrefix.put(MICROCEBUS_MURINUS, "ENSMIC");
		ensemblStableIDPrefix.put(CULEX_PIPIENS, "CPIJ");
		ensemblStableIDPrefix.put(PONGO_PYGMAEUS, "ENSPPY");
		// stable ID prefixes - Vega databases
		vegaStableIDPrefix.put(HOMO_SAPIENS, "OTTHUM");
		vegaStableIDPrefix.put(MUS_MUSCULUS, "OTTMUS");
		vegaStableIDPrefix.put(CANIS_FAMILIARIS, "OTTCAN");
		vegaStableIDPrefix.put(DANIO_RERIO, "OTTDAR");

	}

	// -----------------------------------------------------------------
	/**
	 * Resolve an alias to a Species object.
	 * 
	 * @param speciesAlias
	 *          The alias (e.g. human, homosapiens, hsapiens)
	 * @return The species object corresponding to alias, or Species.UNKNOWN if it
	 *         cannot be resolved.
	 */
	public static Species resolveAlias(String speciesAlias) {

		String alias = speciesAlias.toLowerCase();

		// --------------------------------------

		if (in(alias, "human,hsapiens,homosapiens,homo_sapiens")) {

			return HOMO_SAPIENS;

		}

		// -------------------------------------

		if (in(alias, "mosquito,anopheles,agambiae,anophelesgambiae,anopheles_gambiae")) {

			return ANOPHELES_GAMBIAE;

		}

		// --------------------------------------

		if (in(alias, "elegans,celegans,caenorhabditiselegans,caenorhabditis_elegans")) {

			return CAENORHABDITIS_ELEGANS;

		}

		// --------------------------------------

		if (in(alias, "briggsae,cbriggsae,caenorhabditisbriggsae,caenorhabditis_briggsae")) {

			return CAENORHABDITIS_BRIGGSAE;

		}

		// --------------------------------------

		if (in(alias, "zebrafish,danio,drerio,daniorerio,danio_rerio")) {

			return DANIO_RERIO;

		}

		// --------------------------------------
		if (in(alias, "pufferfish,fugu,frubripes,fugurubripes,fugu_rubripes,takifugu,trubripes,takifugurubripes,takifugu_rubripes")) {

			return TAKIFUGU_RUBRIPES;

		}

		// --------------------------------------

		if (in(alias, "drosophila,dmelongaster,drosophilamelanogaster,drosophila_melanogaster")) {

			return DROSOPHILA_MELANOGASTER;

		}

		// --------------------------------------

		if (in(alias, "culex,culex_pipiens,culexpipiens")) {

			return CULEX_PIPIENS;

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

		if (in(alias, "chimp,chimpanzee,ptroglodytes,pantroglodytes,pan_troglodytes")) {

			return PAN_TROGLODYTES;

		}

		// --------------------------------------

		if (in(alias, "chicken,chick,ggallus,gallusgallus,gallus_gallus")) {

			return GALLUS_GALLUS;

		}

		// --------------------------------------

		if (in(alias, "tetraodon,tnigroviridis,tetraodonnigroviridis,tetraodon_nigroviridis")) {

			return TETRAODON_NIGROVIRIDIS;

		}

		// --------------------------------------

		if (in(alias, "honeybee,honey_bee,apis,amellifera,apismellifera,apis_mellifera")) {

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

		if (in(alias, "pipid,pipidfrog,xenopus,xtropicalis,xenopustropicalis,xenopus_tropicalis")) {

			return XENOPUS_TROPICALIS;

		}

		// --------------------------------------

		if (in(
				alias,
				"opossum,monodelphis,mdomestica,mdomesticus,monodelphisdomestica,monodelphisdomesticus,monodelphis_domestica,monodelphis_domesticus")) {

			return MONODELPHIS_DOMESTICA;

		}

		// --------------------------------------

		if (in(alias, "yeast,saccharomyces,scerevisiae,saccharomycescerevisiae,saccharomyces_cerevisiae")) {

			return SACCHAROMYCES_CEREVISIAE;

		}

		// --------------------------------------

		if (in(alias, "cionaintestinalis,ciona_intestinalis,ciona_int")) {

			return CIONA_INTESTINALIS;

		}

		// --------------------------------------

		if (in(alias, "macacamulatta,macaca_mulatta,rhesusmacaque,rhesus_macaque,macaque")) {

			return MACACA_MULATTA;

		}

		// --------------------------------------

		if (in(alias, "elephant,loxodonta_africana,nelly,loxodonta,african_elephant,african_savannah_elephant,african_bush_elephant")) {

			return LOXODONTA_AFRICANA;

		}

		// --------------------------------------

		if (in(alias, "armadillo,dasypus_novemcinctus,daisy,dasypus,nine_banded_armadillo,nine-banded_armadillo,texas_armadillo")) {

			return DASYPUS_NOVEMCINCTUS;

		}

		// --------------------------------------

		if (in(alias, "medaka,oryzias_latipes,oryzias,japanese_medaka,japanese_rice_fish,japanese_ricefish,japanese_killifish")) {

			return ORYZIAS_LATIPES;

		}

		// --------------------------------------

		if (in(alias, "tenrec,echinops_telfairi,echinops,small_madagascar_hedgehog,lesser_hedgehog_tenrec")) {

			return ECHINOPS_TELFAIRI;

		}

		// --------------------------------------

		if (in(alias, "rabbit,oryctolagus_cuniculus,oryctolagus,domestic_rabbit,bunny,japanese_white_rabbit,european_rabbit")) {

			return ORYCTOLAGUS_CUNICULUS;

		}

		// --------------------------------------

		if (in(alias, "cat,felis_catus,fcatus,felis,domestic_cat")) {

			return FELIS_CATUS;

		}

		// --------------------------------------

		if (in(alias, "bushbaby,bush_baby,galago,small_eared_galago,ogarnettii,otolemur_garnettii,otolemur")) {

			return OTOLEMUR_GARNETTII;

		}

		// --------------------------------------

		if (in(alias, "microbat,little_brown_bat,mlucifugus,myotis_lucifugus,myotis")) {

			return MYOTIS_LUCIFUGUS;

		}

		// --------------------------------------

		if (in(alias, "shrew,common_shrew,commonShrew,european_shrew,sorex_araneus,saraneus,sorex")) {

			return SOREX_ARANEUS;

		}
		// --------------------------------------
                if (in(alias, "pika,Americanpika,American_pika,ochotona_princeps,oprinceps,ochotona")) {

			return OCHOTONA_PRINCEPS;

		}
		// --------------------------------------
		if (in(alias, "hedgehog,erinaceus_europaeus,european_hedgehog,eeuropaeus")) {

			return ERINACEUS_EUROPAEUS;

		}
		// --------------------------------------
		if (in(alias, "guineapig,guinea_pig,cavia_porcellus,cporcellus")) {

			return CAVIA_PORCELLUS;

		}
		// --------------------------------------
		if (in(alias, "treeshrew,tbelangeri,tupaia_belangeri,northern_tree_shrew,common_tree_shrew")) {

			return TUPAIA_BELANGERI;

		}
		// --------------------------------------
		if (in(alias, "squirrel,stridecemlineatus,thirteen-lined_ground_squirrel,spermophilus_tridecemlineatus,spermophilus_tridecemlineatus_arenicola")) {

			return SPERMOPHILUS_TRIDECEMLINEATUS;

		}

		// --------------------------------------

		if (in(alias, "pig,boar,wildboar,wild_boar,susscrofa,sus_scrofa")) {

			return SUS_SCROFA;

		}

		// --------------------------------------

		if (in(alias, "savignyi,ciona_savignyi,csavignyi,cionasavignyi")) {

			return CIONA_SAVIGNYI;

		}

		// --------------------------------------

		if (in(alias, "stickleback,gas_aculeatus,gasaculeatus,gasterosteus_aculeatus,gasterosteusaculeatus")) {

			return GASTEROSTEUS_ACULEATUS;

		}

		// --------------------------------------

		if (in(alias, "aedes,aedes_aegypti,aedesaegypti")) {

			return AEDES_AEGYPTI;

		}

		// --------------------------------------

		if (in(alias, "platypus,ornithorhynchus_anatinus,oanatius")) {

			return ORNITHORHYNCHUS_ANATINUS;

		}

		// --------------------------------------

		if (in(alias, "horse,equus_caballus,equus,mr_ed,ecaballus")) {

			return EQUUS_CABALLUS;

		}
		// --------------------------------------

		if (in(alias, "lamprey,sea_laprey,pmarinus,petromyzon")) {

			return PETROMYZON_MARINUS;

		}
		// --------------------------------------

		if (in(alias, "lizard,anole,anolis_lizard,anolis,anolis_carolinensis")) {

			return ANOLIS_CAROLINENSIS;

		}
		if (in(alias, "mouse_lemur,mouselemur,microcebus,microcebus_murinus")) {

			return MICROCEBUS_MURINUS;

		}
		if (in(alias, "orangutan,orang-utan,ppygmaeus,pongo_pygmaeus")) {

			return PONGO_PYGMAEUS;

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
	 *          The species to look up.
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
	 *          The taxonomy ID to look up.
	 * @return The species associated with t, or Species.UNKNOWN if none is found.
	 */
	public static Species getSpeciesFromTaxonomyID(String t) {

		Species result = UNKNOWN;

		if (taxonIDToSpecies.containsKey(t)) {
			result = (Species) taxonIDToSpecies.get(t);
		} else {
			logger.warning("Cannot get species for taxonomy ID " + t + " returning Species.UNKNOWN");
		}

		return result;

	}

	// -----------------------------------------------------------------
	/**
	 * Return true if alias appears somewhere in a string.
	 */
	private static boolean in(String alias, String list) {

		String[] aliases = list.split(",");
		for (int i = 0; i < aliases.length; i++) {
			if (alias.equals(aliases[i])) {
				return true;
			}
		}

		return false;

	}

	// -------------------------------------------------------------------------
	/**
	 * Return a Species object corresponding to a particular assembly prefix.
	 * 
	 * @param prefix
	 *          The assembly prefix.
	 * 
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
	 *          The species.
	 * @return The assembly prefix for s.
	 */
	public static String getAssemblyPrefixForSpecies(Species s) {

		return (String) speciesToAssemblyPrefix.get(s);

	}

//-------------------------------------------------------------------------
	/**
	 * Get the stable ID prefix for a species.
	 * 
	 * @param s
	 *          The species.
	 * @param t The type of database.
	 * @return The stable ID prefix for s. Note "IGNORE" is used for imported species.
	 */
	public static String getStableIDPrefixForSpecies(Species s, DatabaseType t) {

		String result = "";
		
		if (t.equals(DatabaseType.CORE)) {
		result = (String) ensemblStableIDPrefix.get(s);
		} else if (t.equals(DatabaseType.VEGA)) {
			result = (String) vegaStableIDPrefix.get(s);
		}
		
		if (result == null || result.equals("")) {
			logger.warning("Can't get stable ID prefix for " +s.toString() + " " + t.toString() + " database");
		}
		
		return result;

	}

	// -----------------------------------------------------------------

}
