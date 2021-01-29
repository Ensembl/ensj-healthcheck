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

package org.ensembl.healthcheck;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ensembl.healthcheck.util.ActionAppendable;
import org.ensembl.healthcheck.util.ProcessExec;

public class SystemCommand {

	public void runCmd(
			String[] cmdLineItems, 
			Map<String,String> environmentVars,
			ActionAppendable stdout,
			ActionAppendable stderr
		) {
		try {
			int exit = ProcessExec.exec(
				cmdLineItems, 
				stdout, 
				stderr, 
				false, 
				environmentVars
			);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void runCmd(
			String[] cmdLineItems, 
			ActionAppendable stdout,
			ActionAppendable stderr
		) {
		runCmd(cmdLineItems, defaultEnvironment(), stdout, stderr);
	}
	
	public void runCmd(
			String[] cmdLineItems, 
			Map<String,String> environmentVars
		) {
		
		runCmd(
			cmdLineItems, 
			environmentVars, 
			new ActionAppendable() {
				@Override public void process(String message) {
					System.out.println(message);
				}
			}, 
			new ActionAppendable() {
				@Override public void process(String message) {
					System.err.println(message);
				}
			} 
		);
	}
	
	public Map<String,String> defaultEnvironment() {
		return new HashMap<String,String>(System.getenv());
	}
	
	public void runCmd(String[] cmdLineItems) {		
		runCmd(cmdLineItems, defaultEnvironment());
	}
	
	public void runCmd(List<String> param) {
		
		String[] cmdLineItems = param.toArray(new String[] { "" });
		runCmd(cmdLineItems);		
	}
	
	public boolean checkCanExecute(String programName) {
		
		String fullPath = findInSystemPath(programName);		
		return new File(fullPath).canExecute();
	}
	
	public String findInSystemPath(String programName) {
		
		List<String> param = new LinkedList<String>(); 
		
		param.add("which");			
		param.add(programName);		
		
		String[] cmdLineItems = param.toArray(new String[] { "" });
		Map<String,String> environmentVars = new HashMap<String,String>(System.getenv());
		
		final StringBuffer locationOfProgram = new StringBuffer();
		
		try {
			int exit = ProcessExec.exec(
				cmdLineItems, 
				new ActionAppendable() {
					@Override public void process(String message) {
						locationOfProgram.append(message);
					}
				}, 
				new ActionAppendable() {
					@Override public void process(String message) {
						System.err.println(message);
					}
				}, 
				false, 
				environmentVars
			);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Trim is important, because which returns a carriage return at the end.
		return locationOfProgram.toString().trim();
	}
	
}
