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

package org.ensembl.healthcheck.autogroups;

import java.util.List;
import java.util.logging.Logger;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.ensembl.healthcheck.TestsInGroups;

/**
 * Class for generating java sources for classes defining testgroups using
 * StringTemplate as a template engine.
 *
 */
public class Generator {

	static final Logger log = Logger.getLogger(Generator.class.getCanonicalName());
	
	private final String templateGroup;
	private final String templateDir;
	private final String templateName;
	
	/**
	 * 
	 * Standard constructor with reasonable defaults for the tasks at hand.
	 * 
	 */
	public Generator() {
		
		templateGroup = "myGroup";
		templateDir   = "resources/devel/";
		templateName  = "testgroup";
	}

	/**
	 * @param templateGroup The group as in StringTemplate, not used as far
	 *     as I know. 
	 * @param templateDir   The directory in which the templates are.
	 * @param templateName The name of the template used for creating the
	 *     classes.
	 */
	public Generator(
			String templateGroup,
			String templateDir,
			String templateName
		) {
		
		this.templateGroup = templateGroup;
		this.templateDir   = templateDir;
		this.templateName  = templateName;
	}

	/**
	 * Static helper method to help users of the generate method in this class
	 * to create a valid class name.
	 * 
	 * @param originalName The original name that this class would have been 
	 *     given, but may contain illegal characters.
	 *      
	 * @return: A String that looks like originalName, but can be used as a
	 *     class name.  
	 * 
	 */
	public static String toJavaClassName(String originalName) {
		
		String[] parts = originalName.split("[-|_]");
		
		StringBuffer result = new StringBuffer();
		for (String part : parts) {
			
			String firstLetter  = part.substring(0, 1);
			String restOfString = part.substring(1, part.length());
			
//			log.warning(
//				  "firstLetter: "  + firstLetter
//				+ " restOfString: " + restOfString
//			);
			
			result.append(firstLetter.toUpperCase() + restOfString);
		}
		return result.toString();
	}
	
	/**
	 * @param className
	 * @param packageName
	 * @param groupName
	 * @param enstestcaseNames
	 * @return string for parameters
	 */
	public String generate(
		String className,
		String packageName,
		String groupName,
		List<String> enstestcaseNames
	) {
		
		StringTemplateGroup group =  new StringTemplateGroup(
			templateGroup, 
			templateDir, 
			DefaultTemplateLexer.class
		);
		StringTemplate testGroupTemplate = group.getInstanceOf(templateName);

		testGroupTemplate.setAttribute("className",   className);
		testGroupTemplate.setAttribute("packageName", packageName);
		testGroupTemplate.setAttribute("groupName",   groupName);
		testGroupTemplate.setAttribute("testList",    enstestcaseNames);

		return testGroupTemplate.toString();
	}

}
