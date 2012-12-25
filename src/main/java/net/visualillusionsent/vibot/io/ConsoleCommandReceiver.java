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
package net.visualillusionsent.vibot.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import net.visualillusionsent.vibot.CommandParser;
import net.visualillusionsent.vibot.VIBot;
import net.visualillusionsent.vibot.io.exception.VIBotException;
import net.visualillusionsent.vibot.io.irc.Channel;
import net.visualillusionsent.vibot.io.irc.User;
import net.visualillusionsent.vibot.io.logging.BotLogMan;

/**
 * Listens for commands/messages given via the Console/Terminal
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
public final class ConsoleCommandReceiver extends Thread {
    /**
     * System.in BufferedReader
     */
    private final BufferedReader consoleread;

    /**
     * Constructs a new {@code ConsoleCommandReceiver}
     */
    public ConsoleCommandReceiver() {
        super("ConsoleCommandReceiver-Thread");
        this.setDaemon(true);
        consoleread = new BufferedReader(new InputStreamReader(System.in));
    }

    /**
     * Runs to listen for input
     */
    public void run() {
        while (!VIBot.isShuttingDown()) {
            String inLine = null;
            try {
                while ((inLine = consoleread.readLine()) != null) {
                    if (inLine.isEmpty() || !VIBot.isConnected()) {
                        System.out.println("");
                        continue;
                    }

                    String[] args = inLine.split(" ");
                    if (!CommandParser.parseBotCommand(Channel.CONSOLE, User.BOT_CONSOLE, args)) {
                        String chan = inLine.split(" ")[0];
                        if (chan.startsWith("#")) {
                            if (inLine.length() > (chan.length() + 1)) {
                                //VIBot.sendBotMessage(chan, inLine.substring(chan.length() + 1));
                                //TODO???
                            }
                        }
                    }
                }
            }
            catch (IOException e) {}
            catch (VIBotException vibe) {
                BotLogMan.warning("An exception occured in the ConsoleCommandReceiver-Thread: ", vibe);
            }
        }
        try {
            consoleread.close();
        }
        catch (IOException e) {}
    }
}
