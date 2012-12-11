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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InterruptedIOException;
import java.net.Socket;

import net.visualillusionsent.vibot.VIBot;
import net.visualillusionsent.vibot.io.logging.BotLogMan;

/**
 * A Thread which reads lines from the IRC server. It then passes these lines to
 * the PircBot without changing them. This running Thread also detects
 * disconnection from the server and is thus used by the OutputThread to send
 * lines to the server.
 * 
 * @since VIBot 1.0
 * @author Jason (darkdiplomat)
 */
public class InputThread extends Thread {

    private VIBot bot = null;
    private Socket socket = null;
    private BufferedReader breader = null;
    private BufferedWriter bwriter = null;
    private boolean isConnected = true;
    private volatile boolean disposed = false;
    private volatile boolean running = true;

    public static final int MAX_LINE_LENGTH = 512;

    /**
     * The InputThread reads lines from the IRC server and allows the VIBot to
     * handle them.
     * 
     * @param bot
     *            An instance of the underlying VIBot.
     * @param breader
     *            The BufferedReader that reads lines from the server.
     * @param bwriter
     *            The BufferedWriter that sends lines to the server.
     */
    public InputThread(VIBot bot, Socket socket, BufferedReader breader, BufferedWriter bwriter) {
        super("InputThread-Thread");
        this.bot = bot;
        this.socket = socket;
        this.breader = breader;
        this.bwriter = bwriter;
    }

    /**
     * Sends a raw line to the IRC server as soon as possible, bypassing the
     * outgoing message queue.
     * 
     * @param line
     *            The raw line to send to the IRC server.
     */
    public void sendRawLine(String line) {
        OutputThread.sendRawLine(bot, bwriter, line);
    }

    /**
     * Returns true if this InputThread is connected to an IRC server. The
     * result of this method should only act as a rough guide, as the result may
     * not be valid by the time you act upon it.
     * 
     * @return True if still connected.
     */
    public boolean isConnected() {
        return isConnected;
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
                try {
                    String line = null;
                    while ((line = breader.readLine()) != null) {
                        try {
                            bot.handleLine(line);
                        }
                        catch (Throwable t) {
                            BotLogMan.warning("An exception occured in the InputThread: ", t);
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
                    this.sendRawLine("PING " + (System.currentTimeMillis() / 1000));
                    // Now we go back to listening for stuff from the server...
                }
            }
        }
        catch (Exception e) {
            // Do nothing.
        }

        // If we reach this point, then we must have disconnected.
        try {
            socket.close();
        }
        catch (Exception e) {
            // Just assume the socket was already closed.
        }

        disconnected();
    }

    private void disconnected() {
        if (!disposed) {
            BotLogMan.warning("Disconnected from server...");
            isConnected = false;
            new ReconnectionThread(bot).start();
        }
    }

    /**
     * Closes the socket without onDisconnect being called subsequently.
     */
    public void dispose() {
        disposed = true;
        running = false;
        interrupt();
        try {
            socket.close();
        }
        catch (Exception e) {
            // Just assume the socket was already closed.
        }
    }
}
