package org.ensembl.healthcheck.testcase.funcgen;

import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionBase;

public class ComparePreviousVersionRegulatoryBuildEpigenomes extends ComparePreviousVersionBase {

    public ComparePreviousVersionRegulatoryBuildEpigenomes() {
        setTeamResponsible(Team.FUNCGEN);
        setDescription("Checks that epigenomes in the regulatory build aren't lost.");
    }
    @Override
    protected Map getCounts(DatabaseRegistryEntry dbre) {
      return getCountsBySQL(dbre, "select epigenome.name, count(*) from epigenome join regulatory_build_epigenome using (epigenome_id) join regulatory_build using (regulatory_build_id) where regulatory_build.is_current = True group by epigenome.name;");
    }

    @Override
    protected String entityDescription() {
        return "Epigenomes in regulatory build";
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

