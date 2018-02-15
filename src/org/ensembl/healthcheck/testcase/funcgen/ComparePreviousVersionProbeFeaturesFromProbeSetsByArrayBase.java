package org.ensembl.healthcheck.testcase.funcgen;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.Team;


abstract public class ComparePreviousVersionProbeFeaturesFromProbeSetsByArrayBase extends RegulationComparePreviousVersion{
  
    public ComparePreviousVersionProbeFeaturesFromProbeSetsByArrayBase() {
        setTeamResponsible(Team.FUNCGEN);
    }

    protected Map getNormalisedProbePerArrayCounts(DatabaseRegistryEntry dbre) {
      
      Map<String, Integer> arrayNameToAverageProbeSetSize = getAverageProbeSetSizePerArrayCounts(dbre);
      Map<String, Integer> rawProbeFeaturePerArrayCounts  = getRawProbeFeaturePerArrayCounts(dbre);
      
      // Normalise the counts for arrays with probe sets
      // Arrays that don't have probe sets will be skipped.
      Map<String, Integer> normalisedResult = new HashMap<String, Integer>();
      Iterator<String> iterator = rawProbeFeaturePerArrayCounts.keySet().iterator();
      
      while (iterator.hasNext()) {
        String currentArray = iterator.next();
        int normalisedProbeFeatureCount;
        
        if (arrayNameToAverageProbeSetSize.containsKey(currentArray)) {
          normalisedProbeFeatureCount = rawProbeFeaturePerArrayCounts.get(currentArray) / arrayNameToAverageProbeSetSize.get(currentArray);
          normalisedResult.put(currentArray, normalisedProbeFeatureCount);
        }
        
      }
      return normalisedResult;
    }
    @Override
    protected Map getCounts(DatabaseRegistryEntry dbre) {
      return getNormalisedProbePerArrayCounts(dbre);
    }
    protected Map<String, Integer> getRawProbeFeaturePerArrayCounts(DatabaseRegistryEntry dbre) {
      String sql = "select array.name, count(distinct probe_feature.probe_feature_id) from array join array_chip using (array_id) join probe using (array_chip_id) join probe_feature using (probe_id) join analysis using (analysis_id) where analysis.logic_name like \"%transcript%\" group by analysis.logic_name, array.name";
      return getCountsBySQL(dbre, sql);
    }

    protected Map<String, Integer> getAverageProbeSetSizePerArrayCounts(DatabaseRegistryEntry dbre) {
      
      // This creates a map from all arrays that are organised into probe 
      // sets to the average number of probes per probe sets. 
      //
      // This will be used to normalise the total counts.
      //
      // Affymetrix uses probe sets, but calls them probes.
      //
      // So what is called a "probe set" in Ensembl is actually a probe for 
      // Affymetrix. In order to make the numbers reported by this test
      // comparable to the manufacturer's data sheet, the numbers reported are
      // divided by the number of probes per probe set for arrays that are
      // organised into probe sets.
      //
      String sql = 
            "select "
          + "    array.name, "
          + "    count(distinct probe_id)/count(distinct probe_set_id) "
          + "from "
          + "    probe_set join probe using (probe_set_id) join array_chip on (array_chip.array_chip_id = probe.array_chip_id) join array using (array_id) "
          + "group by "
          + "    array.name, array.vendor";
      Map<String, Integer> arrayNameToAverageProbeSetSize = getCountsBySQL(dbre, sql);
      return arrayNameToAverageProbeSetSize;
    }

    @Override
    protected double threshold() {
        return 1;
    }
    @Override
    protected boolean testUpperThreshold(){
        return true;
    }

    @Override
    protected double minimum() {
      return 0;
    }

}
