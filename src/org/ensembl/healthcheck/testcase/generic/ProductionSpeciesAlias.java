package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.SqlTemplate;
import java.util.regex.Matcher;


public class ProductionSpeciesAlias extends SingleDatabaseTestCase {
  
  public ProductionSpeciesAlias() {
    addToGroup("production");
    addToGroup("release");
    addToGroup("pre-compara-handover");
    addToGroup("post-compara-handover");
    
    setDescription("Check that all the species aliases in production are also in the core databases");
    setPriority(Priority.AMBER);
    setEffect("Missing aliases can not be searched for");
    setFix("Re-run populate_species_meta.pl script");
    setTeamResponsible(Team.RELEASE_COORDINATOR);
    setSecondTeamResponsible(Team.GENEBUILD);
  }

  public void types() {
    removeAppliesToType(DatabaseType.SANGER_VEGA);
  }
  
  @Override
  public boolean run(DatabaseRegistryEntry dbre) {

    boolean result = true;

    String species = dbre.getSpecies().toString();

    Connection con = dbre.getConnection();
    DatabaseRegistryEntry prodDbre = getProductionDatabase();
    Connection prodCon = prodDbre.getConnection();

    List<String> dbAliases = DBUtils.getColumnValuesList(con, "SELECT DISTINCT(meta_value) FROM meta WHERE meta_key = 'species.alias'");
    List<String> productionAliases = DBUtils.getColumnValuesList(prodCon, 
         "SELECT sa.alias FROM species_alias sa, species s " +
          "WHERE s.species_id = sa.species_id " +
          "AND s.db_name = '" + species + "' AND " +
          "s.is_current = 1 AND sa.is_current = 1");

// Looking for aliases which have been added directly into the species database
// These should always be added to the production database, then synced across using the populate_species_meta script
    result &= checkHasAlias(dbre, dbAliases, productionAliases, "production");

// Looking for aliases which are missing from the species database
// This means the populate_species_meta script has not been run since the entry was added to the production database
// The populate_species_meta script is located in ensembl/misc-scripts/production_database/scripts
    result &= checkHasAlias(dbre, productionAliases, dbAliases, "species");

    result &= checkUrl(dbre, prodDbre, species);

    return result;

    }

    private <T extends CharSequence> boolean checkHasAlias(DatabaseRegistryEntry dbre, Collection<T> core, Collection<T> toRemove, String type) {
      Collection<String> dbOnly = (Collection<String>)CollectionUtils.subtract(core, toRemove);
      if (dbOnly.isEmpty()) {
        return true;
      } else {
        for (String key: dbOnly) {
          String msg = String.format("Species alias '%s' is not in the %s database", key, type);
          ReportManager.problem(this, dbre.getConnection(), msg);
        }
        return false;
      }
    }

// Looking for species URL name
// Should be both in the production and the core databases
// Should start with a capital letter and have underscores between the names
    private <T extends CharSequence> boolean checkUrl(DatabaseRegistryEntry dbre, DatabaseRegistryEntry prodDbre, String species) {
      SqlTemplate t = DBUtils.getSqlTemplate(dbre);
      SqlTemplate prodt = DBUtils.getSqlTemplate(prodDbre);
      String sql = "SELECT meta_value FROM meta WHERE meta_key = 'species.url'";
      String prodSql = "SELECT url_name FROM species WHERE db_name = ?"; 
      String url = t.queryForDefaultObject(sql, String.class);
      String prodUrl = prodt.queryForDefaultObject(prodSql, String.class, species);
      if (url.equals(prodUrl)) {
        if (url.matches("^[A-Z]{1}[a-z]*(_[a-z]*){1,2}")) {
          ReportManager.correct(this, dbre.getConnection(), "species.url " + url + " is the same in both databases and is in the correct format");
          return true;
        } else {
          ReportManager.problem(this, dbre.getConnection(), "species.url " + url + " is not in the correct format. Should start with a capital letter and have underscores to separate names");
          return false;
        }
      } else {
        ReportManager.problem(this, dbre.getConnection(), "species.url " + url + " in database does not match " + prodUrl + " in the production database");
        return false;
      }
    }


}
