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
package net.visualillusionsent.vibot.io.irc;

import net.visualillusionsent.vibot.io.configuration.BotConfig;

/**
 * A Thread which is responsible for sending messages to the IRC server.
 * Messages are obtained from the outgoing message queue and sent immediately if
 * possible. If there is a flood of messages, then to avoid getting kicked from
 * a channel, we put a small delay between each one.
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 * @author Paul James Mutton (PircBot)
 */
public class IRCOutput extends Thread {
    private final IRCConnection irc_conn;

    /**
     * Constructs an OutputThread for the underlying VIBot. All messages sent
     * to the IRC server are sent by this OutputThread to avoid hammering the
     * server. Messages are sent immediately if possible. If there are multiple
     * messages queued, then there is a delay imposed.
     * 
     * @param irc_conn
     *            The {@link IRCConnection} instance
     */
    public IRCOutput(IRCConnection irc_conn) {
        super("IRCOutput-Thread");
        this.irc_conn = irc_conn;
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

                String line = irc_conn.getQueueNext();
                if (line != null) {
                    irc_conn.sendRawLine(line);
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

    /**
     * Disposes of the IRCOutputThread
     */
    public final synchronized void dispose() {
        interrupt();
    }
}
