package org.ensembl.healthcheck.testcase;

/**
 * 
 * Internal class that holds perl configurations
 *
 */
public class PerlScriptConfig {

	private final String perlBinary;
	private final String perlOptions;

	public PerlScriptConfig(String perlBinary, String perlOptions) {
		this.perlBinary = perlBinary;
		this.perlOptions = perlOptions;
	}

	public String getPerlBinary() {
		return perlBinary;
	}

	public String getPerlOptions() {
		return perlOptions;
	}
};
