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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Handler;
import java.util.logging.Logger;

import net.visualillusionsent.utils.VersionChecker;
import net.visualillusionsent.vibot.api.plugin.BotPlugin;
import net.visualillusionsent.vibot.api.plugin.BotPluginLoader;
import net.visualillusionsent.vibot.io.ConsoleCommandReceiver;
import net.visualillusionsent.vibot.io.IdentServer;
import net.visualillusionsent.vibot.io.configuration.BotConfig;
import net.visualillusionsent.vibot.io.exception.IRCException;
import net.visualillusionsent.vibot.io.exception.NickAlreadyInUseException;
import net.visualillusionsent.vibot.io.exception.VIBotException;
import net.visualillusionsent.vibot.io.irc.Channel;
import net.visualillusionsent.vibot.io.irc.IRCConnection;
import net.visualillusionsent.vibot.io.logging.BotLogMan;

/**
 * The VIBot main class
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 * @author Paul James Mutton (PircBot)
 */
public final class VIBot {

    /**
     * IRC Finger response
     */
    private final String finger = "Do you get off on fingering bots?!";

    /**
     * Real Name of the {@code VIBot} as represented by: VIBot v{Major}.{Minor}.{Build} Java IRC Bot
     */
    private final String real_name;

    /**
     * The current nick for the {@code VIBot}
     */
    private String nick;

    /**
     * The {@link IRCConnection} instance
     */
    private IRCConnection irc_conn;

    /**
     * {@code VIBot} Version as Major.Minor.Build or UNDEFINED if the Manifest was missing
     */
    private static String vb;

    private static String version;

    private static String build;

    /**
     * {@code VIBot} running instance
     */
    private static VIBot instance;

    /**
     * {@link VersionChecker} instance
     */
    private static VersionChecker vc;

    /**
     * Tells whether {@code VIBot} is shutting down or not
     */
    private static volatile boolean shuttingdown = false;

    private static volatile boolean isLaunched = false;

    static final FakePlugin FAKE_PLUGIN;

    static {
        instance = new VIBot();
        FAKE_PLUGIN = new FakePlugin(instance);
    }

    /**
     * Constructs a new {@code VIBot} object<br>
     * This Class should not be externally constructed
     */
    private VIBot() {
        if (instance != null) {
            throw new IllegalStateException("Only one VIBot instance may be created at a time.");
        }
        this.real_name = "VIBot v".concat(getVersionBuild()).concat(" Visual Illusions Java IRC Bot");
    }

    public static final Class<?> checkFake() {
        return FakePlugin.class;
    }

    public static final boolean fakeEqualsFake(BotPlugin fake) {
        return FAKE_PLUGIN == fake;
    }

    /**
     * Returns the current nick of the {@code VIBot}.
     * <b>Note:</b> that if you have just changed your nick,
     * this method will still return the old nick until confirmation
     * of the nick change is received from the server.
     * <p>
     * The nick returned by this method is maintained only by the {@code VIBot} class and is guaranteed to be correct in the context of the IRC server.
     * 
     * @return The current nick of the {@code VIBot}.
     */
    public static final String getBotNick() {
        return instance.nick;
    }

    /**
     * Returns the current nick of the {@code VIBot}.
     * <b>Note:</b> that if you have just changed your nick,
     * this method will still return the old nick until confirmation
     * of the nick change is received from the server.
     * <p>
     * The nick returned by this method is maintained only by the {@code VIBot} class and is guaranteed to be correct in the context of the IRC server.
     * 
     * @return The current nick of the {@code VIBot}.
     */
    public final String getNick() {
        return nick;
    }

    /**
     * Sets the internal nick of the bot. This is only to be called by the {@link IRCConnection} class in response to notification of nick changes that apply to us.
     * 
     * @param nick
     *            The new nick.
     * @throws SecurityException
     *             if something calls this method other than the {@link IRCConnection} instance created within {@code VIBot}
     */
    public final void setNick(String nick, IRCConnection irc_conn) {
        if (irc_conn == null || irc_conn != this.irc_conn) {
            throw new SecurityException("setNick should only be called from within IRCConnection");
        }
        this.nick = nick;
    }

    /**
     * Attempt to change the current nick (nickname) of the bot when it is
     * connected to an IRC server. After confirmation of a successful nick
     * change, the getNick method will return the new nick.
     * 
     * @param newNick
     *            The new nick to use.
     */
    public static final void changeBotNick(String newNick) {
        if (newNick == null) {
            throw new NullPointerException("Nick cannot be null");
        }
        else if (newNick.trim().isEmpty()) {
            throw new IllegalArgumentException("Nick cannot be empty");
        }
        instance.changeNick(newNick);
    }

    /**
     * Attempt to change the current nick (nickname) of the bot when it is
     * connected to an IRC server. After confirmation of a successful nick
     * change, the getNick method will return the new nick.
     * 
     * @param newNick
     *            The new nick to use.
     */
    private final void changeNick(String newNick) {
        irc_conn.sendRawLine("NICK ".concat(newNick));
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
     */
    public static final void identify() {
        instance.identify(BotConfig.getNickServPassword());
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
        irc_conn.sendRawLine("NICKSERV IDENTIFY ".concat(password));
    }

    /**
     * Gets the Real Name of the {@code VIBot}
     * 
     * @return the real name of the {@code VIBot}
     */
    public static final String getBotRealName() {
        return instance.real_name;
    }

    /**
     * Gets the Real Name of the {@code VIBot}
     * 
     * @return the real name of the {@code VIBot}
     */
    public final String getRealName() {
        return real_name;
    }

    /**
     * Gets the internal version of the {@code VIBot}.
     * 
     * @return The version of the {@code VIBot}.
     */
    public final String getVersion() {
        if (version != null) {
            return version;
        }
        return getBotVersion();
    }

    /**
     * Gets the internal build of the {@code VIBot}.
     * 
     * @return The build of the {@code VIBot}.
     */
    public final String getBuild() {
        if (build != null) {
            return build;
        }
        return getBotBuild();
    }

    /**
     * Gets the internal version and build of the {@code VIBot} formated as Major.Minor.Build
     * 
     * @return The version of the {@code VIBot}.
     */
    public final String getVersionBuild() {
        if (vb != null) {
            return vb;
        }
        return getBotVersionBuild();
    }

    /**
     * Gets the internal finger message of the {@code VIBot}.
     * 
     * @return The finger message of the {@code VIBot}.
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
     * Joins a {@link Channel}.
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
     * Parts a {@link Channel}.
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
     * Parts a {@link Channel}.
     * 
     * @param channel
     *            The name of the channel to leave.
     */
    public final void partChannel(String channel) {
        irc_conn.sendRawLine("PART ".concat(channel));
    }

    /**
     * Parts a {@link Channel} with a reason.
     * 
     * @param channel
     *            The name of the channel to leave.
     * @param reason
     *            The reason for leaving.
     */
    public final void part(String channel, String reason) {
        irc_conn.sendRawLine("PART ".concat(channel).concat(" :").concat(reason));
    }

    public static final void inviteUser(Channel channel, String nick) {
        instance.sendInvite(channel, nick);
    }

    /**
     * Sends an invitation to join a channel. Some channels can be marked as
     * "invite-only", so it may be useful to allow a bot to invite people into
     * it.
     * 
     * @param channel
     *            The channel you are inviting the user to join.
     * @param nick
     *            The nick of the user to invite
     */
    public final void sendInvite(Channel channel, String nick) {
        irc_conn.sendRawLine("INVITE ".concat(nick).concat(" :").concat(channel.getName()));
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
        if (isConnected()) {
            return;
        }

        instance.irc_conn.reconnect();
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
        irc_conn.sendRawLine("QUIT :".concat(reason));
    }

    /**
     * Issues a request for a list of all channels on the IRC server. When the
     * VIBot receives information for each channel, it will call the
     * onChannelInfo method, which you will need to override if you want it to
     * do anything useful.
     */
    public final void listChannels() {
        irc_conn.sendRawLine("LIST");
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
     */
    public final void listChannels(String parameters) {
        if (parameters == null) {
            irc_conn.sendRawLine("LIST");
        }
        else {
            irc_conn.sendRawLine("LIST ".concat(parameters));
        }
    }

    /**
     * Returns whether or not the VIBot is currently connected to a server. The
     * result of this method should only act as a rough guide, as the result may
     * not be valid by the time you act upon it.
     * 
     * @return True if and only if the VIBot is currently connected to a server.
     */
    public final static synchronized boolean isConnected() {
        return instance != null && instance.irc_conn != null && instance.irc_conn.isConnected();
    }

    /**
     * Terminates the VIBot
     * 
     * @param code
     *            The System Exit code to use
     */
    public final static synchronized void terminate(String quitMessage, int code) {
        BotLogMan.warning("VIBot shutting down...");
        shuttingdown = true;

        if (instance != null) {
            if (isConnected()) {
                if (quitMessage != null) {
                    instance.quitServer(quitMessage);
                }
                else if (BotConfig.getQuitMessage() != null) {
                    instance.quitServer(BotConfig.getQuitMessage());
                }
                else {
                    instance.quitServer();
                }
            }
            instance.dispose();
        }

        BotPluginLoader.disableAllBotPlugins(instance);

        for (Handler hand : Logger.getLogger("VIBot").getHandlers()) {
            if (hand != null) {
                hand.close();
            }
        }
        System.out.println("VIBot is now shutdown.");
        System.exit(code);
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
                version = "UNDEFINED";
            }
        }
        catch (Exception e) {
            BotLogMan.warning(e.getMessage());
            version = "UNDEFINED";
        }

        return version;
    }

    /**
     * Get the VIBot build. (values specified in the manifest)
     * 
     * @return the build of this VIBot
     */
    public static final String getBotBuild() {
        if (build != null) {
            return build;
        }
        try {
            Manifest manifest = getBotManifest();
            Attributes mainAttribs = manifest.getMainAttributes();
            build = mainAttribs.getValue("Build");
            if (build == null) {
                build = "UNDEFINED";
            }
        }
        catch (Exception e) {
            BotLogMan.warning(e.getMessage());
            build = "UNDEFINED";
        }

        return build;
    }

    /**
     * Get the VIBot Version and Build as Major.Minor.Build (values specified in the manifest)
     * 
     * @return the version and build of this VIBot
     */
    public static final String getBotVersionBuild() {
        if (vb != null) {
            return vb;
        }
        if (version == null) {
            getBotVersion();
        }
        if (build == null) {
            getBotBuild();
        }
        vb = version + "." + build;
        return vb;
    }

    /**
     * The main method to start the VIBot
     * 
     * @param args
     *            no-gui: disables the gui
     */
    public final static void main(String[] args) {
        if (isLaunched) {
            return;
        }
        isLaunched = true;
        try {
            BotLogMan.info("Visual Illusions IRC Bot starting...");
            BotLogMan.info("VIBot Version: ".concat(getBotVersionBuild()));
            vc = new VersionChecker("VIBot", version, build, "http://visualillusionsent.net/vibot/vibot_versions.php?name=VIBot", true, false);
            if (!vc.isLatest()) {
                BotLogMan.info(vc.getUpdateAvailibleMessage());
            }
            CommandParser.getInstance();
            BotConfig.getInstance();
            BotPluginLoader.loadPlugins();
            new ConsoleCommandReceiver().start();

            //instance = new VIBot();
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
            terminate("Error Code 1", 1);
        }
    }
}
