package org.ensembl.healthcheck.testcase.generic;

import static java.lang.String.format;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;
import org.ensembl.healthcheck.testcase.Priority;

/**
 * Tries to avoid users specifying data files like 
 * "human_frontal_lobe_rnaseq.bam". The data files API automatically deals
 * with file extensions
 * 
 * @author ayates
 */
public class DataFiles extends AbstractTemplatedTestCase {

  public DataFiles() {
    addToGroup("release");
    addToGroup("pre-compara-handover");
    addToGroup("post-compara-handover");
    setDescription("Check that the data_file tables are correctly formatted. Includes searching for bad file extensions in names");
    setPriority(Priority.AMBER);
    setTeamResponsible(Team.GENEBUILD);
  }

  public void types() {
    removeAppliesToType(DatabaseType.SANGER_VEGA);
  }
  
  @Override
  protected boolean runTest(DatabaseRegistryEntry dbre) {
    return runExtensionTest(dbre);
  }

  protected boolean runExtensionTest(DatabaseRegistryEntry dbre) {
    String sql = "select name from data_file where name LIKE ?";
    List<String> names = getSqlTemplate(dbre).queryForDefaultObjectList(sql, String.class, "%.%");
    boolean ok = true;
    Pattern p = Pattern.compile("\\.(\\w+)$");
    for(String n: names) {
      Matcher m = p.matcher(n);
      if(m.find()) {
        String msg = format("The data_file %s has a possible extension '%s'. Do not specify extensions in name; The DataFiles API will add this for you", n, m.group(1));
        ReportManager.problem(this, dbre.getConnection(), msg);
      }
    }
    return ok;
  }
}
