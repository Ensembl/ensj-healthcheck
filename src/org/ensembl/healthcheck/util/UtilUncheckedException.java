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
