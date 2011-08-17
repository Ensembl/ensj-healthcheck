/**
 * File: AbstractPerlModuleBasedTestCase.java
 * Created by: dstaines
 * Created on: Mar 23, 2010
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseServer;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.LogMapperPerl2Java;
import org.ensembl.healthcheck.util.ActionAppendable;
import org.ensembl.healthcheck.util.TemplateBuilder;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * @author dstaines
 *
 */
public abstract class AbstractPerlModuleBasedTestCase extends AbstractPerlBasedTestCase {

	private static final String SCRIPT = "./perl/run_healthcheck.pl -host $host$ -port $port$ -user $user$ -pass $pass$ -dbname $dbname$ -species_id $species_id$ -module $module$";
	private final LogMapperPerl2Java logMapper;
	private final Formatter perlLogMessagesFormatter;

	protected Map<Handler, Formatter> savedFormatter = new HashMap<Handler, Formatter>();

	public AbstractPerlModuleBasedTestCase() {
		super();
		logMapper = new LogMapperPerl2Java(logger);
		perlLogMessagesFormatter = createPerlLogMessageFormatter();
	}
	
	/**
	 * 
	 * Returns the name of the perl module with the healthcheck test.
	 * 
	 * @return
	 * 
	 */
	protected abstract String getModule();

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.AbstractPerlBasedTestCase#getPerlScript()
	 */
	@Override
	protected String getPerlScript(
			DatabaseRegistryEntry dbre, 
			int speciesId
	) {
		DatabaseServer srv = dbre.getDatabaseServer();
		return TemplateBuilder.template(SCRIPT, "host", srv.getHost(),
				"port", srv.getPort(),
				"user", srv.getUser(),
				"pass", srv.getPass(),
				"dbname", dbre.getName(),
				"module", getModule(),
				"species_id", speciesId);
	}
	
	/**
	 * <p>
	 * 	Creates a formatter for log messages originating in a perl based 
	 * healthcheck.
	 * </p>
	 * 
	 * @return
	 */
	protected Formatter createPerlLogMessageFormatter() {
		
		return new Formatter() {
			
			@Override public String formatMessage(LogRecord logRecord) {
				return logRecord.getMessage();
			}
			
			@Override public String format(LogRecord logRecord) {
				return formatMessage(logRecord) + "\n";
			}			
		};
	}
	
	/**
	 * <p>
	 * 	Forward a message received from the healthcheck to the ReportManager  
	 * or the logging system.
	 * </p>
	 * 
	 * @param message
	 */
	protected void dispatchMessage(final EnsTestCase e, final Connection c, String message) {

		// Parse output from the healthcheck with prefixes from 
		// Bio::EnsEMBL::Healthcheck, forward to appropriate destinations.
		//
		if (message.startsWith("PROGRESS")) {

			// Progress messages go to the logger on the fine level.
			//Formatter savedFormatter = logger.getHandlers()[0].getFormatter();
			
			logger.fine(message);
			//Logger.getLogger("ProgressLogger").info(message);
			return;
		}
		
		if (message.startsWith("LOG")) {

			// Log messages from perl get forwarded to the java logger.
			logMapper.log(message);
			return;
		}
		
		if (message.startsWith("PROBLEM")) {
			
			// Only count messages as a problem, if they are 
			// prefixed with PROBLEM. The prefix is created by 
			// Bio::EnsEMBL::Healthcheck.
			//
			ReportManager.problem(e, c, message);
			return;
		}

		if (message.startsWith("CORRECT")) {
		
			ReportManager.correct(e, c, message);
			return;
		}
		
		if (message.trim().length()==0) {
			return;
		}
		
		// Otherwise pass this on as a warning. Definitely makes sense for the
		// API complaining about being run on the wrong version of a database.
		//
		logger.warning(message);
	}
	
//	/**
//	 * <p>
//	 * 	Only one processor for output from perl healthchecks. We don't care
//	 * whether something was printed to stdout or stderr. What is done with
//	 * it depends on the prefix set by the output methods in 
//	 * Bio::EnsEMBL::Healthcheck.
//	 * </p>
//	 * 
//	 */
//	protected Appendable createOutputProcessor(final EnsTestCase e, final Connection c) {
//		return 
//			new ActionAppendable() {
//				@Override public void process(String message) {
//					dispatchMessage(e, c, message.trim());
//				}
//				
//			};
//	}
//	
//	@Override protected Appendable createStdoutProcessor(final EnsTestCase e, final Connection c) {
//		return createOutputProcessor(e, c);
//	}
//
//	@Override protected Appendable createStderrProcessor(final EnsTestCase e, final Connection c) {
//		return createOutputProcessor(e, c);
//	}
	
	protected void setPerlFriendlyLogFormatters() {
		
		Handler[] configuredHandler = logger.getHandlers(); 
		
		// It is possible that no Handler has been configured. In that case we
		// create and add one to the logger.
		//
		if (configuredHandler.length == 0) {
			logger.addHandler(new ConsoleHandler());
			configuredHandler = logger.getHandlers();
		}
		
		// Now there is at least one configuredHandler.
		//
		for (Handler h : configuredHandler) {
			
			savedFormatter.put(h, h.getFormatter());
			h.setFormatter(perlLogMessagesFormatter);
			
			if (h.getFormatter() != perlLogMessagesFormatter) {
				throw new RuntimeException("Unable to set Formatter!");
			}
		}
	}
	
	protected void removePerlFriendlyLogFormatters() {
		
		for (Handler h : logger.getHandlers()) {
			
			h.setFormatter(savedFormatter.get(h));
		}
	}
	@Override
	public boolean run(final DatabaseRegistryEntry dbre) {
		
		setPerlFriendlyLogFormatters();
		boolean savedUseParentHandler = logger.getUseParentHandlers();
		logger.setUseParentHandlers(false);

		boolean passes = super.run(dbre);
		
		removePerlFriendlyLogFormatters();
		logger.setUseParentHandlers(savedUseParentHandler);
		
		return passes;
	}
}


