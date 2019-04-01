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

package org.ensembl.healthcheck.test;

import java.io.File;

import org.ensembl.healthcheck.util.Utils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @version $Revision$
 * @author glenn
 */
public class UtilsTest {

    // -----------------------------------------------------------------

    /**
     * Test of readPropertiesFile method, of class
     * org.ensembl.healthcheck.util.Utils.
     */
    @Test
    public void testReadPropertiesFile() {

        Assert.assertNotNull(Utils.readSimplePropertiesFile("database.defaults.properties"));

    }

    // -----------------------------------------------------------------
    @Test
    public void testgetSubDirs() {

        String startDir = System.getProperty("user.dir");
        System.out.println("Checking subdirectories of " + startDir);
        String[] subdirs = Utils.getSubDirs(startDir);
        Assert.assertNotNull(subdirs);
        Assert.assertTrue(subdirs.length > 3, "Fewer than expected subdirectories");
        for (int i = 0; i < subdirs.length; i++) {
            Assert.assertNotNull(subdirs[i]);
            File f = new File(subdirs[i]);
            Assert.assertTrue(f.isDirectory(), "Got a file where we should have only got directories");
            System.out.println("\t" + subdirs[i]);
        }

        subdirs = Utils.getSubDirs("/some/madeup/dir");
        Assert.assertNotNull(subdirs);
        Assert.assertEquals(0, subdirs.length);

    }

    // -----------------------------------------------------------------
    @Test
    public void testFilterArray() {

        String[] source = {"a", "b", "c", "d", "e", "f"};
        String[] remove = {"c", "e"};
        Object[] filtered = Utils.filterArray(source, remove);
        Assert.assertEquals(filtered.length, 4);

    }

    // -----------------------------------------------------------------

}
