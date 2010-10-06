package org.ensembl.healthcheck.configuration;

import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * 
 * Interface for specifying the parameters for the primary and secondary
 * database connection details.
 * 
 * @author michael
 *
 */
public interface ConfigureHost {

	@Option	(description = "The host for the database server you wish to connect to.")
	String getHost();
	boolean isHost();

	@Option	(description = "The port for the database server you wish to connect to.")	
	String getPort();
	boolean isPort();
	
	@Option	(description = "The user for the database server you wish to connect to.")	
	String getUser();
	boolean isUser();
	
	@Option	(description = "The password (if required) for the database server you wish to connect to.")	
	String getPassword();
	boolean isPassword();

	@Option	(description = "")
	String getDriver();
	boolean isDriver();

	@Option	(description = "")
	String getHost2();
	boolean isHost2();

	@Option	(description = "")	
	String getPort2();
	boolean isPort2();
	
	@Option	(description = "")	
	String getUser2();
	boolean isUser2();
	
	@Option	(description = "")	
	String getPassword2();
	boolean isPassword2();

	@Option	(description = "")
	String getDriver2();
	boolean isDriver2();

	@Option(
			longName    = "secondary.host",
			description = "Some tests require a second database. This "
				+ "configures the hostname of the second database server."
	)
	String getSecondaryHost();
	boolean isSecondaryHost();

	@Option(
			longName    = "secondary.port",
			description = "Some tests require a second database. This "
				+ "configures the port of the second database server."
	)
	String getSecondaryPort();
	boolean isSecondaryPort();
	
	@Option(
			longName    = "secondary.user",
			description = "Some tests require a second database. This "
				+ "configures the user name for the second database server."
	)
	String getSecondaryUser();
	boolean isSecondaryUser();
	
	@Option(
			longName    = "secondary.password",
			description = "Some tests require a second database. This "
				+ "configures the password for the second database server."
	)
	String getSecondaryPassword();
	boolean isSecondaryPassword();

	@Option(
			longName="secondary.driver",
			description = "Some tests require a second database. This "
				+ "configures the driver for the second database server."
	)
	String getSecondaryDriver();
	boolean isSecondaryDriver();
}

