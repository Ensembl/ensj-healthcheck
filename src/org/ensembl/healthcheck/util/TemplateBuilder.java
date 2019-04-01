/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple naive implementation of a templating builder class which attempts
 * to be a {@link java.text.MessageFormat} with named parameters. Each parameter
 * is evaluated and replaced with the String representation of the value. e.g.
 *
 * <h5>Template</h5>
 * <code>
 * <pre>
 * Hello $world$
 * </pre>
 * </code>
 *
 * <h5>Java Code</h5>
 * <code>
 * <pre>
 * TemplateBuilder builder = new TemplateBuilder(&quot;Hello $world$&quot;);
 * builder.addPlaceHolder(&quot;world&quot;, &quot;there&quot;);
 * String generatedString = builder.generate();
 * System.out.println(generatedString); //// Prints &quot;Hello there&quot;
 * </pre>
 * </code>
 *
 * <p>
 * The default implementation always uses $ to delimit placeholder positions
 * but there is nothing to prevent you from using another value such as &#35;
 * or &quot;.
 * 
 * <p>
 * The code also allows you to setup the template once, bind placeholders 
 * generate the String and then call {@link #clearPlaceholders()} to
 * start binding a new set of parameters.
 *
 * <p>
 * If this implementation does not work then perhaps you need something with
 * a bit more welly like Velocity or StringTemplate. Both of these solutions
 * allow you to define macros & more advanced filtering/manipulation techniques
 * as part of your template rather than having to do the manipulation in Java
 * code.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class TemplateBuilder {

	private static final String DEFAULT_DELIM = "$";

	private final String delim;
	private final String template;
	private final Map<String, Object> placeHolders;

	/**
	 * Used when you want to generate a template quickly but do not
	 * want to go through the hassle of creating one of these objects
	 * and setting the holders. You can use it as:
	 *
	 * <code>
	 * String message = TemplateBuilder.template("hello $greeting$ $name$",
	 *   "greeting", "there", "name", "Andy");
	 * </code>
	 *
	 * @param template The template you wish to use. $ flank the placeholder
	 * @param placeHolders The placeholder array. Look at {@link #addPlaceHolders(Object[])}
	 * for more info on the format
	 * @return The final generated String
	 */
	public static String template(String template, Object... placeHolders) {
		TemplateBuilder builder = new TemplateBuilder(template);
		builder.addPlaceHolders(placeHolders);
		return builder.generate();
	}

	public TemplateBuilder(final String template) {
		this(DEFAULT_DELIM, template);
	}

	public TemplateBuilder(final String template, final Map<String,Object> placeHolders) {
		this(DEFAULT_DELIM, template, placeHolders);
	}

	public TemplateBuilder(final String delim, final String template) {
		this(delim, template, new LinkedHashMap<String, Object>());
	}

	public TemplateBuilder(final String delim, final String template, final Map<String,Object> placeHolders) {
		this.delim = delim;
		this.template = template;
		this.placeHolders = placeHolders;
	}

	public void addPlaceHolder(String key, Object value) {
		placeHolders.put(key, value);
	}

	public void addPlaceHolders(Map<String,Object> placeHolders) {
		this.placeHolders.putAll(placeHolders);
	}

	/**
	 * Removes all the currently set placeholders. Useful if you want to
	 * reuse the same template object
	 */
	public void clearPlaceholders() {
		this.placeHolders.clear();
	}

	/**
	 * Works in the same way the Perl array to hash code works where every
	 * odd element is a key and every even element is the value. It's a very
	 * un-Java way of encoding this information but it does allow us to populate
	 * a template quickly.
	 */
	public void addPlaceHolders(Object... params) {
		if((params.length % 2) != 0) {
			throw new UtilUncheckedException("Given params array was not of " +
					"even length. Length was "+params.length);
		}

		for(int i=0; i<params.length; i = i+2) {
			this.addPlaceHolder(String.valueOf(params[i]), params[i+1]);
		}
	}

	/**
	 * Performs the generating procedure by looping through all the given
	 * key value pairs and performing String replace operations.
	 */
	public String generate() {
		String output = template;
		for(Map.Entry<String,Object> entry: placeHolders.entrySet()) {
			String targetEntry = generatePlaceholder(entry.getKey());
			output = output.replace(targetEntry, String.valueOf(entry.getValue()));
    }

		return output;
	}

	/**
	 * Potentially useful shortcut for using the builder. This delegates to
	 * {@link #generate()} so you can push this into your debug statements
	 * like:
	 *
	 * <p>
	 * <code>
	 * TemplateBuilder tb = new TemplateBuilder(&quot;Found $val$ values&quot;);
	 * tb.addPlaceHolders(&quot;val&quot;, 127131);
	 * getLog().info(tb);
	 * </code>
	 *
	 * <p>
	 * However you can use the {@link #template(String, Object[])} method for
	 * even more convenient logging code.
	 */
	public String toString() {
		return generate();
	}

	/**
	 * Surrounds the placeholder delim to the given String
	 */
	private String generatePlaceholder(String placeholder) {
		return delim+placeholder+delim;
	}
}
