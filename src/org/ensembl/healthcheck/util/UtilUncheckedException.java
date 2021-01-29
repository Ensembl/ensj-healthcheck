/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2021] EMBL-European Bioinformatics Institute
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
 * File: UtilUncheckedException.java
 * Created by: dstaines
 * Created on: Nov 11, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.util;

/**
 * Generic unchecked util exceptions to be used by org.ensembl.healthcheck.util classes
 * @author dstaines
 *
 */
public class UtilUncheckedException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	public UtilUncheckedException(String message) {
		super(message);
	}
	public UtilUncheckedException(String message, Throwable cause) {
		super(message, cause);
	}

}
