package werewolf.log;

import java.io.IOException;
import java.util.logging.ErrorManager;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class WerewolfLogger
{
	private static final boolean LOG_TO_FILE = false;

	private static FileHandler		fileTxt;
	private static SimpleFormatter	formatterTxt;

	private static FileHandler	fileHTML;
	private static Formatter	formatterHTML;

	private static Handler console;

	public static void setup() throws IOException
	{
		if (!WerewolfLogger.LOG_TO_FILE)
			return;

		// Get the global logger to configure it
		Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		logger.setLevel(Level.INFO);

		WerewolfLogger.fileTxt = new FileHandler("log.txt");
		WerewolfLogger.fileHTML = new FileHandler("log.html");
		WerewolfLogger.console = new Handler()
		{
			@Override
			public void close() throws SecurityException
			{
			}

			@Override
			public void flush()
			{
			}

			@Override
			public void publish(LogRecord record)
			{
				System.out.println("Blarg.");
				if (this.getFormatter() == null)
					this.setFormatter(new SimpleFormatter());

				try
				{
					String message = this.getFormatter().format(record);
					if (record.getLevel().intValue() == Level.WARNING.intValue() || record.getLevel().intValue() == Level.SEVERE.intValue())
						System.err.println(message);
					else
						System.out.println(message);
				} catch (Exception exception)
				{
					this.reportError(null, exception, ErrorManager.FORMAT_FAILURE);
					return;
				}

			}
		};

		// create txt Formatter
		WerewolfLogger.formatterTxt = new SimpleFormatter();
		WerewolfLogger.fileTxt.setFormatter(WerewolfLogger.formatterTxt);
		logger.addHandler(WerewolfLogger.fileTxt);

		// create HTML Formatter
		WerewolfLogger.formatterHTML = new HtmlFormatter();
		WerewolfLogger.fileHTML.setFormatter(WerewolfLogger.formatterHTML);
		logger.addHandler(WerewolfLogger.fileHTML);

		logger.addHandler(WerewolfLogger.console);
	}
}
