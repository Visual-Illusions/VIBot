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

public final class BotLogMan {
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

    public static void consoleMessage(String msg) {
        logger.log(BotLevel.CONSOLE_MESSAGE, msg);
    }

    public static void unknownIncoming(String msg) {
        logger.log(BotLevel.INCOMING, msg);
    }

    public static void outgoing(String msg) {
        logger.log(BotLevel.OUTGOING, msg);
    }

    public static void ping(String msg) {
        logger.log(BotLevel.PING, msg);
    }

    public static void serverPing(String msg) {
        logger.log(BotLevel.SERVER_PING, msg);
    }

    public static void channelMessage(String msg) {
        logger.log(BotLevel.CHANNEL_MESSAGE, msg);
    }

    public static void privateMessage(String msg) {
        logger.log(BotLevel.PRIVATE_MESSAGE, msg);
    }

    public static void command(String msg) {
        logger.log(BotLevel.COMMAND, msg);
    }

    public static void notice(String msg) {
        logger.log(BotLevel.NOTICE, msg);
    }

    public static void join(String msg) {
        logger.log(BotLevel.JOIN, msg);
    }

    public static void part(String msg) {
        logger.log(BotLevel.PART, msg);
    }

    public static void info(String msg) {
        logger.info(msg);
    }

    public static void info(String msg, Throwable thrown) {
        logger.log(Level.INFO, msg, thrown);
    }

    public static void warning(String msg) {
        logger.warning(msg);
    }

    public static void warning(String msg, Throwable thrown) {
        logger.log(Level.WARNING, msg, thrown);
    }

    public static void severe(String msg) {
        logger.severe(msg);
    }

    public static void severe(String msg, Throwable thrown) {
        logger.log(Level.SEVERE, msg, thrown);
    }

}
