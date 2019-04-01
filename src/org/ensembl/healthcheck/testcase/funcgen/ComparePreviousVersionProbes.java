package org.ensembl.healthcheck.testcase.funcgen;

import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.Team;

public class ComparePreviousVersionProbes extends RegulationComparePreviousVersion {

    public ComparePreviousVersionProbes() {
        setTeamResponsible(Team.FUNCGEN);
        setDescription("Checks for loss of Probes between database versions");
    }
    @Override
    protected Map getCounts(DatabaseRegistryEntry dbre) {
      return getCountsBySQL(dbre, "select array.name, count(distinct probe_id) from array join array_chip using (array_id) join probe using (array_chip_id) group by array.name order by array.name");
    }

    @Override
    protected String entityDescription() {
        return "Probes";
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
    @Override
    protected boolean compareReturnProblem(){
        //return true;
        return false;
    }
}

