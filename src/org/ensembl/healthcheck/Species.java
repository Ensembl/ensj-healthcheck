/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2017] EMBL-European Bioinformatics Institute
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

package org.ensembl.healthcheck;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.ensembl.healthcheck.DatabaseType;

public enum Species {
	// defined new Species and properties: taxonomy_id, assemblyprefix, stableIDprefix, alias
	OCTODON_DEGUS(10160, "OctDeg", "ENSODE", "degu,brush_tailed_rat,10160"),
	CHINCHILLA_LANIGERA(34839, "ChiLan", "ENSCLA", "long_tailed_chinchilla,Chinchilla_lanigera,chinchilla,chinchilla_lanigera"),
	CAVIA_APEREA(37548, "CavAp", "ENSCAP",""),
	FUKOMYS_DAMARENSIS(885580,"DMR_v","ENSFDA",""), 
	JACULUS_JACULUS(51337,"JacJac","ENSJJA",""),
	MESOCRICETUS_AURATUS(10036,"MesAur","ENSMAU",""),
	MUS_CAROLI(10089,"CAROLI_EIJ","MGP_CAROLIEiJ_",""),
	MUS_PAHARI(10093,"PAHARI_EIJ","MGP_PahariEiJ_",""),
	NANNOSPALAX_GALILI(1026970,"S.galili","ENSNGA",""),
	PEROMYSCUS_MANICULATUS_BAIRDII(230844,"Pman","ENSPEM",""),
	MICROTUS_OCHROGASTER(79684,"MicOch","ENSMOC",""), 
	HETEROCEPHALUS_GLABER_FEMALE(10181,"HetGla_female","ENSHGL",""),
	HETEROCEPHALUS_GLABER_MALE(10181,"HetGla","ENSHGL",""),
	CRICETULUS_GRISEUS_CHOK1GSHD(10029,"CHOK1GS_HD","ENSCGR",""),
	CRICETULUS_GRISEUS_CRIGRI(10029,"CriGri","ENSCGR",""),
	
	AOTUS_NANCYMAAE (37293, "Anan", "ENSANA",""),
	CEBUS_CAPUCINUS (9516, "Cebus_imitator", "ENSCCA",""),
	CERCOCEBUS_ATYS (9531, "Caty", "ENSCAT",""),
	COLOBUS_ANGOLENSIS_PALLIATUS (9531, "Cang.pa", "ENSCAN",""),
	MACACA_FASCICULARIS (9541, "Macaca_fascicularis", "ENSMFA",""),
	MACACA_NEMESTRINA (9545, "Mnem", "ENSMNE",""),
	MANDRILLUS_LEUCOPHAEUS (956, "Mleu.le", "ENSMLE",""),
	PAN_PANISCUS (9597, "panpan", "ENSPPA",""),
	PROPITHECUS_COQUERELI (379532, "Pcoq", "ENSPCO",""),
	RHINOPITHECUS_BIETI (66062, "ASM169854", "ENSRBI",""),
	RHINOPITHECUS_ROXELLANA (61622, "Rrox", "ENSRRO",""),
	NOMASCUS_LEUCOGENYS (61853, "Nleu", "ENSNLE",""),
	


	
	AEDES_AEGYPTI(7159, "", "IGNORE", "aedes,aedesaegypti,aedes_aegypti"),
	AILUROPODA_MELANOLEUCA(9646, "ailMel", "ENSAME", "panda,giant panda,ailuropoda melanoleuca,ailuropoda_melanoleuca"),
	ANAS_PLATYRHYNCHOS(8839, "BGI_duck", "ENSAPL","anapla,apla,mallard,anas_platyrhynchos,aplatyrhynchos,duck,anas platyrhynchos"),
	ANOLIS_CAROLINENSIS(28377, "AnoCar", "ENSACA", "lizard,anole,anolis_lizard,anolis,anolis_carolinensis"),
	ANOPHELES_GAMBIAE(7165, "AgamP", "IGNORE", "mosquito,anopheles,agambiae,anophelesgambiae,anopheles_gambiae"),
	APIS_MELLIFERA(7460, "AMEL", "IGNORE", "honeybee,honey_bee,apis,amellifera,apismellifera,apis_mellifera"),
        ASTYANAX_MEXICANUS(7994, "AstMex", "ENSAMX", "amex,amexicanus,astmex,astyanax mexicanus,astyanax_mexicanus,cave fish"),
	BOS_TAURUS(9913, "UMD", "ENSBTA", "cow,btaurus,bostaurus,bos_taurus"),
	CAENORHABDITIS_BRIGGSAE(6238, "CBR", "IGNORE", "briggsae,cbriggsae,caenorhabditisbriggsae,caenorhabditis_briggsae"),
	CAENORHABDITIS_ELEGANS(6239, "WBcel", "IGNORE", "elegans,celegans,caenorhabditiselegans,caenorhabditis_elegans"),
	CALLITHRIX_JACCHUS(9483, "C_jacchus", "ENSCJA", "marmoset,white-tufted-ear marmoset,callithrix_jacchus,callithrix jacchus,Callithrix_jacchus,Callithrix jacchus,callithrix"),
	CANIS_FAMILIARIS(9615, "CanFam", "ENSCAF", "dog,doggy,cfamiliaris,canisfamiliaris,canis_familiaris"),
        CAVIA_PORCELLUS(10141, "CAVPOR", "ENSCPO", "guineapig,guinea_pig,cporcellus,cavia_porcellus"),
        CERATOTHERIUM_SIMUM_SIMUM(73337,"CerSimSim","ENSCSI","ceratotherium simum simum,ceratotherium_simum_simum,cersim,csim,csimum_simum,rhinoceros"),
	CHOLOEPUS_HOFFMANNI(9358, "choHof", "ENSCHO", "Sloth,Two-toed_sloth,Hoffmans_two-fingered_sloth,choloepus_hoffmanni"),
        CHLOROCEBUS_SABAEUS(60711,"ChlSab","ENSCSA","chlorocebus_sabaeus,chlorocebus_aethiops_sabaeus,vervet monkey,african green monkey,green monkey"), 
	CIONA_INTESTINALIS(7719, "KH", "ENSCIN", "cionaintestinalis,ciona_int,ciona_intestinalis"),
	CIONA_SAVIGNYI(51511, "CSAV", "ENSCSAV", "savignyi,cionasavignyi,csavignyi,ciona_savignyi"),
        CRICETULUS_GRISEUS(10029, "CriGri", "ENSCGR", "hamster,chinese_hamster,cgriseus,cricetulus_griseus"),
	CULEX_PIPIENS(7175, "CpiJ", "CPIJ", "culex,culexpipiens,culex_pipiens"),
	DANIO_RERIO(7955, "GRCz", "ENSDAR", "zebrafish,danio,drerio,daniorerio,danio_rerio"),
	DASYPUS_NOVEMCINCTUS(9361, "DasNov", "ENSDNO", "armadillo,daisy,dasypus,nine_banded_armadillo,nine-banded_armadillo,texas_armadillo,dasypus_novemcinctus"),
	DIPODOMYS_ORDII(10020, "Dord", "ENSDOR", "ords_kangaroo_rat,ordskangaroorat,kangaroo_rat, kangaroorat , dipodomys_ordii"),
	DROSOPHILA_ANANASSAE(7217, "dana", "IGNORE", "drosophila,ananassae,drosophilaananassae,drosophila_ananassae,dana"),
	DROSOPHILA_GRIMSHAWI(7222, "dgri", "IGNORE", "drosophila,grimshawi,drosophilagrimshawi,drosophila_grimshawi,dgri"),
	DROSOPHILA_MELANOGASTER(7227, "BDGP", "IGNORE", "drosophila,dmelongaster,drosophilamelanogaster,drosophila_melanogaster"),
	DROSOPHILA_PSEUDOOBSCURA(7237, "BCM-HGSC", "IGNORE", "drosophila,pseudoobscura,drosophilapseudoobscura,drosophila_pseudoobscura,dpse"),
	DROSOPHILA_WILLISTONI(7260, "dwil", "IGNORE", "drosophila,willistoni,drosophilawillistonii,drosophila_willistoni,dwil"),
	DROSOPHILA_YAKUBA(7245, "dyak", "IGNORE", "drosophila,yakuba,drosophilayakuba,drosophila_yakuba,dyak"),
	ECHINOPS_TELFAIRI(9371, "TENREC", "ENSETE", "tenrec,echinops,small_madagascar_hedgehog,lesser_hedgehog_tenrec,echinops_telfairi"),
	EQUUS_CABALLUS(9796, "EquCab", "ENSECA", "horse,equus,mr_ed,ecaballus,equus_caballus"),
	ERINACEUS_EUROPAEUS(9365, "HEDGEHOG", "ENSEEU", "hedgehog,european_hedgehog,eeuropaeus,erinaceus_europaeus"),
	FICEDULA_ALBICOLLIS(59894, "FicAlb", "ENSFAL", "flycatcher,falbicollis,collared_flycatcher,f_albicollis,ficalb"),
	FELIS_CATUS(9685, "Felis_catus", "ENSFCA", "cat,fcatus,felis,domestic_cat,felis_catus"),
        GADUS_MORHUA(8049, "gadMor", "ENSGMO", "cod,gadus_morhua,gmorhua,atlantic_cod"),
	GALLUS_GALLUS(9031, "Gallus_gallus-", "ENSGAL", "chicken,chick,ggallus,gallusgallus,gallus_gallus"),
	GASTEROSTEUS_ACULEATUS(69293, "BROADS", "ENSGAC", "stickleback,gas_aculeatus,gasaculeatus,gasterosteusaculeatus,gasterosteus_aculeatus"),
	GORILLA_GORILLA(9595, "gorGor", "ENSGGO", "gorilla,gorilla_gorilla,ggor"),
	HETEROCEPHALUS_GLABER(10181, "HetGla", "ENSHGL", "naked_mole_rat,heterocephalus_glaber,hglaber"),
	HOMO_SAPIENS(9606, "GRCh", "ENS", "human,hsapiens,homosapiens,homo_sapiens"),
	ICTIDOMYS_TRIDECEMLINEATUS(43179, "spetri", "ENSSTO", "squirrel,stridecemlineatus,thirteen-lined_ground_squirrel,ictidomys_tridecemlineatus_arenicola,ictidomys_tridecemlineatus"),
        LATIMERIA_CHALUMNAE(7897,"LatCha", "ENSLAC","coelacanth,latimeria_chalumnae,latimeria,l_chalumnae,Latimeria chalumnae"),
        LEPISOSTEUS_OCULATUS(7918, "LepOcu","ENSLOC","spotted_gar"),
        LOXODONTA_AFRICANA(9785, "LoxAfr", "ENSLAF", "elephant,nelly,loxodonta,african_elephant,african_savannah_elephant,african_bush_elephant,loxodonta_africana"),
	MACACA_MULATTA(9544, "Mmul", "ENSMMU", "macacamulatta,rhesusmacaque,rhesus_macaque,macaque,macaca_mulatta"),
  NOTAMACROPUS_EUGENII(9315, "Meug", "ENSMEU", "wallaby,tammar_wallaby,natomacropuseugenii,n_eugenii,notamacropus_eugenii,Notamacropus eugenii,macropuseugenii,m_eugenii,tammarwallaby,Macropus eugenii,macropus_eugenii"),
	MELEAGRIS_GALLOPAVO(9103, "UMD", "ENSMGA", "turkey,common turkey,wild turkey,meleagris_gallopavo, meleagris_gallopavo"),
	MELOPSITTACUS_UNDULATUS(13146, "MelUnd", "ENSMUN", "budgerigar,melopsittacus_undulatus,mundulatus"),
	MICROCEBUS_MURINUS(30608, "Mmur", "ENSMIC", "mouse_lemur,mouselemur,microcebus,microcebus_murinus"),
	MONODELPHIS_DOMESTICA(13616, "BROADO", "ENSMOD", "opossum,monodelphis,mdomestica,mdomesticus,monodelphisdomestica,monodelphisdomesticus,monodelphis_domesticus,monodelphis_domestica"),
	MUS_MUSCULUS(10090, "GRCm", "ENSMUS", "mouse,mmusculus,musmusculus,mus_musculus"),
	MUS_MUSCULUS_129S1SVIMJ(10090, "129S1_SvImJ", "MGP_129S1SvImJ_", ""), 
	MUS_MUSCULUS_AJ(10090, "A_J", "MGP_AJ_", ""),
	MUS_MUSCULUS_AKRJ(10090, "AKR_J", "MGP_AKRJ_", ""),
	MUS_MUSCULUS_BALBCJ(10090, "BALB_cJ", "MGP_BALBcJ_", ""),
	MUS_MUSCULUS_C3HHEJ(10090, "C3H_HeJ", "MGP_C3HHeJ_", ""),
	MUS_MUSCULUS_C57BL6NJ(10090, "C57BL_6NJ", "MGP_C57BL6NJ_", ""),
	MUS_MUSCULUS_CASTEIJ(10091, "CAST_EiJ", "MGP_CASTEiJ_", ""),
	MUS_MUSCULUS_CBAJ(10090, "CBA_J", "MGP_CBAJ_", ""),
	MUS_MUSCULUS_DBA2J(10090, "DBA_2J", "MGP_DBA2J_", ""),
	MUS_MUSCULUS_FVBNJ(10090, "FVB_NJ", "MGP_FVBNJ_", ""),
	MUS_MUSCULUS_LPJ(10090, "LP_J", "MGP_LPJ_", ""),
	MUS_MUSCULUS_NODSHILTJ(10090, "NOD_ShiLtJ", "MGP_NODShiLtJ_", ""),
	MUS_MUSCULUS_NZOHLLTJ(10090, "NZO_HlLtJ", "MGP_NZOHlLtJ_", ""),
	MUS_MUSCULUS_PWKPHJ(39442, "PWK_PhJ", "MGP_PWKPhJ_", ""),
	MUS_MUSCULUS_WSBEIJ(10092, "WSB_EiJ", "MGP_WSBEiJ_", ""),
	MUS_SPRETUS_SPRETEIJ(10096, "SPRET_EiJ", "MGP_SPRETEiJ_", ""),
        MUSTELA_PUTORIUS_FURO(9669, "MusPutFur", "ENSMPU", "ferret,domestic ferret,Mustela_putorius_furo,Mustela putorius furo"),
	MYOTIS_LUCIFUGUS(59463, "Myoluc", "ENSMLU", "microbat,little_brown_bat,mlucifugus,myotis,myotis_lucifugus"),
	NOMASCUS_LEUCOGENYS(61853, "NLEU", "ENSNLE","gibbon,nleu,nomleu,nleugogenys,nomascus_leucogenys"),
	OCHOTONA_PRINCEPS(9978, "OchPri", "ENSOPR", "pika,Americanpika,American_pika,oprinceps,ochotona,ochotona_princeps"),
	OREOCHROMIS_NILOTICUS(8128, "Orenil", "ENSONI", "tilapia,Oreochromis niloticus,oreochromis niloticus,Oreochromis_niloticus,oreochromis_niloticus,Nile tilapia,nile tilapia,Nile_tilapia,nile_tilapia,O. niloticus"),
	ORNITHORHYNCHUS_ANATINUS(9258, "OANA", "ENSOAN", "platypus,oanatius,ornithorhynchus_anatinus"),
        ORYCTEROPUS_AFER_AFER(1230840,"OryAfe","ENSOAF","aardvark,oafe,oafer_after,oryafe,orycteropus afer afer,orycteropus_afer_afer"), 
	ORYCTOLAGUS_CUNICULUS(9986, "OryCun", "ENSOCU", "rabbit,oryctolagus,domestic_rabbit,bunny,japanese_white_rabbit,european_rabbit,oryctolagus_cuniculus"),
	ORYZIAS_LATIPES(8090, "MEDAKA", "ENSORL", "medaka,oryzias,japanese_medaka,japanese_rice_fish,japanese_ricefish,japanese_killifish,oryzias_latipes"),
	OTOLEMUR_GARNETTII(30611, "OtoGar", "ENSOGA", "bushbaby,bush_baby,galago,small_eared_galago,ogarnettii,otolemur,otolemur_garnettii"),
	OVIS_ARIES(9940, "Oar", "ENSOAR", "ovis_aries,oaries,oviari,sheep"),
	PAN_TROGLODYTES(9598, "CHIMP", "ENSPTR", "chimp,chimpanzee,ptroglodytes,pantroglodytes,pan_troglodytes"),
        PAPIO_ANUBIS(9555,"PapAnu", "ENSPAN", "papio_anubis"),
	PAPIO_HAMADRYAS(9557, "Pham", "ENSPHA", "baboon,Papio_hamadryas,papio_hamadryas,papio_hamadryas,sacred_baboon,western_baboon,red_baboon"),
	PELODISCUS_SINENSIS(13735, "PelSin", "ENSPSI", "Chinese_softshell_turtle,turtle,softshell_turtle,Trionyx_sinensis"),
	PETROMYZON_MARINUS(7757, "Pmarinus", "ENSPMA", "lamprey,sea_lamprey,pmarinus,petromyzon,petromyzon_marinus"),
	POECILIA_FORMOSA(48698, "PoeFor", "ENSPFO", "amazon molly,poecilia_formosa,pformosa,poefor,pfor"),
        PHYSETER_MACROCEPHALUS(9755,"PhyMac", "ENSPMC", "physeter_macrocephalus, sperm whale, pmac, pmacrocephalus, physeter macrocephalus, phymac"),
	PONGO_ABELII(9601, "PPYG", "ENSPPY", "orangutan,orang-utan,pabellii,pongo_abelii"),
	PROCAVIA_CAPENSIS(9813, "PROCAP", "ENSPCA", "cape_rock_hyrax,caperockhyrax,procaviacapensis,procavia_capensis"),
	PTEROPUS_VAMPYRUS(132908, "PTEVAM", "ENSPVA", "large_flying_fox,largeflyingfox,pteropusvampyrus,pteropus_vampyrus"),
	RATTUS_NORVEGICUS(10116, "Rnor", "ENSRNO", "rat,rnovegicus,rattusnorvegicus,rattus_norvegicus"),
	SACCHAROMYCES_CEREVISIAE(4932, "R", "IGNORE", "yeast,saccharomyces,scerevisiae,saccharomycescerevisiae,saccharomyces_cerevisiae"),
	SAIMIRI_BOLIVIENSIS(39432, "SaiBol", "ENSSBO", "saimiri_boliviensis,sboliviensis,squirrel_monkey,bolivian_squirrel_monkey,squirrelmonkey"),
	SARCOPHILUS_HARRISII(9305, "devil", "ENSSHA", "devil,Sarcophilus_harrisii,sarcophilus_harrisii,tasmanian_devil,taz"),
	SOREX_ARANEUS(42254, "COMMON_SHREW", "ENSSAR", "shrew,common_shrew,commonShrew,european_shrew,saraneus,sorex,sorex_araneus"),
	SUS_SCROFA(9823, "Sscrofa", "ENSSSC", "pig,boar,wildboar,wild_boar,susscrofa,sus_scrofa"),
	TAENIOPYGIA_GUTTATA(59729, "taeGut", "ENSTGU", "zebrafinch,zebra_finch,taeniopygia_guttata,taeniopygiaguttata,tguttata,poephila_guttata,taenopygia_guttata"),
	TAKIFUGU_RUBRIPES(31033, "FUGU", "ENSTRU", "pufferfish,fugu,frubripes,fugurubripes,fugu_rubripes,takifugu,trubripes,takifugurubripes,takifugu_rubripes"),
  CARLITO_SYRICHTA(1868482, "TARSYR", "ENSTSY", "philippine_tarsier,philippinetarsier,carlitosyrichta,carlito_syrichta,Carlito syrichta,tarsiussyrichta,tarsius_syrichta"),
	TETRAODON_NIGROVIRIDIS(99883, "TETRAODON", "IGNORE", "tetraodon,tnigroviridis,tetraodonnigroviridis,tetraodon_nigroviridis"),
	TUPAIA_BELANGERI(37347, "TREESHREW", "ENSTBE", "treeshrew,tbelangeri,northern_tree_shrew,common_tree_shrew,tupaia_belangeri"),
	TURSIOPS_TRUNCATUS(9739, "TURTRU", "ENSTTR", "bottlenosed_dolphin,dolphin,tursiopstruncatus,tursiops_truncatus"),
	VICUGNA_PACOS(30538, "VICPAC", "ENSVPA", "alpaca,vicugnapacos,vicugna_pacos"),
	XENOPUS_TROPICALIS(8364, "JGI", "ENSXET", "pipid,pipidfrog,xenopus,xtropicalis,xenopustropicalis,xenopus_tropicalis"),
	XIPHOPHORUS_MACULATUS(8083, "Xipmac", "ENSXMA", "xiphophorous_maculatus,platyfish,southern_platyfish"),
//        MASTER_SCHEMA(0, "", "", "master_schema,masterschema,schema"),
	HEALTHCHECK(0, "", "", ""),
	HELP(0, "", "", ""),
	NCBI_TAXONOMY(0, "", "", ""),
	SYSTEM(0, "", "", ""),
	ENSEMBL_WEBSITE(0, "", "", ""),
	UNKNOWN(0, "", "", ""),
	ANCESTRAL_SEQUENCES(0, "", "", "ancestral,ancestor");

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
                        case RATTUS_NORVEGICUS:
                                vegaStableIDPrefix.put(Species.RATTUS_NORVEGICUS, "OTTRNO");
                                break;
                        case SUS_SCROFA:
                                vegaStableIDPrefix.put(Species.SUS_SCROFA, "OTTSUS");
                                break;
                        }

		}
	}

	private final int taxonID;

	private final String assemblyPrefix;

	private final String stableIDPrefix;

	private final String alias;
	
	private final Set<String> aliasSet;

	private Species(int tax_id, String assembly, String stableID, String alias) {
		this.taxonID = tax_id;
		this.assemblyPrefix = assembly;
		this.stableIDPrefix = stableID;
		this.alias = alias;
		
		//Build a hash set of lowercased aliases rather than using splits of aliases everytime
		Set<String> aliasSet = new HashSet<String>();
		for(String a: alias.split(",")) {
		  aliasSet.add(a.toLowerCase().trim());
		}
		aliasSet.add(this.name().toLowerCase());
		this.aliasSet = aliasSet;
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
		  if(s.aliasSet.contains(alias)) {
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
