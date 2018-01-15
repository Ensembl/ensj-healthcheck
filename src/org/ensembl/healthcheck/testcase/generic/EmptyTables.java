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
 * Copyright (C) 2004 EBI, GRL
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

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;

// import org.apache.commons.collections.CollectionUtils;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.Utils;
import org.ensembl.healthcheck.util.SqlTemplate;
import org.ensembl.healthcheck.util.CollectionUtils;


/**
 * Check that all tables have data.
 */
public class EmptyTables extends SingleDatabaseTestCase {

    // list of tables grouped by category 
    private Set<String> idMappingTables, densityTables, markerTables,
	miscTables, karyotypeTables, ditagDataTables;
    // list of species which contain data for the above tables
    private Set<Species> idMappingSpecies, densitySpecies, markerSpecies,
	miscSpecies, karyotypeSpecies, ditagDataSpecies;
    // a map of database types to a list of tables which are
    // allowed to be empty for that particular db type
    Map<DatabaseType, Set<String> > allowedEmptyTablesMap;

    /**
     * Creates a new instance of EmptyTablesTestCase
     */
    public EmptyTables() {

	setDescription("Checks that all tables have data");

	setTeamResponsible(Team.GENEBUILD);
        setSecondTeamResponsible(Team.RELEASE_COORDINATOR);

	// initialize lists of tables grouped by category 
	// and the corresponding species
	initTablesAndSpeciesLists();
    }

    // ---------------------------------------------------------------------

    /**
     * Define what tables are to be checked.
     */
    private Set<String> getTablesToCheck(final DatabaseRegistryEntry dbre) {

	Species species = dbre.getSpecies();
	DatabaseType type = dbre.getType();
	boolean karyotype = karyotypeExists(dbre);

	Set<String> tables = this.getTableNames(dbre, type, species);

	// ----------------------------------------------------

	// the list of tables to check for ancestral sequences is 
	// already set by method getTableNames
	if(species == Species.ANCESTRAL_SEQUENCES) {
	    return tables; 
	}

	// do no check for emptyness for some tables for certain 
	// database types (i.e. CORE and VEGA), and certain species
	if(allowedEmptyTablesMap.containsKey(type)) {
	    tables.removeAll(allowedEmptyTablesMap.get(type));

	    if(idMappingSpecies.contains(species) == false) {
		tables.removeAll(idMappingTables); // don't check id mapping tables
	    }
	    if(densitySpecies.contains(species) == false && !karyotype) {
		// don't check density tables
		// WARNING: these tables are populated only for species with a karyotype
		tables.removeAll(densityTables);
	    }
	    if(markerSpecies.contains(species) == false) {
		// don't check marker tables
		tables.removeAll(markerTables);
	    }
	    if(miscSpecies.contains(species) == false) {
		// don't check misc tables
		tables.removeAll(miscTables);
	    }
	    if(karyotypeSpecies.contains(species) == false) {
		// don't check karyotype banding tables
		tables.removeAll(karyotypeTables);
	    }
	    if(ditagDataSpecies.contains(species) == false) {
		// don't check ditag data tables
		tables.removeAll(ditagDataTables);
	    }
	    
	}
 
	// ad-hoc adjustment for zebrafish in VEGA
	if (type == DatabaseType.VEGA && species == Species.DANIO_RERIO) {
	    tables.remove("ontology_xref");
	}

	return tables;

    }

    // ---------------------------------------------------------------------

    /**
     * Check that every table has more than 0 rows.
     * 
     * @param dbre
     *          The database to check.
     * @return true if the test passed.
     */
    public boolean run(DatabaseRegistryEntry dbre) {

	boolean result = true;

	Set<String> tables = getTablesToCheck(dbre);
	Connection con = dbre.getConnection();

	for (String table: tables) {

	    // logger.finest("Checking that " + table + " has rows");

	    if (!tableHasRows(con, table)) {

		ReportManager.problem(this, con, table + " has zero rows");
		result = false;

	    }
	}

	if (result) {
	    ReportManager.correct(this, con, "All required tables have data");
	}

	return result;

    } // run

    // -----------------------------------------------------------------

    protected boolean karyotypeExists(DatabaseRegistryEntry dbre) {
	Connection con = dbre.getConnection();
	SqlTemplate t = DBUtils.getSqlTemplate(dbre);
	boolean result = true;
	String sqlKaryotype = "SELECT count(*) FROM seq_region_attrib sa, attrib_type at WHERE at.attrib_type_id = sa.attrib_type_id AND code = 'karyotype_rank'";
	int karyotype = t.queryForDefaultObject(sqlKaryotype, Integer.class);
	if (karyotype == 0) {
	    result = false;
	}
	return result;
    }

    // -----------------------------------------------------------------

    private Set<String> getTableNames(final DatabaseRegistryEntry dbre, final DatabaseType type, final Species species) {
	
	if (species == Species.ANCESTRAL_SEQUENCES) {
	    // Only a few tables need to be filled in ancestral databases
	    return CollectionUtils.createLinkedHashSet("meta", "coord_system", "dna", "seq_region");
	}

	if (type == DatabaseType.OTHERFEATURES || type == DatabaseType.CDNA) {
	    // Only a few tables need to be filled in EST
	    return CollectionUtils.createLinkedHashSet("analysis", "analysis_description", "assembly", 
						       "attrib_type", "coord_system", "dna_align_feature", 
						       "external_db", "meta_coord", "meta", "misc_set", 
						       "seq_region", "seq_region_attrib", "unmapped_reason");
	
	} else if (type == DatabaseType.RNASEQ ) {
	    // the same for RNASEQ
	    return CollectionUtils.createLinkedHashSet("analysis", "analysis_description", "assembly", 
						       "attrib_type", "coord_system", "data_file", 
						       "dna_align_feature", "external_db", "meta_coord", 
						       "meta", "misc_set", "seq_region", "seq_region_attrib", 
						       "unmapped_reason");

	}	

	// get the full list of tables
	Set<String> tables = 
	    CollectionUtils.createLinkedHashSet(DBUtils.getTableNames(dbre.getConnection()));

	// remove views since we don't care if they're empty
	Set<String> views = 
	    CollectionUtils.createLinkedHashSet(DBUtils.getViews(dbre.getConnection()).toArray(new String[]{}));
	tables.removeAll(views);
	
	// remove backup tables (starting with backup_)
	// they are allowed to be empty
	Set<String> backUpTables = new HashSet<String>();
	for (String table : tables) {
	    if (table.startsWith("backup_")) {
		backUpTables.add(table); 
	    }
	}

	tables.removeAll(backUpTables);

	return tables;
    }

    private void initTablesAndSpeciesLists() {
	/*
	  NOTE:
	  If a species list is empty, it is assumed all 
	  available species have data on the corresponding
	  tables
	 */

	// ID mapping tables, species
	idMappingTables = 
	    CollectionUtils.createLinkedHashSet("gene_archive", "peptide_archive", 
						"mapping_session", "stable_id_event");
	idMappingSpecies = new HashSet<Species>(); // ID mapping related tables are checked in a separate test case

	// density tables and species
	densityTables = CollectionUtils.createLinkedHashSet("density_feature", "density_type");
	densitySpecies = new HashSet<Species>(); // all species should have density mapping data (provided they have karyotype)

	// marker tables and species
	markerTables =
	    CollectionUtils.createLinkedHashSet("map", "marker", "marker_map_location", 
						"marker_synonym", "marker_feature");
	markerSpecies =
	    CollectionUtils.createLinkedHashSet(Species.HOMO_SAPIENS,
						Species.MUS_MUSCULUS,
						Species.RATTUS_NORVEGICUS,
						Species.DANIO_RERIO,
						Species.BOS_TAURUS,
						Species.CANIS_FAMILIARIS,
						Species.GALLUS_GALLUS,
						Species.MACACA_MULATTA,
						Species.SUS_SCROFA);

	// misc tables and species
	miscTables =
	    CollectionUtils.createLinkedHashSet("misc_feature", "misc_feature_misc_set", 
						"misc_set", "misc_attrib");
	miscSpecies = 
	    CollectionUtils.createLinkedHashSet(Species.HOMO_SAPIENS,
						Species.DANIO_RERIO);
	
	// karyotype banding tables and species
	// only certain species have a karyotype banding
	karyotypeTables = CollectionUtils.createLinkedHashSet("karyotype");
	karyotypeSpecies = 
	    CollectionUtils.createLinkedHashSet(Species.DROSOPHILA_MELANOGASTER,
						Species.HOMO_SAPIENS,
						Species.MUS_MUSCULUS,
						Species.RATTUS_NORVEGICUS);

	// ditag data tables and species
	// only human, mouse and medaka currently have ditag data
	ditagDataTables = CollectionUtils.createLinkedHashSet("ditag", "ditag_feature");
	ditagDataSpecies = 
	    CollectionUtils.createLinkedHashSet(Species.HOMO_SAPIENS,
						Species.MUS_MUSCULUS,
						Species.ORYZIAS_LATIPES);


	// init map of database types to a list of tables which are 
	// allowed to be empty for that particular db type
	allowedEmptyTablesMap = new HashMap<DatabaseType, Set<String> >();
	allowedEmptyTablesMap.put(DatabaseType.CORE, 
				  CollectionUtils.createLinkedHashSet("alt_allele", "alt_allele_attrib", "alt_allele_group", "assembly_exception", "data_file", 
								      "dnac", "seq_region_mapping", "unconventional_transcript_association", 
								      "operon", "operon_transcript", "operon_transcript_gene", 
								      "intron_supporting_evidence", "transcript_intron_supporting_evidence", "associated_xref", 
								      "associated_group", "qtl", "qtl_feature", "qtl_synonym"));
	allowedEmptyTablesMap.put(DatabaseType.VEGA, 
				  CollectionUtils.createLinkedHashSet("alt_allele", "alt_allele_attrib", "alt_allele_group", "assembly_exception", "data_file", 
								      "dnac", "seq_region_mapping", "unconventional_transcript_association", 
								      "operon", "operon_transcript", "operon_transcript_gene", 
								      "intron_supporting_evidence", "transcript_intron_supporting_evidence", "associated_xref", 
								      "associated_group", "qtl", "qtl_feature", "qtl_synonym", 
								      "affy_array", "affy_feature", "affy_probe", 
								      "ditag", "ditag_feature", "dna", 
								      "external_synonym", "identity_xref", 
								      "map", "mapping_session", "marker", 
								      "marker_feature", "marker_map_location", "marker_synonym", 
								      "misc_attrib", "misc_feature", "misc_feature_misc_set", 
								      "misc_set", "prediction_exon", "prediction_transcript", 
								      "repeat_consensus", "repeat_feature", "simple_feature", 
								      "supporting_feature", "transcript_attrib", "unconventional_transcript_association", 
								      "dependent_xref", "seq_region_synonym", "density_feature", "unmapped_object",
								      "mapping_set", "density_type", "genome_statistics"));

    }

} // EmptyTablesTestCase
