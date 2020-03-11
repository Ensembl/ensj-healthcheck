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

package org.ensembl.healthcheck.testcase.funcgen;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.ensembl.CoreDbNotFoundException;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.MissingMetaKeyException;
import org.ensembl.healthcheck.ReportManager;

import java.lang.NullPointerException;

public class ExternalFeatureFilesExist extends AbstractExternalFileUsingTestcase {

	public void types() {
		appliesToType(DatabaseType.FUNCGEN);
	}
		
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean testPassed = true;
		
		String sql = "select path from data_file where table_name='external_feature_file'";
		String speciesAssemblyDbFileRootDir;
		
		try {
			
			speciesAssemblyDbFileRootDir = getSpeciesAssemblyDataFileBasePath(dbre);
			
		} catch (CoreDbNotFoundException e) {
			
			ReportManager.problem(this, dbre.getConnection(), e.getMessage());
			testPassed = false;
			return testPassed;
			
		} catch (MissingMetaKeyException e) {
			
			ReportManager.problem(this, dbre.getConnection(), e.getMessage());
			testPassed = false;
			return testPassed;
			
		}

		try {
			Statement stmt = dbre.getConnection().createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				
				String relativeFilePath = rs.getString("path");
				String fullFileName = speciesAssemblyDbFileRootDir + relativeFilePath;
				
				boolean fileExists = new File(fullFileName).isFile();
				
				if (fileExists) {
					logger.info("ok " + fullFileName);
				} else {
					logger.severe("not ok " + fullFileName);
					testPassed = false;
				}
			}
		} catch (SQLException e) {
			testPassed = false;
			e.printStackTrace();
			return testPassed;
		}
		return testPassed;
	}

}
