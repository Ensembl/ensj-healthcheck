/**
 * File: AbstractPerlBasedTestCase.java
 * Created by: dstaines
 * Created on: Nov 13, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase;

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;

/**
 * <p>
 * Base class for invoking a perl script to carry out the test and parse the
 * output.
 * </p>
 * 
 * @author dstaines
 * 
 */
public abstract class AbstractPerlBasedTestCase extends
		AbstractShellBasedTestCase {

	public static final String PERLOPTS = "perlopts";
	public static final String PERL = "perl";

	protected String PERL5LIB = null;

	public String getPERL5LIB() {
		return PERL5LIB;
	}

	public void setPERL5LIB(String pERL5LIB) {
		PERL5LIB = pERL5LIB;
	}

	protected PerlScriptConfig config;

	public PerlScriptConfig getConfig() {
		if (config == null) {
			config = new PerlScriptConfig(System.getProperty(PERL),
					System.getProperty(PERLOPTS));
		}
		return config;
	}

	public void setConfig(PerlScriptConfig config) {
		this.config = config;
	}

	public AbstractPerlBasedTestCase() {
	}

	/**
	 * @return String perl script and relevant arguments to invoke with perl
	 *         binary and options from
	 *         {@link AbstractPerlBasedTestCase#getConfig()}
	 */
	protected abstract String getPerlScript(DatabaseRegistryEntry dbre,
			int speciesId);

	protected String createCommandLine(final DatabaseRegistryEntry dbre,
			int speciesId) {
		String commandLine = getPerlScript(dbre, speciesId);

		if (config != null) {

			if (!config.getPerlBinary().isEmpty()) {

				if (config.getPerlOptions().isEmpty()) {

					commandLine = config.getPerlBinary() + " " + commandLine;

				} else {

					commandLine = config.getPerlBinary() + " "
							+ config.getPerlOptions() + " " + commandLine;
				}
			}
		}
		return commandLine;
	}

	protected String[] environmentVarsToSet() {
		if (StringUtils.isEmpty(getPERL5LIB())) {
			return new String[] {};
		} else {
			return new String[] { "PERL5LIB=" + getPERL5LIB() };
		}
	}
}
