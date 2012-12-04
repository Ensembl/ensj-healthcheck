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
 * "human_frontal_lobe_rnaseq.bam", rhe data files API automatically deals
 * with file extensions, or " human_frontal_lobe_rnaseq" (extrenous spaces)
 * 
 * @author ayates
 */
public class DataFiles extends AbstractTemplatedTestCase {

  public DataFiles() {
    addToGroup("release");
    addToGroup("pre-compara-handover");
    addToGroup("post-compara-handover");
    setDescription("Check that the data_file tables are correctly formatted. Includes searching for bad file extensions and spaces in names");
    setPriority(Priority.AMBER);
    setTeamResponsible(Team.GENEBUILD);
  }

  public void types() {
    removeAppliesToType(DatabaseType.SANGER_VEGA);
  }
  
  @Override
  protected boolean runTest(DatabaseRegistryEntry dbre) {
    boolean ok = runExtensionTest(dbre);
    ok &= runSpaceTest(dbre);
    return ok;
  }
  
  protected boolean testByPattern(DatabaseRegistryEntry dbre, Pattern p, StringCallback callback) {
    boolean ok = true;
    String sql = "select name from data_file";
    List<String> names = getSqlTemplate(dbre).queryForDefaultObjectList(sql, String.class);
    for(String name: names) {
      Matcher m = p.matcher(name);
      if(m.find()) {
        ok = false;
        String msg = callback.report(dbre, name, m);
        ReportManager.problem(this, dbre.getConnection(), msg);
      }
    }
    return ok;
  }

  protected boolean runExtensionTest(DatabaseRegistryEntry dbre) {
    Pattern p = Pattern.compile("\\.([A-Za-z]+)$");
    StringCallback callback = new StringCallback(){
      @Override
      public String report(DatabaseRegistryEntry dbre, String name, Matcher m) {
        return format("The data_file '%s' has a possible extension '%s'. " +
        		"Do not specify extensions in name; The DataFiles API will add " +
        		"this for you", name, m.group(1));
      }
    };
    return testByPattern(dbre, p, callback);
  }
  
  protected boolean runSpaceTest(DatabaseRegistryEntry dbre) {
    Pattern p = Pattern.compile("\\s");
    StringCallback callback = new StringCallback(){
      @Override
      public String report(DatabaseRegistryEntry dbre, String name, Matcher m) {
        return format("The data_file '%s' has a space in the name '%s'. " +
        		"Do not use spaces", name);
      }
    };
    return testByPattern(dbre, p, callback);
  }
  
  private interface StringCallback {
    String report(DatabaseRegistryEntry dbre, String name, Matcher m);
  }
}
