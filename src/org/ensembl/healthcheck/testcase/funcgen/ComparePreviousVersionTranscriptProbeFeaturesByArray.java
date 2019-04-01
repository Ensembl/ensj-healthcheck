package org.ensembl.healthcheck.testcase.funcgen;

import java.util.Map;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.Team;

public class ComparePreviousVersionTranscriptProbeFeaturesByArray extends RegulationComparePreviousVersion {

    public ComparePreviousVersionTranscriptProbeFeaturesByArray() {
        setTeamResponsible(Team.FUNCGEN);
        setDescription("Checks for loss of probes features from transcript mappings for each array that is not organised into probe sets.");
    }

    protected Map<String, Integer> getCounts(DatabaseRegistryEntry dbre) {
      return getCountsBySQL(dbre, "select array.name, count(distinct probe_feature.probe_feature_id) from array join array_chip using (array_id) join probe using (array_chip_id) join probe_feature using (probe_id) join analysis using (analysis_id) where analysis.logic_name like \"%transcript%\" group by analysis.logic_name, array.name");
    }
    @Override
    protected String entityDescription() {
        return "probe features from transcript mapping";
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

