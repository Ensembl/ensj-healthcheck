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

import java.io.FilenameFilter;

/**
 * Implementation of FilenameFilter that looks for class files.
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
