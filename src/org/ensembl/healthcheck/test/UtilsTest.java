package org.ensembl.healthcheck.test;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ensembl.healthcheck.util.Utils;

/**
 * @version $Revision$
 * @author glenn
 */
public class UtilsTest extends TestCase {

    public UtilsTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(UtilsTest.class);

        return suite;
    }

    // -----------------------------------------------------------------

    /**
     * Test of readPropertiesFile method, of class
     * org.ensembl.healthcheck.util.Utils.
     */
    public void testReadPropertiesFile() {

        assertNotNull(Utils.readSimplePropertiesFile("database.properties"));

    }

    // -----------------------------------------------------------------

    public void testgetSubDirs() {

        String startDir = System.getProperty("user.dir");
        System.out.println("Checking subdirectories of " + startDir);
        String[] subdirs = Utils.getSubDirs(startDir);
        assertNotNull(subdirs);
        assertTrue("Fewer than expected subdirectories", subdirs.length > 3);
        for (int i = 0; i < subdirs.length; i++) {
            assertNotNull(subdirs[i]);
            File f = new File(subdirs[i]);
            assertTrue("Got a file where we should have only got directories", f.isDirectory());
            System.out.println("\t" + subdirs[i]);
        }

        subdirs = Utils.getSubDirs("/some/madeup/dir");
        assertNotNull(subdirs);
        assertTrue(subdirs.length == 0);

    }

    // -----------------------------------------------------------------

    public void testFilterArray() {

        String[] source = {"a", "b", "c", "d", "e", "f"};
        String[] remove = {"c", "e"};
        Object[] filtered = Utils.filterArray(source, remove);
        assertEquals(filtered.length, 4);

    }

    // -----------------------------------------------------------------

}
