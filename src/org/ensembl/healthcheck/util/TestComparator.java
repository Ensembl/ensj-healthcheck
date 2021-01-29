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


package org.ensembl.healthcheck.util;

import java.io.Serializable;
import java.util.Comparator;

import org.ensembl.healthcheck.testcase.EnsTestCase;

/**
 * Implementation of Comparator for comparing EnsTestCase objects.
 */

public class TestComparator implements Comparator<EnsTestCase>, Serializable {

  private static final long serialVersionUID = 1L;

    /**
     * Implementation of Compare interface. Compares on test name.
     * 
     * @param o1
     *          The first EnsTestCase (as an Object) to compare.
     * @param o2
     *          The first EnsTestCase (as an Object) to compare.
     * @return Result of natural comparison of test name strings.
     */
    public int compare(EnsTestCase o1, EnsTestCase o2) {
        return o1.getShortTestName().compareTo(o2.getShortTestName());
    }

}
