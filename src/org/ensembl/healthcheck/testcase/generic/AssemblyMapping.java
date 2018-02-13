/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2018] EMBL-European Bioinformatics Institute
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

/**
 * AssemblyMapping
 * 
 * @author dstaines
 * @author $Author$
 * @version $Revision$
 */
package org.ensembl.healthcheck.testcase.generic;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;
import org.ensembl.healthcheck.util.SqlTemplate;
import org.ensembl.healthcheck.util.StringListMapRowMapper;

/**
 * Test to ensure assembly.mapping meta value refers to existent coordinate
 * systems
 * 
 * @author dstaines
 * 
 */
public class AssemblyMapping extends AbstractTemplatedTestCase {

    private final static String CS_SQL = "select name,version "
        + "from coord_system where species_id=?";
    private final static String ASS_MAP_SQL = "select meta_value "
        + "from meta where meta_key='assembly.mapping' and species_id=?";
    private final static Pattern ASS_MAP = Pattern.compile("([^:]+)(:(.+))?");

    public AssemblyMapping() {
        super();
        this.appliesToType(DatabaseType.CORE);
        this.setDescription("Test to make sure that the coord_systems and versions "
            + "referred to in assembly.mapping exist");
        setTeamResponsible(Team.GENEBUILD);
    }

        /**
         * Data is only tested in core database, as the tables are in sync
         */
        public void types() {

            removeAppliesToType(DatabaseType.OTHERFEATURES);
            removeAppliesToType(DatabaseType.RNASEQ);
            removeAppliesToType(DatabaseType.CDNA);

        }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase#runTest(org
     * .ensembl.healthcheck.DatabaseRegistryEntry)
     */
    @Override
    protected boolean runTest(DatabaseRegistryEntry dbre) {
        SqlTemplate templ = getSqlTemplate(dbre);
        boolean result = true;
        for (int speciesId : dbre.getSpeciesIds()) {
            // 1. get coord_systems
            Map<String, List<String>> cs = templ.queryForMap(CS_SQL,
                new StringListMapRowMapper(), speciesId);
            // 2. get assembly mapping
            List<String> assMap = templ.queryForDefaultObjectList(ASS_MAP_SQL,
                String.class, speciesId);
            for (String map : assMap) {
                for (String mapElem : map.split("[#|]")) {
                    Matcher m = ASS_MAP.matcher(mapElem);
                    if (m.matches()) {
                        if (m.groupCount() == 3) {
                            String name = m.group(1);
                            String version = m.group(3);
                            List<String> versions = cs.get(name);
                            if (versions == null) {
                                ReportManager.problem(this, dbre.getConnection(),
                                    "No coordinate system named '" + name
                                    + "' found for mapping " + assMap);
                                result = false;
                            } else {
                                if (!versions.contains(version)) {
                                    ReportManager.problem(this,
                                    dbre.getConnection(),
                                        "No coordinate system named '" + name
                                        + "' with version " + version
                                        + " found for mapping " + assMap);
                                    result = false;
                                }
                            }
                        } else {
                            ReportManager.problem(
                                this,
                                dbre.getConnection(),
                                    "Could not parse assembly mapping element " + mapElem
                                    + " from " + assMap
                                    + " does not match the expected pattern "
                                    + ASS_MAP.pattern());
                            result = false;
                        }
                    } else {
                        ReportManager.problem(this, dbre.getConnection(),
                            "Assembly mapping element " + mapElem + " from " + assMap
                            + " does not match the expected pattern "
                            + ASS_MAP.pattern());
                        result = false;
                    }
                }
            }
        }
        return result;
    }

}
