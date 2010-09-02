package org.ensembl.healthcheck.configuration;

import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * Interface for specifying the connection details and database name to
 * which the results of healthchecks will be written, if the reporterType
 * is "database".
 * 
 * @author michael
 *
 */
public interface ConfigureHealthcheckDatabase {

		// The database where the results of healthchecks are written to, if
		// the ReporterType is set to database
		//
		@Option(longName="output.database")
		String getOutputDatabase();
		boolean isOutputDatabase();

		@Option(longName="output.host")
		String getOutputHost();
		boolean isOutputHost();
		
		@Option(longName="output.port")
		String getOutputPort();
		boolean isOutputPort();
		
		@Option(longName="output.user")
		String getOutputUser();
		boolean isOutputUser();
		
		@Option(longName="output.password")
		String getOutputPassword();
		boolean isOutputPassword();
		
		@Option(longName="output.driver")
		String getOutputDriver();
		boolean isOutputDriver();
		
		@Option(longName="output.release")
		String getOutputRelease();
		boolean isOutputRelease();	

		@Option(longName="output.schemafile")
		String getOutputSchemafile();
		boolean isOutputSchemafile();	
}
