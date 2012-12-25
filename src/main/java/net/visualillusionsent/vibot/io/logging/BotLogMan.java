/* 
 * Copyright 2012 Visual Illusions Entertainment.
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
 * You should have received a copy of the GNU Lesser General Public License along with VIUtils.
 * If not, see http://www.gnu.org/licenses/lgpl.html
 */
package net.visualillusionsent.vibot.io.logging;

import java.io.File;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.visualillusionsent.vibot.VIBot;
import net.visualillusionsent.vibot.io.configuration.BotConfig;

/**
 * The {@link VIBot} {@link Logger} Manager
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
public final class BotLogMan {
    /**
     * The {@link Logger} instance
     */
    private static Logger logger;

    static {
        logger = Logger.getLogger("VIBot");
        File logDir = new File("logs/");
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
        try {
            BotLogFormat lf = new BotLogFormat();
            ConsoleHandler chand = new ConsoleHandler();
            FileHandler fhand = new FileHandler("logs/botlog%g.log", 52428800, 150, true);
            chand.setLevel(Level.ALL);
            fhand.setLevel(Level.ALL);
            logger.setLevel(Level.ALL);
            logger.setUseParentHandlers(false);
            chand.setFormatter(lf);
            fhand.setFormatter(lf);
            fhand.setEncoding("UTF-8");
            chand.setEncoding("UTF-8");
            logger.addHandler(chand);
            logger.addHandler(fhand);
        }
        catch (IOException e) {
            logger.warning("Fail to initialize Logging Formats!");
        }
    }

    /**
     * Logs a console message
     * 
     * @param msg
     *            the message to be logged
     */
    public static void consoleMessage(String msg) {
        logger.log(BotLevel.CONSOLE_MESSAGE, msg);
    }

    /**
     * Logs an incoming line
     * 
     * @param msg
     *            the message to be logged
     */
    public static void incoming(String msg) {
        if (BotConfig.getDebug()) {
            logger.log(BotLevel.INCOMING, msg);
        }
    }

    /**
     * Logs an outgoing line
     * 
     * @param msg
     *            the message to be logged
     */
    public static void outgoing(String msg) {
        logger.log(BotLevel.OUTGOING, msg);
    }

    /**
     * Logs a ping
     * 
     * @param msg
     *            the message to be logged
     */
    public static void ping(String msg) {
        logger.log(BotLevel.PING, msg);
    }

    /**
     * Logs a server ping
     * 
     * @param msg
     *            the message to be logged
     */
    public static void serverPing(String msg) {
        logger.log(BotLevel.SERVER_PING, msg);
    }

    /**
     * Logs a channel message
     * 
     * @param msg
     *            the message to be logged
     */
    public static void channelMessage(String msg) {
        logger.log(BotLevel.CHANNEL_MESSAGE, msg);
    }

    /**
     * Logs a private message
     * 
     * @param msg
     *            the message to be logged
     */
    public static void privateMessage(String msg) {
        logger.log(BotLevel.PRIVATE_MESSAGE, msg);
    }

    /**
     * Logs a command
     * 
     * @param msg
     *            the message to be logged
     */
    public static void command(String msg) {
        logger.log(BotLevel.COMMAND, msg);
    }

    /**
     * Logs a notice
     * 
     * @param msg
     *            the message to be logged
     */
    public static void notice(String msg) {
        logger.log(BotLevel.NOTICE, msg);
    }

    /**
     * Logs a join event
     * 
     * @param msg
     *            the message to be logged
     */
    public static void join(String msg) {
        logger.log(BotLevel.JOIN, msg);
    }

    /**
     * Logs a part event
     * 
     * @param msg
     *            the message to be logged
     */
    public static void part(String msg) {
        logger.log(BotLevel.PART, msg);
    }

    /**
     * Logs an informational message
     * 
     * @param msg
     *            the message to be logged
     */
    public static void info(String msg) {
        logger.info(msg);
    }

    /**
     * Logs an informational message
     * 
     * @param msg
     *            the message to be logged
     * @param thrown
     *            the {@link Throwable} thrown
     */
    public static void info(String msg, Throwable thrown) {
        logger.log(Level.INFO, msg, thrown);
    }

    /**
     * Logs a warning message
     * 
     * @param msg
     *            the message to be logged
     */
    public static void warning(String msg) {
        logger.warning(msg);
    }

    /**
     * Logs an warning message
     * 
     * @param msg
     *            the message to be logged
     * @param thrown
     *            the {@link Throwable} thrown
     */
    public static void warning(String msg, Throwable thrown) {
        logger.log(Level.WARNING, msg, thrown);
    }

    /**
     * Logs a severe message
     * 
     * @param msg
     *            the message to be logged
     */
    public static void severe(String msg) {
        logger.severe(msg);
    }

    /**
     * Logs an severe message
     * 
     * @param msg
     *            the message to be logged
     * @param thrown
     *            the {@link Throwable} thrown
     */
    public static void severe(String msg, Throwable thrown) {
        logger.log(Level.SEVERE, msg, thrown);
    }

}
