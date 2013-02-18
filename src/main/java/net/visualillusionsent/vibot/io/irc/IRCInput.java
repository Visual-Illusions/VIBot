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

import java.io.BufferedReader;
import java.io.InterruptedIOException;

import net.visualillusionsent.vibot.io.logging.BotLogMan;

/**
 * A Thread which reads lines from the IRC server. It then passes these lines to
 * the VIBot without changing them. This running Thread also detects
 * disconnection from the server.
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 * @author Paul James Mutton (PircBot)
 */
public final class IRCInput extends Thread {
    private volatile boolean running = true;
    private final IRCConnection irc_conn;

    /**
     * The InputThread reads lines from the IRC server and allows the VIBot to
     * handle them.
     * 
     * @param irc_conn
     *            An {@link IRCConnection} instance
     */
    public IRCInput(IRCConnection irc_conn) {
        super("IRCInput-Thread");
        this.irc_conn = irc_conn;
    }

    /**
     * Called to start this Thread reading lines from the IRC server. When a
     * line is read, this method calls the handleLine method in the PircBot,
     * which may subsequently call an 'onXxx' method in the PircBot subclass. If
     * any subclass of Throwable (i.e. any Exception or Error) is thrown by your
     * method, then this method will print the stack trace to the standard
     * output. It is probable that the PircBot may still be functioning normally
     * after such a problem, but the existance of any uncaught exceptions in
     * your code is something you should really fix.
     */
    public void run() {
        try {
            while (running) {
                BufferedReader breader = irc_conn.getReader();
                try {
                    String line = null;
                    while ((line = breader.readLine()) != null) {
                        try {
                            irc_conn.handleLine(line);
                        }
                        catch (Throwable t) {
                            BotLogMan.warning("An exception occured in the IRCInput-Thread: ", t);
                        }
                    }
                    if (line == null) {
                        // The server must have disconnected us.
                        running = false;
                    }
                }
                catch (InterruptedIOException iioe) {
                    // This will happen if we haven't received anything from the
                    // server for a while.
                    // So we shall send it a ping to check that we are still
                    // connected.
                    irc_conn.sendRawLine("PING " + (System.currentTimeMillis() / 1000));
                    // Now we go back to listening for stuff from the server...
                }
            }
        }
        catch (Exception e) {
            // Do nothing.
        }

        // If we reach this point, then we must have disconnected.
        try {
            irc_conn.closeSocket();
        }
        catch (Exception e) {
            // Just assume the socket was already closed.
        }

        irc_conn.disconnected();
    }

    /**
     * Closes the socket without onDisconnect being called subsequently.
     */
    public final synchronized void dispose() {
        running = false;
        interrupt();
        try {
            irc_conn.closeSocket();
        }
        catch (Exception e) {
            // Just assume the socket was already closed.
        }
    }
}
