package org.ensembl.healthcheck.configuration;

import java.io.File;
import java.util.List;
import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * 
 * Interface for the -c or --conf parameter with which properties files
 * are specified.
 * 
 * @author michael
 *
 */
public interface ConfigureConfiguration {

	// A list of names of configuration files
	@Option(shortName="c", description = "Name of one or many configuration "
		+ "files. Parameters in configuration files override each other. If a"
		+ " parameter is provided in more than one file, the first occurrence "
		+ " is used."
	) 
	List<File> getConf();
	boolean isConf();
	
}