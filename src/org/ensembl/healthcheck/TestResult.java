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

/**
 * <p>Title: TestResult.java</p>
 * <p>Description: Object to hold information about a test result.</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Organisation: EMBL</p>
 * <p>Created on March 12, 2003, 1:08 PM</p>
 * @author Glenn Proctor <glenn@ebi.ac.uk>
 * @version
 */

package org.ensembl.healthcheck;

public class TestResult {
  
  private String name;
  private boolean result;
  private String message;
  
  // -------------------------------------------------------------------------
  /** 
   * Create a new (no-args) TestResult object.
   **/
  public TestResult() {
  }
  
  // -------------------------------------------------------------------------
  /**
   * Create a new TestResult.
   * @param name The name of this test result.
   * @param result Whether the test passed or failed.
   */
  public TestResult(String name, boolean result) {
    this.name = name;
    this.result = result;
  }
  
  // -------------------------------------------------------------------------
  /**
   * Create a new TestResult.
   * @param name The name of this test result.
   * @param result Whether the test passed or failed.
   * @param message An explanatory message.
   */
  public TestResult(String name, boolean result, String message) {
    this(name, result);
    this.message = message;
  }
  
  // -------------------------------------------------------------------------
  /**
   * Set the test result.
   * @param b The new result.
   */
  public void setResult(boolean b) {
    result = b;
  }
  
  // -------------------------------------------------------------------------
  /**
   * Get the test result.
   * @return The result.
   */
  public boolean getResult() {
    return result;
  }
  
  // -------------------------------------------------------------------------
  /**
   * Get the test result description message.
   * @return The message.
   */
  public String getMessage() {
    return message;
  }
  
  // -------------------------------------------------------------------------
  /**
   * Set the result description.
   * @param s The new message.
   */
  public void setMessage(String s) {
    message = s;
  }
  
  // -------------------------------------------------------------------------
  /**
   * Get the test name.
   * @return The name.
   */
  public String getName() {
    return name;
  }
  
  // -------------------------------------------------------------------------
  /**
   * Set the test name.
   * @param s The new name.
   */
  public void setName(String s) {
    name = s;
  }
  
  // -------------------------------------------------------------------------

} // TestResult

