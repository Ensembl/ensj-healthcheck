/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2020] EMBL-European Bioinformatics Institute
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

import java.util.HashMap;
import java.util.Map;

/**
 * A default implementation of the {@link MapRowMapper} intended as an
 * extension point to help when creating these call back objects. This version
 * will return a HashMap of the given generic types.
 *
 * @author ayates
 * @author dstaines
 */
public abstract class AbstractMapRowMapper<K, T> implements MapRowMapper<K, T>{

	public Map<K, T> getMap() {
		return new HashMap<K,T>();
	}
}
