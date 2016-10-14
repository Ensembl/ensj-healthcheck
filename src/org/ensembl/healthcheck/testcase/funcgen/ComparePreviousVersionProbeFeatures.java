package org.ensembl.healthcheck.testcase.funcgen;

import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionBase;

public class ComparePreviousVersionProbeFeatures extends ComparePreviousVersionBase {

    public ComparePreviousVersionProbeFeatures() {
        setTeamResponsible(Team.FUNCGEN);
        setDescription("Checks for loss of probes between database versions");
    }
    @Override
    protected Map getCounts(DatabaseRegistryEntry dbre) {
      return getCountsBySQL(dbre, "select 'all_probe_features', count(*) from probe_feature");
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

