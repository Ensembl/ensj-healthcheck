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

	@Option	String getHost();
	boolean isHost();

	@Option	String getPort();
	boolean isPort();
	
	@Option	String getUser();
	boolean isUser();
	
	@Option	String getPassword();
	boolean isPassword();

	@Option	String getDriver();
	boolean isDriver();

	@Option(longName="secondary.host")
	String getSecondaryHost();
	boolean isSecondaryHost();

	@Option(longName="secondary.port")
	String getSecondaryPort();
	boolean isSecondaryPort();
	
	@Option(longName="secondary.user")
	String getSecondaryUser();
	boolean isSecondaryUser();
	
	@Option(longName="secondary.password")
	String getSecondaryPassword();
	boolean isSecondaryPassword();

	@Option(longName="secondary.driver")
	String getSecondaryDriver();
	boolean isSecondaryDriver();
}

