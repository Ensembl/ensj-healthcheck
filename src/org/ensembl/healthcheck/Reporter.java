/*
 Copyright (C) 2004 EBI, GRL
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.ensembl.healthcheck;

import org.ensembl.healthcheck.testcase.EnsTestCase;

/**
 * Interface to be implemented by any class that provides reports on the status of a test case.
 */
public interface Reporter {

    /**
     * Called when a message needs to be stored.
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
