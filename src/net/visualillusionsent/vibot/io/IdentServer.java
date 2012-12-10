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
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import net.visualillusionsent.vibot.VIBot;
import net.visualillusionsent.vibot.io.configuration.BotConfig;
import net.visualillusionsent.vibot.io.exception.VIBotException;
import net.visualillusionsent.vibot.io.logging.BotLogMan;

/**
 * A simple IdentServer (also know as "The Identification Protocol"). An ident
 * server provides a means to determine the identity of a user of a particular
 * TCP connection.
 * <p>
 * Most IRC servers attempt to contact the ident server on connecting hosts in order to determine the user's identity. A few IRC servers will not allow you to connect unless this information is provided.
 * <p>
 * So when a VIBot is run on a machine that does not run an ident server, it may be necessary to provide a "faked" response by starting up its own ident server and sending out apparently correct responses.
 * 
 * @since VIBot 1.0
 * @author Jason Jones (darkdiplomat)
 */
public class IdentServer extends Thread {
    private ServerSocket ss = null;

    /**
     * Constructs and starts an instance of an IdentServer that will respond to
     * a client with the provided login.
     * <p>
     * The ident server will wait for up to 120 seconds before shutting down. Otherwise, it will shut down as soon as it has responded to an ident request.
     * 
     * @param bot
     *            The VIBot instance that will be used to log to.
     * @throws VIBotException
     */
    public IdentServer(VIBot bot) throws VIBotException {
        super("IdentServer-Thread");
        int port = BotConfig.getIdentPort();
        try {
            ss = new ServerSocket(port);
            ss.setSoTimeout(120000);
        }
        catch (Exception e) {
            throw new VIBotException("Could not start the ident server on port ".concat(String.valueOf(port)).concat("."), e);
        }

        BotLogMan.info("Ident server running on port ".concat(String.valueOf(BotConfig.getIdentPort())).concat(" for the next 120 seconds..."));
        this.start();
    }

    /**
     * Waits for a client to connect to the ident server before making an
     * appropriate response. Note that this method is started by the class
     * constructor.
     */
    public void run() {
        try {
            Socket socket = ss.accept();
            socket.setSoTimeout(120000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            String line = reader.readLine();
            while (line != null) {
                BotLogMan.info("Ident request received: " + line);
                line = line.concat(" : USERID : UNIX : ").concat(BotConfig.getLogin());
                writer.write(line.concat("\r\n"));
                writer.flush();
                BotLogMan.info("Ident reply sent: ".concat(line));
                writer.close();
                break;
            }
        }
        catch (Exception e) {
            // We're not really concerned with what went wrong, are we?
        }

        try {
            ss.close();
        }
        catch (Exception e) {
            // Doesn't really matter...
        }

        BotLogMan.info("The Ident server has been shut down.");
    }
}
