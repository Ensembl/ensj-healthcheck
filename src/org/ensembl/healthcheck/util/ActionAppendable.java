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

package org.ensembl.healthcheck.util;

import java.io.IOException;

/**
 * <p>
 * 	Class for capturing output from a subprocess and using this output elsewhere.
 * </p>
 * 
 * <p>
 * 	Data can be written to this object by using the Appendable interface it 
 * implements. What is done with the data can be detemined by overwriting the
 * process method.
 * </p>
 *
 * <p>
 * 	This class can be used whenever it is necessary to pass on an Appendable 
 * object to capture input. Usually StringBuffers are used for this. When using
 * a StringBuffer one has to wait for the process to return in order to process
 * what has been written to it.
 * </p>
 * 
 * <p>
 * 	By using this object one can immediately have anything written to the 
 * Appendable object forwarded to some other part of the program, for 
 * example to  display it. Overwrite the process method to decide what should 
 * be done with any output.
 * </p>
 */
public abstract class ActionAppendable implements Appendable {
	
	abstract public void process(String s); 
	
	@Override
	public Appendable append(final CharSequence csq) throws IOException {
		
		process(csq.toString());
		return this;
	}

	@Override
	public Appendable append(final char c) throws IOException {

		process(Character.toString(c));
		return this;
	}

	@Override
	public Appendable append(CharSequence csq, int start, int end) throws IOException {
		
		throw new NoSuchMethodError("This method should not be needed at the moment.");
	}
}

