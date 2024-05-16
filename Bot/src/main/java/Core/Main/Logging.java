package Core.Main;

import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import serilogj.Log;
import serilogj.LoggerConfiguration;
import serilogj.events.LogEventLevel;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.*;

import static serilogj.sinks.seq.SeqSinkConfigurator.seq;

public class Logging
{
	public static final DateFormat df = new SimpleDateFormat("dd/LLL/yyyy - HH:mm (zzz)", Locale.US);
	public static Logger out;
	
	public static void activate()
	{
		Log.setLogger(new LoggerConfiguration()
				              .writeTo(seq("http://seq:5341/"))
				              .setMinimumLevel(LogEventLevel.Verbose)
				              .createLogger());
		
		out = Logger.getLogger("TheArmory_Logger");
		out.setUseParentHandlers(false);
		out.setLevel(Level.ALL);

		CustomFormatter formatter = new CustomFormatter();
		CustomConsoleHandler handler = new CustomConsoleHandler();
		CustomPrintStream stream = new CustomPrintStream(out);
		CustomErrorPrintstream stream1 = new CustomErrorPrintstream(out);

		handler.setFormatter(formatter);
		out.addHandler(handler);

		stream.attachOut();
		stream1.attachOut();
	}
	
	public static void exception(Throwable e)
	{
		if (e instanceof ErrorResponseException ex) {
			if (ex.getErrorCode() == 10008) {
				System.err.println("Unknown Message error! Fix this!");
				return;
			}
		}

		String preFix = Startup.debug ? getPrefix(Level.SEVERE, System.currentTimeMillis()) : "";
		StringBuilder builder = new StringBuilder();

		builder.append(e).append("\n");
		for (StackTraceElement g : e.getStackTrace()) {
			builder.append(preFix).append("\t at ").append(g.toString()).append("\n");
		}

		System.err.println(builder);
		Log.error(e, "Exception: {error}", e.getMessage());
	}

	protected static String getPrefix(Level level, Long time)
	{
		StringBuilder preFix = new StringBuilder();

		if (!Startup.jarFile) {
			preFix.append("[").append(Logging.df.format(new Date(time))).append("]").append(" - ");
		}

		if (Startup.debug && level != Level.SEVERE) {
			preFix.append("[").append(level).append("] - ");
			preFix.append("[").append(Thread.currentThread().getName()).append(":").append(Thread.currentThread().getId()).append("] - ");


			StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
			for (StackTraceElement stack : stacks) {

				if (stack != null) {
					String className = stack.getClassName();

					if (stack.getClassName().startsWith(Logging.class.getPackage().getName()) || stack.isNativeMethod() || className.startsWith("java") || stack.getClassName().startsWith("org.slf4j") || stack.getFileName() != null && stack.getFileName().toLowerCase().contains(
						"slf4jlogger") || stack.getClassName().startsWith("org.eclipse") || stack.getMethodName().equalsIgnoreCase("handleException")) {
						continue;
					}
				}


				preFix.append("[").append(stack.getFileName()).append("][").append(stack.getMethodName()).append(":").append(stack.getLineNumber()).append("] - ");
				break;
			}
		}

		return preFix.toString();
	}


	public static class CustomFormatter extends Formatter
	{

		public String format(LogRecord record)
		{
			String prefix = Logging.getPrefix(record.getLevel(), record.getMillis());

			if (record.getMessage() == null || record.getMessage().isEmpty() || record.getMessage().equalsIgnoreCase("\n")) {
				return "";
			}

			StringBuilder builder = new StringBuilder();

			builder.append(prefix);
			builder.append(formatMessage(record));

			if (!record.getMessage().isEmpty()) {
				if (record.getMessage() == null || !record.getMessage().contains("\n")) {
					builder.append("\n");
				}
			}

			return builder.toString();
		}
	}

	public static class CustomConsoleHandler extends ConsoleHandler
	{
		@Override
		public void publish(LogRecord record)
		{
			try {
				String message = getFormatter().format(record);
				
				if(record.getLevel() == Level.WARNING){
					Log.warning(message);
				}else if(record.getLevel() == Level.INFO){
					Log.information(message);
				}

				if (record.getLevel() == Level.SEVERE || record.getLevel() == Level.WARNING) {
					System.err.write(message.getBytes());
				} else {
					System.out.write(message.getBytes());
				}

			} catch (Exception exception) {
				reportError(null, exception, ErrorManager.FORMAT_FAILURE);
			}
		}
	}

	public static class CustomPrintStream extends PrintStream
	{
		private final Logger log;

		CustomPrintStream(Logger log)
		{

			super(System.out, true);
			this.log = log;
		}

		void attachOut()
		{
			System.setOut(this);
		}

		@Override
		public void print(String s)
		{
			log.log(Level.INFO, s);
		}

		@Override
		public void println(String s)
		{
			print(s);
		}
	}

	public static class CustomErrorPrintstream extends PrintStream
	{
		private final Logger log;

		CustomErrorPrintstream(Logger log)
		{
			super(System.err, true);
			this.log = log;
		}

		void attachOut()
		{
			System.setErr(this);
		}

		@Override
		public void print(String s)
		{
			log.log(Level.SEVERE, s);
		}

		@Override
		public void println(String s)
		{
			print(s);
		}
	}

}