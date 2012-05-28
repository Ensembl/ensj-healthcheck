package org.ensembl.healthcheck.testcase.generic;

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
  
  private Set<String> productionLogicNames = null;

  public ProductionAnalysisLogicName() {
    addToGroup("production");
    addToGroup("release");
    addToGroup("pre-compara-handover");
    addToGroup("post-compara-handover");
    
    setDescription("Check that the content of the analysis logic names in the core databases are subsets of production");
    setPriority(Priority.AMBER);
    setEffect("Discrepancies between tables can cause problems");
    setFix("Resync tables");
    setTeamResponsible(Team.RELEASE_COORDINATOR);
  }

  public void types() {
    removeAppliesToType(DatabaseType.SANGER_VEGA);
  }
  
  @Override
  protected boolean runTest(DatabaseRegistryEntry dbre) {
    Set<String> coreLogicNames = getLogicNamesDb(dbre);
    Set<String> productionLogicNames = getLogicNamesFromProduction();
    Set<String> missingNames = new HashSet<String>(coreLogicNames);
    missingNames.removeAll(productionLogicNames);
    if(!missingNames.isEmpty()) {
      for(String name: missingNames) {
        String msg = String.format("The logic_name '%s' is missing from production. Add and resync", name);
        ReportManager.problem(this, dbre.getConnection(), msg);
      }
      return false;
    }
    return true;
  }
  
  private Set<String> getLogicNamesDb(DatabaseRegistryEntry dbre) {
    SqlTemplate t = DBUtils.getSqlTemplate(dbre);
    String sql = "select logic_name from analysis";
    List<String> results = t.queryForDefaultObjectList(sql, String.class);
    return new HashSet<String>(results);
  }
  
  private Set<String> getLogicNamesFromProduction() {
    if(productionLogicNames == null) {
      SqlTemplate t = DBUtils.getSqlTemplate(getProductionDatabase());
      String sql = "select logic_name from analysis_description";
      List<String> results = t.queryForDefaultObjectList(sql, String.class);
      productionLogicNames = new HashSet<String>(results);
    }
    return productionLogicNames;
  }
}
