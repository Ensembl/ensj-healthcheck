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

package org.ensembl.healthcheck.util;

import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Converts the row of the result set into an Object[] output. There are
 * two constructors available. The first assumes null args constructor will
 * force this mapper to push all objects in the result set output into the
 * outputting array. The second version takes an int array of the positions
 * of the elements in the result set you want back. For example I may
 * query for something but only want the first and third column back as a
 * result set. My usage of this class would be:
 *
 * <pre>
 * <code>
 * RowMapper<Object[]> mapper = new ObjectArrayRowMapper(new int[]{1,3});
 * List<Object[]> list = template.queryForList("select a, b, c from tab", mapper);
 * </code>
 * </pre>
 *
 * Note the shortcut syntax for the int array where {} can denote the array
 * contents and that the indexing used position 1 based indexing (as the
 * result set would expect not as a Java array would use).
 *
 * @author ayates
 * @author dstaines (adapted for pure JDBC use)
 */
public class ObjectArrayRowMapper implements RowMapper<Object[]> {

	private final int[] columns;

	public ObjectArrayRowMapper() {
		this.columns = new int[0];
	}

	/**
	 * Takes in the columns to get. Must be indexed as the result set does i.e.
	 * from 1 NOT from 0
	 */
	public ObjectArrayRowMapper(int[] columns) {
		this.columns = columns;
	}

	public Object[] mapRow(ResultSet resultSet, int position) throws SQLException {
		Object[] output = null;
		int[] mappedColumns = columns;

		if(isEmpty(mappedColumns)) {
			int rsColumns = resultSet.getMetaData().getColumnCount();
			mappedColumns = new int[rsColumns];
			for(int i=0; i<rsColumns; i++) {
				mappedColumns[i] = i+1;
			}
		}

		output = new Object[mappedColumns.length];

		for(int i=0; i<output.length; i++) {
			output[i] = resultSet.getObject(mappedColumns[i]);
		}

		return output;
	}

	/**
	 * @param mappedColumns
	 * @return
	 */
	private static boolean isEmpty(int[] mappedColumns) {
		return mappedColumns==null||mappedColumns.length==0;
	}
}
