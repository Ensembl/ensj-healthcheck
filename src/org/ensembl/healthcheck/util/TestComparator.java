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

package org.ensembl.healthcheck.util;

import java.io.Serializable;
import java.util.Comparator;

import org.ensembl.healthcheck.testcase.EnsTestCase;

/**
 * Implementation of Comparator for comparing EnsTestCase objects.
 */

public class TestComparator implements Comparator, Serializable {

    /**
     * Implementation of Compare interface. Compares on test name.
     * 
     * @param o1
     *          The first EnsTestCase (as an Object) to compare.
     * @param o2
     *          The first EnsTestCase (as an Object) to compare.
     * @return Result of natural comparison of test name strings.
     */
    public int compare(Object o1, Object o2) {

        String n1 = ((EnsTestCase) o1).getShortTestName();
        String n2 = ((EnsTestCase) o2).getShortTestName();

        return n1.compareTo(n2);

    }

}
