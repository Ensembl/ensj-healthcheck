/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2020] EMBL-European Bioinformatics Institute
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


/**
 * <p>
 * Title: TestReadingClasspath.java
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Created on March 18, 2004, 9:53 AM
 * </p>
 * 
 * @author Glenn Proctor <glenn@ebi.ac.uk>
 * @version $Revision$
 */

package org.ensembl.healthcheck.test;

import org.ensembl.healthcheck.util.Utils;

/**
 * Test searching classpaths.
 */
public final class TestReadingClasspath {

    private TestReadingClasspath() { }
    
    /**
     * Command-line entrypoint.
     * @param args Command line arguments.
     */
    public static void main(final String[] args) {

        String[] allPaths = Utils.splitClassPath(System.getProperty("java.class.path"), ":");
        System.out.println("\nSystem classpath: ");
        System.out.println(System.getProperty("java.class.path"));

        String[] localPaths = Utils.grepPaths(allPaths, "healthcheck");

        System.out.println("\nLocal paths:");
        Utils.printArray(localPaths);

        String[] jarFiles = Utils.grepPaths(localPaths, ".jar");
        System.out.println("\nJar files and contents:");
        if (jarFiles.length > 0) {
            for (int i = 0; i < jarFiles.length; i++) {
                System.out.println(jarFiles[i]);
                Utils.printJarFileContents(jarFiles[i]);
            }
        } else {
            System.out.println("No jar files found in classpath.");
        }

        System.out.println("\n");

    } // main

    // -------------------------------------------------------------------------

} // TestReadingClasspath
