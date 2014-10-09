/*
 * Copyright [1999-2014] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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


import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;

import org.ensembl.healthcheck.configuration.ConfigureHealthcheckDatabase;
import org.ensembl.healthcheck.configuration.ConfigurationUserParameters;
import org.ensembl.healthcheck.configuration.ConfigureConfiguration;
import org.ensembl.healthcheck.configuration.ConfigureHost;
import org.ensembl.healthcheck.configurationmanager.ConfigurationFactory.ConfigurationType;
import org.ensembl.healthcheck.configurationmanager.ConfigurationFactory;
import org.ensembl.healthcheck.configurationmanager.ConfigurationException;
import org.ensembl.healthcheck.configurationmanager.ConfigurationDumper;
import org.ensembl.healthcheck.configurationmanager.ConfigurationDumper;
import org.ensembl.healthcheck.util.CreateHealthCheckDB;
import org.ensembl.healthcheck.util.DBUtils;


/**
 * 
 * Runs test which can be configured on the command line and stores them in a
 * database.
 * 
 */
public class ParallelConfigurableTestRunner extends TestRunner {


        /**
         * The configuration object from which configuration information is
         * retrieved.
         */

        protected final static String DEFAULT_PROPERTIES_FILE = "database.release.defaults.properties";
        protected final ConfigurationUserParameters configuration;

        private static final String MEMORY_RUSAGE = "select[mem>2000] rusage[mem=2000]";
        private static final String MEMORY_RESERVATION = "2000";

        static final Logger log = Logger.getLogger(ParallelConfigurableTestRunner.class.getCanonicalName());
        protected final SystemPropertySetter systemPropertySetter;

        /**
         * @param configuration
         *            - A configuration object of type ConfigurationUserParameters
         *
         *            Creates a ConfigurableTestRunner using the parameters from the
         *            configuration object.
         *
         */

        public ParallelConfigurableTestRunner(ConfigurationUserParameters configuration) {

                log.config("Using classpath: \n\n" + Debug.classpathToString());

                this.configuration = configuration;
                this.systemPropertySetter = new SystemPropertySetter(configuration);

                DBUtils.setHostConfiguration((ConfigureHost) configuration);

        }

        /**
         * @param args
         *            - The command line arguments
         *
         *            Creates a ConfigurableTestRunner using command line arguments.
         *            The configuration parameters are taken from the command line
         *            and from the properties files that can be specifies on the
         *            command line with the --conf option. The
         *            DEFAULT_PROPERTIES_FILE is added as well.
         *
         */

        public ParallelConfigurableTestRunner(String[] args) {

                this(createConfigurationObj(args));

        }


        public static void main(String[] args) {

                try {

                        ParallelConfigurableTestRunner parallelConfigurableTestRunner = new ParallelConfigurableTestRunner(
                                        args);
                        parallelConfigurableTestRunner.run();

                } catch (ConfigurationException e) {

                        ParallelConfigurableTestRunner.logger.log(Level.INFO, e.getMessage());
                }
        }

        protected void run() {

                CreateHealthCheckDB c = new CreateHealthCheckDB((ConfigureHealthcheckDatabase) configuration);
                boolean reportDatabaseExistsAlready = c.databaseExists(configuration.getOutputDatabase());

                // Create the database to which tests will be written

                if (reportDatabaseExistsAlready) {
                        logger.info("Reporting database "
                               + configuration.getOutputDatabase()
                               + " already exists, will reuse.");
                        System.out.println("Reporting database "
                               + configuration.getOutputDatabase()
                               + " already exists, will reuse.");
                } else {
                        logger.info("Reporting database "
                               + configuration.getOutputDatabase()
                               + " does not exist, will create.");
                        System.out.println("Reporting database "
                               + configuration.getOutputDatabase()
                               + " does not exist, will create.");
                        c.run();
                }

                systemPropertySetter.setPropertiesForReportManager_connectToOutputDatabase();
                ReportManager.connectToOutputDatabase();

                systemPropertySetter.setPropertiesForReportManager_createDatabaseSession();
                ReportManager.createDatabaseSession();
                systemPropertySetter.setPropertiesForHealthchecks();

                submitJobs();

        }

        /**
         * Submit new job for each test_database entry.
         *
         */
        protected void submitJobs() {

                int numberOfTestsRun = 0;

                int jobNumber = 0;
                List<String> jobNames = new ArrayList<String>();
                String dir = System.getProperty("user.dir");
                String runConfigurable = dir + File.separator + "run-configurable-testrunner.sh";
                long sessionID = ReportManager.getSessionID();

                for (String database : configuration.getTestDatabases()) {

                        String currentJobName = "Job_" + jobNumber;

                        String[] cmd = {"bsub", "-q", "long", "-J", currentJobName, "-R", MEMORY_RUSAGE, "-M", MEMORY_RESERVATION, "-R", "select[myens_staging1<=800]", "-R", "select[myens_staging2<=800]", "-R", "select[myens_livemirror<=400]",
                        "-R", "rusage[myens_staging1=10:myens_staging1=10:myens_livemirror=50]", "-o", "healthcheck_%J.out", "-e", "healthcheck_%J.err", runConfigurable, "-d", database, "--sessionID", "" + sessionID };

                        jobNames.add(currentJobName);

                        execCmd(cmd);
                        System.out.println("Submitted job with database regexp " + database);
                        jobNumber++;

                 }
                 System.out.println("Submitted session dependency job");

        } // submitJobs

        /**
         * Used for creating layered constructors.
         */
        protected static ConfigurationUserParameters createConfigurationObj(
                        String[] args) {

                // A temporary configuration object for accessing the command line
                // parameters in which the user configures where the configuration
                // files are located. Since only this information is of interest at
                // this point the configuration object is subcast to the
                // ConfigureConfiguration interface.
                //
                ConfigureConfiguration conf = new ConfigurationFactory<ConfigurationUserParameters>(
                                ConfigurationUserParameters.class, (Object[]) args)
                                .getConfiguration(ConfigurationType.Commandline);

                // Get the list of property files the user specified and add the
                // default property file to it.
                //
                List<File> propertyFileNames = new ArrayList<File>();

                // The user is not required to provide a property file. In this case
                // only the
                // command line arguments and the default properties file will be used.
                //
                if (conf.isConf()) {
                        propertyFileNames.addAll(conf.getConf());
                }
                propertyFileNames.add(new File(DEFAULT_PROPERTIES_FILE));

                // Use this to create the final configuration object.
                ConfigurationFactory<ConfigurationUserParameters> confFact = new ConfigurationFactory(
                                ConfigurationUserParameters.class, args, propertyFileNames);

                // Users may want to know what went into the the configuration of this,
                // So some information on where the configuration information came
                // from is compiled here and sent to the logger.
                //
                StringBuffer msg = new StringBuffer();

                //
                // Create a few logging messages about the configuration information
                // that that will be used. Useful for debugging.
                //

                // Information about the command line arguments and which property files
                // are used
                msg.append("Creating configuration for this run.\n\n");
                msg.append("The following arguments were specified on the command line:\n");

                for (String arg : args) {
                        if (arg.startsWith("-")) {
                                msg.append("\n");
                        }
                        msg.append(" " + arg);
                }

                msg.append("\n\nThe following property files will be used:\n\n");

                for (File propertyFileName : propertyFileNames) {
                        msg.append("  - " + propertyFileName.getName() + "\n");
                }

                log.config(msg.toString());

                // Finally create the configuration object.
                ConfigurationUserParameters configuration = confFact
                                .getConfiguration(ConfigurationType.Cascading);

                // Show user the final configuration settings that will be used.
                log.config("The following settings will be used:\n\n"
                                + new ConfigurationDumper<ConfigurationUserParameters>()
                                                .dump(configuration));

                return configuration;
        }

        /**
         * Used for executing bsub commands.
         *
         * @param cmd
         *
         */
        protected void execCmd(String[] cmd) {

                try {

                        Process p = Runtime.getRuntime().exec(cmd);

                        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                        String s = null;

                        while ((s = stdInput.readLine()) != null) {
                                System.out.println(s);
                        }

                        while ((s = stdError.readLine()) != null) {
                                System.out.println(s);
                        }

                        stdInput.close();
                        stdError.close();

                } catch (Exception ioe) {
                        System.err.println("Error in head job " + ioe.getMessage());
                }
        }

}


