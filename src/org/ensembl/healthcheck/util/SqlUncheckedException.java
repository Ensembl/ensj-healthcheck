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