/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
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
      info.put("responible", (t.getTeamResponsible() != null ? t.getTeamResponsible().name() : null));
      info.put("secondResponsible", (t.getSecondTeamResponsible() != null ? t.getSecondTeamResponsible().name() : null));
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
