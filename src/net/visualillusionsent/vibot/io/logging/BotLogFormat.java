package net.visualillusionsent.vibot.io.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * Bot Logger Formatter
 * 
 * @author darkdiplomat
 */
final class BotLogFormat extends SimpleFormatter {
    private SimpleDateFormat dateform = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private String linesep = System.getProperty("line.separator");

    @Override
    public final String format(LogRecord rec) {
        StringBuilder message = new StringBuilder();

        message.append(dateform.format(rec.getMillis()));
        message.append(" [" + rec.getLevel().getName() + "] ");
        message.append(rec.getMessage());
        message.append(linesep);

        if (rec.getThrown() != null) {
            StringWriter stringwriter = new StringWriter();
            rec.getThrown().printStackTrace(new PrintWriter(stringwriter));
            message.append(stringwriter.toString());
        }

        return message.toString();
    }
}
