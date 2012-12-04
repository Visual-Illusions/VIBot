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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Handler;
import java.util.logging.Logger;

import net.visualillusionsent.vibot.commands.CommandParser;
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
import net.visualillusionsent.vibot.plugin.hook.HookManager;

/**
 * The VIBot main class
 * <p>
 * VIBot is designed to use Java 7 or higher<br>
 * VIBot is designed using PircBot as a reference<br>
 * VIBot contains a Plugin API for adding on to the Bot or to be used as just a stand-alone Channel administration solution
 * 
 * @since VIBot 1.0
 * @author Jason (darkdiplomat)
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
    private Hashtable<String, String> topics = new Hashtable<String, String>();
    private int[] dccPorts = null;
    private HookManager hm;
    private static String botVersion = null, build = null;
    private static VIBot instance;
    private static volatile boolean shuttingdown = false;

    private VIBot() throws VIBotException {
        this.outQueue = new Queue();
        this.dccManager = new DccManager(this);
        this.hm = HookManager.getInstance();

        this.version = "VIBot v" + getBotVersion() + " Java IRC Bot - http://visualillusionsent.net";
    }

    /**
     * The main method to start the VIBot
     * 
     * @param args
     *            currently takes 0 arguments
     */
    public static void main(String[] args) {
        BotLogMan.info("Visual Illusions IRC Bot Starting...");
        BotLogMan.info("VIBot Version: ".concat(getBotVersion()));
        BotLogMan.info("VIMod Build: ".concat(getBuild()));
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

    public static final Manifest getBotManifest() throws VIBotException {
        try {
            URL url = Thread.currentThread().getContextClassLoader().getResource(JarFile.MANIFEST_NAME);
            return new Manifest(url.openStream());
        }
        catch (Exception e) {
            throw new VIBotException("Unable to retrieve Manifest! (Missing?)", e);
        }
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

    public static void joinChannel(String channel) {
        if (!channel.startsWith("#")) {
            return;
        }
        instance.join(channel);
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

        this.onConnect();

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

    final void join(String channel) {
        this.sendRawLine("JOIN " + channel);
    }

    /**
     * Joins a channel with a key.
     * 
     * @param channel
     *            The name of the channel to join (eg "#cs").
     * @param key
     *            The key that will be used to join the channel.
     */
    final void join(String channel, String key) {
        this.join(channel + " " + key);
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
    public final DccFileTransfer dccSendFile(File file, String nick, int timeout) {
        DccFileTransfer transfer = new DccFileTransfer(this, dccManager, file, nick, timeout);
        transfer.doSend(true);
        return transfer;
    }

    /**
     * Receives a file that is being sent to us by a DCC SEND request. Please
     * use the onIncomingFileTransfer method to receive files.
     * 
     * @deprecated As of VIBot 1.2.0, use {@link #onIncomingFileTransfer(DccFileTransfer)}
     */
    protected final void dccReceiveFile(File file, long address, int port, int size) {
        throw new RuntimeException("dccReceiveFile is deprecated, please use sendFile");
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
     * @since VIBot 0.9.8
     * @param nick
     *            The nick of the user we are trying to establish a chat with.
     * @param timeout
     *            The number of milliseconds to wait for the recipient to accept
     *            the chat connection (we recommend about 120000).
     * @return a DccChat object that can be used to send and recieve lines of
     *         text. Returns <b>null</b> if the connection could not be made.
     * @see DccChat
     */
    public final DccChat dccSendChatRequest(String nick, int timeout) {
        DccChat chat = null;
        try {
            ServerSocket ss = null;

            int[] ports = getDccPorts();
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
            long ipNum = ipToLong(ip);

            sendCTCPCommand(nick, "DCC CHAT chat " + ipNum + " " + port);

            // The client may now connect to us to chat.
            Socket socket = ss.accept();

            // Close the server socket now that we've finished with it.
            ss.close();

            chat = new DccChat(this, nick, socket);
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
            this.onServerPing(line.substring(5));
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
                    this.onUnknown(line);
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
                            // ACTION request, dropped
                            break;
                        case "PING":
                            // PING request
                            this.sendRawLine("NOTICE " + sourceNick + " :\u0001PING " + request.substring(5) + "\u0001");
                            this.onPing(sourceNick, sourceLogin, sourceHostname, target, request.substring(5));
                            BotLogMan.ping(request.substring(5));
                            break;
                        case "TIME":
                            // TIME request
                            this.sendRawLine("NOTICE " + sourceNick + " :\u0001TIME " + new Date().toString() + "\u0001");
                            this.onTime(sourceNick, sourceLogin, sourceHostname, target);
                            break;
                        case "FINGER":
                            this.sendRawLine("NOTICE " + sourceNick + " :\u0001FINGER " + finger + "\u0001");
                            break;
                        default:
                            if ((tokenizer = new StringTokenizer(request)).countTokens() >= 5 && tokenizer.nextToken().equals("DCC")) {
                                // This is a DCC request.
                                boolean success = dccManager.processRequest(sourceNick, sourceLogin, sourceHostname, request);
                                if (!success) {
                                    // The DccManager didn't know what to do with the line.
                                    this.onUnknown(line);
                                }
                            }
                            else {
                                // An unknown CTCP message - ignore it.
                                this.onUnknown(line);
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
                            System.out.println(channel + " " + user);
                            boolean cont = CommandParser.parseBotCommand(channel, user, args);
                            BotLogMan.command(user.getNick() + (cont ? " used " : "attempted ") + " Command " + args[0]);
                        }
                    }
                    else {
                        hm.callMessageHook(channel, user, message);
                        BotLogMan.channelMessage("[" + channel.getName() + "] <" + user.getPrefix() + user.getNick() + "> " + message);
                    }
                }
                else {
                    // This is a private message to us.
                    String message = line.substring(line.indexOf(" :") + 2);
                    user = new User("", sourceNick);
                    if (message.startsWith(String.valueOf(BotConfig.getCommandPrefix()))) {
                        String[] args = message.substring(1).split(" ");
                        boolean cont = CommandParser.parseBotCommand(channel, user, args);
                        BotLogMan.command(user.getNick() + (cont ? " used " : "attempted ") + " Command " + args[0]);
                    }
                    else {
                        hm.callMessageHook(channel, user, message);
                        BotLogMan.channelMessage("[PM] <" + user.getPrefix() + user.getNick() + "> " + message);
                    }
                    this.onPrivateMessage(sourceNick, sourceLogin, sourceHostname, line.substring(line.indexOf(" :") + 2));
                }
                break;
            case "JOIN":
                // Someone is joining a channel.
                if (!sourceNick.equals(this.nick)) {
                    if (channel.getUser(sourceNick) == null) {
                        user = new User("", sourceNick, this);
                        channel.addUser(user);
                        hm.callJoinHook(channel, user);
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
                this.onPart(target, sourceNick, sourceLogin, sourceHostname);
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
                this.onNickChange(sourceNick, sourceLogin, sourceHostname, newNick);
                break;
            case "NOTICE":
                // Someone is sending a notice.
                this.onNotice(sourceNick, sourceLogin, sourceHostname, target, line.substring(line.indexOf(" :") + 2));
                //logger.log(BotLevel.NOTICE, line);
                break;
            case "QUIT":
                // Someone has quit from the IRC server.
                if (sourceNick.equals(this.getNick())) {
                    this.removeAllChannels();
                }
                else if (line.substring(line.indexOf(" :") + 2).equals("*.net *.split")) {
                    //do nothing
                }
                else {
                    this.removeUserAll(sourceNick);
                }
                this.onQuit(sourceNick, sourceLogin, sourceHostname, line.substring(line.indexOf(" :") + 2));
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
                this.onKick(target, sourceNick, sourceLogin, sourceHostname, recipient, line.substring(line.indexOf(" :") + 2));
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
                channel.setTopic(line.substring(line.indexOf(" :") + 2));
                this.onTopic(target, line.substring(line.indexOf(" :") + 2), sourceNick, System.currentTimeMillis(), true);
                break;
            case "INVITE":
                // Somebody is inviting somebody else into a channel.
                this.onInvite(target, sourceNick, sourceLogin, sourceHostname, line.substring(line.indexOf(" :") + 2));
                break;
            default:
                // If we reach this point, then we've found something that the
                // VIBot Doesn't currently deal with.
                this.onUnknown(line);
                break;
        }
    }

    /**
     * This method is called once the VIBot has successfully connected to the
     * IRC server.
     */
    protected void onConnect() {

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
        int thirdSpace = response.indexOf(' ', secondSpace + 1);
        int colon = response.indexOf(':');
        String channel = response.substring(firstSpace + 1, secondSpace);
        String topic = response.substring(colon + 1);
        ReplyConstants rc = ReplyConstants.fromCode(code);
        if (rc != null) {
            switch (rc) {
                case RPL_LIST:
                    // This is a bit of information about a channel.

                    int userCount = 0;
                    try {
                        userCount = Integer.parseInt(response.substring(secondSpace + 1, thirdSpace));
                    }
                    catch (NumberFormatException e) {
                        // Stick with the value of zero.
                    }

                    this.onChannelInfo(channel, userCount, topic);
                    break;

                case RPL_TOPIC:
                    // This is topic information about a channel we've just joined.
                    topics.put(channel, topic);
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

                    topic = (String) topics.get(channel);
                    topics.remove(channel);

                    this.onTopic(channel, topic, setBy, date, false);
                    break;
                case RPL_NAMREPLY:
                    // This is a list of nicks in a channel that we've just joined.
                    int channelEndIndex = response.indexOf(" :");
                    channel = response.substring(response.lastIndexOf(' ', channelEndIndex - 1) + 1, channelEndIndex);
                    Channel chan = getChannel(channel);
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

        this.onServerResponse(code, response);
    }

    /**
     * This method is called when we receive a numeric response from the IRC
     * server.
     * <p>
     * Numerics in the range from 001 to 099 are used for client-server connections only and should never travel between servers. Replies generated in response to commands are found in the range from 200 to 399. Error replies are found in the range from 400 to 599.
     * <p>
     * For example, we can use this method to discover the topic of a channel when we join it. If we join the channel #test which has a topic of &quot;I am King of Test&quot; then the response will be &quot; <code>VIBot #test :I Am King of Test</code>&quot; with a code of 332 to signify that this is a topic. (This is just an example - note that overriding the <code>onTopic</code> method is an easier way of finding the topic for a channel). Check the IRC RFC for the full list of other command
     * response codes.
     * <p>
     * VIBot implements the interface ReplyConstants, which contains contstants that you may find useful here.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @param code
     *            The three-digit numerical code for the response.
     * @param response
     *            The full response from the IRC server.
     * @see ReplyConstants
     */
    protected void onServerResponse(int code, String response) {}

    /**
     * This method is called when we receive a user list from the server after
     * joining a channel.
     * <p>
     * Shortly after joining a channel, the IRC server sends a list of all users in that channel. The VIBot collects this information and calls this method as soon as it has the full list.
     * <p>
     * To obtain the nick of each user in the channel, call the getNick() method on each User object in the array.
     * <p>
     * At a later time, you may call the getUsers method to obtain an up to date list of the users in the channel.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @since VIBot 1.0.0
     * @param channel
     *            The name of the channel.
     * @param users
     *            An array of User objects belonging to this channel.
     * @see User
     */
    protected void onUserList(String channel, User[] users) {}

    /**
     * This method is called whenever a message is sent to a channel.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @param channel
     *            The channel to which the message was sent.
     * @param sender
     *            The nick of the person who sent the message.
     * @param login
     *            The login of the person who sent the message.
     * @param hostname
     *            The hostname of the person who sent the message.
     * @param message
     *            The actual message sent to the channel.
     */
    protected void onMessage(String channel, String sender, String login, String hostname, String message) {}

    /**
     * This method is called whenever a private message is sent to the VIBot.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @param sender
     *            The nick of the person who sent the private message.
     * @param login
     *            The login of the person who sent the private message.
     * @param hostname
     *            The hostname of the person who sent the private message.
     * @param message
     *            The actual message.
     */
    protected void onPrivateMessage(String sender, String login, String hostname, String message) {}

    /**
     * This method is called whenever an ACTION is sent from a user. E.g. such
     * events generated by typing "/me goes shopping" in most IRC clients.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @param sender
     *            The nick of the user that sent the action.
     * @param login
     *            The login of the user that sent the action.
     * @param hostname
     *            The hostname of the user that sent the action.
     * @param target
     *            The target of the action, be it a channel or our nick.
     * @param action
     *            The action carried out by the user.
     */
    protected void onAction(String sender, String login, String hostname, String target, String action) {}

    /**
     * This method is called whenever we receive a notice.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @param sourceNick
     *            The nick of the user that sent the notice.
     * @param sourceLogin
     *            The login of the user that sent the notice.
     * @param sourceHostname
     *            The hostname of the user that sent the notice.
     * @param target
     *            The target of the notice, be it our nick or a channel name.
     * @param notice
     *            The notice message.
     */
    protected void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice) {}

    /**
     * This method is called whenever someone (possibly us) joins a channel
     * which we are on.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @param channel
     *            The channel which somebody joined.
     * @param sender
     *            The nick of the user who joined the channel.
     * @param login
     *            The login of the user who joined the channel.
     * @param hostname
     *            The hostname of the user who joined the channel.
     */
    protected void onJoin(String channel, String sender, String login, String hostname) {}

    /**
     * This method is called whenever someone (possibly us) parts a channel
     * which we are on.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @param channel
     *            The channel which somebody parted from.
     * @param sender
     *            The nick of the user who parted from the channel.
     * @param login
     *            The login of the user who parted from the channel.
     * @param hostname
     *            The hostname of the user who parted from the channel.
     */
    protected void onPart(String channel, String sender, String login, String hostname) {}

    /**
     * This method is called whenever someone (possibly us) changes nick on any
     * of the channels that we are on.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @param oldNick
     *            The old nick.
     * @param login
     *            The login of the user.
     * @param hostname
     *            The hostname of the user.
     * @param newNick
     *            The new nick.
     */
    protected void onNickChange(String oldNick, String login, String hostname, String newNick) {}

    /**
     * This method is called whenever someone (possibly us) is kicked from any
     * of the channels that we are in.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @param channel
     *            The channel from which the recipient was kicked.
     * @param kickerNick
     *            The nick of the user who performed the kick.
     * @param kickerLogin
     *            The login of the user who performed the kick.
     * @param kickerHostname
     *            The hostname of the user who performed the kick.
     * @param recipientNick
     *            The unfortunate recipient of the kick.
     * @param reason
     *            The reason given by the user who performed the kick.
     */
    protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {}

    /**
     * This method is called whenever someone (possibly us) quits from the
     * server. We will only observe this if the user was in one of the channels
     * to which we are connected.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @param sourceNick
     *            The nick of the user that quit from the server.
     * @param sourceLogin
     *            The login of the user that quit from the server.
     * @param sourceHostname
     *            The hostname of the user that quit from the server.
     * @param reason
     *            The reason given for quitting the server.
     */
    protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {}

    /**
     * This method is called whenever a user sets the topic, or when VIBot joins
     * a new channel and discovers its topic.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @param channel
     *            The channel that the topic belongs to.
     * @param topic
     *            The topic for the channel.
     * @deprecated As of 1.2.0, replaced by {@link #onTopic(String,String,String,long,boolean)}
     */
    protected void onTopic(String channel, String topic) {}

    /**
     * This method is called whenever a user sets the topic, or when VIBot joins
     * a new channel and discovers its topic.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @param channel
     *            The channel that the topic belongs to.
     * @param topic
     *            The topic for the channel.
     * @param setBy
     *            The nick of the user that set the topic.
     * @param date
     *            When the topic was set (milliseconds since the epoch).
     * @param changed
     *            True if the topic has just been changed, false if the topic
     *            was already there.
     */
    protected void onTopic(String channel, String topic, String setBy, long date, boolean changed) {}

    /**
     * After calling the listChannels() method in VIBot, the server will start
     * to send us information about each channel on the server. You may override
     * this method in order to receive the information about each channel as
     * soon as it is received.
     * <p>
     * Note that certain channels, such as those marked as hidden, may not appear in channel listings.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @param channel
     *            The name of the channel.
     * @param userCount
     *            The number of users visible in this channel.
     * @param topic
     *            The topic for this channel.
     * @see #listChannels() listChannels
     */
    protected void onChannelInfo(String channel, int userCount, String topic) {}

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

                if (atPos == '+' || atPos == '-') {
                    pn = atPos;
                }
                else if (atPos == 'o') {
                    if (pn == '+') {
                        this.updateUser(channel, UserMode.OP, channel.getUser(params[p]));
                        // onOp(channel, sourceNick, sourceLogin,
                        // sourceHostname, params[p]);
                    }
                    else {
                        this.updateUser(channel, UserMode.DEOP, channel.getUser(params[p]));
                        // onDeop(channel, sourceNick, sourceLogin,
                        // sourceHostname, params[p]);
                    }
                    p++;
                }
                else if (atPos == 'v') {
                    if (pn == '+') {
                        this.updateUser(channel, UserMode.VOICE, channel.getUser(params[p]));
                        // onVoice(channel, sourceNick, sourceLogin,
                        // sourceHostname, params[p]);
                    }
                    else {
                        this.updateUser(channel, UserMode.DEVOICE, channel.getUser(params[p]));
                        // onDeVoice(channel, sourceNick, sourceLogin,
                        // sourceHostname, params[p]);
                    }
                    p++;
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

            // this.onMode(channel, sourceNick, sourceLogin, sourceHostname,
            // mode);
        }
        else {
            // The mode of a user is being changed.
            String nick = target;
            this.onUserMode(nick, sourceNick, sourceLogin, sourceHostname, mode);
        }
    }

    /**
     * Called when the mode of a channel is set.
     * <p>
     * You may find it more convenient to decode the meaning of the mode string by overriding the onOp, onDeOp, onVoice, onDeVoice, onChannelKey, onDeChannelKey, onChannelLimit, onDeChannelLimit, onChannelBan or onDeChannelBan methods as appropriate.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @param channel
     *            The channel that the mode operation applies to.
     * @param sourceNick
     *            The nick of the user that set the mode.
     * @param sourceLogin
     *            The login of the user that set the mode.
     * @param sourceHostname
     *            The hostname of the user that set the mode.
     * @param mode
     *            The mode that has been set.
     */
    protected void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode) {}

    /**
     * Called when the mode of a user is set.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @since VIBot 1.2.0
     * @param targetNick
     *            The nick that the mode operation applies to.
     * @param sourceNick
     *            The nick of the user that set the mode.
     * @param sourceLogin
     *            The login of the user that set the mode.
     * @param sourceHostname
     *            The hostname of the user that set the mode.
     * @param mode
     *            The mode that has been set.
     */
    protected void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode) {}

    /**
     * Called when a user (possibly us) gets granted operator status for a
     * channel.
     * <p>
     * This is a type of mode change and is also passed to the onMode method in the VIBot class.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @since VIBot 0.9.5
     * @param channel
     *            The channel in which the mode change took place.
     * @param sourceNick
     *            The nick of the user that performed the mode change.
     * @param sourceLogin
     *            The login of the user that performed the mode change.
     * @param sourceHostname
     *            The hostname of the user that performed the mode change.
     * @param recipient
     *            The nick of the user that got 'opped'.
     */
    protected void onOp(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {}

    /**
     * Called when a user (possibly us) gets operator status taken away.
     * <p>
     * This is a type of mode change and is also passed to the onMode method in the VIBot class.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @since VIBot 0.9.5
     * @param channel
     *            The channel in which the mode change took place.
     * @param sourceNick
     *            The nick of the user that performed the mode change.
     * @param sourceLogin
     *            The login of the user that performed the mode change.
     * @param sourceHostname
     *            The hostname of the user that performed the mode change.
     * @param recipient
     *            The nick of the user that got 'deopped'.
     */
    protected void onDeop(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {}

    /**
     * Called when a user (possibly us) gets voice status granted in a channel.
     * <p>
     * This is a type of mode change and is also passed to the onMode method in the VIBot class.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @since VIBot 0.9.5
     * @param channel
     *            The channel in which the mode change took place.
     * @param sourceNick
     *            The nick of the user that performed the mode change.
     * @param sourceLogin
     *            The login of the user that performed the mode change.
     * @param sourceHostname
     *            The hostname of the user that performed the mode change.
     * @param recipient
     *            The nick of the user that got 'voiced'.
     */
    protected void onVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {}

    /**
     * Called when a user (possibly us) gets voice status removed.
     * <p>
     * This is a type of mode change and is also passed to the onMode method in the VIBot class.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @since VIBot 0.9.5
     * @param channel
     *            The channel in which the mode change took place.
     * @param sourceNick
     *            The nick of the user that performed the mode change.
     * @param sourceLogin
     *            The login of the user that performed the mode change.
     * @param sourceHostname
     *            The hostname of the user that performed the mode change.
     * @param recipient
     *            The nick of the user that got 'devoiced'.
     */
    protected void onDeVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {}

    /**
     * Called when a channel key is set. When the channel key has been set,
     * other users may only join that channel if they know the key. Channel keys
     * are sometimes referred to as passwords.
     * <p>
     * This is a type of mode change and is also passed to the onMode method in the VIBot class.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @since VIBot 0.9.5
     * @param channel
     *            The channel in which the mode change took place.
     * @param sourceNick
     *            The nick of the user that performed the mode change.
     * @param sourceLogin
     *            The login of the user that performed the mode change.
     * @param sourceHostname
     *            The hostname of the user that performed the mode change.
     * @param key
     *            The new key for the channel.
     */
    protected void onSetChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) {}

    /**
     * Called when a channel key is removed.
     * <p>
     * This is a type of mode change and is also passed to the onMode method in the VIBot class.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @since VIBot 0.9.5
     * @param channel
     *            The channel in which the mode change took place.
     * @param sourceNick
     *            The nick of the user that performed the mode change.
     * @param sourceLogin
     *            The login of the user that performed the mode change.
     * @param sourceHostname
     *            The hostname of the user that performed the mode change.
     * @param key
     *            The key that was in use before the channel key was removed.
     */
    protected void onRemoveChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) {}

    /**
     * Called when a user limit is set for a channel. The number of users in the
     * channel cannot exceed this limit.
     * <p>
     * This is a type of mode change and is also passed to the onMode method in the VIBot class.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @since VIBot 0.9.5
     * @param channel
     *            The channel in which the mode change took place.
     * @param sourceNick
     *            The nick of the user that performed the mode change.
     * @param sourceLogin
     *            The login of the user that performed the mode change.
     * @param sourceHostname
     *            The hostname of the user that performed the mode change.
     * @param limit
     *            The maximum number of users that may be in this channel at the
     *            same time.
     */
    protected void onSetChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname, int limit) {}

    /**
     * Called when the user limit is removed for a channel.
     * <p>
     * This is a type of mode change and is also passed to the onMode method in the VIBot class.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @since VIBot 0.9.5
     * @param channel
     *            The channel in which the mode change took place.
     * @param sourceNick
     *            The nick of the user that performed the mode change.
     * @param sourceLogin
     *            The login of the user that performed the mode change.
     * @param sourceHostname
     *            The hostname of the user that performed the mode change.
     */
    protected void onRemoveChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}

    /**
     * Called when a user (possibly us) gets banned from a channel. Being banned
     * from a channel prevents any user with a matching hostmask from joining
     * the channel. For this reason, most bans are usually directly followed by
     * the user being kicked :-)
     * <p>
     * This is a type of mode change and is also passed to the onMode method in the VIBot class.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @since VIBot 0.9.5
     * @param channel
     *            The channel in which the mode change took place.
     * @param sourceNick
     *            The nick of the user that performed the mode change.
     * @param sourceLogin
     *            The login of the user that performed the mode change.
     * @param sourceHostname
     *            The hostname of the user that performed the mode change.
     * @param hostmask
     *            The hostmask of the user that has been banned.
     */
    protected void onSetChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {}

    /**
     * Called when a hostmask ban is removed from a channel.
     * <p>
     * This is a type of mode change and is also passed to the onMode method in the VIBot class.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @since VIBot 0.9.5
     * @param channel
     *            The channel in which the mode change took place.
     * @param sourceNick
     *            The nick of the user that performed the mode change.
     * @param sourceLogin
     *            The login of the user that performed the mode change.
     * @param sourceHostname
     *            The hostname of the user that performed the mode change.
     * @param hostmask
     */
    protected void onRemoveChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {}

    /**
     * Called when topic protection is enabled for a channel. Topic protection
     * means that only operators in a channel may change the topic.
     * <p>
     * This is a type of mode change and is also passed to the onMode method in the VIBot class.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @since VIBot 0.9.5
     * @param channel
     *            The channel in which the mode change took place.
     * @param sourceNick
     *            The nick of the user that performed the mode change.
     * @param sourceLogin
     *            The login of the user that performed the mode change.
     * @param sourceHostname
     *            The hostname of the user that performed the mode change.
     */
    protected void onSetTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}

    /**
     * Called when topic protection is removed for a channel.
     * <p>
     * This is a type of mode change and is also passed to the onMode method in the VIBot class.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @since VIBot 0.9.5
     * @param channel
     *            The channel in which the mode change took place.
     * @param sourceNick
     *            The nick of the user that performed the mode change.
     * @param sourceLogin
     *            The login of the user that performed the mode change.
     * @param sourceHostname
     *            The hostname of the user that performed the mode change.
     */
    protected void onRemoveTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}

    /**
     * Called when a channel is set to only allow messages from users that are
     * in the channel.
     * <p>
     * This is a type of mode change and is also passed to the onMode method in the VIBot class.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @since VIBot 0.9.5
     * @param channel
     *            The channel in which the mode change took place.
     * @param sourceNick
     *            The nick of the user that performed the mode change.
     * @param sourceLogin
     *            The login of the user that performed the mode change.
     * @param sourceHostname
     *            The hostname of the user that performed the mode change.
     */
    protected void onSetNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}

    /**
     * Called when a channel is set to allow messages from any user, even if
     * they are not actually in the channel.
     * <p>
     * This is a type of mode change and is also passed to the onMode method in the VIBot class.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @since VIBot 0.9.5
     * @param channel
     *            The channel in which the mode change took place.
     * @param sourceNick
     *            The nick of the user that performed the mode change.
     * @param sourceLogin
     *            The login of the user that performed the mode change.
     * @param sourceHostname
     *            The hostname of the user that performed the mode change.
     */
    protected void onRemoveNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}

    /**
     * Called when a channel is set to 'invite only' mode. A user may only join
     * the channel if they are invited by someone who is already in the channel.
     * <p>
     * This is a type of mode change and is also passed to the onMode method in the VIBot class.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @since VIBot 0.9.5
     * @param channel
     *            The channel in which the mode change took place.
     * @param sourceNick
     *            The nick of the user that performed the mode change.
     * @param sourceLogin
     *            The login of the user that performed the mode change.
     * @param sourceHostname
     *            The hostname of the user that performed the mode change.
     */
    protected void onSetInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}

    /**
     * Called when a channel has 'invite only' removed.
     * <p>
     * This is a type of mode change and is also passed to the onMode method in the VIBot class.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @since VIBot 0.9.5
     * @param channel
     *            The channel in which the mode change took place.
     * @param sourceNick
     *            The nick of the user that performed the mode change.
     * @param sourceLogin
     *            The login of the user that performed the mode change.
     * @param sourceHostname
     *            The hostname of the user that performed the mode change.
     */
    protected void onRemoveInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}

    /**
     * Called when a channel is set to 'moderated' mode. If a channel is
     * moderated, then only users who have been 'voiced' or 'opped' may speak or
     * change their nicks.
     * <p>
     * This is a type of mode change and is also passed to the onMode method in the VIBot class.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @since VIBot 0.9.5
     * @param channel
     *            The channel in which the mode change took place.
     * @param sourceNick
     *            The nick of the user that performed the mode change.
     * @param sourceLogin
     *            The login of the user that performed the mode change.
     * @param sourceHostname
     *            The hostname of the user that performed the mode change.
     */
    protected void onSetModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}

    /**
     * Called when a channel has moderated mode removed.
     * <p>
     * This is a type of mode change and is also passed to the onMode method in the VIBot class.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @since VIBot 0.9.5
     * @param channel
     *            The channel in which the mode change took place.
     * @param sourceNick
     *            The nick of the user that performed the mode change.
     * @param sourceLogin
     *            The login of the user that performed the mode change.
     * @param sourceHostname
     *            The hostname of the user that performed the mode change.
     */
    protected void onRemoveModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}

    /**
     * Called when a channel is marked as being in private mode.
     * <p>
     * This is a type of mode change and is also passed to the onMode method in the VIBot class.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @since VIBot 0.9.5
     * @param channel
     *            The channel in which the mode change took place.
     * @param sourceNick
     *            The nick of the user that performed the mode change.
     * @param sourceLogin
     *            The login of the user that performed the mode change.
     * @param sourceHostname
     *            The hostname of the user that performed the mode change.
     */
    protected void onSetPrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}

    /**
     * Called when a channel is marked as not being in private mode.
     * <p>
     * This is a type of mode change and is also passed to the onMode method in the VIBot class.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @since VIBot 0.9.5
     * @param channel
     *            The channel in which the mode change took place.
     * @param sourceNick
     *            The nick of the user that performed the mode change.
     * @param sourceLogin
     *            The login of the user that performed the mode change.
     * @param sourceHostname
     *            The hostname of the user that performed the mode change.
     */
    protected void onRemovePrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}

    /**
     * Called when a channel is set to be in 'secret' mode. Such channels
     * typically do not appear on a server's channel listing.
     * <p>
     * This is a type of mode change and is also passed to the onMode method in the VIBot class.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @since VIBot 0.9.5
     * @param channel
     *            The channel in which the mode change took place.
     * @param sourceNick
     *            The nick of the user that performed the mode change.
     * @param sourceLogin
     *            The login of the user that performed the mode change.
     * @param sourceHostname
     *            The hostname of the user that performed the mode change.
     */
    protected void onSetSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}

    /**
     * Called when a channel has 'secret' mode removed.
     * <p>
     * This is a type of mode change and is also passed to the onMode method in the VIBot class.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @since VIBot 0.9.5
     * @param channel
     *            The channel in which the mode change took place.
     * @param sourceNick
     *            The nick of the user that performed the mode change.
     * @param sourceLogin
     *            The login of the user that performed the mode change.
     * @param sourceHostname
     *            The hostname of the user that performed the mode change.
     */
    protected void onRemoveSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}

    /**
     * Called when we are invited to a channel by a user.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @since VIBot 0.9.5
     * @param targetNick
     *            The nick of the user being invited - should be us!
     * @param sourceNick
     *            The nick of the user that sent the invitation.
     * @param sourceLogin
     *            The login of the user that sent the invitation.
     * @param sourceHostname
     *            The hostname of the user that sent the invitation.
     * @param channel
     *            The channel that we're being invited to.
     */
    protected void onInvite(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String channel) {}

    /**
     * This method used to be called when a DCC SEND request was sent to the
     * VIBot. Please use the onIncomingFileTransfer method to receive files, as
     * it has better functionality and supports resuming.
     * 
     * @deprecated As of VIBot 1.2.0, use {@link #onIncomingFileTransfer(DccFileTransfer)}
     */
    protected void onDccSendRequest(String sourceNick, String sourceLogin, String sourceHostname, String filename, long address, int port, int size) {}

    /**
     * This method used to be called when a DCC CHAT request was sent to the
     * VIBot. Please use the onIncomingChatRequest method to accept chats, as it
     * has better functionality.
     * 
     * @deprecated As of VIBot 1.2.0, use {@link #onIncomingChatRequest(DccChat)}
     */
    protected void onDccChatRequest(String sourceNick, String sourceLogin, String sourceHostname, long address, int port) {}

    /**
     * This method is called whenever a DCC SEND request is sent to the VIBot.
     * This means that a client has requested to send a file to us. This
     * abstract implementation performs no action, which means that all DCC SEND
     * requests will be ignored by default. If you wish to receive the file,
     * then you may override this method and call the receive method on the
     * DccFileTransfer object, which connects to the sender and downloads the
     * file.
     * <p>
     * Example:
     * 
     * <pre>
     * public void onIncomingFileTransfer(DccFileTransfer transfer) {
     *     // Use the suggested file name.
     *     File file = transfer.getFile();
     *     // Receive the transfer and save it to the file, allowing resuming.
     *     transfer.receive(file, true);
     * }
     * </pre>
     * <p>
     * <b>Warning:</b> Receiving an incoming file transfer will cause a file to be written to disk. Please ensure that you make adequate security checks so that this file does not overwrite anything important!
     * <p>
     * Each time a file is received, it happens within a new Thread in order to allow multiple files to be downloaded by the VIBot at the same time.
     * <p>
     * If you allow resuming and the file already partly exists, it will be appended to instead of overwritten. If resuming is not enabled, the file will be overwritten if it already exists.
     * <p>
     * You can throttle the speed of the transfer by calling the setPacketDelay method on the DccFileTransfer object, either before you receive the file or at any moment during the transfer.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @since VIBot 1.2.0
     * @param transfer
     *            The DcccFileTransfer that you may accept.
     * @see DccFileTransfer
     */
    public void onIncomingFileTransfer(DccFileTransfer transfer) {}

    /**
     * This method gets called when a DccFileTransfer has finished. If there was
     * a problem, the Exception will say what went wrong. If the file was sent
     * successfully, the Exception will be null.
     * <p>
     * Both incoming and outgoing file transfers are passed to this method. You can determine the type by calling the isIncoming or isOutgoing methods on the DccFileTransfer object.
     * 
     * @since VIBot 1.2.0
     * @param transfer
     *            The DccFileTransfer that has finished.
     * @param e
     *            null if the file was transfered successfully, otherwise this
     *            will report what went wrong.
     * @see DccFileTransfer
     */
    public void onFileTransferFinished(DccFileTransfer transfer, Exception e) {}

    /**
     * This method will be called whenever a DCC Chat request is received. This
     * means that a client has requested to chat to us directly rather than via
     * the IRC server. This is useful for sending many lines of text to and from
     * the bot without having to worry about flooding the server or any
     * operators of the server being able to "spy" on what is being said. This
     * abstract implementation performs no action, which means that all DCC CHAT
     * requests will be ignored by default.
     * <p>
     * If you wish to accept the connection, then you may override this method and call the accept() method on the DccChat object, which connects to the sender of the chat request and allows lines to be sent to and from the bot.
     * <p>
     * Your bot must be able to connect directly to the user that sent the request.
     * <p>
     * Example:
     * 
     * <pre>
     * public void onIncomingChatRequest(DccChat chat) {
     *     try {
     *         // Accept all chat, whoever it's from.
     *         chat.accept();
     *         chat.sendLine(&quot;Hello&quot;);
     *         String response = chat.readLine();
     *         chat.close();
     *     }
     *     catch (IOException e) {}
     * }
     * </pre>
     * 
     * Each time this method is called, it is called from within a new Thread so that multiple DCC CHAT sessions can run concurrently.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @since VIBot 1.2.0
     * @param chat
     *            A DccChat object that represents the incoming chat request.
     * @see DccChat
     */
    public void onIncomingChatRequest(DccChat chat) {}

    /**
     * This method is called whenever we receive a VERSION request. This
     * abstract implementation responds with the VIBot's _version string, so if
     * you override this method, be sure to either mimic its functionality or to
     * call super.onVersion(...);
     * 
     * @param sourceNick
     *            The nick of the user that sent the VERSION request.
     * @param sourceLogin
     *            The login of the user that sent the VERSION request.
     * @param sourceHostname
     *            The hostname of the user that sent the VERSION request.
     * @param target
     *            The target of the VERSION request, be it our nick or a channel
     *            name.
     */
    protected void onVersion(String sourceNick, String sourceLogin, String sourceHostname, String target) {

    }

    /**
     * This method is called whenever we receive a PING request from another
     * user.
     * <p>
     * This abstract implementation responds correctly, so if you override this method, be sure to either mimic its functionality or to call super.onPing(...);
     * 
     * @param sourceNick
     *            The nick of the user that sent the PING request.
     * @param sourceLogin
     *            The login of the user that sent the PING request.
     * @param sourceHostname
     *            The hostname of the user that sent the PING request.
     * @param target
     *            The target of the PING request, be it our nick or a channel
     *            name.
     * @param pingValue
     *            The value that was supplied as an argument to the PING
     *            command.
     */
    protected void onPing(String sourceNick, String sourceLogin, String sourceHostname, String target, String pingValue) {
        this.sendRawLine("NOTICE " + sourceNick + " :\u0001PING " + pingValue + "\u0001");
    }

    /**
     * The actions to perform when a PING request comes from the server.
     * <p>
     * This sends back a correct response, so if you override this method, be sure to either mimic its functionality or to call super.onServerPing(response);
     * 
     * @param response
     *            The response that should be given back in your PONG.
     */
    protected void onServerPing(String response) {
        this.sendRawLine("PONG " + response);
    }

    /**
     * This method is called whenever we receive a TIME request.
     * <p>
     * This abstract implementation responds correctly, so if you override this method, be sure to either mimic its functionality or to call super.onTime(...);
     * 
     * @param sourceNick
     *            The nick of the user that sent the TIME request.
     * @param sourceLogin
     *            The login of the user that sent the TIME request.
     * @param sourceHostname
     *            The hostname of the user that sent the TIME request.
     * @param target
     *            The target of the TIME request, be it our nick or a channel
     *            name.
     */
    protected void onTime(String sourceNick, String sourceLogin, String sourceHostname, String target) {
        this.sendRawLine("NOTICE " + sourceNick + " :\u0001TIME " + new Date().toString() + "\u0001");
    }

    /**
     * This method is called whenever we receive a FINGER request.
     * <p>
     * This abstract implementation responds correctly, so if you override this method, be sure to either mimic its functionality or to call super.onFinger(...);
     * 
     * @param sourceNick
     *            The nick of the user that sent the FINGER request.
     * @param sourceLogin
     *            The login of the user that sent the FINGER request.
     * @param sourceHostname
     *            The hostname of the user that sent the FINGER request.
     * @param target
     *            The target of the FINGER request, be it our nick or a channel
     *            name.
     */
    protected void onFinger(String sourceNick, String sourceLogin, String sourceHostname, String target) {

    }

    /**
     * This method is called whenever we receive a line from the server that the
     * VIBot has not been programmed to recognise.
     * <p>
     * The implementation of this method in the VIBot abstract class performs no actions and may be overridden as required.
     * 
     * @param line
     *            The raw line that was received from the server.
     */
    protected void onUnknown(String line) {
        // And then there were none :)
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
     * @since VIBot 1.0.0
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
     * @since VIBot 0.9.9
     * @return The number of lines in the outgoing message Queue.
     */
    public final int getOutgoingQueueSize() {
        return outQueue.size();
    }

    /**
     * A convenient method that accepts an IP address represented as a long and
     * returns an integer array of size 4 representing the same IP address.
     * 
     * @since VIBot 0.9.4
     * @param address
     *            the long value representing the IP address.
     * @return An int[] of size 4.
     */
    public int[] longToIp(long address) {
        int[] ip = new int[4];
        for (int i = 3; i >= 0; i--) {
            ip[i] = (int) (address % 256);
            address = address / 256;
        }
        return ip;
    }

    /**
     * A convenient method that accepts an IP address represented by a byte[] of
     * size 4 and returns this as a long representation of the same IP address.
     * 
     * @since VIBot 0.9.4
     * @param address
     *            the byte[] of size 4 representing the IP address.
     * @return a long representation of the IP address.
     */
    public long ipToLong(byte[] address) {
        if (address.length != 4) {
            throw new IllegalArgumentException("byte array must be of length 4");
        }
        long ipNum = 0;
        long multiplier = 1;
        for (int i = 3; i >= 0; i--) {
            int byteVal = (address[i] + 256) % 256;
            ipNum += byteVal * multiplier;
            multiplier *= 256;
        }
        return ipNum;
    }

    /**
     * Returns the InetAddress used by the VIBot. This can be used to find the
     * I.P. address from which the VIBot is connected to a server.
     * 
     * @since VIBot 1.4.4
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
     * @since VIBot 1.4.4
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
     * @since VIBot 1.4.4
     * @return The current DCC InetAddress, or null if left as default.
     */
    public InetAddress getDccInetAddress() {
        return dccInetAddress;
    }

    /**
     * Returns the set of port numbers to be used when sending a DCC chat or
     * file transfer. This is useful when you are behind a firewall and need to
     * set up port forwarding. The array of port numbers is traversed in
     * sequence until a free port is found to listen on. A DCC tranfer will fail
     * if all ports are already in use. If set to null, <i>any</i> free port
     * number will be used.
     * 
     * @since VIBot 1.4.4
     * @return An array of port numbers that VIBot can use to send DCC
     *         transfers, or null if any port is allowed.
     */
    public int[] getDccPorts() {
        if (dccPorts == null || dccPorts.length == 0) {
            return null;
        }
        // Clone the array to prevent external modification.
        return (int[]) dccPorts.clone();
    }

    /**
     * Sets the choice of port numbers that can be used when sending a DCC chat
     * or file transfer. This is useful when you are behind a firewall and need
     * to set up port forwarding. The array of port numbers is traversed in
     * sequence until a free port is found to listen on. A DCC tranfer will fail
     * if all ports are already in use. If set to null, <i>any</i> free port
     * number will be used.
     * 
     * @since VIBot 1.4.4
     * @param ports
     *            The set of port numbers that VIBot may use for DCC transfers,
     *            or null to let it use any free port (default).
     */
    public void setDccPorts(int[] ports) {
        if (ports == null || ports.length == 0) {
            dccPorts = null;
        }
        else {
            // Clone the array to prevent external modification.
            dccPorts = (int[]) ports.clone();
        }
    }

    /**
     * Returns true if and only if the object being compared is the exact same
     * instance as this VIBot. This may be useful if you are writing a multiple
     * server IRC bot that uses more than one instance of VIBot.
     * 
     * @since VIBot 0.9.9
     * @return true if and only if Object o is a VIBot and equal to this.
     */
    public boolean equals(Object o) {
        // This probably has the same effect as Object.equals, but that may
        // change...
        if (o instanceof VIBot) {
            VIBot other = (VIBot) o;
            return other == this;
        }
        return false;
    }

    /**
     * Returns the hashCode of this VIBot. This method can be called by hashed
     * collection classes and is useful for managing multiple instances of
     * VIBots in such collections.
     * 
     * @since VIBot 0.9.9
     * @return the hash code for this instance of VIBot.
     */
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Returns a String representation of this object. You may find this useful
     * for debugging purposes, particularly if you are using more than one VIBot
     * instance to achieve multiple server connectivity. The format of this
     * String may change between different versions of VIBot but is currently
     * something of the form <br>
     * <code>
     *   VIBot[ Version=x.y.z Connected=%b Server{irc.esper.net}
     *   Port{6667}
     *   Password{}
     * </code>
     * 
     * @since VIBot 1.0
     * @return a String representation of this object.
     */
    public String toString() {
        return String.format("VIBot[Version=%s Connected=%b Server=%s Port=%d", getBotVersion(), isConnected(), BotConfig.getServer(), BotConfig.getServerPort());
    }

    /**
     * Disposes of all thread resources used by this VIBot. This may be useful
     * when writing bots or clients that use multiple servers (and therefore
     * multiple VIBot instances) or when integrating a VIBot with an existing
     * program.
     * <p>
     * Each VIBot runs its own threads for dispatching messages from its outgoing message queue and receiving messages from the server. Calling dispose() ensures that these threads are stopped, thus freeing up system resources and allowing the VIBot object to be garbage collected if there are no other references to it.
     * <p>
     * Once a VIBot object has been disposed, it should not be used again. Attempting to use a VIBot that has been disposed may result in unpredictable behaviour.
     * 
     * @since 1.2.2
     */
    public synchronized void dispose() {
        // System.out.println("disposing...");
        outputThread.interrupt();
        inputThread.dispose();
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

    private final void updateUser(Channel channel, UserMode userMode, User user) {
        channel.removeUser(user);

        switch (userMode) {
            case OP:
                user.Op();
                break;
            case DEOP:
                user.deOp();
                break;
            case VOICE:
                user.Voice();
                break;
            case DEVOICE:
                user.deVoice();
                break;
        }

        channel.addUser(user);
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

    public final List<Channel> getChannels() {
        return Collections.unmodifiableList(channels);
    }
}
