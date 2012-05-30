package org.ensembl.healthcheck.testcase.generic;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.SqlTemplate;

public class ProductionAnalysisLogicName extends AbstractTemplatedTestCase {
  
  public ProductionAnalysisLogicName() {
    addToGroup("production");
    addToGroup("release");
    addToGroup("pre-compara-handover");
    addToGroup("post-compara-handover");
    
    setDescription("Check that the content of the analysis logic names in the core databases are subsets of production");
    setPriority(Priority.AMBER);
    setEffect("Discrepancies between tables can cause problems");
    setFix("Resync tables");
    setTeamResponsible(Team.GENEBUILD);
    setTeamResponsible(Team.RELEASE_COORDINATOR);
  }

  public void types() {
    removeAppliesToType(DatabaseType.SANGER_VEGA);
  }
  
  @Override
  protected boolean runTest(DatabaseRegistryEntry dbre) {
    boolean result = true;
    Set<String> coreLogicNames = getLogicNamesDb(dbre);
    Set<String> productionLogicNames = getLogicNamesFromProduction(dbre);
    result &= testForIdentity(dbre, coreLogicNames, productionLogicNames, "production");
    result &= testForIdentity(dbre, productionLogicNames, coreLogicNames, "core");
    return result;
  }
  
  /**
   * Minuses the elements in the second collection from the first. Anything
   * remaining in the first collection cannot exist in the second set 
   */
  private <T extends CharSequence> boolean testForIdentity(DatabaseRegistryEntry dbre, Collection<T> core, Collection<T> toRemove, String type) {
    Set<T> missing = new HashSet<T>(core);
    missing.removeAll(toRemove);
    if(missing.isEmpty()) {
      return true;
    }
    for(CharSequence name: missing) {
      String msg = String.format("The logic name '%s' is missing from %s", name, type);
      ReportManager.problem(this, dbre.getConnection(), msg);
    }
    return false;
  }
  
  private Set<String> getLogicNamesDb(DatabaseRegistryEntry dbre) {
    SqlTemplate t = DBUtils.getSqlTemplate(dbre);
    String sql = "select logic_name from analysis";
    List<String> results = t.queryForDefaultObjectList(sql, String.class);
    return new HashSet<String>(results);
  }
  
  private Set<String> getLogicNamesFromProduction(DatabaseRegistryEntry dbre) {
    SqlTemplate t = DBUtils.getSqlTemplate(getProductionDatabase());
    String sql = "select logic_name from full_analysis_description where full_db_name =?";
    String name = DBUtils.getShortDatabaseName(dbre.getConnection());
    List<String> results = t.queryForDefaultObjectList(sql, String.class, name);
    return new HashSet<String>(results);
  }
}
