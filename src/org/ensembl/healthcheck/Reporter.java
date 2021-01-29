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


package org.ensembl.healthcheck;

import org.ensembl.healthcheck.testcase.EnsTestCase;

/**
 * Interface to be implemented by any class that provides reports on the status of a test case.
 */
public interface Reporter {

    /**
     * Called by the ReportManager when a message needs to be stored.
     * 
     * @param reportLine
     *          The report to store.
     */
    void message(ReportLine reportLine);

    /**
     * Should be called just before a test case is run.
     * 
     * @param testCase
     *          The test case about to be run.
     * @param dbre
     *          The database which testCase is to be run on, or null of no/several databases.
     */
    void startTestCase(EnsTestCase testCase, DatabaseRegistryEntry dbre);

    /**
     * Should be called just after a test case has been run.
     * 
     * @param testCase
     *          The test case that was run.
     * @param result
     *          The result of testCase.
     * @param dbre
     *          The database which testCase was run on, or null of no/several databases.
     */
    void finishTestCase(EnsTestCase testCase, boolean result, DatabaseRegistryEntry dbre);

}
