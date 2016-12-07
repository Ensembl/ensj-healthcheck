package org.ensembl.healthcheck.testcase.funcgen;

import java.util.Map;
import org.ensembl.healthcheck.DatabaseRegistryEntry;

public class ComparePreviousVersionGenomicProbeFeaturesByArray extends ComparePreviousVersionProbeFeaturesByArrayBase {

    public ComparePreviousVersionGenomicProbeFeaturesByArray() {
        setDescription("Checks for loss of probes features from genomic mappings for each array.");
    }

    protected Map<String, Integer> getRawProbeFeaturePerArrayCounts(DatabaseRegistryEntry dbre) {
      return getCountsBySQL(dbre, "select array.name, count(distinct probe_feature.probe_feature_id) from array join array_chip using (array_id) join probe using (array_chip_id) join probe_feature using (probe_id) join analysis using (analysis_id) where analysis.logic_name not like \"%transcript%\" group by analysis.logic_name, array.name");
    }
    @Override
    protected String entityDescription() {
        return "probe features from genomic mapping";
    }
}

