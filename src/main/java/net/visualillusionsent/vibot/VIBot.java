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
package net.visualillusionsent.vibot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Handler;
import java.util.logging.Logger;

import net.visualillusionsent.utils.IPAddressUtils;
import net.visualillusionsent.utils.VersionChecker;
import net.visualillusionsent.vibot.api.plugin.BotPluginLoader;
import net.visualillusionsent.vibot.api.plugin.CommandParser;
import net.visualillusionsent.vibot.io.ConsoleCommandReceiver;
import net.visualillusionsent.vibot.io.IdentServer;
import net.visualillusionsent.vibot.io.configuration.BotConfig;
import net.visualillusionsent.vibot.io.dcc.DccChat;
import net.visualillusionsent.vibot.io.dcc.DccFileTransfer;
import net.visualillusionsent.vibot.io.exception.IRCException;
import net.visualillusionsent.vibot.io.exception.NickAlreadyInUseException;
import net.visualillusionsent.vibot.io.exception.VIBotException;
import net.visualillusionsent.vibot.io.irc.Channel;
import net.visualillusionsent.vibot.io.irc.IRCConnection;
import net.visualillusionsent.vibot.io.irc.User;
import net.visualillusionsent.vibot.io.logging.BotLogMan;

/**
 * The VIBot main class
 * <p>
 * VIBot is designed to use Java 7 or higher<br>
 * and contains a Plugin API for adding on to the Bot or to be used as just a stand-alone Channel administration solution.<br>
 * Classes and source code contained within VIBot are licensed under the GNU Lesser General Public License unless otherwise noted as being PircBot source code derivative,<br>
 * in which the licenses of PircBot are then applied.
 * <p>
 * Plugins designed for VIBot are hearby released of any licenses imposed on the VIBot sources and are the property of the Plugin Author to license as they wish.<br>
 * Visual Illusions Entertainment
 * <p>
 * This class contains code derived from PircBot<br>
 * PircBot is Copyrighted: Paul James Mutton, 2001-2009, <a href="http://www.jibble.org/">http://www.jibble.org/</a><br>
 * and dual Licensed under the <a href="http://www.gnu.org/licenses/gpl.html">GNU General Public License</a>/<a href="http://www.jibble.org/licenses/commercial-license.php">www.jibble.org Commercial License</a>
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 * @author Paul James Mutton (PircBot)
 */
public final class VIBot {
    /**
     * 
     */
    private final String finger = "Do you get off on fingering bots?!";

    /**
     * Real Name of the VIBot as represented by: VIBot v*.* Java IRC Bot
     */
    private final String real_name;

    /**
     * The current nick for the VIBot
     */
    private String nick;

    /**
     * The {@link IRCConnection} instance
     */
    private IRCConnection irc_conn;
    private InetAddress dccInetAddress;

    private static String version = null;
    private static String build = null;
    private static VIBot instance;
    private static VersionChecker vc;
    private static volatile boolean shuttingdown = false;

    private VIBot() {
        if (instance != null) {
            throw new IllegalStateException("Only one VIBot instance may be created at a time.");
        }
        this.real_name = "VIBot v".concat(getVersion()).concat(" Java IRC Bot");
    }

    /**
     * Returns the current nick of the bot.
     * <b>Note:</b> that if you have just changed your nick,
     * this method will still return the old nick until confirmation
     * of the nick change is received from the server.
     * <p>
     * The nick returned by this method is maintained only by the VIBot class and is guaranteed to be correct in the context of the IRC server.
     * 
     * @return The current nick of the bot.
     */
    public static final String getBotNick() {
        return instance.nick;
    }

    /**
     * Returns the current nick of the bot.
     * <b>Note:</b> that if you have just changed
     * your nick, this method will still return the old nick until confirmation
     * of the nick change is received from the server.
     * <p>
     * The nick returned by this method is maintained only by the VIBot class and is guaranteed to be correct in the context of the IRC server.
     * 
     * @return The current nick of the bot.
     */
    public final String getNick() {
        return nick;
    }

    /**
     * Sets the internal nick of the bot. This is only to be called by the IRCConnection
     * class in response to notification of nick changes that apply to us.
     * 
     * @param nick
     *            The new nick.
     */
    public final void setNick(String nick) {
        this.nick = nick;
    }

    public static final void changeNick(String nick) {
        if (nick == null) {
            throw new NullPointerException("Nick cannot be null");
        }
        else if (nick.trim().isEmpty()) {
            throw new IllegalArgumentException("Nick cannot be empty");
        }
        instance.nickChange(nick);
    }

    /**
     * Attempt to change the current nick (nickname) of the bot when it is
     * connected to an IRC server. After confirmation of a successful nick
     * change, the getNick method will return the new nick.
     * 
     * @param newNick
     *            The new nick to use.
     */
    private final void nickChange(String newNick) {
        irc_conn.sendRawLine("NICK ".concat(newNick));
    }

    public static final void identify() {
        instance.identify(BotConfig.getNickServPassword());
    }

    public final String getRealName() {
        return real_name;
    }

    /**
     * Gets the internal version of the VIBot.
     * 
     * @return The version of the VIBot.
     */
    public final String getVersion() {
        if (version != null) {
            return version;
        }
        return getBotVersion();
    }

    /**
     * Gets the internal finger message of the VIBot.
     * 
     * @return The finger message of the VIBot.
     */
    public final String getFinger() {
        return finger;
    }

    /**
     * Joins a {@link Channel}.
     * 
     * @param channel
     *            The name of the channel to join (eg "#vi_irc").
     */
    public static final void joinChannel(String channel) {
        instance.join(channel);
    }

    /**
     * Joins a channel that requires a key.
     * 
     * @param channel
     *            The name of the channel to join (eg "#vi_irc").
     * @param key
     *            the key to the channel
     */
    public static final void joinChannel(String channel, String key) {
        if (channel == null) {
            throw new NullPointerException("Channel cannot be null");
        }
        else if (!channel.startsWith("#")) {
            throw new IllegalArgumentException("Channel must start with #");
        }
        instance.join(channel.concat(" ").concat(key));
    }

    /**
     * Joins a channel that requires a key.
     * 
     * @param channel
     *            The name of the channel to join (eg "#vi_irc").
     * @param key
     *            the key to the channel
     */
    public final void join(String channel, String key) {
        if (channel == null) {
            throw new NullPointerException("Channel cannot be null");
        }
        else if (!channel.startsWith("#")) {
            throw new IllegalArgumentException("Channel must start with #");
        }
        else if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        join(channel.concat(" ").concat(key));
    }

    /**
     * Joins a channel.
     * 
     * @param channel
     *            The name of the channel to join (eg "#vi_irc").
     */
    public final void join(String channel) {
        if (channel == null) {
            throw new NullPointerException("Channel cannot be null");
        }
        else if (!channel.startsWith("#")) {
            throw new IllegalArgumentException("Channel must start with #");
        }
        irc_conn.sendRawLine("JOIN ".concat(channel));
    }

    /**
     * Parts a channel.
     * 
     * @param channel
     *            The name of the channel to leave.
     */
    public static void partChannel(String channel, String reason) {
        if (channel == null) {
            throw new NullPointerException("Channel cannot be null");
        }
        else if (!channel.startsWith("#")) {
            throw new IllegalArgumentException("Channel must start with '#'");
        }
        if (reason != null && !reason.trim().isEmpty()) {
            instance.part(channel, reason);
        }
        else {
            instance.part(channel, "disconnect.genericReason");
        }
    }

    /**
     * Parts a channel.
     * 
     * @param channel
     *            The name of the channel to leave.
     */
    public final void partChannel(String channel) {
        irc_conn.sendRawLine("PART ".concat(channel));
    }

    /**
     * @param channel
     * @param reason
     */
    public final void part(String channel, String reason) {
        irc_conn.sendRawLine("PART ".concat(channel).concat(" :").concat(reason));
    }

    /**
     * Sends an invitation to join a channel. Some channels can be marked as
     * "invite-only", so it may be useful to allow a bot to invite people into
     * it.
     * 
     * @param nick
     *            The nick of the user to invite
     * @param channel
     *            The channel you are inviting the user to join.
     */
    public final void sendInvite(String nick, String channel) {
        irc_conn.sendRawLine("INVITE " + nick + " :" + channel);
    }

    /**
     * The main method to start the VIBot
     * 
     * @param args
     *            currently uses no arguments
     */
    public final static void main(String[] args) {
        if (instance != null) {
            //Prevent someone from trying to break the Bot
            return;
        }
        try {
            BotLogMan.info("Visual Illusions IRC Bot starting...");
            BotLogMan.info("VIBot Version: ".concat(getBotVersion()));
            vc = new VersionChecker("VIBot", getBotVersion(), "http://visualillusionsent.net/vibot/vibot_versions.php?name=VIBot");
            if (!vc.isLatest()) {
                BotLogMan.info(vc.getUpdateAvailibleMessage());
            }
            CommandParser.getInstance();
            BotConfig.getInstance();
            BotPluginLoader.getInstance().loadPlugins();

            instance = new VIBot();
            if (BotConfig.useIdentServer()) {
                try {
                    new IdentServer(instance);
                }
                catch (IOException ioe) {
                    BotLogMan.warning("IdentServer Failed: ", ioe);
                }
            }

            // Connect to the server.
            Socket socket = new Socket(BotConfig.getServer(), BotConfig.getServerPort());
            BotLogMan.info("Connecting to server...");

            //Set the encoding and open socket
            BufferedReader breader = new BufferedReader(new InputStreamReader(socket.getInputStream(), BotConfig.getEncoding()));
            BufferedWriter bwriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), BotConfig.getEncoding()));

            instance.irc_conn = new IRCConnection(instance, socket, breader, bwriter);
            instance.irc_conn.connect();
        }
        catch (Exception e) {
            BotLogMan.severe("Unexpected exception caught: ", e);
            terminate(1);
        }
        new ConsoleCommandReceiver().start();
    }

    /**
     * Checks if VIBot is shutting down
     * 
     * @return {@code true} if shutting down, {@code false} otherwise
     */
    public final static boolean isShuttingDown() {
        return shuttingdown;
    }

    /**
     * Gets the VIBot's manifest file from the .jar
     * 
     * @return manifest of VIBot
     * @throws VIBotException
     *             if there was an issue getting the manifest
     */
    public static final Manifest getBotManifest() throws VIBotException {
        Manifest toRet = null;
        VIBotException vibe = null;
        JarFile jar = null;
        try {
            jar = new JarFile(System.getProperty("java.class.path"));
            toRet = jar.getManifest();
        }
        catch (Exception e) {
            vibe = new VIBotException("Unable to retrieve Manifest! (Missing?)", e);
        }
        finally {
            if (jar != null) {
                try {
                    jar.close();
                }
                catch (IOException e) {}
            }
            if (vibe != null) {
                throw vibe;
            }
        }
        return toRet;
    }

    /**
     * Get the VIBot version. (values specified in the manifest)
     * 
     * @return the Version of this VIBot
     */
    public static final String getBotVersion() {
        if (version != null) {
            return version;
        }
        try {
            Manifest manifest = getBotManifest();
            Attributes mainAttribs = manifest.getMainAttributes();
            version = mainAttribs.getValue("Version");
            if (version == null) {
                version = "*";
            }
        }
        catch (Exception e) {
            BotLogMan.warning(e.getMessage());
        }

        version = version.concat(".").concat(getBuild());
        if (version.equals("*.*")) {
            version = "UNDEFINED";
        }
        return version;
    }

    /**
     * Get the VIBot build number. (values specified in the manifest)
     * 
     * @return the Build number of this VIBot
     */
    public static final String getBuild() {
        if (build != null) {
            return build;
        }
        try {
            Manifest manifest = getBotManifest();
            Attributes mainAttribs = manifest.getMainAttributes();
            build = mainAttribs.getValue("Build");
            if (build == null) {
                build = "*";
            }
        }
        catch (Exception e) {
            BotLogMan.warning(e.getMessage());
        }
        return build;
    }

    public final static synchronized void terminate(int code) {
        BotLogMan.warning("VIBot shutting down...");
        shuttingdown = true;

        if (instance != null) {
            if (instance.isConnected() && BotConfig.getQuitMessage() != null) {
                instance.quitServer(BotConfig.getQuitMessage());
            }
            instance.dispose();
        }

        BotPluginLoader.getInstance().disableAll(instance);

        for (Handler hand : Logger.getLogger("VIBot").getHandlers()) {
            if (hand != null) {
                hand.close();
            }
        }
        System.out.println("VIBot is now shutdown.");
        System.exit(code);
    }

    /**
     * Reconnects to the IRC server that we were previously connected to. If
     * necessary, the appropriate port number and password will be used. This
     * method will throw an IrcException if we have never connected to an IRC
     * server previously.
     * 
     * @throws IOException
     *             if it was not possible to connect to the server.
     * @throws IrcException
     *             if the server would not let us join it.
     * @throws NickAlreadyInUseException
     *             if our nick is already in use on the server.
     * @throws VIBotException
     * @code.derivative PircBot
     */
    public final synchronized void reconnect() throws IOException, IRCException, NickAlreadyInUseException, VIBotException {
        irc_conn.connect();
    }

    /**
     * Quits from the IRC server. Providing we are actually connected to an IRC
     * server, the onDisconnect() method will be called as soon as the IRC
     * server disconnects us.
     * 
     * @code.derivative PircBot
     */
    public final void quitServer() {
        this.quitServer("Disconnecting...");
    }

    /**
     * Quits from the IRC server with a reason. Providing we are actually
     * connected to an IRC server, the onDisconnect() method will be called as
     * soon as the IRC server disconnects us.
     * 
     * @param reason
     *            The reason for quitting the server.
     * @code.derivative PircBot
     */
    public final void quitServer(String reason) {
        irc_conn.sendRawLine("QUIT :" + reason);
    }

    /**
     * Identify the bot with NickServ, supplying the appropriate password. Some
     * IRC Networks (such as freenode) require users to <i>register</i> and
     * <i>identify</i> with NickServ before they are able to send private
     * messages to other users, thus reducing the amount of spam. If you are
     * using an IRC network where this kind of policy is enforced, you will need
     * to make your bot <i>identify</i> itself to NickServ before you can send
     * private messages. Assuming you have already registered your bot's nick
     * with NickServ, this method can be used to <i>identify</i> with the
     * supplied password. It usually makes sense to identify with NickServ
     * immediately after connecting to a server.
     * <p>
     * This method issues a raw NICKSERV command to the server, and is therefore safer than the alternative approach of sending a private message to NickServ. The latter approach is considered dangerous, as it may cause you to inadvertently transmit your password to an untrusted party if you connect to a network which does not run a NickServ service and where the untrusted party has assumed the nick "NickServ". However, if your IRC network is only compatible with the private message approach,
     * you may typically identify like so:
     * 
     * <pre>
     * sendMessage(&quot;NickServ&quot;, &quot;identify PASSWORD&quot;);
     * </pre>
     * 
     * @param password
     *            The password which will be used to identify with NickServ.
     */
    private final void identify(String password) {
        irc_conn.sendRawLine("NICKSERV IDENTIFY " + password);
    }

    /**
     * Issues a request for a list of all channels on the IRC server. When the
     * VIBot receives information for each channel, it will call the
     * onChannelInfo method, which you will need to override if you want it to
     * do anything useful.
     * 
     * @see #onChannelInfo(String,int,String) onChannelInfo
     */
    public final void listChannels() {
        this.listChannels(null);
    }

    /**
     * Issues a request for a list of all channels on the IRC server. When the
     * VIBot receives information for each channel, it will call the
     * onChannelInfo method, which you will need to override if you want it to
     * do anything useful.
     * <p>
     * Some IRC servers support certain parameters for LIST requests. One example is a parameter of ">10" to list only those channels that have more than 10 users in them. Whether these parameters are supported or not will depend on the IRC server software.
     * 
     * @param parameters
     *            The parameters to supply when requesting the list.
     * @see #onChannelInfo(String,int,String) onChannelInfo
     */
    public final void listChannels(String parameters) {
        if (parameters == null) {
            irc_conn.sendRawLine("LIST");
        }
        else {
            irc_conn.sendRawLine("LIST " + parameters);
        }
    }

    /**
     * Sends a file to another user. Resuming is supported. The other user must
     * be able to connect directly to your bot to be able to receive the file.
     * <p>
     * You may throttle the speed of this file transfer by calling the setPacketDelay method on the DccFileTransfer that is returned.
     * <p>
     * This method may not be overridden.
     * 
     * @param file
     *            The file to send.
     * @param nick
     *            The user to whom the file is to be sent.
     * @param timeout
     *            The number of milliseconds to wait for the recipient to
     *            acccept the file (we recommend about 120000).
     * @return The DccFileTransfer that can be used to monitor this transfer.
     * @see DccFileTransfer
     */
    public final DccFileTransfer dccSendFile(File file, User user, int timeout) {
        DccFileTransfer transfer = new DccFileTransfer(this, irc_conn.getDCCManager(), file, user, timeout);
        transfer.doSend(true);
        return transfer;
    }

    /**
     * Attempts to establish a DCC CHAT session with a client. This method
     * issues the connection request to the client and then waits for the client
     * to respond. If the connection is successfully made, then a DccChat object
     * is returned by this method. If the connection is not made within the time
     * limit specified by the timeout value, then null is returned.
     * <p>
     * It is <b>strongly recommended</b> that you call this method within a new Thread, as it may take a long time to return.
     * <p>
     * This method may not be overridden.
     * 
     * @param nick
     *            The nick of the user we are trying to establish a chat with.
     * @param timeout
     *            The number of milliseconds to wait for the recipient to accept
     *            the chat connection (we recommend about 120000).
     * @return a DccChat object that can be used to send and recieve lines of
     *         text. Returns <b>null</b> if the connection could not be made.
     * @see DccChat
     */
    public final DccChat dccSendChatRequest(User user, int timeout) {
        DccChat chat = null;
        try {
            ServerSocket ss = null;

            int[] ports = BotConfig.getDccPorts();
            if (ports == null) {
                // Use any free port.
                ss = new ServerSocket(0);
            }
            else {
                for (int i = 0; i < ports.length; i++) {
                    try {
                        ss = new ServerSocket(ports[i]);
                        // Found a port number we could use.
                        break;
                    }
                    catch (Exception e) {
                        // Do nothing; go round and try another port.
                    }
                }
                if (ss == null) {
                    // No ports could be used.
                    throw new IOException("All ports returned by getDccPorts() are in use.");
                }
            }

            ss.setSoTimeout(timeout);
            int port = ss.getLocalPort();

            InetAddress inetAddress = getDccInetAddress();
            if (inetAddress == null) {
                inetAddress = getInetAddress();
            }
            byte[] ip = inetAddress.getAddress();
            long ipNum = IPAddressUtils.ipv4ToLong(ip);

            irc_conn.sendCTCPCommand(user.getNick(), "DCC CHAT chat " + ipNum + " " + port);

            // The client may now connect to us to chat.
            Socket socket = ss.accept();

            // Close the server socket now that we've finished with it.
            ss.close();

            chat = new DccChat(this, user, socket);
        }
        catch (Exception e) {
            // Do nothing.
        }
        return chat;
    }

    /**
     * Returns whether or not the VIBot is currently connected to a server. The
     * result of this method should only act as a rough guide, as the result may
     * not be valid by the time you act upon it.
     * 
     * @return True if and only if the VIBot is currently connected to a server.
     */
    public final synchronized boolean isConnected() {
        return irc_conn != null && irc_conn.isConnected();
    }

    /**
     * Gets the number of lines currently waiting in the outgoing message Queue.
     * If this returns 0, then the Queue is empty and any new message is likely
     * to be sent to the IRC server immediately.
     * 
     * @return The number of lines in the outgoing message Queue.
     */
    //    public final int getOutgoingQueueSize() {
    //        return outQueue.size();
    //    }

    /**
     * Returns the InetAddress used by the VIBot. This can be used to find the
     * I.P. address from which the VIBot is connected to a server.
     * 
     * @return The current local InetAddress, or null if never connected.
     */
    public InetAddress getInetAddress() {
        return irc_conn.getInetAddress();
    }

    /**
     * Sets the InetAddress to be used when sending DCC chat or file transfers.
     * This can be very useful when you are running a bot on a machine which is
     * behind a firewall and you need to tell receiving clients to connect to a
     * NAT/router, which then forwards the connection.
     * 
     * @param dccInetAddress
     *            The new InetAddress, or null to use the default.
     */
    public void setDccInetAddress(InetAddress dccInetAddress) {
        this.dccInetAddress = dccInetAddress;
    }

    /**
     * Returns the InetAddress used when sending DCC chat or file transfers. If
     * this is null, the default InetAddress will be used.
     * 
     * @return The current DCC InetAddress, or null if left as default.
     */
    public InetAddress getDccInetAddress() {
        return dccInetAddress;
    }

    /**
     * Disposes of all thread resources used by this VIBot.
     */
    public synchronized void dispose() {
        try {
            irc_conn.dispose();
        }
        catch (Exception e) {}
        instance = null;
    }

    /**
     * Returns true if and only if the object being compared is the exact same
     * instance as this VIBot. This may be useful if you are writing a multiple
     * server IRC bot that uses more than one instance of VIBot.
     * 
     * @return {@code true} if and only if Object obj is a VIBot and equal to this.
     */
    public final boolean equals(Object obj) {
        if (!(obj instanceof VIBot)) {
            return false;
        }
        VIBot other = (VIBot) obj;
        return other == this;
    }

    /**
     * Returns the hashCode of this VIBot. This method can be called by hashed
     * collection classes and is useful for managing multiple instances of
     * VIBots in such collections.
     * 
     * @return the hash code for this instance of VIBot.
     */
    public final int hashCode() {
        return super.hashCode();
    }

    /**
     * Returns a String representation of this object. You may find this useful
     * for debugging purposes, particularly if you are using more than one VIBot
     * instance to achieve multiple server connectivity. The format of this
     * String may change between different versions of VIBot but is currently
     * something of the form <br>
     * <code>
     *   VIBot[Version=x.y.z Nick=%s Connected=%b Server=%s Port=%d]
     * </code>
     * 
     * @return a String representation of this object.
     */
    public final String toString() {
        return String.format("VIBot[Version=%s Nick=%s Connected=%b Server=%s Port=%d]", getBotVersion(), nick, isConnected(), BotConfig.getServer(), BotConfig.getServerPort());
    }
}
