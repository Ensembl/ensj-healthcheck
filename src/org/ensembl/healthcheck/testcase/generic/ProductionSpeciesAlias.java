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

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.SqlTemplate;
import org.ensembl.healthcheck.util.StringListMapRowMapper;

public class ProductionSpeciesAlias extends SingleDatabaseTestCase {

    public ProductionSpeciesAlias() {
        setDescription("Check that all the species aliases in production are also in the core databases");
        setPriority(Priority.AMBER);
        setEffect("Missing aliases can not be searched for");
        setFix("Re-run populate_species_meta.pl script");
        setTeamResponsible(Team.GENEBUILD);
        setSecondTeamResponsible(Team.RELEASE_COORDINATOR);
    }


    @Override
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

        String species = dbre.getSpecies().toString();

        Connection con = dbre.getConnection();
        DatabaseRegistryEntry prodDbre = getProductionDatabase();
        Connection prodCon = prodDbre.getConnection();

        List<String> dbAliases = DBUtils.getColumnValuesList(con,
                "SELECT DISTINCT(meta_value) FROM meta WHERE meta_key = 'species.alias'");
        List<String> productionAliases = DBUtils.getColumnValuesList(prodCon,
                "SELECT sa.alias FROM species_alias sa, species s " + "WHERE s.species_id = sa.species_id "
                        + "AND s.db_name = '" + species + "' AND " + "s.is_current = 1 AND sa.is_current = 1");

        result &= checkName(dbre, prodDbre, species);

        if (result) {
            // Looking for aliases which have been added directly into the
            // species
            // database
            // These should always be added to the production database, then
            // synced
            // across using the populate_species_meta script
            result &= checkHasAlias(dbre, dbAliases, productionAliases, "production");

            // Looking for aliases which are missing from the species database
            // This means the populate_species_meta script has not been run
            // since
            // the entry was added to the production database
            // The populate_species_meta script is located in
            // ensembl/misc-scripts/production_database/scripts
            result &= checkHasAlias(dbre, productionAliases, dbAliases, "species");
            result &= checkUrl(dbre, prodDbre, species);
            result &= checkScientific(dbre, prodDbre, species);
            result &= checkConsistent(dbre, species);
        }
        return result;

    }

    private <T extends CharSequence> boolean checkHasAlias(DatabaseRegistryEntry dbre, Collection<T> core,
            Collection<T> toRemove, String type) {
        Collection<String> dbOnly = (Collection<String>) CollectionUtils.subtract(core, toRemove);
        if (dbOnly.isEmpty()) {
            return true;
        } else {
            for (String key : dbOnly) {
                String msg = String.format("Species alias '%s' is not in the %s database", key, type);
                ReportManager.problem(this, dbre.getConnection(), msg);
            }
            return false;
        }
    }

    // Checking species URL name
    // Should be both in the production and the core databases
    // Should start with a capital letter and have underscores between the names
    private <T extends CharSequence> boolean checkUrl(DatabaseRegistryEntry dbre, DatabaseRegistryEntry prodDbre,
            String species) {
        SqlTemplate t = DBUtils.getSqlTemplate(dbre);
        SqlTemplate prodt = DBUtils.getSqlTemplate(prodDbre);
        String sql = "SELECT meta_value FROM meta WHERE meta_key = 'species.url'";
        String prodSql = "SELECT url_name FROM species WHERE db_name = ?";
        String url = t.queryForDefaultObject(sql, String.class);

        List<String> prodUrlL = prodt.queryForDefaultObjectList(prodSql, String.class, species);
        if (prodUrlL.isEmpty()) {
            ReportManager.problem(this, dbre.getConnection(),
                    "Species " + species + " not found in the production database");
            return false;
        } else {
            String prodUrl = prodUrlL.get(0);
            if (url.equals(prodUrl)) {
                if (url.matches("^[A-Z]{1}[a-z0-9]*(_[a-zA-Z0-9]*)+")) {
                    ReportManager.correct(this, dbre.getConnection(),
                            "species.url '" + url + "' is the same in both databases and is in the correct format");
                    return true;
                } else {
                    ReportManager.problem(this, dbre.getConnection(), "species.url '" + url
                            + "' is not in the correct format. Should start with a capital letter and have underscores to separate names");
                    return false;
                }
            } else {
                ReportManager.problem(this, dbre.getConnection(), "species.url '" + url
                        + "' in database does not match '" + prodUrl + "' in the production database");
                return false;
            }
        }
    }

    // Checking species production name
    // Should be both in the production and the core databases
    // Should contain only lower case caracters and underscores
    private <T extends CharSequence> boolean checkName(DatabaseRegistryEntry dbre, DatabaseRegistryEntry prodDbre,
            String species) {
        SqlTemplate t = DBUtils.getSqlTemplate(dbre);
        SqlTemplate prodt = DBUtils.getSqlTemplate(prodDbre);
        String sql = "SELECT meta_value FROM meta WHERE meta_key = 'species.production_name'";
        String prodSql = "SELECT production_name FROM species WHERE db_name = ?";
        String name = t.queryForDefaultObject(sql, String.class);
        List<String> prodNameL = prodt.queryForDefaultObjectList(prodSql, String.class, species);
        if (prodNameL.isEmpty()) {
            ReportManager.problem(this, dbre.getConnection(),
                    "species " + species + " not found in production database");
            return false;
        } else if (prodNameL.size() > 1) {
            ReportManager.problem(this, dbre.getConnection(),
                    "Multiple entries for species " + species + " found in production database");
            return false;
        } else {
            String prodName = prodNameL.get(0);
            if (name.equals(prodName)) {
                if (name.matches("^[a-z0-9_]*$")) {
                    ReportManager.correct(this, dbre.getConnection(), "species.production_name '" + name
                            + "' is the same in both databases and is in the correct format");
                    return true;
                } else {
                    ReportManager.problem(this, dbre.getConnection(), "species.production_name '" + name
                            + "' is not in the correct format. It should only contain lower case caracters and underscores");
                    return false;
                }
            } else {
                ReportManager.problem(this, dbre.getConnection(), "species.production_name '" + name
                        + "' in database does not match '" + prodName + "' in the production database");
                return false;
            }
        }
    }

    // Checking species scientific name
    // Should be both in the production and the core databases
    // Should start with a capital letter and have spaces between the names
    private <T extends CharSequence> boolean checkScientific(DatabaseRegistryEntry dbre, DatabaseRegistryEntry prodDbre,
            String species) {
        SqlTemplate t = DBUtils.getSqlTemplate(dbre);
        SqlTemplate prodt = DBUtils.getSqlTemplate(prodDbre);
        String sql = "SELECT meta_value FROM meta WHERE meta_key = 'species.scientific_name'";
        String prodSql = "SELECT scientific_name FROM species WHERE db_name = ?";
        String url = t.queryForDefaultObject(sql, String.class);
        String prodUrl = prodt.queryForDefaultObject(prodSql, String.class, species);
        if (url.equals(prodUrl)) {
            if (url.matches("^[A-Z]{1}[a-z0-9]*( [a-z0-9]*){1,2}")) {
                ReportManager.correct(this, dbre.getConnection(), "species.scientific_name '" + url
                        + "' is the same in both databases and is in the correct format");
                return true;
            } else {
                ReportManager.problem(this, dbre.getConnection(), "species.scientific_name '" + url
                        + "' is not in the correct format. Should start with a capital letter and have underscores to separate names");
                return false;
            }
        } else {
            ReportManager.problem(this, dbre.getConnection(), "species.scientific '" + url
                    + "' in database does not match '" + prodUrl + "' in the production database");
            return false;
        }
    }

    // Checking all species meta keys are consistent
    // Once removed all the capitalisation and separators, should all be the
    // same
    private <T extends CharSequence> boolean checkConsistent(DatabaseRegistryEntry dbre, String species) {
        boolean result = true;
        SqlTemplate t = DBUtils.getSqlTemplate(dbre);
        StringBuilder shortName = new StringBuilder();
        for (String speciesChunk : species.split("_")) {
            shortName.append(speciesChunk);
        }
        String all_sql = "SELECT meta_value, meta_key FROM meta WHERE meta_key in ('species.scientific_name', 'species.production_name', 'species.url')";
        Map<String, List<String>> keys = t.queryForMap(all_sql, new StringListMapRowMapper());
        String sql = "SELECT meta_value FROM meta WHERE meta_key in ('species.scientific_name', 'species.production_name', 'species.url')";
        List<String> names = t.queryForDefaultObjectList(sql, String.class);
        for (String name : names) {
            StringBuilder fullName = new StringBuilder();
            for (String nameChunk : name.split("_| ")) {
                fullName.append(nameChunk.toLowerCase());
            }
            if (fullName.equals(shortName)) {
                ReportManager.problem(this, dbre.getConnection(),
                        keys.get(name) + " has a meta value which does not match the correct species name " + species);
                result = false;
            }
        }
        return result;
    }

}
