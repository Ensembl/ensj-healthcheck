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

package org.ensembl.healthcheck.util;

import java.io.*;

/**
 * <p>Title: ClassFileFilenameFilter.java</p>
 * <p>Description: Implementation of FilenameFilter that looks for class files.</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Organisation: EMBL</p>
 * <p>Created on March 12, 2003, 10:11 AM</p>
 * @author Glenn Proctor <glenn@ebi.ac.uk>
 * @version $Revision$
 */

public class ClassFileFilenameFilter implements FilenameFilter {
  
  /**
   * Check if the filename ends in .class. The check is case insensitive.
   * @param file The file object (not used)
   * @param str The file name.
   * @return True if str ends in .class (case insensitive).
   */
  public boolean accept(java.io.File file, String str) {
      
    return str.toLowerCase().indexOf(".class") > -1 ? true : false;
    
  } // accept
  
} //ClassFileFilenameFilter
