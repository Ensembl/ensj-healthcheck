package org.ensembl.healthcheck.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ensembl.healthcheck.util.SQLParser;

/**
 * @author glenn
 */
public class SQLParserTest extends TestCase {

    SQLParser parser;

    public SQLParserTest(java.lang.String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(SQLParserTest.class);
        return suite;
    }

    public void setUp() {
        parser = new SQLParser();
    }

    /** Test of parse method, of class org.ensembl.healthcheck.util.SQLParser. */
    public void testParse() {
        System.out.println("testParse");

        try {
            parser.parse("/homes/glenn/work/ensj-healthcheck/table.sql");
        } catch (java.io.FileNotFoundException fnfe) {
            fail("Cannot find file");
        }
        assertNotNull(parser.getLines());

    }

    /**
     * Test of populateBatch method, of class
     * org.ensembl.healthcheck.util.SQLParser.
     */
    public void testPopulateBatch() {
        System.out.println("testPopulateBatch");

        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }

    /**
     * Test of getLines method, of class
     * org.ensembl.healthcheck.util.SQLParser.
     */
    public void testGetLines() {
        System.out.println("testGetLines");
        assertNotNull(parser.getLines());
    }

    /**
     * Test of setLines method, of class
     * org.ensembl.healthcheck.util.SQLParser.
     */
    public void testSetLines() {
        System.out.println("testSetLines");
        ArrayList inList = new ArrayList();
        inList.add("abcdef");
        parser.setLines(inList);
        List outList = parser.getLines();
        assertNotNull(outList);
        assertEquals(1, outList.size());
        String line = (String) outList.get(0);
        assertEquals("abcdef", line);

    }

    // Add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}

}
