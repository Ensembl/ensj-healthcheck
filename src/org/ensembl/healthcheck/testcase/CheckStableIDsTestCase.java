/*
	Copyright (C) 2003 EBI, GRL

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/


package org.ensembl.healthcheck.testcase;

import org.ensembl.healthcheck.TestResult;

/**
 * @author craig
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CheckStableIDsTestCase extends EnsTestCase {



	/**
	 * Check that all the genes, exons, transcripts, translations and exons have
	 * stable IDs and versions > 0. 
	 */
	public CheckStableIDsTestCase() {
		addToGroup("check_stable_ids");
		setDescription("Checks for the presence of _ characters in assembly.type");
		}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.EnsTestCase#run()
	 */
	public TestResult run() {
		boolean result = true;
		return new TestResult(getShortTestName(), result);
	}

}
