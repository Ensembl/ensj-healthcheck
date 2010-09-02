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
	@Option(shortName="c") 
	List<File> getConf();
	boolean isConf();
	
}