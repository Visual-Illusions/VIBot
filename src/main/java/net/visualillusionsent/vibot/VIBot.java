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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Handler;
import java.util.logging.Logger;

import net.visualillusionsent.utils.DateUtils;
import net.visualillusionsent.utils.IPAddressUtils;
import net.visualillusionsent.utils.UtilityException;
import net.visualillusionsent.utils.VersionChecker;
import net.visualillusionsent.vibot.commands.CommandParser;
import net.visualillusionsent.vibot.events.EventManager;
import net.visualillusionsent.vibot.io.ConsoleCommandReceiver;
import net.visualillusionsent.vibot.io.DccChat;
import net.visualillusionsent.vibot.io.DccFileTransfer;
import net.visualillusionsent.vibot.io.DccManager;
import net.visualillusionsent.vibot.io.IdentServer;
import net.visualillusionsent.vibot.io.InputThread;
import net.visualillusionsent.vibot.io.OutputThread;
import net.visualillusionsent.vibot.io.Queue;
import net.visualillusionsent.vibot.io.configuration.BotConfig;
import net.visualillusionsent.vibot.io.exception.IRCException;
import net.visualillusionsent.vibot.io.exception.NickAlreadyInUseException;
import net.visualillusionsent.vibot.io.exception.VIBotException;
import net.visualillusionsent.vibot.io.logging.BotLogMan;
import net.visualillusionsent.vibot.plugin.BotPluginLoader;

/**
 * The VIBot main class
 * <p>
 * VIBot is designed to use Java 7 or higher<br>
 * VIBot contains a Plugin API for adding on to the Bot or to be used as just a stand-alone Channel administration solution
 * <p>
 * This class is contains code derived from PircBot <br>
 * PircBot is Copyrighted: Paul James Mutton, 2001-2009, http://www.jibble.org/<br>
 * and dual Licensed under the GNU General Public License/www.jibble.org Commercial License
 * 
 * @since VIBot 1.0
 * @author Jason (darkdiplomat)
 * @author Paul James Mutton (PircBot)
 */
public final class VIBot {
    private final String version, channelPrefixes = "#", finger = "Do you get off on fingering bots?!";
    private String nick;
    private InputThread inputThread;
    private OutputThread outputThread;
    private Queue outQueue;
    private InetAddress inetAddress;
    private InetAddress dccInetAddress = null;
    private DccManager dccManager;
    private ArrayList<Channel> channels = new ArrayList<Channel>();
    private EventManager manager;
    private static String botVersion = null, build = null;
    private static VIBot instance;
    private static VersionChecker vc;
    private static volatile boolean shuttingdown = false;

    private VIBot() throws VIBotException {
        this.outQueue = new Queue();
        this.dccManager = new DccManager(this);
        this.manager = EventManager.getInstance();
        this.version = "VIBot v".concat(getBotVersion()).concat(" Java IRC Bot");
    }

    /**
     * The main method to start the VIBot
     * 
     * @param args
     *            currently uses no arguments
     */
    public static void main(String[] args) {
        BotLogMan.info("Visual Illusions IRC Bot Starting...");
        BotLogMan.info("VIBot Version: ".concat(getBotVersion()));
        vc = new VersionChecker("VIBot", getBotVersion(), "http://visualillusionsent.net/vibot/vibot_versions.php?name=VIBot");
        if (!vc.isLatest()) {
            BotLogMan.info(vc.getUpdateAvailibleMessage());
        }
        CommandParser.getInstance();
        BotConfig.getInstance();
        BotPluginLoader.getInstance().loadPlugins();
        try {
            instance = new VIBot();
            if (BotConfig.useIdentServer()) {
                try {
                    new IdentServer(instance);
                }
                catch (VIBotException vibe) {
                    BotLogMan.warning("", vibe);
                }
            }

            try {
                instance.connect();
            }
            catch (NickAlreadyInUseException naiue) {
                throw new VIBotException("The Nick was already in use...", naiue);
            }
            catch (IOException ioe) {
                throw new VIBotException("An IOException has occured... ", ioe);
            }
            catch (IRCException irce) {
                throw new VIBotException("An IRCException has occured...", irce);
            }
        }
        catch (VIBotException vibe) {
            BotLogMan.severe("", vibe);
            BotLogMan.severe("VIBot will now shut down...");
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
     * Gets an {@link List} of {@link Channel}s this VIBot is in
     * 
     * @return {@link List} of {@link Channel}s
     */
    public static List<Channel> getAllChannels() {
        return instance.getChannels();
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
        if (botVersion != null) {
            return botVersion;
        }
        try {
            Manifest manifest = getBotManifest();
            Attributes mainAttribs = manifest.getMainAttributes();
            botVersion = mainAttribs.getValue("Specification-Version");
            if (botVersion == null) {
                botVersion = "*UNKNOWN-VERSION*";
            }
        }
        catch (Exception e) {
            BotLogMan.warning(e.getMessage());
        }

        botVersion = botVersion.concat(".").concat(getBuild());
        return botVersion;
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
            build = mainAttribs.getValue("Implementation-Build");
            if (build == null) {
                build = "*UNKNOWN-BUILD*";
            }
        }
        catch (Exception e) {
            BotLogMan.warning(e.getMessage());
        }
        return build;
    }

    public final static void terminate(int code) {
        shuttingdown = true;

        if (instance != null) {
            if (instance.isConnected() && BotConfig.getQuitMessage() != null) {
                instance.quitServer(BotConfig.getQuitMessage());
            }
            instance.dispose();
        }

        for (Handler hand : Logger.getLogger("VIBot").getHandlers()) {
            if (hand != null) {
                hand.close();
            }
        }
        System.out.println("VIBot is now shutdown.");
        System.exit(code);
    }

    /**
     * Sends a message to the console
     * 
     * @param message
     */
    public static void sendConsoleMessage(String message) {
        System.out.println(message);
    }

    /**
     * Checks if a {@link Channel} is muted
     * 
     * @param channel
     *            the {@link Channel} to check
     * @return {@code true} if muted, {@code false} otherwise
     */
    public static boolean isChannelMuted(String channel) {
        Channel chan = instance.getChannel(channel);
        if (chan != null) {
            return chan.isMuted();
        }
        return false;
    }

    public static void muteChannel(String channel) {
        Channel chan = instance.getChannel(channel);
        if (chan != null && !chan.isMuted()) {
            chan.toggleMute();
        }
    }

    public static void unMuteChannel(String channel) {
        Channel chan = instance.getChannel(channel);
        if (chan != null && chan.isMuted()) {
            chan.toggleMute();
        }
    }

    public static void sendBotMessage(String target, String message) {
        instance.sendMessage(target, message);
    }

    public static void sendBotAction(String target, String action) {
        instance.sendAction(target, action);
    }

    public static String getBotNick() {
        return instance.getNick();
    }

    public static void partChannel(String channel, String reason) {
        if (!channel.startsWith("#")) {
            return;
        }
        if (reason != null && !reason.trim().isEmpty()) {
            instance.part(channel, reason);
        }
        else {
            instance.part(channel, "disconnect.genericReason");
        }
    }

    public static void changeNick(String nick) {
        if (nick == null || nick.trim().isEmpty()) {
            return;
        }
        instance.nickChange(nick);
    }

    public static void identify() {
        instance.identify(BotConfig.getNickServPassword());
    }

    /**
     * Attempt to connect to the specified IRC server using the supplied
     * password. The onConnect method is called upon success.
     * 
     * @param hostname
     *            The hostname of the server to connect to.
     * @param port
     *            The port number to connect to on the server.
     * @param password
     *            The password to use to join the server.
     * @throws IOException
     *             if it was not possible to connect to the server.
     * @throws IrcException
     *             if the server would not let us join it.
     * @throws NickAlreadyInUseException
     *             if our nick is already in use on the server.
     * @throws VIBotException
     *             if a VIBotException occurs
     */
    final synchronized void connect() throws IOException, IRCException, NickAlreadyInUseException, VIBotException {
        if (isConnected()) {
            throw new IOException("The VIBot is already connected to an IRC server.  Disconnect first.");
        }

        // Don't clear the outqueue - there might be something important in it!

        // Clear everything we may have know about channels.
        this.removeAllChannels();

        // Connect to the server.
        Socket socket = new Socket(BotConfig.getServer(), BotConfig.getServerPort());
        BotLogMan.info("Connecting to server...");

        inetAddress = socket.getLocalAddress();

        InputStreamReader inputStreamReader = null;
        OutputStreamWriter outputStreamWriter = null;

        //Set the encoding and open socket
        inputStreamReader = new InputStreamReader(socket.getInputStream(), BotConfig.getEncoding());
        outputStreamWriter = new OutputStreamWriter(socket.getOutputStream(), BotConfig.getEncoding());

        BufferedReader breader = new BufferedReader(inputStreamReader);
        BufferedWriter bwriter = new BufferedWriter(outputStreamWriter);

        // Attempt to join the server.
        if (BotConfig.getServerPassword() != null && !BotConfig.getServerPassword().isEmpty()) {
            OutputThread.sendRawLine(this, bwriter, "PASS ".concat(BotConfig.getServerPassword()));
        }

        String nick = BotConfig.getBotName();
        OutputThread.sendRawLine(this, bwriter, "NICK ".concat(nick));
        OutputThread.sendRawLine(this, bwriter, "USER " + BotConfig.getLogin() + " 8 * :" + this.getVersion());

        inputThread = new InputThread(this, socket, breader, bwriter);

        // Read stuff back from the server to see if we connected.
        String line = null;
        int tries = 1;
        while ((line = breader.readLine()) != null) {

            this.handleLine(line);

            int firstSpace = line.indexOf(" ");
            int secondSpace = line.indexOf(" ", firstSpace + 1);
            if (secondSpace >= 0) {
                String code = line.substring(firstSpace + 1, secondSpace);

                if (code.equals("004")) {
                    // We're connected to the server.
                    break;
                }
                else if (code.equals("433")) {
                    if (BotConfig.autoNickChange()) {
                        tries++;
                        nick = BotConfig.getBotName().concat("_").concat(String.valueOf(tries));
                        OutputThread.sendRawLine(this, bwriter, "NICK " + nick);
                    }
                    else {
                        socket.close();
                        inputThread = null;
                        throw new NickAlreadyInUseException(line);
                    }
                }
                else if (code.equals("439")) {
                    // No action required.
                }
                else if (code.startsWith("5") || code.startsWith("4")) {
                    socket.close();
                    inputThread = null;
                    throw new IRCException("Could not log into the IRC server: " + line);
                }
            }
            this.setNick(nick);

        }

        BotLogMan.info("Logged onto server: ".concat(BotConfig.getServer()));

        // This makes the socket timeout on read operations after 5 minutes.
        socket.setSoTimeout(300000);

        // Now start the InputThread to read all other lines from the server.
        inputThread.start();

        // Now start the outputThread that will be used to send all messages.
        if (outputThread == null) {
            outputThread = new OutputThread(this, outQueue);
            outputThread.start();
        }

        //Identify
        String nickserv_pass = BotConfig.getNickServPassword();
        if (nickserv_pass != null && !nickserv_pass.isEmpty()) {
            this.sendRawLine("NICKSERV IDENTIFY ".concat(BotConfig.getNickServPassword()));
        }

        //Join pre-defined channels
        for (String chan : BotConfig.getChannels()) {
            join(chan);
        }

        manager.callConnectEvent();
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
     */
    public final synchronized void reconnect() throws IOException, IRCException, NickAlreadyInUseException, VIBotException {
        if (BotConfig.getServer() == null) {
            throw new IRCException("Cannot reconnect to an IRC server because we were never connected to one previously!");
        }
        connect();
    }

    /**
     * Joins a channel.
     * 
     * @param channel
     *            The name of the channel to join (eg "#cs").
     */
    public static void joinChannel(String channel) {
        if (!channel.startsWith("#")) {
            return;
        }
        instance.join(channel);
    }

    final void join(String channel, String key) {
        this.join(channel.concat(" ").concat(key));
    }

    final void join(String channel) {
        this.sendRawLine("JOIN ".concat(channel));
    }

    /**
     * Parts a channel.
     * 
     * @param channel
     *            The name of the channel to leave.
     */
    public final void partChannel(String channel) {
        this.sendRawLine("PART " + channel);
    }

    final void part(String channel, String reason) {
        this.sendRawLine("PART " + channel + " :" + reason);
    }

    /**
     * Quits from the IRC server. Providing we are actually connected to an IRC
     * server, the onDisconnect() method will be called as soon as the IRC
     * server disconnects us.
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
     */
    public final void quitServer(String reason) {
        this.sendRawLine("QUIT :" + reason);
    }

    /**
     * Sends a raw line to the IRC server as soon as possible, bypassing the
     * outgoing message queue.
     * 
     * @param line
     *            The raw line to send to the IRC server.
     */
    public final synchronized void sendRawLine(String line) {
        if (isConnected()) {
            inputThread.sendRawLine(line);
        }
    }

    /**
     * Sends a raw line through the outgoing message queue.
     * 
     * @param line
     *            The raw line to send to the IRC server.
     */
    public final synchronized void sendRawLineViaQueue(String line) {
        if (line != null) {
            if (isConnected()) {
                outQueue.add(line);
            }
        }
    }

    /**
     * Sends a message to a channel or a private message to a user. These
     * messages are added to the outgoing message queue and sent at the earliest
     * possible opportunity.
     * <p>
     * Some examples: -
     * 
     * <pre>
     * // Send the message &quot;Hello!&quot; to the channel #cs.
     * sendMessage(&quot;#cs&quot;, &quot;Hello!&quot;);
     * 
     * // Send a private message to Paul that says &quot;Hi&quot;.
     * sendMessage(&quot;Paul&quot;, &quot;Hi&quot;);
     * </pre>
     * 
     * You may optionally apply colours, boldness, underlining, etc to the message by using the <code>Colors</code> class.
     * 
     * @param target
     *            The name of the channel or user nick to send to.
     * @param message
     *            The message to send.
     * @see Colors
     */
    public final void sendMessage(String target, String message) {
        outQueue.add("PRIVMSG " + target + " :" + message);
    }

    /**
     * Sends an action to the channel or to a user.
     * 
     * @param target
     *            The name of the channel or user nick to send to.
     * @param action
     *            The action to send.
     * @see Colors
     */
    public final void sendAction(String target, String action) {
        sendCTCPCommand(target, "ACTION " + action);
    }

    /**
     * Sends a notice to the channel or to a user.
     * 
     * @param target
     *            The name of the channel or user nick to send to.
     * @param notice
     *            The notice to send.
     */
    public final void sendNotice(String target, String notice) {
        outQueue.add("NOTICE " + target + " :" + notice);
    }

    /**
     * Sends a CTCP command to a channel or user. (Client to client protocol).
     * Examples of such commands are "PING <number>", "FINGER", "VERSION", etc.
     * For example, if you wish to request the version of a user called "Dave",
     * then you would call <code>sendCTCPCommand("Dave", "VERSION");</code>. The
     * type of response to such commands is largely dependant on the target
     * client software.
     * 
     * @param target
     *            The name of the channel or user to send the CTCP message to.
     * @param command
     *            The CTCP command to send.
     */
    public final void sendCTCPCommand(String target, String command) {
        outQueue.add("PRIVMSG " + target + " :\u0001" + command + "\u0001");
    }

    /**
     * Attempt to change the current nick (nickname) of the bot when it is
     * connected to an IRC server. After confirmation of a successful nick
     * change, the getNick method will return the new nick.
     * 
     * @param newNick
     *            The new nick to use.
     */
    final void nickChange(String newNick) {
        this.sendRawLine("NICK " + newNick);
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
    final void identify(String password) {
        this.sendRawLine("NICKSERV IDENTIFY " + password);
    }

    /**
     * Set the mode of a channel. This method attempts to set the mode of a
     * channel. This may require the bot to have operator status on the channel.
     * For example, if the bot has operator status, we can grant operator status
     * to "Dave" on the #cs channel by calling setMode("#cs", "+o Dave"); An
     * alternative way of doing this would be to use the op method.
     * 
     * @param channel
     *            The channel on which to perform the mode change.
     * @param mode
     *            The new mode to apply to the channel. This may include zero or
     *            more arguments if necessary.
     * @see #op(String,String) op
     */
    public final void setMode(String channel, String mode) {
        this.sendRawLine("MODE " + channel + " " + mode);
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
        this.sendRawLine("INVITE " + nick + " :" + channel);
    }

    /**
     * Bans a user from a channel. An example of a valid hostmask is
     * "*!*compu@*.18hp.net". This may be used in conjunction with the kick
     * method to permanently remove a user from a channel. Successful use of
     * this method may require the bot to have operator status itself.
     * 
     * @param channel
     *            The channel to ban the user from.
     * @param hostmask
     *            A hostmask representing the user we're banning.
     */
    public final void ban(String channel, String hostmask) {
        this.sendRawLine("MODE " + channel + " +b " + hostmask);
    }

    /**
     * Unbans a user from a channel. An example of a valid hostmask is
     * "*!*compu@*.18hp.net". Successful use of this method may require the bot
     * to have operator status itself.
     * 
     * @param channel
     *            The channel to unban the user from.
     * @param hostmask
     *            A hostmask representing the user we're unbanning.
     */
    public final void unBan(String channel, String hostmask) {
        this.sendRawLine("MODE " + channel + " -b " + hostmask);
    }

    /**
     * Grants operator privilidges to a user on a channel. Successful use of
     * this method may require the bot to have operator status itself.
     * 
     * @param channel
     *            The channel we're opping the user on.
     * @param nick
     *            The nick of the user we are opping.
     */
    public final void op(String channel, String nick) {
        this.setMode(channel, "+o " + nick);
    }

    /**
     * Removes operator privilidges from a user on a channel. Successful use of
     * this method may require the bot to have operator status itself.
     * 
     * @param channel
     *            The channel we're deopping the user on.
     * @param nick
     *            The nick of the user we are deopping.
     */
    public final void deOp(String channel, String nick) {
        this.setMode(channel, "-o " + nick);
    }

    /**
     * Grants voice privilidges to a user on a channel. Successful use of this
     * method may require the bot to have operator status itself.
     * 
     * @param channel
     *            The channel we're voicing the user on.
     * @param nick
     *            The nick of the user we are voicing.
     */
    public final void voice(String channel, String nick) {
        this.setMode(channel, "+v " + nick);
    }

    /**
     * Removes voice privilidges from a user on a channel. Successful use of
     * this method may require the bot to have operator status itself.
     * 
     * @param channel
     *            The channel we're devoicing the user on.
     * @param nick
     *            The nick of the user we are devoicing.
     */
    public final void deVoice(String channel, String nick) {
        this.setMode(channel, "-v " + nick);
    }

    /**
     * Set the topic for a channel. This method attempts to set the topic of a
     * channel. This may require the bot to have operator status if the topic is
     * protected.
     * 
     * @param channel
     *            The channel on which to perform the mode change.
     * @param topic
     *            The new topic for the channel.
     */
    public final void setTopic(String channel, String topic) {
        this.sendRawLine("TOPIC " + channel + " :" + topic);
    }

    /**
     * Kicks a user from a channel. This method attempts to kick a user from a
     * channel and may require the bot to have operator status in the channel.
     * 
     * @param channel
     *            The channel to kick the user from.
     * @param nick
     *            The nick of the user to kick.
     */
    public final void kick(String channel, String nick) {
        this.kick(channel, nick, "");
    }

    /**
     * Kicks a user from a channel, giving a reason. This method attempts to
     * kick a user from a channel and may require the bot to have operator
     * status in the channel.
     * 
     * @param channel
     *            The channel to kick the user from.
     * @param nick
     *            The nick of the user to kick.
     * @param reason
     *            A description of the reason for kicking a user.
     */
    public final void kick(String channel, String nick, String reason) {
        this.sendRawLine("KICK " + channel + " " + nick + " :" + reason);
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
            this.sendRawLine("LIST");
        }
        else {
            this.sendRawLine("LIST " + parameters);
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
        DccFileTransfer transfer = new DccFileTransfer(this, dccManager, file, user, timeout);
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

            sendCTCPCommand(user.getNick(), "DCC CHAT chat " + ipNum + " " + port);

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
     * This method handles events when any line of text arrives from the server,
     * then calling the appropriate method in the VIBot. This method is
     * protected and only called by the InputThread for this instance.
     * <p>
     * This method may not be overridden!
     * 
     * @param line
     *            The raw line of text from the server.
     * @throws VIBotException
     */
    public void handleLine(String line) throws VIBotException {
        // this.log(line);

        // Check for server pings.
        if (line.startsWith("PING ")) {
            // Respond to the ping and return immediately.
            // logger.log(BotLevel.SERVERPING, line);
            this.sendRawLine("PONG ".concat(line.substring(5)));
            return;
        }

        String sourceNick = "";
        String sourceLogin = "";
        String sourceHostname = "";

        StringTokenizer tokenizer = new StringTokenizer(line);
        String senderInfo = tokenizer.nextToken();
        String command = tokenizer.nextToken();
        String target = null;

        int exclamation = senderInfo.indexOf("!");
        int at = senderInfo.indexOf("@");
        if (senderInfo.startsWith(":")) {
            if (exclamation > 0 && at > 0 && exclamation < at) {
                sourceNick = senderInfo.substring(1, exclamation);
                sourceLogin = senderInfo.substring(exclamation + 1, at);
                sourceHostname = senderInfo.substring(at + 1);
            }
            else {

                if (tokenizer.hasMoreTokens()) {
                    String token = command;

                    int code = -1;
                    try {
                        code = Integer.parseInt(token);
                    }
                    catch (NumberFormatException e) {
                        // Keep the existing value.
                    }

                    if (code != -1) {
                        String errorStr = token;
                        String response = line.substring(line.indexOf(errorStr, senderInfo.length()) + 4, line.length());
                        this.processServerResponse(code, response);
                        // Return from the method.
                        return;
                    }
                    else {
                        // This is not a server response.
                        // It must be a nick without login and hostname.
                        // (or maybe a NOTICE or suchlike from the server)
                        sourceNick = senderInfo;
                        target = token;
                    }
                }
                else {
                    // We don't know what this line means.
                    //this.onUnknown(line);
                    // Return from the method;
                    return;
                }

            }
        }

        command = command.toUpperCase();
        if (sourceNick.startsWith(":")) {
            sourceNick = sourceNick.substring(1);
        }
        if (target == null) {
            target = tokenizer.nextToken();
        }
        if (target.startsWith(":")) {
            target = target.substring(1);
        }

        Channel channel = null;
        if (channelPrefixes.indexOf(target.charAt(0)) >= 0) {
            channel = getChannel(target);
            if (channel == null) {
                channel = new Channel(target, this);
                channels.add(channel);
            }
        }
        User user = null;
        if (channel != null) {
            user = channel.getUser(sourceNick);
        }
        // Check for CTCP requests.
        switch (command) {
            case "PRIVMSG":
                if (line.indexOf(":\u0001") > 0 && line.endsWith("\u0001")) {
                    String request = line.substring(line.indexOf(":\u0001") + 2, line.length() - 1).trim();

                    switch (request) {
                        case "VERSION":
                            // VERSION request
                            this.sendRawLine("NOTICE " + sourceNick + " :\u0001VERSION " + version + "\u0001");
                            break;
                        case "ACTION":
                            // ACTION request
                            break;
                        case "PING":
                            // PING request
                            this.sendRawLine("NOTICE " + sourceNick + " :\u0001PING " + request.substring(5) + "\u0001");
                            //this.onPing(sourceNick, sourceLogin, sourceHostname, target, request.substring(5));
                            BotLogMan.ping(request.substring(5));
                            break;
                        case "TIME":
                            // TIME request
                            this.sendRawLine("NOTICE " + sourceNick + " :\u0001TIME " + new Date().toString() + "\u0001");
                            //this.onTime(sourceNick, sourceLogin, sourceHostname, target);
                            break;
                        case "FINGER":
                            this.sendRawLine("NOTICE " + sourceNick + " :\u0001FINGER " + finger + "\u0001");
                            break;
                        default:
                            if ((tokenizer = new StringTokenizer(request)).countTokens() >= 5 && tokenizer.nextToken().equals("DCC")) {
                                // This is a DCC request.
                                user = new User("", sourceNick, this);
                                user.setHost(sourceHostname);
                                user.setLogin(sourceLogin);
                                boolean success = dccManager.processRequest(user, request);
                                if (!success) {
                                    // The DccManager didn't know what to do with the line.
                                    //this.onUnknown(line);
                                }
                            }
                            else {
                                // An unknown CTCP message - ignore it.
                                //this.onUnknown(line);
                            }
                            break;
                    }
                }
                else if (channel != null) {
                    // This is a normal message to a channel.
                    String message = line.substring(line.indexOf(" :") + 2);
                    if (message.startsWith(String.valueOf(BotConfig.getCommandPrefix()))) {
                        if ((!channel.isMuted() && !channel.isUserIgnored(user)) || (user.isBotOwner() || user.isOp())) {
                            String[] args = message.substring(1).split(" ");
                            boolean cont = CommandParser.parseBotCommand(channel, user, args);
                            BotLogMan.command(user.getNick() + (cont ? " used " : "attempted ") + " Command " + args[0]);
                        }
                    }
                    else {
                        manager.callChannelMessageEvent(channel, user, message);
                        BotLogMan.channelMessage("[" + channel.getName() + "] <" + user.getPrefix() + user.getNick() + "> " + message);
                    }
                }
                else {
                    // This is a private message to us.
                    String message = line.substring(line.indexOf(" :") + 2);
                    user = new User("", sourceNick, this);
                    if (message.startsWith(String.valueOf(BotConfig.getCommandPrefix()))) {
                        String[] args = message.substring(1).split(" ");
                        boolean cont = CommandParser.parseBotCommand(channel, user, args);
                        BotLogMan.command(user.getNick() + (cont ? " used " : "attempted ") + " Command " + args[0]);
                    }
                    else {
                        manager.callPrivateMessageEvent(channel, user, message);
                        BotLogMan.channelMessage("[PM] <" + user.getPrefix() + user.getNick() + "> " + message);
                    }
                }
                break;
            case "JOIN":
                // Someone is joining a channel.
                if (!sourceNick.equals(this.nick)) {
                    if (channel.getUser(sourceNick) == null) {
                        user = new User("", sourceNick, this);
                        channel.addUser(user);
                        manager.callJoinEvent(channel, user);
                    }
                }
                else {
                    channel.sendMessage(BotConfig.getJoinMessage());
                }
                BotLogMan.join("[" + channel.getName() + "] " + sourceNick + " has joined.");
                break;
            case "PART":
                // Someone is parting from a channel.
                if (sourceNick.equals(this.getNick())) {
                    channels.remove(channel);
                }
                else {
                    user = channel.getUser(sourceNick);
                    channel.removeUser(user);
                }
                //this.onPart(target, sourceNick, sourceLogin, sourceHostname);
                if (user != null) {
                    //BotLogMan.log(BotLevel.PART, "[" + channel.getName() + "] " + user.getNick() + " has parted.");
                }
                break;
            case "NICK":
                // Somebody is changing their nick.
                String newNick = target;
                if (sourceNick.equals(this.getNick())) {
                    // Update our nick if it was us that changed nick.
                    this.setNick(newNick);
                }
                else {
                    user = new User("", sourceNick, this);
                    this.renameUser(user, newNick);
                }
                //this.onNickChange(sourceNick, sourceLogin, sourceHostname, newNick);
                break;
            case "NOTICE":
                // Someone is sending a notice.
                //this.onNotice(sourceNick, sourceLogin, sourceHostname, target, line.substring(line.indexOf(" :") + 2));
                BotLogMan.notice(line);
                break;
            case "QUIT":
                // Someone has quit from the IRC server.
                if (line.substring(line.indexOf(" :") + 2).equals("*.net *.split")) {
                    //do nothing
                }
                else if (sourceNick.equals(this.getNick())) {
                    this.removeAllChannels();
                }
                else {
                    this.removeUserAll(sourceNick);
                }
                //this.onQuit(sourceNick, sourceLogin, sourceHostname, line.substring(line.indexOf(" :") + 2));
                break;
            case "KICK":
                // Somebody has been kicked from a channel.
                String recipient = tokenizer.nextToken();
                if (recipient.equals(this.getNick())) {
                    channels.remove(channel);
                }
                else {
                    user = channel.getUser(recipient);
                    channel.removeUser(user);
                }
                //this.onKick(target, sourceNick, sourceLogin, sourceHostname, recipient, line.substring(line.indexOf(" :") + 2));
                break;
            case "MODE":
                // Somebody is changing the mode on a channel or user.
                String mode = line.substring(line.indexOf(target, 2) + target.length() + 1);
                if (mode.startsWith(":")) {
                    mode = mode.substring(1);
                }
                this.processMode(target, sourceNick, sourceLogin, sourceHostname, mode);
                break;
            case "TOPIC":
                // Someone is changing the topic.
                Topic topic = new Topic(line.substring(line.indexOf(" :") + 2));
                try {
                    topic.setDate(DateUtils.longToDate(System.currentTimeMillis()));
                }
                catch (UtilityException e) {}

                topic.setSetBy(sourceNick);
                channel.setTopic(topic);
                break;
            case "INVITE":
                // Somebody is inviting somebody else into a channel.
                //this.onInvite(target, sourceNick, sourceLogin, sourceHostname, line.substring(line.indexOf(" :") + 2));
                break;
            default:
                // If we reach this point, then we've found something that the
                // VIBot Doesn't currently deal with.
                //this.onUnknown(line);
                break;
        }
    }

    /**
     * This method is called by the VIBot when a numeric response is received
     * from the IRC server. We use this method to allow VIBot to process various
     * responses from the server before then passing them on to the
     * onServerResponse method.
     * <p>
     * Note that this method is private and should not appear in any of the javadoc generated documenation.
     * 
     * @param code
     *            The three-digit numerical code for the response.
     * @param response
     *            The full response from the IRC server.
     */
    private final void processServerResponse(int code, String response) {
        int firstSpace = response.indexOf(' ');
        int secondSpace = response.indexOf(' ', firstSpace + 1);
        //        int thirdSpace = response.indexOf(' ', secondSpace + 1);
        int colon = response.indexOf(':');
        String channel = response.substring(firstSpace + 1, secondSpace);
        String topic = response.substring(colon + 1);
        Channel chan;
        ReplyConstants rc = ReplyConstants.fromCode(code);
        if (rc != null) {
            switch (rc) {
                case RPL_LIST:
                    // This is a bit of information about a channel.

                    //                    int userCount = 0;
                    //                    try {
                    //                        userCount = Integer.parseInt(response.substring(secondSpace + 1, thirdSpace));
                    //                    }
                    //                    catch (NumberFormatException e) {
                    //                        // Stick with the value of zero.
                    //                    }

                    //this.onChannelInfo(channel, userCount, topic);
                    break;

                case RPL_TOPIC:
                    // This is topic information about a channel we've just joined.
                    Topic to = new Topic(topic);
                    chan = getChannel(channel);
                    if (chan != null) {
                        chan.setTopic(to);
                    }
                    break;

                case RPL_TOPICINFO:
                    StringTokenizer tokenizer = new StringTokenizer(response);
                    tokenizer.nextToken();
                    channel = tokenizer.nextToken();
                    String setBy = tokenizer.nextToken();
                    long date = 0;
                    try {
                        date = Long.parseLong(tokenizer.nextToken()) * 1000;
                    }
                    catch (NumberFormatException e) {
                        // Stick with the default value of zero.
                    }

                    chan = getChannel(channel);
                    try {
                        chan.getTopic().setDate(DateUtils.longToDate(date));
                    }
                    catch (UtilityException e) {}

                    chan.getTopic().setSetBy(setBy);
                    break;
                case RPL_NAMREPLY:
                    // This is a list of nicks in a channel that we've just joined.
                    int channelEndIndex = response.indexOf(" :");
                    channel = response.substring(response.lastIndexOf(' ', channelEndIndex - 1) + 1, channelEndIndex);
                    chan = getChannel(channel);
                    if (chan == null) {
                        chan = new Channel(channel, this);
                    }
                    tokenizer = new StringTokenizer(response.substring(response.indexOf(" :") + 2));

                    while (tokenizer.hasMoreTokens()) {
                        String nick = tokenizer.nextToken();
                        String prefix = "";
                        if (nick.contains("~")) {
                            //User is an Owner of the IRC Server
                            prefix += "~";
                        }
                        if (nick.contains("&")) {
                            //User is an Admin of the IRC Server
                            prefix += "&";
                        }
                        if (nick.contains("@")) {
                            // User is an operator in this channel.
                            prefix += "@";
                        }
                        if (nick.contains("%")) {
                            // User is half-op in this channel.
                            prefix += "%";
                        }
                        if (nick.contains("+")) {
                            // User is voiced in this channel.
                            prefix += "+";
                        }

                        nick = nick.substring(prefix.length());
                        chan.addUser(new User(prefix, nick, this));
                    }
                    channels.add(chan);
                    break;
                case RPL_ENDOFNAMES:
                    // This is the end of a NAMES list, so we know that we've got
                    // the full list of users in the channel that we just joined.
                    channel = response.substring(response.indexOf(' ') + 1, response.indexOf(" :"));
                    //User[] users = this.getUsers(channel);
                    //this.onUserList(channel, users);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Called when the mode of a channel is set. We process this in order to
     * call the appropriate onOp, onDeop, etc method before finally calling the
     * override-able onMode method.
     * <p>
     * Note that this method is private and is not intended to appear in the javadoc generated documentation.
     * 
     * @param target
     *            The channel or nick that the mode operation applies to.
     * @param sourceNick
     *            The nick of the user that set the mode.
     * @param sourceLogin
     *            The login of the user that set the mode.
     * @param sourceHostname
     *            The hostname of the user that set the mode.
     * @param mode
     *            The mode that has been set.
     */
    private final void processMode(String target, String sourceNick, String sourceLogin, String sourceHostname, String mode) {

        if (channelPrefixes.indexOf(target.charAt(0)) >= 0) {
            // The mode of a channel is being changed.
            Channel channel = getChannel(target);
            StringTokenizer tok = new StringTokenizer(mode);
            String[] params = new String[tok.countTokens()];

            int t = 0;
            while (tok.hasMoreTokens()) {
                params[t] = tok.nextToken();
                t++;
            }

            char pn = ' ';
            int p = 1;

            // All of this is very large and ugly, but it's the only way of
            // providing
            // what the users want :-/
            for (int i = 0; i < params[0].length(); i++) {
                char atPos = params[0].charAt(i);

                switch (atPos) {
                    case '+':
                    case '-':
                        pn = atPos;
                        break;
                    case 'o':
                        if (pn == '+') {
                            channel.getUser(params[p]).op();
                        }
                        else {
                            channel.getUser(params[p]).deOp();
                        }
                        p++;
                        break;
                    case 'v':
                        if (pn == '+') {
                            channel.getUser(params[p]).voice();
                        }
                        else {
                            channel.getUser(params[p]).deVoice();
                        }
                        p++;
                    case 'k':
                }
                if (atPos == '+' || atPos == '-') {
                    pn = atPos;
                }
                else if (atPos == 'o') {

                }
                else if (atPos == 'v') {

                }
                else if (atPos == 'k') {
                    if (pn == '+') {
                        // onSetChannelKey(channel, sourceNick, sourceLogin,
                        // sourceHostname, params[p]);
                    }
                    else {
                        // onRemoveChannelKey(channel, sourceNick, sourceLogin,
                        // sourceHostname, params[p]);
                    }
                    p++;
                }
                else if (atPos == 'l') {
                    if (pn == '+') {
                        // onSetChannelLimit(channel, sourceNick, sourceLogin,
                        // sourceHostname, Integer.parseInt(params[p]));
                        p++;
                    }
                    else {
                        // onRemoveChannelLimit(channel, sourceNick,
                        // sourceLogin, sourceHostname);
                    }
                }
                else if (atPos == 'b') {
                    if (pn == '+') {
                        // onSetChannelBan(channel, sourceNick, sourceLogin,
                        // sourceHostname,params[p]);
                    }
                    else {
                        // onRemoveChannelBan(channel, sourceNick, sourceLogin,
                        // sourceHostname, params[p]);
                    }
                    p++;
                }
                else if (atPos == 't') {
                    if (pn == '+') {
                        // onSetTopicProtection(channel, sourceNick,
                        // sourceLogin, sourceHostname);
                    }
                    else {
                        // onRemoveTopicProtection(channel, sourceNick,
                        // sourceLogin, sourceHostname);
                    }
                }
                else if (atPos == 'n') {
                    if (pn == '+') {
                        // onSetNoExternalMessages(channel, sourceNick,
                        // sourceLogin, sourceHostname);
                    }
                    else {
                        // onRemoveNoExternalMessages(channel, sourceNick,
                        // sourceLogin, sourceHostname);
                    }
                }
                else if (atPos == 'i') {
                    if (pn == '+') {
                        // onSetInviteOnly(channel, sourceNick, sourceLogin,
                        // sourceHostname);
                    }
                    else {
                        // onRemoveInviteOnly(channel, sourceNick, sourceLogin,
                        // sourceHostname);
                    }
                }
                else if (atPos == 'm') {
                    if (pn == '+') {
                        // onSetModerated(channel, sourceNick, sourceLogin,
                        // sourceHostname);
                    }
                    else {
                        // onRemoveModerated(channel, sourceNick, sourceLogin,
                        // sourceHostname);
                    }
                }
                else if (atPos == 'p') {
                    if (pn == '+') {
                        // onSetPrivate(channel, sourceNick, sourceLogin,
                        // sourceHostname);
                    }
                    else {
                        // onRemovePrivate(channel, sourceNick, sourceLogin,
                        // sourceHostname);
                    }
                }
                else if (atPos == 's') {
                    if (pn == '+') {
                        // onSetSecret(channel, sourceNick, sourceLogin,
                        // sourceHostname);
                    }
                    else {
                        // onRemoveSecret(channel, sourceNick, sourceLogin,
                        // sourceHostname);
                    }
                }
            }
        }
        else {
            // The mode of a user is being changed.
        }
    }

    /**
     * Sets the internal nick of the bot. This is only to be called by the VIBot
     * class in response to notification of nick changes that apply to us.
     * 
     * @param nick
     *            The new nick.
     */
    private final void setNick(String nick) {
        this.nick = nick;
    }

    /**
     * Returns the current nick of the bot. Note that if you have just changed
     * your nick, this method will still return the old nick until confirmation
     * of the nick change is received from the server.
     * <p>
     * The nick returned by this method is maintained only by the VIBot class and is guaranteed to be correct in the context of the IRC server.
     * 
     * @return The current nick of the bot.
     */
    public String getNick() {
        return nick;
    }

    /**
     * Gets the internal version of the VIBot.
     * 
     * @return The version of the VIBot.
     */
    public final String getVersion() {
        return version;
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
     * Returns whether or not the VIBot is currently connected to a server. The
     * result of this method should only act as a rough guide, as the result may
     * not be valid by the time you act upon it.
     * 
     * @return True if and only if the VIBot is currently connected to a server.
     */
    public final synchronized boolean isConnected() {
        return inputThread != null && inputThread.isConnected();
    }

    /**
     * Gets the maximum length of any line that is sent via the IRC protocol.
     * The IRC RFC specifies that line lengths, including the trailing \r\n must
     * not exceed 512 bytes. Hence, there is currently no option to change this
     * value in VIBot. All lines greater than this length will be truncated
     * before being sent to the IRC server.
     * 
     * @return The maximum line length (currently fixed at 512)
     */
    public final int getMaxLineLength() {
        return InputThread.MAX_LINE_LENGTH;
    }

    /**
     * Gets the number of lines currently waiting in the outgoing message Queue.
     * If this returns 0, then the Queue is empty and any new message is likely
     * to be sent to the IRC server immediately.
     * 
     * @return The number of lines in the outgoing message Queue.
     */
    public final int getOutgoingQueueSize() {
        return outQueue.size();
    }

    /**
     * Returns the InetAddress used by the VIBot. This can be used to find the
     * I.P. address from which the VIBot is connected to a server.
     * 
     * @return The current local InetAddress, or null if never connected.
     */
    public InetAddress getInetAddress() {
        return inetAddress;
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
            inputThread.dispose();
            outputThread.dispose();
        }
        catch (Exception e) {}
        instance = null;
    }

    /**
     * Remove a user from all channels in our memory.
     */
    private final void removeUserAll(String sourceNick) {
        synchronized (channels) {
            for (Channel channel : channels) {
                User user = channel.getUser(sourceNick);
                if (user != null) {
                    channel.removeUser(user);
                }
            }
        }
    }

    /**
     * Rename a user if they appear in any of the channels we know about.
     */
    private final void renameUser(User user, String newNick) {
        synchronized (channels) {
            for (Channel channel : channels) {
                channel.getUser(user.getNick()).setNick(newNick);
            }
        }
    }

    /**
     * Removes all channels from our memory of users.
     */
    private final void removeAllChannels() {
        synchronized (channels) {
            channels = new ArrayList<Channel>();
        }
    }

    public final Channel getChannel(String chan) {
        synchronized (channels) {
            for (Channel channel : channels) {
                if (channel.equals(chan)) {
                    return channel;
                }
            }
        }
        return null;
    }

    final List<Channel> getChannels() {
        return Collections.unmodifiableList(channels);
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
