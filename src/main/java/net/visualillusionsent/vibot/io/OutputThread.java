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
 *
 * Parts of this file are derived from PircBot
 * Copyright Paul James Mutton, 2001-2009, http://www.jibble.org/
 *
 * PircBot is dual-licensed, allowing you to choose between the GNU
 * General Public License (GPL) and the www.jibble.org Commercial License.
 * Since the GPL may be too restrictive for use in a proprietary application,
 * a commercial license is also provided. Full license information can be
 * found at http://www.jibble.org/licenses/
 */
package net.visualillusionsent.vibot.io;

import java.io.BufferedWriter;

import net.visualillusionsent.vibot.VIBot;
import net.visualillusionsent.vibot.io.configuration.BotConfig;
import net.visualillusionsent.vibot.io.logging.BotLogMan;

/**
 * A Thread which is responsible for sending messages to the IRC server.
 * Messages are obtained from the outgoing message queue and sent immediately if
 * possible. If there is a flood of messages, then to avoid getting kicked from
 * a channel, we put a small delay between each one.
 * 
 * @since VIBot 1.0
 * @author Jason (darkdiplomat)
 */
public class OutputThread extends Thread {

    private VIBot bot = null;
    private Queue outQueue = null;

    /**
     * Constructs an OutputThread for the underlying VIBot. All messages sent
     * to the IRC server are sent by this OutputThread to avoid hammering the
     * server. Messages are sent immediately if possible. If there are multiple
     * messages queued, then there is a delay imposed.
     * 
     * @param bot
     *            The underlying VIBot instance.
     * @param outQueue
     *            The Queue from which we will obtain our messages.
     */
    public OutputThread(VIBot bot, Queue outQueue) {
        super("OutputThread-Thread");
        this.bot = bot;
        this.outQueue = outQueue;
    }

    /**
     * A static method to write a line to a BufferedOutputStream and then pass
     * the line to the log method of the supplied PircBot instance.
     * 
     * @param bot
     *            The underlying VIBot instance.
     * @param out
     *            The BufferedOutputStream to write to.
     * @param line
     *            The line to be written. "\r\n" is appended to the end.
     * @param encoding
     *            The charset to use when encoing this string into a byte array.
     */
    public static void sendRawLine(VIBot bot, BufferedWriter bwriter, String line) {
        if (line.length() > bot.getMaxLineLength() - 2) {
            line = line.substring(0, bot.getMaxLineLength() - 2);
        }
        synchronized (bwriter) {
            try {
                bwriter.write(line.concat("\r\n"));
                bwriter.flush();
                if (!line.startsWith("PONG :")) {
                    BotLogMan.outgoing(line);
                }
            }
            catch (Exception e) {
                // Silent response - just lose the line.
            }
        }
    }

    /**
     * This method starts the Thread consuming from the outgoing message Queue
     * and sending lines to the server.
     */
    public void run() {
        try {
            while (true) {
                // Small delay to prevent spamming of the channel
                sleep(BotConfig.getMessageDelay());

                String line = outQueue.next();
                if (line != null) {
                    bot.sendRawLine(line);
                }
                else {
                    break;
                }
            }
        }
        catch (InterruptedException e) {
            // Just let the method return naturally...
        }
    }

    public void dispose() {
        this.interrupt();
    }
}
