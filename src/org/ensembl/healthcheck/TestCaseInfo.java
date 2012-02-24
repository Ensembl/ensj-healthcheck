package org.ensembl.healthcheck;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.ensembl.healthcheck.testcase.EnsTestCase;
import org.ensembl.healthcheck.util.CollectionUtils;

import uk.co.flamingpenguin.jewel.JewelException;
import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.CliFactory;
import uk.co.flamingpenguin.jewel.cli.Option;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TestCaseInfo {
  
  public static interface CmdLine {
    
    @Option(shortName = "f", longName = "file", description = "JSON output location")
    String getFile();
    boolean isFile();

    @Option(helpRequest = true, description = "This message")
    boolean getHelp();
  }

  public static void main(String[] args) throws JewelException, IOException {
    
    CmdLine cli = null;
    try {
      cli = CliFactory.parseArguments(CmdLine.class, args);
    }
    catch(ArgumentValidationException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
    
    TestRegistry testRegistry = new DiscoveryBasedTestRegistry();
    List<EnsTestCase> testCases = testRegistry.getAll();
    List<Map<String, Object>> list = CollectionUtils.createArrayList();
    for(final EnsTestCase t: testCases) {
      Map<String, Object> info = CollectionUtils.createHashMap();
      info.put("package", t.getClass().getPackage().getName());
      info.put("name", t.getName());
      info.put("shortTestName", t.getShortTestName());
      info.put("description", t.getDescription());
      info.put("priority", t.getPriority());
      if(t.getTeamResponsible() != null) {
        info.put("responible", t.getTeamResponsible().name());
      }
      else {
        info.put("responible", null);
      }
      info.put("groups", t.getGroups());
      list.add(info);
    }
    
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    String output = gson.toJson(list);
    
    if(cli.isFile()) {
      FileUtils.writeStringToFile(new File(cli.getFile()), output, "UTF-8");
    }
    else {
      System.out.println(output);
    }
  }

}
