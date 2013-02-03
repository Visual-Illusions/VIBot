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

import java.util.LinkedList;
import java.util.StringTokenizer;

import net.visualillusionsent.vibot.api.events.EventManager;
import net.visualillusionsent.vibot.io.irc.IRCConnection;
import net.visualillusionsent.vibot.io.irc.User;

/**
 * This class is used to process DCC events from the server.
 * <p>
 * This class is contains code derived from PircBot <br>
 * PircBot is Copyrighted: Paul James Mutton, 2001-2009, http://www.jibble.org/<br>
 * and dual Licensed under the GNU General Public License/www.jibble.org Commercial License
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 * @author Paul James Mutton (PircBot)
 */
public class DccManager {

    private IRCConnection irc_conn;
    private LinkedList<DccFileTransfer> awaitingResume = new LinkedList<DccFileTransfer>();

    /**
     * Constructs a DccManager to look after all DCC SEND and CHAT events.
     * 
     * @param irc_conn
     *            The {@link IRCConnection} for handling the DCC
     * @code.derivative PircBot
     */
    public DccManager(IRCConnection irc_conn) {
        this.irc_conn = irc_conn;
    }

    /**
     * Processes a DCC request.
     * 
     * @return True if the type of request was handled successfully.
     * @code.derivative PircBot
     */
    public boolean processRequest(User user, String request) {
        StringTokenizer tokenizer = new StringTokenizer(request);
        tokenizer.nextToken();
        String type = tokenizer.nextToken();
        String filename = tokenizer.nextToken();

        DccFileTransfer transfer = null;
        long address;
        int port;
        switch (type) {
            case "SEND":
                address = Long.parseLong(tokenizer.nextToken());
                port = Integer.parseInt(tokenizer.nextToken());
                long size = -1;
                try {
                    size = Long.parseLong(tokenizer.nextToken());
                }
                catch (Exception e) {
                    // Stick with the old value.
                }

                transfer = new DccFileTransfer(irc_conn, this, user, type, filename, address, port, size);
                EventManager.activateIncomingFileTransferEvent(transfer);
                return true;

            case "RESUME":
                port = Integer.parseInt(tokenizer.nextToken());
                long progress = Long.parseLong(tokenizer.nextToken());

                transfer = null;
                synchronized (awaitingResume) {
                    for (int index = 0; index < awaitingResume.size(); index++) {
                        transfer = awaitingResume.get(index);
                        if (transfer.getUser().equals(user) && transfer.getPort() == port) {
                            awaitingResume.remove(index);
                            break;
                        }
                    }
                }

                if (transfer != null) {
                    transfer.setProgress(progress);
                    irc_conn.sendCTCPCommand(user.getNick(), "DCC ACCEPT file.ext ".concat(String.valueOf(port)).concat(" ").concat(String.valueOf(progress)));
                }
                return true;

            case "ACCEPT":
                port = Integer.parseInt(tokenizer.nextToken());
                //long progress = Long.parseLong(tokenizer.nextToken());

                transfer = null;
                synchronized (awaitingResume) {
                    for (int index = 0; index < awaitingResume.size(); index++) {
                        transfer = awaitingResume.get(index);
                        if (transfer.getUser().equals(user) && transfer.getPort() == port) {
                            awaitingResume.remove(index);
                            break;
                        }
                    }
                }

                if (transfer != null) {
                    transfer.doReceive(transfer.getFile(), true);
                }
                return true;

            case "CHAT":
                address = Long.parseLong(tokenizer.nextToken());
                port = Integer.parseInt(tokenizer.nextToken());

                final DccChat chat = new DccChat(irc_conn, user, address, port);

                new Thread() {
                    public void run() {
                        EventManager.activateIncomingChatRequestEvent(chat);
                    }
                }.start();
            default:
                return false;
        }
    }

    /**
     * Add this DccFileTransfer to the list of those awaiting possible resuming.
     * 
     * @param transfer
     *            the DccFileTransfer that may be resumed.
     * @code.derivative PircBot
     */
    void addAwaitingResume(DccFileTransfer transfer) {
        synchronized (awaitingResume) {
            awaitingResume.add(transfer);
        }
    }

    /**
     * Remove this transfer from the list of those awaiting resuming.
     * 
     * @code.derivative PircBot
     */
    void removeAwaitingResume(DccFileTransfer transfer) {
        awaitingResume.remove(transfer);
    }
}
