package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.util.DBUtils;

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

    Collection<String> dbOnly = (Collection<String>)CollectionUtils.subtract(dbAliases, productionAliases);

    if (!dbOnly.isEmpty()) {
      for (String key: dbOnly) {
        String msg = String.format("Species alias '%s' is not in the production database, did you forget to add it?");
        ReportManager.problem(this, con, msg);
      }
      result = false;
    } else {
      ReportManager.correct(this, con, "All aliases in the core database are also in the production database");
    }

    Collection<String> prodOnly = (Collection<String>)CollectionUtils.subtract(productionAliases, dbAliases);

    if (!prodOnly.isEmpty()) {
      for (String key: prodOnly) {
        String msg = String.format("Species alias '%s' is not in the core database, did you forget to run the populate_species_meta script?");
        ReportManager.problem(this, con, msg);
      }
      result = false;
    } else {
      ReportManager.correct(this, con, "All aliases in the production database are also in the core database");
    }

    return result;
  }

}
