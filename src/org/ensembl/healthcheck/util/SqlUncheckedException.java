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
 * SqlUncheckedException
 * 
 * @author dstaines
 * @author $Author$
 * @version $Revision$
 */
package org.ensembl.healthcheck.util;

/**
   * Internal runtime exception class used to capture any issues and
   * throw up to higher levels of control. This allows for any
   * JDBC operation to fail fast.
   */
public class SqlUncheckedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SqlUncheckedException(String message, Throwable cause) {
		super(message, cause);
	}

	public SqlUncheckedException(String message) {
		super(message);
	}

}
