/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
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
 * File: ProcessExec.java
 * Created by: dstaines
 * Created on: Oct 25, 2006
 * CVS:  $Id$
 */
package org.ensembl.healthcheck.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.commons.io.IOUtils;

/**
 * Class to consume a stream from an executing process to avoid lockups. Code
 * derived but extensively modified from <a
 * href="http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html">JavaWorld
 * article</a>
 *
 * @author dstaines
 *
 */
class StreamGobbler extends Thread {

	InputStream is;

	boolean discard = false;

	Appendable out;

	StreamGobbler(InputStream is, Appendable out, boolean discard) {
		this.is = is;
		this.out = out;
		this.discard = discard;
	}

	public void run() {
		try {
			if (discard) {
				while (is.read() != -1) {
				}
			} else {
				streamToBuffer(is, out);
			}
		} catch (IOException ioe) {
			
			throw new RuntimeException(ioe);
			
		} finally {
			InputOutputUtils.closeQuietly(is);
		}
	}

	public static void streamToBuffer(InputStream is, Appendable out)
			throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = null;
		while ((line = reader.readLine()) != null) {
			out.append(line);
			out.append('\n');
		}
		reader.close();
	}

}

/**
 * Class to execute a process safely, avoiding hangups by consuming the output
 *
 * @author dstaines
 *
 */
public class ProcessExec {

	private static final String SHELL_FLAGS = "-c";
	private static final String SHELL = "/bin/sh";
	
	/**
	 * Time to wait in milliseconds for stream gobblers to finish
	 */
	private static final int TIMEOUT = 10000;

	/**
	 * Execute the command, capturing the output and error streams to
	 * Appendables
	 *
	 * @param command
	 * @param out
	 * @param err
	 * @return exit code
	 * @throws IOException
	 */
	public static int exec(String command, Appendable out, Appendable err)
			throws IOException {
		return exec(command, out, err, false);
	}

	public static int exec(String command, Appendable out, Appendable err, String[] environment)
			throws IOException {
		return exec(command, out, err, false, environment);
	}

    /**
	 * Execute the command, capturing the output and error streams to
	 * Appendables
	 *
	 * @param commandarray
	 * @param out
	 * @param err
	 * @return exit code
	 * @throws IOException
	 */
	public static int exec(String[] commandarray, Appendable out, Appendable err)
			throws IOException {
		return exec(commandarray, out, err, false);
	}

    /**
	 * Execute the command, discarding output and error
	 *
	 * @param command
	 * @return exit code
	 * @throws IOException
	 */
	public static int exec(String command) throws IOException {
		return exec(command, null, null, true);
	}

    /**
	 * Execute the command, discarding output and error
	 *
	 * @param commandarray
	 * @return exit code
	 * @throws IOException
	 */
	public static int exec(String[] commandarray) throws IOException {
		return exec(commandarray, null, null, true);
	}


    /**
	 * Execute the command, capturing output/error into the supplied streams if
	 * required
	 *
	 * @param command
	 * @param out
	 * @param err
	 * @return
	 * @throws Exception
	 */
	private static int exec(String command, Appendable out, Appendable err,
			boolean discard) throws IOException {
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec(command);
		
		return waitForProcess(out, err, discard, proc);
	}

	private static int exec(
			String command, 
			Appendable out, 
			Appendable err,
			boolean discard, 
			String[] environment
	) throws IOException {
		
		Runtime rt = Runtime.getRuntime();
		
		Process proc = rt.exec(
				command, 
				environment
		);

		return waitForProcess(out, err, discard, proc);
	}
	public static int exec(
			String[] command, 
			Appendable out, 
			Appendable err,
			boolean discard, 
			String[] environment
	) throws IOException {
		
		Runtime rt = Runtime.getRuntime();
		
		Process proc = rt.exec(
				command, 
				environment
		);		

		return waitForProcess(out, err, discard, proc);
	}

	public static int exec(
			String[] command, 
			Appendable out, 
			Appendable err,
			boolean discard, 
			Map<String,String> environmentVars
	) throws IOException {
		

		return exec(
			command, 
			out, 
			err,
			discard, 
			environmentMapToString(environmentVars)
		);
	}

	/**
	 * 
	 * <p>
	 * 	Converts the map representing the environment to be set to the
	 * array of "key=value" representation that the Runtime.exec method
	 * wants.
	 * </p>
	 * 
	 * @return k-v array
	 */
	protected static String[] environmentMapToString(Map<String,String> environmentVars) {
		
		String[] environment = new String[environmentVars.keySet().size()];
		
		int currentIndex=0;
		for (String currentKey : environmentVars.keySet()) {
			
			environment[currentIndex]=currentKey + "=" + environmentVars.get(currentKey);
			currentIndex++;
		}
		
		return environment;
	}	
	
     /**
	 * Execute the command, capturing output/error into the supplied streams if
	 * required
	 *
	 * @param commandarray
	 * @param out
	 * @param err
	 * @return
	 * @throws Exception
	 */
	private static int exec(String[] commandarray, Appendable out, Appendable err,
			boolean discard) throws IOException {
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec(commandarray);
		return waitForProcess(out, err, discard, proc);
	}

    /**
	 * Execute the specified shell command, capturing output and error
	 *
	 * @param command
	 * @return exit code
	 * @throws IOException
	 */
	public static int execShell(
			String command, 
			Appendable out,
			Appendable err
		) throws IOException {
		
		return execShell(command, out, err, false);
	}

	/**
	 * Execute the specified shell command, discarding output and error
	 *
	 * @param command
	 * @return exit code
	 * @throws IOException
	 */
	public static int execShell(String command) throws IOException {
		return execShell(command, (Appendable) null, (Appendable) null, true);
	}
	
	private static Process createShellProcessObject(String command) throws IOException {
		
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec(new String[] { SHELL, SHELL_FLAGS, command });
		return proc;
	}

	private static int execShell(
			String command, 
			Appendable out,
			Appendable err, 
			boolean discard
		) throws IOException {
		
		return waitForProcess(out, err, discard, createShellProcessObject(command));
	}

	private static int execShell(
			String command, 
			Thread outputGobbler, 
			Thread errorGobbler,
			boolean discard
		) throws IOException {
		
		return waitForProcess(outputGobbler, errorGobbler, discard, createShellProcessObject(command));
	}

	private static int waitForProcess(
			Thread outputGobbler, 
			Thread errorGobbler,
			boolean discard, 
			Process proc
		) {
		
		// kick them off
		errorGobbler.start();
		outputGobbler.start();

		// return exit status
		try {

			// get the exit status of the command once finished
			//
			int exit = proc.waitFor();
			
			// now wait for the output and error to be read with a suitable
			// timeout
			//
			outputGobbler.join(TIMEOUT);
			errorGobbler.join(TIMEOUT);
			return exit;

		} catch (InterruptedException e) {

			// While the Process is running, the Thread is in the state 
			// called "WAITING". If an interrupt has been requested and caught
			// within the thread, reinterrupting is requested.
			//
			// See
			// http://download.oracle.com/javase/1,5.0/docs/guide/misc/threadPrimitiveDeprecation.html
			// chapter
			// Section "How do I stop a thread that waits for long periods (e.g., for input)?"
			// for more details on this.
			// 
			Thread.currentThread().interrupt();
			
			return -1;

		} finally {
			
			// to make sure the all streams are closed to avoid open file handles
			//
			IOUtils.closeQuietly(proc.getErrorStream());
			IOUtils.closeQuietly(proc.getInputStream());
			IOUtils.closeQuietly(proc.getOutputStream());
			
			// Otherwise we will be waiting for the process to terminate on 
			// its own instead of interrupting as the user has requested.
			//
			proc.destroy();
		}
	}
	
	private static int waitForProcess(
			Appendable out, 
			Appendable err,
			boolean discard, 
			Process proc
		) {
		// any error message?
		StreamGobbler errorGobbler = new StreamGobbler(
				proc.getErrorStream(),
				err, 
				discard
		);

		// any output?
		StreamGobbler outputGobbler = new StreamGobbler(
				proc.getInputStream(),
				out, 
				discard
		);

		return waitForProcess(
				outputGobbler,
				errorGobbler,
				discard,
				proc
		);
	}

	/**
	 * Execute the command and discard the output and error. If the process is
	 * timeconsuming, please use
	 * {@link #exec(String, Appendable, Appendable)} to avoid hangups
	 *
	 * @param command
	 * @return exit code
	 * @throws IOException
	 */
	public static int execDirect(String command) throws IOException {
		return execDirect(command, null, null, true);
	}

	/**
	 * Execute the command and capture the output and error. If the process is
	 * timeconsuming, please use
	 * {@link #exec(String, Appendable, Appendable)} to avoid hangups
	 *
	 * @param command
	 * @param out
	 * @param err
	 * @return exit code
	 * @throws IOException
	 */
	public static int execDirect(String command, Appendable out,
			Appendable err) throws IOException {
		return execDirect(command, out, err, true);
	}

	/**
	 * @param command
	 * @param out
	 * @param err
	 * @param discard
	 * @return
	 * @throws IOException
	 */
	private static int execDirect(String command, Appendable out,
			Appendable err, boolean discard) throws IOException {

		// return exit status
		try {
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec(command);
			// get the exit status of the command once finished
			int exit = proc.waitFor();
			if (!discard) {
				StreamGobbler.streamToBuffer(proc.getInputStream(), out);
				StreamGobbler.streamToBuffer(proc.getErrorStream(), out);
			}
			return exit;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return -1;
		}
	}

}
