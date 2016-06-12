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


public class WerewolfLogger {
    private static final boolean LOG_TO_FILE = false;
    
    private static FileHandler     fileTxt;
    private static SimpleFormatter formatterTxt;

    private static FileHandler fileHTML;
    private static Formatter   formatterHTML;

    private static Handler console;

    public static void setup() throws IOException {
        if (!LOG_TO_FILE)
            return;
        
        // Get the global logger to configure it
        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        logger.setLevel(Level.INFO);

        fileTxt = new FileHandler("log.txt");
        fileHTML = new FileHandler("log.html");
        console = new Handler() {
            @Override
            public void publish(LogRecord record) {
                System.out.println("Blarg.");
                if (getFormatter() == null)
                    setFormatter(new SimpleFormatter());

                try {
                    String message = getFormatter().format(record);
                    if (record.getLevel().intValue() == Level.WARNING.intValue() ||
                        record.getLevel().intValue() == Level.SEVERE.intValue())
                        System.err.println(message);
                    else
                        System.out.println(message);
                } catch (Exception exception) {
                    reportError(null, exception, ErrorManager.FORMAT_FAILURE);
                    return;
                }

            }

            @Override
            public void close() throws SecurityException {}

            @Override
            public void flush() {}
        };

        // create txt Formatter
        formatterTxt = new SimpleFormatter();
        fileTxt.setFormatter(formatterTxt);
        logger.addHandler(fileTxt);


        // create HTML Formatter
        formatterHTML = new HtmlFormatter();
        fileHTML.setFormatter(formatterHTML);
        logger.addHandler(fileHTML);

        logger.addHandler(console);
    }
}
