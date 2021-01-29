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

package org.ensembl.healthcheck.test;

import java.util.ArrayList;
import java.util.List;

import org.ensembl.healthcheck.util.SQLParser;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @author glenn
 */
public class SQLParserTest  {

    SQLParser parser;


    @BeforeTest
    public void setUp() {
        parser = new SQLParser();
    }

//    /** Test of parse method, of class org.ensembl.healthcheck.util.SQLParser. */
//    public void testParse() {
//        try {
//            parser.parse("/homes/glenn/work/ensj-healthcheck/table.sql");
//        } catch (java.io.FileNotFoundException fnfe) {
//            fail("Cannot find file");
//        }
//        assertNotNull(parser.getLines());
//
//    }

//    /**
//     * Test of populateBatch method, of class
//     * org.ensembl.healthcheck.util.SQLParser.
//     */
//    public void testPopulateBatch() {
//        System.out.println("testPopulateBatch");
//
//        // Add your test code below by replacing the default call to fail.
//        fail("The test case is empty.");
//    }

//    /**
//     * Test of getLines method, of class
//     * org.ensembl.healthcheck.util.SQLParser.
//     */
//    public void testGetLines() {
//        System.out.println("testGetLines");
//        assertNotNull(parser.getLines());
//    }

    /**
     * Test of setLines method, of class
     * org.ensembl.healthcheck.util.SQLParser.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSetLines() {
        System.out.println("testSetLines");
        ArrayList<String> inList = new ArrayList<String>();
        inList.add("abcdef");
        parser.setLines(inList);
        List<String> outList = (List<String>)parser.getLines();
        Assert.assertNotNull(outList);
        Assert.assertEquals(1, outList.size());
        String line = (String) outList.get(0);
        Assert.assertEquals("abcdef", line);

    }

}
