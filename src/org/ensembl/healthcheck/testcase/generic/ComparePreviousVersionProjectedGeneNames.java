/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2021] EMBL-European Bioinformatics Institute
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

/*
 * Copyright (C) 2003 EBI, GRL
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.ensembl.healthcheck.testcase.generic;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Compare the gene names in the current database with those from the equivalent database on the secondary server.
 */

public class ComparePreviousVersionProjectedGeneNames extends SingleDatabaseTestCase {

    /**
     * Create a new testcase.
     */
    public ComparePreviousVersionProjectedGeneNames() {

        setDescription("Compare gene names in the current database with those from the equivalent database on the secondary server.");
        setTeamResponsible(Team.CORE);
        setSecondTeamResponsible(Team.RELEASE_COORDINATOR);

    }

    /**
     * This only applies to core and Vega databases.
     */
    public void types() {

        removeAppliesToType(DatabaseType.OTHERFEATURES);
        removeAppliesToType(DatabaseType.ESTGENE);
        removeAppliesToType(DatabaseType.CDNA);
        removeAppliesToType(DatabaseType.RNASEQ);

    }

    // ----------------------------------------------------------------------

    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;
        boolean nochange = true;

        Connection currentCon = dbre.getConnection();

        DatabaseRegistryEntry previous = getEquivalentFromSecondaryServer(dbre);
        if (previous == null) {
            return result;
        }
        Connection previousCon = previous.getConnection();

        // Get data from previous database, compare each one with equivalent on
        // current
        float displayXrefCount = new Integer(DBUtils.getRowCount(currentCon, "SELECT COUNT(1) FROM gene WHERE display_xref_id IS NOT NULL"));
        float displayXrefPreviousCount = new Integer(DBUtils.getRowCount(previousCon, "SELECT COUNT(1) FROM gene WHERE display_xref_id IS NOT NULL"));
        float PreviousCount = new Integer(DBUtils.getRowCount(previousCon, "SELECT COUNT(1) FROM gene"));

        if (displayXrefCount == 0 || displayXrefPreviousCount == 0) {
            ReportManager.problem(this, currentCon, "display xref count is 0 in the current or previous database");
            result = false;
            return result;
        }

        String previousSQL = "SELECT stable_id, db_name, dbprimary_acc FROM gene LEFT JOIN xref ON display_xref_id = xref_id LEFT JOIN external_db USING(external_db_id) WHERE xref.info_type = 'PROJECTION'";
        String currentSQL = "SELECT stable_id, db_name, dbprimary_acc FROM gene LEFT JOIN xref ON display_xref_id = xref_id LEFT JOIN external_db USING(external_db_id) WHERE xref.info_type = 'PROJECTION' AND stable_id = ?";

        int missingIds = 0;
        int accessionsChanged = 0;

        HashMap<String, Integer> changeCounts = new HashMap<String, Integer>();

        HashMap<String, String> exampleStableIds = new HashMap<String, String>();

        try {

            PreparedStatement previousStmt = previousCon.prepareStatement(previousSQL);
            PreparedStatement currentStmt = currentCon.prepareStatement(currentSQL);

            ResultSet previousRS = previousStmt.executeQuery();

            while (previousRS.next()) {

                String stableId = previousRS.getString(1);
                String previousDbName = previousRS.getString(2);
                String previousAccession = previousRS.getString(3);

                currentStmt.setString(1, stableId);
                ResultSet currentRS = currentStmt.executeQuery();

                if (currentRS == null) {
                    missingIds++;
                    currentRS.close();
                    continue;
                }

                if (!currentRS.next()) {
                    missingIds++;
                    currentRS.close();
                    continue;
                }

                String currentDbName = currentRS.getString(2);
                String currentAccession = currentRS.getString(3);

                if (previousDbName == null) {
                    previousDbName = "null";
                }
                if (previousAccession == null) {
                    previousAccession = "null";
                }

                if (currentDbName == null) {
                    currentDbName = "null";
                }
                if (currentAccession == null) {
                    currentAccession = "null";
                }


                if (!currentAccession.equals(previousAccession) && currentDbName.equals(previousDbName)) {
                    //store counts of display xrefs where accession changed - same source
                    accessionsChanged++;
                }
                if (!currentDbName.equals(previousDbName)) {
                    //store counts of display xrefs where source changed

                    String dbNames = previousDbName + " to " + currentDbName;

                    if (changeCounts.containsKey(dbNames)) {
                        int changeCount = changeCounts.get(dbNames);
                        changeCount++;
                        changeCounts.put(dbNames, changeCount);
                        if (changeCount <= 3) {
                            String exampleSt = exampleStableIds.get(dbNames);
                            exampleSt += " " + stableId;
                            exampleStableIds.put(dbNames, exampleSt);
                        }

                    } else {
                        changeCounts.put(dbNames, 1);
                        exampleStableIds.put(dbNames, ", e.g. " + stableId);
                    }
                }

                currentRS.close();

            }
            previousRS.close();

            currentStmt.close();
            previousStmt.close();


        } catch (SQLException e) {

            System.err.println("Error executing SQL");
            e.printStackTrace();

        }
        float changedSource = 0;
        float totalCount = 0;
        float percentageChange = 0;
        if (changeCounts.size() > 0 || accessionsChanged > 0) {
            Iterator<String> iter = changeCounts.keySet().iterator();

            while (iter.hasNext()) {
                String key = iter.next();
                int changeCount = changeCounts.get(key);
                changedSource += changeCount;
            }
            totalCount = changedSource + accessionsChanged;
            percentageChange = totalCount / PreviousCount * 100;
            if (percentageChange > 5) {
                ReportManager.info(this, currentCon, "Overall gene display xrefs have changed by " + percentageChange);
                nochange = false;
            }
        }

        if (!nochange) {
            DecimalFormat twoDForm = new DecimalFormat("#.##");

            float percentage = missingIds / displayXrefPreviousCount * 100;
            percentage = Float.valueOf(twoDForm.format(percentage));

            if (missingIds > 0 && percentage > 10) {
                ReportManager.problem(this, currentCon, missingIds + "(" + percentage + "%) genes lack projected names in the current database ");
                result = false;
            }

            percentage = accessionsChanged / displayXrefPreviousCount * 100;
            percentage = Float.valueOf(twoDForm.format(percentage));

            if (accessionsChanged > 50 && percentage > 10) {
                ReportManager.problem(this, currentCon, accessionsChanged + "(" + percentage + "%) display xref primary accessions changed for the same source ");
                result = false;
            }
            percentageChange = changedSource / displayXrefPreviousCount * 100;
            percentageChange = Float.valueOf(twoDForm.format(percentageChange));

            //print out counts and percentages of changes
            Iterator<String> iter = changeCounts.keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                int changeCount = changeCounts.get(key);
                percentage = changeCount / displayXrefPreviousCount * 100;
                percentage = Float.valueOf(twoDForm.format(percentage));
                if (percentage > 10 && changeCount > 50) {
                    ReportManager.problem(this, currentCon, changeCount + "(" + percentage
                            + "%) gene display xrefs changed source from " + key + exampleStableIds.get(key));
                    result = false;
                } else if (changeCount == 0) {
                    ReportManager.problem(this, currentCon,
                            "Source " + key + " does not appear in the new database any more");
                    result = false;
                }
            }

        }
        return result;

    }
}
// ----------------------------------------------------------------------
