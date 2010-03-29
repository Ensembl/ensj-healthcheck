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

	StringBuffer out;

	StreamGobbler(InputStream is, StringBuffer out, boolean discard) {
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
			ioe.printStackTrace();
		} finally {
			InputOutputUtils.closeQuietly(is);
		}
	}

	public static void streamToBuffer(InputStream is, StringBuffer out)
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
	 * StringBuffers
	 *
	 * @param command
	 * @param out
	 * @param err
	 * @return
	 * @throws Exception
	 */
	public static int exec(String command, StringBuffer out, StringBuffer err)
			throws IOException {
		return exec(command, out, err, false);
	}

    /**
	 * Execute the command, capturing the output and error streams to
	 * StringBuffers
	 *
	 * @param commandarray
	 * @param out
	 * @param err
	 * @return
	 * @throws Exception
	 */
	public static int exec(String[] commandarray, StringBuffer out, StringBuffer err)
			throws IOException {
		return exec(commandarray, out, err, false);
	}

    /**
	 * Execute the command, discarding output and error
	 *
	 * @param command
	 * @return
	 * @throws Exception
	 */
	public static int exec(String command) throws IOException {
		return exec(command, null, null, true);
	}

    /**
	 * Execute the command, discarding output and error
	 *
	 * @param commandarray
	 * @return
	 * @throws Exception
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
	private static int exec(String command, StringBuffer out, StringBuffer err,
			boolean discard) throws IOException {
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec(command);
		return waitForProcess(out, err, discard, proc);
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
	private static int exec(String[] commandarray, StringBuffer out, StringBuffer err,
			boolean discard) throws IOException {
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec(commandarray);
		return waitForProcess(out, err, discard, proc);
	}

    /**
	 * Execute the specified shell command, capturing output and error
	 *
	 * @param command
	 * @return
	 * @throws Exception
	 */
	public static int execShell(String command, StringBuffer out,
			StringBuffer err) throws IOException {
		return execShell(command, out, err, false);
	}

	/**
	 * Execute the specified shell command, discarding output and error
	 *
	 * @param command
	 * @return
	 * @throws Exception
	 */
	public static int execShell(String command) throws IOException {
		return execShell(command, null, null, true);
	}

	private static int execShell(String command, StringBuffer out,
			StringBuffer err, boolean discard) throws IOException {
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec(new String[] { SHELL, SHELL_FLAGS, command });
		return waitForProcess(out, err, discard, proc);
	}

	private static int waitForProcess(StringBuffer out, StringBuffer err,
			boolean discard, Process proc) {
		// any error message?
		StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(),
				err, discard);

		// any output?
		StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(),
				out, discard);

		// kick them off
		errorGobbler.start();
		outputGobbler.start();

		// return exit status
		try {
			// get the exit status of the command once finished
			int exit = proc.waitFor();
			// now wait for the output and error to be read with a suitable
			// timeout
			outputGobbler.join(TIMEOUT);
			errorGobbler.join(TIMEOUT);
			return exit;
		} catch (InterruptedException e) {
			return -1;
		} finally{
			// to make sure the all streams are closed to avoid open file handles
			IOUtils.closeQuietly(proc.getErrorStream());
			IOUtils.closeQuietly(proc.getInputStream());
			IOUtils.closeQuietly(proc.getOutputStream());
		}
	}

	/**
	 * Execute the command and discard the output and error. If the process is
	 * timeconsuming, please use
	 * {@link #exec(String, StringBuffer, StringBuffer)} to avoid hangups
	 *
	 * @param command
	 * @return
	 * @throws IOException
	 */
	public static int execDirect(String command) throws IOException {
		return execDirect(command, null, null, true);
	}

	/**
	 * Execute the command and capture the output and error. If the process is
	 * timeconsuming, please use
	 * {@link #exec(String, StringBuffer, StringBuffer)} to avoid hangups
	 *
	 * @param command
	 * @param out
	 * @param err
	 * @return
	 * @throws IOException
	 */
	public static int execDirect(String command, StringBuffer out,
			StringBuffer err) throws IOException {
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
	private static int execDirect(String command, StringBuffer out,
			StringBuffer err, boolean discard) throws IOException {

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
