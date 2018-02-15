package org.ensembl.healthcheck.testcase.funcgen;

import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.Team;


public class ComparePreviousVersionProbeFeaturesByArray extends RegulationComparePreviousVersion {

    public ComparePreviousVersionProbeFeaturesByArray() {
        setTeamResponsible(Team.FUNCGEN);
        setDescription("Checks for loss of probes features for each array.");
    }
    @Override
    protected Map getCounts(DatabaseRegistryEntry dbre) {
      return getCountsBySQL(dbre, "select array.name, count(distinct probe_feature.probe_feature_id) from array join array_chip using (array_id) join probe using (array_chip_id) join probe_feature using (probe_id) group by array.name");
    }

    @Override
    protected String entityDescription() {
        return "probe features";
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

