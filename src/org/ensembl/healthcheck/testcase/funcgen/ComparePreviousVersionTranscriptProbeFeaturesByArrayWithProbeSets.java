package org.ensembl.healthcheck.testcase.funcgen;

import java.util.Map;
import org.ensembl.healthcheck.DatabaseRegistryEntry;

public class ComparePreviousVersionTranscriptProbeFeaturesByArrayWithProbeSets extends ComparePreviousVersionProbeFeaturesFromProbeSetsByArrayBase {

    public ComparePreviousVersionTranscriptProbeFeaturesByArrayWithProbeSets() {
        setDescription("Checks for loss of probes features from transcript mappings for each array that is organised into probe sets.");
    }

    protected Map<String, Integer> getRawProbeFeaturePerArrayCounts(DatabaseRegistryEntry dbre) {
      return getCountsBySQL(dbre, "select array.name, count(distinct probe_feature.probe_feature_id) from array join array_chip using (array_id) join probe using (array_chip_id) join probe_feature using (probe_id) join analysis using (analysis_id) where analysis.logic_name like \"%transcript%\" group by analysis.logic_name, array.name");
    }
    @Override
    protected String entityDescription() {
        return "probe features (normalised count) from transcript mapping";
    }
}

