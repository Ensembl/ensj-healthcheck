/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2018] EMBL-European Bioinformatics Institute
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

/**
 * File: AbstractPerlBasedTestCase.java
 * Created by: dstaines
 * Created on: Nov 13, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase;

import java.util.Map;

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
		// set PERL5LIB by default
		String perl5Lib = System.getenv().get("PERL5LIB");
		if (!StringUtils.isEmpty(perl5Lib)) {
			setPERL5LIB(perl5Lib);
		}
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
		
		if (getConfig() != null) {

			if (!StringUtils.isEmpty(getConfig().getPerlBinary())) {

				if (StringUtils.isEmpty(getConfig().getPerlOptions())) {

					commandLine = config.getPerlBinary() + " " + commandLine;

				} else {

					commandLine = config.getPerlBinary() + " "
							+ config.getPerlOptions() + " " + commandLine;
				}
			}
		}
		return commandLine;
	}

	protected Map<String,String> environmentVarsToSet() {
		
		Map<String,String> inheritedEnvironment = super.environmentVarsToSet();
		
		if(!StringUtils.isEmpty(getPERL5LIB())) {
			inheritedEnvironment.put("PERL5LIB", getPERL5LIB());
		}

		return inheritedEnvironment;
		
	}
	
}
