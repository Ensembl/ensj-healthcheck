package org.ensembl.healthcheck.testcase.eg_core;

import java.sql.Connection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseServer;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.SystemCommand;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.ActionAppendable;

public class SchemaPatchesApplied extends SingleDatabaseTestCase {
	
	protected boolean passes;

	public boolean isPasses() {
		return passes;
	}

	public void setPasses(boolean passes) {
		this.passes = passes;
	}

	public boolean run(DatabaseRegistryEntry dbr) {
		
		setPasses(true);
	
		final String schemaPatchScript = "./perlcode/ensembl/misc-scripts/schema_patcher.pl";
		
		SystemCommand systemCommand = new SystemCommand();
	
		// If the script can't be found, the test can terminate right away.
		//
		if (!systemCommand.checkCanExecute(schemaPatchScript)) {
	
			ReportManager.problem(this, (Connection) null,
					"Can't find "+ schemaPatchScript +"! "
					+ this.getShortTestName() + " relies on this program to "
					+ "find missing patches."
			);
			passes = false;
			return passes;
		}
		
		String database = dbr.getName();
		String type     = dbr.getType().getName();
		String release  = dbr.getSchemaVersion();
	
		DatabaseServer srv = dbr.getDatabaseServer();
	
		logger.info("Running " + schemaPatchScript);
	
		final Connection conn = dbr.getConnection();
		final SchemaPatchesApplied thisTestRef = this;
	
		String passwordOption = "";
		
		if (!StringUtils.isEmpty(srv.getPass())) {
			passwordOption = "--pass=" + srv.getPass();
		}
		
		String cmd = 
			schemaPatchScript
			+ " --host=" + srv.getHost()
			+ " --port=" + srv.getPort()
			+ " --user=" + srv.getUser()
			+ " " + passwordOption
			+ " --database=" + database
			+ " --type=" + type
			+ " --from " + release
			+ " --release " + release
			+ " --verbose"
			+ " --dryrun"
			+ " --fix"
			;
		
		logger.info("Running " + cmd);
			
		systemCommand.runCmd(
			new String[] {
				schemaPatchScript,
				"--host=" + srv.getHost(),
				"--port=" + srv.getPort(),
				"--user=" + srv.getUser(),
				"" + passwordOption,
				"--database=" + database,
				"--type=" + type,
				"--from=" + release,
				"--release=" + release,
				"--verbose",
				"--dryrun",
				"--fix"
			},
			new ActionAppendable() {
				@Override public void process(String message) {
					
					if (message.startsWith("Would apply ")) {
						
						//
						// Line 600 in schema_patcher.pl
						// printf( "Would apply patch '%s' (%s)\n",
						//
						//String pat = "Would apply (patch)";
						String pat = "Would apply patch '(patch_.+sql)' \\((.+?)\\)";
						
						Pattern pattern = Pattern.compile(pat);
						Matcher matcher = pattern.matcher(message);
	
						if (matcher.matches()) {
							String patchName = matcher.group(1);
							String type      = matcher.group(2);
							ReportManager.problem(thisTestRef, conn, "The patch file "+patchName+" has not been applied.");
							thisTestRef.setPasses(false);
							
						} else {
							throw new RuntimeException(
								"Can't parse message from script!\n"
								+ "(" + message + ")\n"
								+ "Maybe the script " + schemaPatchScript + " has been updated and the message it outputs no longer is matched by the regular expression."
							);
						}
					}				
				}
			},
			new ActionAppendable() {
				@Override public void process(String message) {
					logger.warning(message);
				}
			}
		);	
		return isPasses();
	}
}
