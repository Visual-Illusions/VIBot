package net.visualillusionsent.vibot.io.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * LogFormat.java - Logging Formatting class
 * 
 * @author darkdiplomat
 */
public final class LogFormat extends SimpleFormatter {
    private SimpleDateFormat dateform = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private String linesep = System.getProperty("line.separator");

    public LogFormat() {
        super();
    }

    public final String format(LogRecord rec) {
        StringBuilder message = new StringBuilder();

        message.append(dateform.format(rec.getMillis()));

        switch (rec.getLevel().intValue()) {
        case 800:
            message.append(" [INFO] ");
            break;
        case 900:
            message.append(" [WARNING] ");
            break;
        case 1000:
            message.append(" [SEVERE] ");
            break;
        case 2000:
            message.append(" [INCOMING LINE] ");
            break;
        case 2100:
            message.append(" [OUTGOING LINE] ");
            break;
        case 2200:
            message.append(" [PING] ");
            break;
        case 2300:
            message.append(" [SERVERPING] ");
            break;
        case 2400:
            message.append(" [CHANMESSAGE] ");
            break;
        case 2500:
            message.append(" [PRIVMESSAGE] ");
            break;
        case 2600:
            message.append(" [COMMAND] ");
            break;
        case 2700:
            message.append(" [NOTICE] ");
            break;
        case 2800:
            message.append(" [JOIN] ");
            break;
        case 2900:
            message.append(" [PART] ");
            break;
        }

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
