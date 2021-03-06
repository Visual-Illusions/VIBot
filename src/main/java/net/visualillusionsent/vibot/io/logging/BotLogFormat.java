/* 
 * Copyright 2012 - 2013 Visual Illusions Entertainment.
 *  
 * This file is part of VIBot.
 *
 * VIBot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * VIBot is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with VIBot.
 * If not, see http://www.gnu.org/licenses/lgpl.html
 */
package net.visualillusionsent.vibot.io.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import net.visualillusionsent.utils.SystemUtils;

/**
 * Bot Logger Formatter
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
final class BotLogFormat extends SimpleFormatter {
    /**
     * The {@link SimpleDateFormat} to use for logging
     */
    private SimpleDateFormat dateform = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    /**
     * Formats the log output
     * 
     * @param rec
     *            the {@link LogRecord} to be formated
     * @return formated {@link LogRecord} as a {@link String}
     */
    @Override
    public final String format(LogRecord rec) {
        StringBuilder message = new StringBuilder();

        message.append(dateform.format(rec.getMillis()));
        message.append(" [" + rec.getLevel().getName() + "] ");
        message.append(rec.getMessage());
        message.append(SystemUtils.LINE_SEP);

        if (rec.getThrown() != null) {
            StringWriter stringwriter = new StringWriter();
            rec.getThrown().printStackTrace(new PrintWriter(stringwriter));
            message.append(stringwriter.toString());
        }

        return message.toString();
    }
}
