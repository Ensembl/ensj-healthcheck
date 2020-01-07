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

package org.ensembl.healthcheck.util;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * 	This class is used to make log messages from a perl module based 
 * healthcheck available in the java part of the healthcheck system.
 * </p>
 * <p>
 * 	The logmapper maps a log message coming from perl to the java logging 
 * system. It relies on the formatting of messages as set in 
 * _initialise_logger in Bio::EnsEMBL::Healthcheck  which 
 * all perl module based healthchecks should inherit from.
 * </p>
 * 
 * @author michael
 *
 */
public class LogMapperPerl2Java {
	
	/**
	 * Pattern that will find the loglevel which will be output by the perl
	 * logger.
	 */
	protected final Pattern logLevelFromMessage;
	
	/**
	 * The logger the convenience method "log" will write to. 
	 */
	protected final Logger  logger;
	
	public LogMapperPerl2Java(Logger logger) {
		
		logLevelFromMessage = Pattern.compile("LOG: (\\w+) ");
		this.logger = logger;
	}
	
	/**
	 * 
	 * <p>
	 * 	Forwards a log message from a perl based healthcheck to the java 
	 * logger.
	 * </p>
	 * 
	 */
	public void log(String message) {
		
		logger.log(
			mapPerlLogToJavaLogLevel(
				parsePerlLogLevelFromMessage(
					message
				)
			), 
			message
		);
	}
	
	public String parsePerlLogLevelFromMessage(String message) {
		
		Matcher m = logLevelFromMessage.matcher(message);
		
		if (m.find()) {
		
			String logLevel = m.group(1);
			return logLevel;
		}
		
		throw new IllegalArgumentException("Can't find loglevel in " + message);
	}
	
	/**
	 * 
	 * <p>
	 * 	Maps the loglevel from perl given as a String and returns the loglevel
	 * in Java that corresponds to that.
	 * </p>
	 * 
	 */
	public Level mapPerlLogToJavaLogLevel(String perlLogLevel) {
		
		if (
			   perlLogLevel.equals("DEBUG")
			|| perlLogLevel.equals("TRACE")
		) { 
			return Level.FINE; 
		}
		if (
			   perlLogLevel.equals("INFO")
			|| perlLogLevel.equals("WARN")
		)  { 
			return Level.INFO; 
		}
		if (
			   perlLogLevel.equals("ERROR")
			|| perlLogLevel.equals("FATAL")
		)  { 
			return Level.SEVERE; 
		}
			
		throw new IllegalArgumentException("Unknown loglevel " + perlLogLevel);
	}
}
