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

/**
 * ChecksumDatabase
 * 
 * @author dstaines
 * @author $Author$
 * @version $Revision$
 */
package org.ensembl.healthcheck.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;

/**
 * Utility to compare checksums from a set of database tables to a persistent
 * file on disk
 * 
 * @author dstaines
 */
public class ChecksumDatabase {

	protected final static String CHECKSUM_SQL = "CHECKSUM TABLE %s EXTENDED";

	protected final String databaseName;
	protected final SqlTemplate templ;
	protected final File checksumFile;
	protected final Collection<String> tables;

	public ChecksumDatabase(DatabaseRegistryEntry dbre, File directory,
			Collection<String> tables) {
		this(dbre.getName(), DBUtils.getSqlTemplate(dbre), directory, tables);
	}

	public ChecksumDatabase(DatabaseRegistryEntry dbre, 
			Collection<String> tables) {
		this(dbre.getName(), DBUtils.getSqlTemplate(dbre), null, tables);
	}

	public ChecksumDatabase(String databaseName, SqlTemplate templ,
			File directory, Collection<String> tables) {
		this.databaseName = databaseName;
		this.templ = templ;
		this.tables = tables;
		
		if (directory==null) {
			checksumFile = null;
		} else {
			directory.mkdirs();
			checksumFile = new File(directory, databaseName + ".chk");
		}
	}

	protected Properties getChecksumFromFile() {
		Properties fileSum = new Properties();
		if (checksumFile.exists()) {
			try {
				fileSum.load(new FileInputStream(checksumFile));
			} catch (IOException e) {
				throw new RuntimeException("Cannot read table properties from "
						+ checksumFile, e);
			}
		}
		return fileSum;
	}

	public Properties getChecksumFromDatabase() {
		Properties dbSum = new Properties();
		for (final String table : tables) {
			dbSum.putAll(templ.queryForMap(
					CHECKSUM_SQL.replaceFirst("%s", table),
					new MapRowMapper<String, String>() {

						@Override
						public String mapRow(ResultSet resultSet, int position)
								throws SQLException {
							return resultSet.getString(2);
						}

						@Override
						public Map<String, String> getMap() {
							return CollectionUtils.createHashMap(1);
						}

						@Override
						public String getKey(ResultSet resultSet)
								throws SQLException {
							return resultSet.getString(1);
						}

						@Override
						public void existingObject(String currentValue,
								ResultSet resultSet, int position)
								throws SQLException {
							throw new RuntimeException();
						}
					}));
		}
		return dbSum;
	}

	public boolean isUpdated() {
		boolean updated = false;
		Properties db = getChecksumFromDatabase();
		Properties fs = getChecksumFromFile();
		for (Entry<Object, Object> e : db.entrySet()) {
			Object fsVal = fs.get(e.getKey());
			if (!e.getValue().equals(fsVal)) {
				updated = true;
				break;
			}
		}
		return updated;
	}

	public void setRead() {
		// Write properties file
		try {
			getChecksumFromDatabase().store(new FileOutputStream(checksumFile),
					null);
		} catch (IOException e) {
			throw new RuntimeException("Cannot write table properties to "
					+ checksumFile, e);
		}
	}

	public void reset() {
		checksumFile.delete();
	}

}
