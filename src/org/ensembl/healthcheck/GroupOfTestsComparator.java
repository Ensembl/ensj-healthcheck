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

package org.ensembl.healthcheck;

import java.util.Comparator;

/**
 * 
 * Compares GroupOfTests by their name. Useful when sorting groups.
 * 
 * @author michael
 *
 */
public class GroupOfTestsComparator implements Comparator<GroupOfTests> {
	public int compare(GroupOfTests arg0, GroupOfTests arg1) {				
		return arg0.getName().compareTo(arg1.getName());
	}
}
