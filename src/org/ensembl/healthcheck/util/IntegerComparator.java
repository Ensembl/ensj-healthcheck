/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
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

/**
 * Implementation of Comparator for comparing Integer objects.
 */

public class IntegerComparator implements Comparator, Serializable {

    /**
     * Implementation of Compare interface. Natural order for o1 and o2.
     * 
     * @param o1
     *          The first Integer to compare.
     * @param o2
     *          The second Integer to compare.
     * @return -1 if o1 <o2, 0 if o1--o2, 1 if o1>o2.
     */
    public int compare(Object o1, Object o2) {

        int i1 = ((Integer) o1).intValue();
        int i2 = ((Integer) o2).intValue();

        int result = 0;
        if (i1 == i2) {
            result = 0;
        } else if (i1 < i2) {
            result = -1;
        } else if (i1 > i2) {
            result = 1;
        }

        return result;

    }

}
