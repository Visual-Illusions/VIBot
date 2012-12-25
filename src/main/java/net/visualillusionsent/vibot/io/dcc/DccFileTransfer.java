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

import java.io.File;
import java.net.Socket;

import net.visualillusionsent.utils.TaskManager;
import net.visualillusionsent.utils.UtilityException;
import net.visualillusionsent.vibot.io.irc.IRCConnection;
import net.visualillusionsent.vibot.io.irc.User;

/**
 * This class is used to administer a DCC file transfer.
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
public class DccFileTransfer {

    /**
     * The default buffer size to use when sending and receiving files.
     */
    public static final int BUFFER_SIZE = 1024;

    private IRCConnection irc_conn;
    private DccManager manager;
    private User user;
    private String type;
    private long address;
    private int port;
    private long size;
    private boolean received;
    private Socket socket = null;
    private long progress = 0;
    private File file = null;
    private int timeout = 0;
    private boolean incoming;
    private long packetDelay = 0;
    private long startTime = 0;

    /**
     * Constructor used for receiving files.
     * 
     * @code.derivative PircBot
     */
    DccFileTransfer(IRCConnection irc_conn, DccManager manager, User user, String type, String filename, long address, int port, long size) {
        this.irc_conn = irc_conn;
        this.manager = manager;
        this.user = user;
        this.type = type;
        this.file = new File(filename);
        this.address = address;
        this.port = port;
        this.size = size;
        this.received = false;
        this.incoming = true;
    }

    /**
     * Constructor used for sending files.
     * 
     * @code.derivative PircBot
     */
    public DccFileTransfer(IRCConnection irc_conn, DccManager manager, File file, User user, int timeout) {
        this.irc_conn = irc_conn;
        this.manager = manager;
        this.user = user;
        this.file = file;
        this.size = file.length();
        this.timeout = timeout;
        this.received = true;
        this.incoming = false;
    }

    /**
     * Receives a DccFileTransfer and writes it to the specified file. Resuming
     * allows a partial download to be continue from the end of the current file
     * contents.
     * 
     * @param file
     *            The file to write to.
     * @param resume
     *            True if you wish to try and resume the download instead of
     *            overwriting an existing file.
     * @code.derivative PircBot
     */
    public synchronized void receive(File file, boolean resume) {
        if (!received) {
            received = true;
            this.file = file;

            if (type.equals("SEND") && resume) {
                progress = file.length();
                if (progress == 0) {
                    doReceive(file, false);
                }
                else {
                    irc_conn.sendCTCPCommand(user.getNick(), "DCC RESUME file.ext ".concat(String.valueOf(port)).concat(" ").concat(String.valueOf(progress)));
                    manager.addAwaitingResume(this);
                }
            }
            else {
                progress = file.length();
                doReceive(file, resume);
            }
        }
    }

    /**
     * Receive the file in a new thread.
     * 
     * @code.derivative PircBot
     */
    void doReceive(final File file, final boolean resume) {
        try {
            TaskManager.executeTask(new FileReceive(this, resume));
        }
        catch (UtilityException e) {}
    }

    /**
     * Method to send the file inside a new thread.
     * 
     * @code.derivative PircBot
     */
    public void doSend(final boolean allowResume) {
        try {
            TaskManager.executeTask(new FileSend(this, timeout, allowResume));
        }
        catch (UtilityException e) {}
    }

    /**
     * Package mutator for setting the progress of the file transfer.
     * 
     * @code.derivative PircBot
     */
    void setProgress(long progress) {
        this.progress = progress;
    }

    /**
     * Returns the {@link User} taking part in this file transfer.
     * 
     * @return the nick of the other user.
     */
    public User getUser() {
        return user;
    }

    /**
     * Returns the suggested file to be used for this transfer.
     * 
     * @return the suggested file to be used.
     */
    public File getFile() {
        return file;
    }

    /**
     * Returns the port number to be used when making the connection.
     * 
     * @return the port number.
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns true if the file transfer is incoming (somebody is sending the
     * file to us).
     * 
     * @return true if the file transfer is incoming.
     */
    public boolean isIncoming() {
        return incoming;
    }

    /**
     * Returns true if the file transfer is outgoing (we are sending the file to
     * someone).
     * 
     * @return true if the file transfer is outgoing.
     */
    public boolean isOutgoing() {
        return !isIncoming();
    }

    /**
     * Sets the delay time between sending or receiving each packet. Default is
     * 0. This is useful for throttling the speed of file transfers to maintain
     * a good quality of service for other things on the machine or network.
     * 
     * @param millis
     *            The number of milliseconds to wait between packets.
     */
    public void setPacketDelay(long millis) {
        packetDelay = millis;
    }

    /**
     * Returns the delay time between each packet that is send or received.
     * 
     * @return the delay between each packet.
     */
    public long getPacketDelay() {
        return packetDelay;
    }

    /**
     * Returns the size (in bytes) of the file being transfered.
     * 
     * @return the size of the file. Returns -1 if the sender did not specify
     *         this value.
     */
    public long getSize() {
        return size;
    }

    /**
     * Returns the progress (in bytes) of the current file transfer. When
     * resuming, this represents the total number of bytes in the file, which
     * may be greater than the amount of bytes resumed in just this transfer.
     * 
     * @return the progress of the transfer.
     */
    public long getProgress() {
        return progress;
    }

    /**
     * Returns the progress of the file transfer as a percentage. Note that this
     * should never be negative, but could become greater than 100% if you
     * attempt to resume a larger file onto a partially downloaded file that was
     * smaller.
     * 
     * @return the progress of the transfer as a percentage.
     */
    public double getProgressPercentage() {
        return 100 * (getProgress() / (double) getSize());
    }

    /**
     * Stops the DCC file transfer by closing the connection.
     */
    public void close() {
        try {
            socket.close();
        }
        catch (Exception e) {
            // Let the DCC manager worry about anything that may go wrong.
        }
    }

    /**
     * Returns the rate of data transfer in bytes per second. This value is an
     * estimate based on the number of bytes transfered since the connection was
     * established.
     * 
     * @return data transfer rate in bytes per second.
     */
    public long getTransferRate() {
        long time = (System.currentTimeMillis() - startTime) / 1000;
        if (time <= 0) {
            return 0;
        }
        return getProgress() / time;
    }

    /**
     * Returns the address of the sender as a long.
     * 
     * @return the address of the sender as a long.
     */
    public long getNumericalAddress() {
        return address;
    }

    DccManager getManager() {
        return manager;
    }

    void setSocket(Socket socket) {
        this.socket = socket;
    }

    Socket getSocket() {
        return socket;
    }

    void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    void setPort(int port) {
        this.port = port;
    }

    IRCConnection getIRCConnection() {
        return irc_conn;
    }
}
