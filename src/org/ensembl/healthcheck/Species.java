package org.ensembl.healthcheck;

import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.EnumMap;

public enum Species {
	// defined new Species and properties: taxonomy_id, assemblyprefix, stableIDprefix, alias
	SARCOPHILUS_HARRISII(9305, "devil", "ENSSHA", "devil,Sarcophilus_harrisii,sarcophilus_harrisii,tasmanian_devil,taz"), AILUROPODA_MELANOLEUCA(9646, "ailMel", "ENSAME",
			"panda,giant panda,ailuropoda melanoleuca,ailuropoda_melanoleuca"), PAPIO_HAMADRYAS(9557, "Pham", "ENSPHA",
			"baboon,Papio_hamadryas,papio_hamadryas,papio_hamadryas,sacred_baboon,western_baboon,red_baboon"), MELEAGRIS_GALLOPAVO(9103, "UMD", "ENSMGA",
			"turkey,common turkey,wild turkey,meleagris_gallopavo, meleagris_gallopavo"), MACROPUS_EUGENII(9315, "Meug", "ENSMEU",
			"wallaby,tammar_wallaby,macropuseugenii,m_eugenii,tammarwallaby,Macropus eugenii,macropus_eugenii"), CALLITHRIX_JACCHUS(9483, "C_jacchus", "ENSCJA",
			"marmoset,white-tufted-ear marmoset,callithrix_jacchus,callithrix jacchus,Callithrix_jacchus,Callithrix jacchus,callithrix"), CHOLOEPUS_HOFFMANNI(9358, "choHof", "ENSCHO",
			"Sloth,Two-toed_sloth,Hoffmans_two-fingered_sloth,choloepus_hoffmanni"), GORILLA_GORILLA(9593, "gorGor", "ENSGGO", "gorilla,gorilla_gorilla,ggor"), TAENIOPYGIA_GUTTATA(59729, "taeGut",
			"ENSTGU", "zebrafinch,zebra_finch,taeniopygia_guttata,taeniopygiaguttata,tguttata,poephila_guttata,taenopygia_guttata"), ORYCTOLAGUS_CUNICULUS(9986, "oryCun", "ENSOCU",
			"rabbit,oryctolagus,domestic_rabbit,bunny,japanese_white_rabbit,european_rabbit,oryctolagus_cuniculus"), GALLUS_GALLUS(9031, "WASHUC", "ENSGAL",
			"chicken,chick,ggallus,gallusgallus,gallus_gallus"), DANIO_RERIO(7955, "Zv", "ENSDAR", "zebrafish,danio,drerio,daniorerio,danio_rerio"), CULEX_PIPIENS(7175, "CpiJ", "CPIJ",
			"culex,culexpipiens,culex_pipiens"), TAKIFUGU_RUBRIPES(31033, "FUGU", "ENSTRU", "pufferfish,fugu,frubripes,fugurubripes,fugu_rubripes,takifugu,trubripes,takifugurubripes,takifugu_rubripes"), CAENORHABDITIS_BRIGGSAE(
			6238, "CBR", "", "briggsae,cbriggsae,caenorhabditisbriggsae,caenorhabditis_briggsae"), FELIS_CATUS(9685, "CAT", "ENSFCA", "cat,fcatus,felis,domestic_cat,felis_catus"), MUS_MUSCULUS(10090,
			"NCBIM", "ENSMUS", "mouse,mmusculus,musmusculus,mus_musculus"), SPERMOPHILUS_TRIDECEMLINEATUS(43179, "SQUIRREL", "ENSSTO",
			"squirrel,stridecemlineatus,thirteen-lined_ground_squirrel,spermophilus_tridecemlineatus_arenicola,spermophilus_tridecemlineatus"), SUS_SCROFA(9823, "Sscrofa", "ENSSSC",
			"pig,boar,wildboar,wild_boar,susscrofa,sus_scrofa"), ERINACEUS_EUROPAEUS(9365, "HEDGEHOG", "ENSEEU", "hedgehog,european_hedgehog,eeuropaeus,erinaceus_europaeus"), MONODELPHIS_DOMESTICA(13616,
			"BROADO", "ENSMOD", "opossum,monodelphis,mdomestica,mdomesticus,monodelphisdomestica,monodelphisdomesticus,monodelphis_domesticus,monodelphis_domestica"), RATTUS_NORVEGICUS(10116, "RGSC",
			"ENSRNO", "rat,rnovegicus,rattusnorvegicus,rattus_norvegicus"), TETRAODON_NIGROVIRIDIS(99883, "TETRAODON", "IGNORE", "tetraodon,tnigroviridis,tetraodonnigroviridis,tetraodon_nigroviridis"), PONGO_ABELII(
			9600, "PPYG", "ENSPPY", "orangutan,orang-utan,pabellii,pongo_abelii"), HEALTHCHECK(0, "", "", ""), EQUUS_CABALLUS(9796, "EquCab", "ENSECA", "horse,equus,mr_ed,ecaballus,equus_caballus"), XENOPUS_TROPICALIS(
			8364, "JGI", "ENSXET", "pipid,pipidfrog,xenopus,xtropicalis,xenopustropicalis,xenopus_tropicalis"), SACCHAROMYCES_CEREVISIAE(4932, "SGD", "IGNORE",
			"yeast,saccharomyces,scerevisiae,saccharomycescerevisiae,saccharomyces_cerevisiae"), MACACA_MULATTA(9544, "MM", "ENSMMU", "macacamulatta,rhesusmacaque,rhesus_macaque,macaque,macaca_mulatta"), CAENORHABDITIS_ELEGANS(
			6239, "WS", "IGNORE", "elegans,celegans,caenorhabditiselegans,caenorhabditis_elegans"), SOREX_ARANEUS(42254, "COMMON_SHREW", "ENSSAR",
			"shrew,common_shrew,commonShrew,european_shrew,saraneus,sorex,sorex_araneus"), HOMO_SAPIENS(9606, "GRCh", "ENS", "human,hsapiens,homosapiens,homo_sapiens"), ORYZIAS_LATIPES(8090, "MEDAKA",
			"ENSORL", "medaka,oryzias,japanese_medaka,japanese_rice_fish,japanese_ricefish,japanese_killifish,oryzias_latipes"), AEDES_AEGYPTI(7159, "", "IGNORE", "aedes,aedesaegypti,aedes_aegypti"), APIS_MELLIFERA(
			7460, "AMEL", "", "honeybee,honey_bee,apis,amellifera,apismellifera,apis_mellifera"), PETROMYZON_MARINUS(7757, "PMAR", "ENSPMA", "lamprey,sea_laprey,pmarinus,petromyzon,petromyzon_marinus"), CIONA_INTESTINALIS(
			7719, "JGI", "ENSCIN", "cionaintestinalis,ciona_int,ciona_intestinalis"), OTOLEMUR_GARNETTII(30611, "BUSHBABY", "ENSOGA",
			"bushbaby,bush_baby,galago,small_eared_galago,ogarnettii,otolemur,otolemur_garnettii"), CANIS_FAMILIARIS(9615, "BROADD", "ENSCAF", "dog,doggy,cfamiliaris,canisfamiliaris,canis_familiaris"), HELP(
			0, "", "", ""), ANOPHELES_GAMBIAE(7165, "AgamP", "IGNORE", "mosquito,anopheles,agambiae,anophelesgambiae,anopheles_gambiae"), DROSOPHILA_MELANOGASTER(7227, "BDGP", "IGNORE",
			"drosophila,dmelongaster,drosophilamelanogaster,drosophila_melanogaster"), DROSOPHILA_PSEUDOOBSCURA(7237, "BCM-HGSC", "IGNORE",
			"drosophila,pseudoobscura,drosophilapseudoobscura,drosophila_pseudoobscura,dpse"), DROSOPHILA_ANANASSAE(7217, "dana", "IGNORE",
			"drosophila,ananassae,drosophilaananassae,drosophila_ananassae,dana"), DROSOPHILA_YAKUBA(7245, "dyak", "IGNORE", "drosophila,yakuba,drosophilayakuba,drosophila_yakuba,dyak"), DROSOPHILA_GRIMSHAWI(
			7222, "dgri", "IGNORE", "drosophila,grimshawi,drosophilagrimshawi,drosophila_grimshawi,dgri"), DROSOPHILA_WILLISTONI(7260, "dwil", "IGNORE",
			"drosophila,willistoni,drosophilawillistonii,drosophila_willistoni,dwil"), MYOTIS_LUCIFUGUS(59463, "MICROBAT", "ENSMLU", "microbat,little_brown_bat,mlucifugus,myotis,myotis_lucifugus"), NCBI_TAXONOMY(
			0, "", "", ""), SYSTEM(0, "", "", ""), CIONA_SAVIGNYI(51511, "CSAV", "ENSCSAV", "savignyi,cionasavignyi,csavignyi,ciona_savignyi"), TUPAIA_BELANGERI(37347, "TREESHREW", "ENSTBE",
			"treeshrew,tbelangeri,northern_tree_shrew,common_tree_shrew,tupaia_belangeri"), PAN_TROGLODYTES(9598, "CHIMP", "ENSPTR", "chimp,chimpanzee,ptroglodytes,pantroglodytes,pan_troglodytes"), GASTEROSTEUS_ACULEATUS(
			69293, "BROADS", "ENSGAC", "stickleback,gas_aculeatus,gasaculeatus,gasterosteusaculeatus,gasterosteus_aculeatus"), ENSEMBL_WEBSITE(0, "", "", ""), ECHINOPS_TELFAIRI(9371, "TENREC", "ENSETE",
			"tenrec,echinops,small_madagascar_hedgehog,lesser_hedgehog_tenrec,echinops_telfairi"), CAVIA_PORCELLUS(10141, "CAVPOR", "ENSCPO", "guineapig,guinea_pig,cporcellus,cavia_porcellus"), LOXODONTA_AFRICANA(
			9785, "LoxAfr", "ENSLAF", "elephant,nelly,loxodonta,african_elephant,african_savannah_elephant,african_bush_elephant,loxodonta_africana"), ANOLIS_CAROLINENSIS(28377, "AnoCar", "ENSACA",
			"lizard,anole,anolis_lizard,anolis,anolis_carolinensis"), ORNITHORHYNCHUS_ANATINUS(9258, "OANA", "ENSOAN", "platypus,oanatius,ornithorhynchus_anatinus"), DASYPUS_NOVEMCINCTUS(9361, "DasNov",
			"ENSDNO", "armadillo,daisy,dasypus,nine_banded_armadillo,nine-banded_armadillo,texas_armadillo,dasypus_novemcinctus"), OCHOTONA_PRINCEPS(9978, "PIKA", "ENSOPR",
			"pika,Americanpika,American_pika,oprinceps,ochotona,ochotona_princeps"), UNKNOWN(0, "", "", ""), MICROCEBUS_MURINUS(30608, "micMur", "ENSMIC",
			"mouse_lemur,mouselemur,microcebus,microcebus_murinus"), BOS_TAURUS(9913, "BTAU", "ENSBTA", "cow,btaurus,bostaurus,bos_taurus"), PROCAVIA_CAPENSIS(9813, "PROCAP", "ENSPCA",
			"cape_rock_hyrax,caperockhyrax,procaviacapensis,procavia_capensis"), PTEROPUS_VAMPYRUS(132908, "PTEVAM", "ENSPVA", "large_flying_fox,largeflyingfox,pteropusvampyrus,pteropus_vampyrus"), TARSIUS_SYRICHTA(
			9478, "TARSYR", "ENSTSY", "philippine_tarsier,philippinetarsier,tarsiussyrichta,tarsius_syrichta"), TURSIOPS_TRUNCATUS(9739, "TURTRU", "ENSTTR",
			"bottlenosed_dolphin,dolphin,tursiopstruncatus,tursiops_truncatus"), VICUGNA_PACOS(30538, "VICPAC", "ENSVPA", "alpaca,vicugnapacos,vicugna_pacos"), DIPODOMYS_ORDII(10020, "DIPORD", "ENSDOR",
			"ords_kangaroo_rat,ordskangaroorat,kangaroo_rat, kangaroorat , dipodomys_ordii"), NOMASCUS_LEUCOGENYS(61853, "NLEU", "ENSNLE","gibbon,nleu,nomleu,nleugogenys,nomascus_leucogenys"), ANCESTRAL_SEQUENCES(0, "", "", "ancestral,ancestor");

	// Taxonomy IDs - see ensembl-compara/sql/taxon.txt
	private static Map<Integer, Species> taxonIDToSpecies = new HashMap<Integer, Species>();

	private static Map<String, Species> assemblyPrefixToSpecies = new HashMap<String, Species>();

	private static Map<Species, String> vegaStableIDPrefix = new EnumMap<Species, String>(Species.class);

	private static Logger logger = Logger.getLogger("HealthCheckLogger");
	// populate the hash tables
	static {
		for (Species s : values()) {
			taxonIDToSpecies.put(s.getTaxonID(), s);
			assemblyPrefixToSpecies.put(s.getAssemblyPrefix(), s);
			// we have to add to the Vega hash the 4 species with Vega annotation
			switch (s) {
			case HOMO_SAPIENS:
				vegaStableIDPrefix.put(Species.HOMO_SAPIENS, "OTTHUM");
				break;
			case MUS_MUSCULUS:
				vegaStableIDPrefix.put(Species.MUS_MUSCULUS, "OTTMUS");
				break;
			case CANIS_FAMILIARIS:
				vegaStableIDPrefix.put(Species.CANIS_FAMILIARIS, "OTTCAN");
				break;
			case DANIO_RERIO:
				vegaStableIDPrefix.put(Species.DANIO_RERIO, "OTTDAR");
				break;
			}

		}
	}

	private final int taxonID;

	private final String assemblyPrefix;

	private final String stableIDPrefix;

	private final String alias;

	private Species(int tax_id, String assembly, String stableID, String alias) {
		this.taxonID = tax_id;
		this.assemblyPrefix = assembly;
		this.stableIDPrefix = stableID;
		this.alias = alias;
	}

	// getters for the properties
	public int getTaxonID() {
		return taxonID;
	};

	public String getAssemblyPrefix() {
		return assemblyPrefix;
	};

	public String getStableIDPrefix() {
		return stableIDPrefix;
	};

	public String getAlias() {
		return alias;
	};

	// methods to mantain backwards compatibility

	// -----------------------------------------------------------------
	/**
	 * Resolve an alias to a Species object.
	 * 
	 * @param speciesAlias
	 *          The alias (e.g. human, homosapiens, hsapiens)
	 * @return The species object corresponding to alias, or Species.UNKNOWN if it cannot be resolved.
	 */
	public static Species resolveAlias(String speciesAlias) {

		String alias = speciesAlias.toLowerCase();

		// --------------------------------------
		for (Species s : values()) {
			if (in(alias, s.getAlias())) {
				return s;
			}
		}
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
		result = Integer.toString(s.getTaxonID());
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
			if (alias.equals(aliases[i].trim())) {
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

		return (String) s.getAssemblyPrefix();

	}

	// -------------------------------------------------------------------------
	/**
	 * Get the stable ID prefix for a species.
	 * 
	 * @param s
	 *          The species.
	 * @param t
	 *          The type of database.
	 * @return The stable ID prefix for s. Note "IGNORE" is used for imported species.
	 */
	public static String getStableIDPrefixForSpecies(Species s, DatabaseType t) {

		String result = "";

		if (t.equals(DatabaseType.CORE)) {
			result = (String) s.getStableIDPrefix();
		} else if (t.equals(DatabaseType.VEGA)) {
			result = (String) vegaStableIDPrefix.get(s);
		}

		if (result == null || result.equals("")) {
			logger.warning("Can't get stable ID prefix for " + s.toString() + " " + t.toString() + " database");
		}

		return result;

	}

	// -------------------------------------------------------------------------
	/**
	 * Get the BioMart table root for a species (e.g. hsapiens, mmusculus)
	 */
	public String getBioMartRoot() {

		String[] bits = this.name().toLowerCase().split("_");

		return bits.length > 1 ? bits[0].substring(0, 1) + bits[1] : "";

	}

	public String toString() {

		return this.name().toLowerCase();
	}

	// -----------------------------------------------------------------
}
