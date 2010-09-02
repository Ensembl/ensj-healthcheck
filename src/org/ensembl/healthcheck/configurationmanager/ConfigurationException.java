package org.ensembl.healthcheck.configurationmanager;

public class ConfigurationException extends RuntimeException {
	
	public ConfigurationException(String msg) {
		super(msg);
	}
	public ConfigurationException(Exception e) {
		super(e);
	}
	public ConfigurationException(String msg, Exception e) {
		super(msg, e);
	}
}
