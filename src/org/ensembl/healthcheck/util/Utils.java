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

/**
 * <p>Title: Utils.java</p>
 * <p>Description: General utilities.</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Organisation: EMBL</p>
 * @author Glenn Proctor <glenn@ebi.ac.uk>
 * @version 1.0
 */

import java.io.*;
import java.util.*;

public class Utils {

  public Utils() {
  }

  /**
   * Read a properties file.
   * @param propertiesFileName The name of the properties file to use.
   * @return The Properties hashtable.
   */
  public static Properties readPropertiesFile(String propertiesFileName) {

    Properties props = new Properties();

    try {

      FileInputStream in = new FileInputStream(propertiesFileName);
      props.load(in);
      in.close();


    } catch (Exception e) {

      e.printStackTrace();
      System.exit(1);

    }

    return props;

  } // readPropertiesFile


} // Utils