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
package net.visualillusionsent.vibot.io.dcc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import net.visualillusionsent.utils.IPAddressUtils;
import net.visualillusionsent.utils.UtilityException;
import net.visualillusionsent.vibot.io.irc.IRCConnection;
import net.visualillusionsent.vibot.io.irc.User;

/**
 * This class is used to allow the bot to interact with a DCC Chat session.
 * <p>
 * This class is contains code derived from PircBot <br>
 * PircBot is Copyrighted: Paul James Mutton, 2001-2009, http://www.jibble.org/<br>
 * and dual Licensed under the GNU General Public License/www.jibble.org Commercial License
 * 
 * @since VIBot 1.0
 * @author Jason Jones (darkdiplomat)
 * @author Paul James Mutton (PircBot)
 * @version 1.0
 */
public class DccChat {
    private User user;
    private BufferedReader reader;
    private BufferedWriter writer;
    private Socket socket;
    private boolean acceptable;
    private long address = 0;
    private int port = 0;

    /**
     * This constructor is used when we are accepting a DCC CHAT request from
     * somebody. It attempts to connect to the client that issued the request.
     * 
     * @param irc_conn
     *            The {@link IRCConnection} instance
     * @param user
     *            The {@link User} sending the request
     * @param address
     *            The address to connect to.
     * @param port
     *            The port number to connect to.
     * @code.derivative PircBot
     */
    DccChat(IRCConnection irc_conn, User user, long address, int port) {
        this.address = address;
        this.port = port;
        this.user = user;
        this.acceptable = true;
    }

    /**
     * This constructor is used after we have issued a DCC CHAT request to
     * somebody. If the client accepts the chat request, then the socket we
     * obtain is passed to this constructor.
     * 
     * @param irc_conn
     *            The {@link IRCConnection} instance
     * @param user
     *            The {@link User} to send the request
     * @param socket
     *            The socket which will be used for the DCC CHAT session.
     * @throws IOException
     *             If the socket cannot be read from.
     * @code.derivative PircBot
     */
    public DccChat(IRCConnection irc_conn, User user, Socket socket) throws IOException {
        this.user = user;
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.acceptable = false;
    }

    /**
     * Accept this DccChat connection.
     * 
     * @code.derivative PircBot
     */
    public synchronized void accept() throws IOException {
        if (acceptable) {
            acceptable = false;
            String ipStr = "";
            try {
                ipStr = IPAddressUtils.ipv4BytestoString(IPAddressUtils.longToIPv4(address));
            }
            catch (UtilityException e) {}
            socket = new Socket(ipStr, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        }
    }

    /**
     * Reads the next line of text from the client at the other end of our DCC
     * Chat connection. This method blocks until something can be returned. If
     * the connection has closed, null is returned.
     * 
     * @return The next line of text from the client. Returns null if the
     *         connection has closed normally.
     * @throws IOException
     *             If an I/O error occurs.
     * @code.derivative PircBot
     */
    public String readLine() throws IOException {
        if (acceptable) {
            throw new IOException("You must call the accept() method of the DccChat request before you can use it.");
        }
        return reader.readLine();
    }

    /**
     * Sends a line of text to the client at the other end of our DCC Chat
     * connection.
     * 
     * @param line
     *            The line of text to be sent. This should not include linefeed
     *            characters.
     * @throws IOException
     *             If an I/O error occurs.
     * @code.derivative PircBot
     */
    public void sendLine(String line) throws IOException {
        if (acceptable) {
            throw new IOException("You must call the accept() method of the DccChat request before you can use it.");
        }
        // No need for synchronization here really...
        writer.write(line + "\r\n");
        writer.flush();
    }

    /**
     * Closes the DCC Chat connection.
     * 
     * @throws IOException
     *             If an I/O error occurs.
     * @code.derivative PircBot
     */
    public void close() throws IOException {
        if (acceptable) {
            throw new IOException("You must call the accept() method of the DccChat request before you can use it.");
        }
        socket.close();
    }

    /**
     * Returns the User taking part in this file transfer.
     * 
     * @return the nick of the other user.
     */
    public User getUser() {
        return user;
    }

    /**
     * Returns the BufferedReader used by this DCC Chat.
     * 
     * @return the BufferedReader used by this DCC Chat.
     */
    public BufferedReader getBufferedReader() {
        return reader;
    }

    /**
     * Returns the BufferedReader used by this DCC Chat.
     * 
     * @return the BufferedReader used by this DCC Chat.
     */
    public BufferedWriter getBufferedWriter() {
        return writer;
    }

    /**
     * Returns the raw Socket used by this DCC Chat.
     * 
     * @return the raw Socket used by this DCC Chat.
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * Returns the address of the sender as a long.
     * 
     * @return the address of the sender as a long.
     */
    public long getNumericalAddress() {
        return address;
    }
}
