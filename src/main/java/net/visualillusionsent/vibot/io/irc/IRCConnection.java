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
package net.visualillusionsent.vibot.io.irc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import net.visualillusionsent.utils.DateUtils;
import net.visualillusionsent.utils.IPAddressUtils;
import net.visualillusionsent.utils.UtilityException;
import net.visualillusionsent.vibot.CommandParser;
import net.visualillusionsent.vibot.Queue;
import net.visualillusionsent.vibot.VIBot;
import net.visualillusionsent.vibot.api.plugin.events.EventManager;
import net.visualillusionsent.vibot.io.ReconnectionThread;
import net.visualillusionsent.vibot.io.configuration.BotConfig;
import net.visualillusionsent.vibot.io.dcc.DccChat;
import net.visualillusionsent.vibot.io.dcc.DccFileTransfer;
import net.visualillusionsent.vibot.io.dcc.DccManager;
import net.visualillusionsent.vibot.io.exception.IRCException;
import net.visualillusionsent.vibot.io.exception.NickAlreadyInUseException;
import net.visualillusionsent.vibot.io.exception.VIBotException;
import net.visualillusionsent.vibot.io.logging.BotLogMan;

public final class IRCConnection {
    private final VIBot bot;
    private final String channelPrefixes = "#";
    private final IRCInput input_thread;
    private final IRCOutput output_thread;
    private final Queue out_Queue;
    private final Socket socket;
    private final BufferedReader breader;
    private final BufferedWriter bwriter;
    private final DccManager dccManager;
    private final EventManager manager;

    private InetAddress dccInetAddress;
    private ArrayList<Channel> channels;
    private boolean connected = false;
    private volatile boolean disposed = false;
    private static IRCConnection lockdown;

    /**
     * The maximum length of any line that is sent via the IRC protocol.<br>
     * The IRC RFC specifies that line lengths, including the trailing \r\n must
     * not exceed 512 bytes. Hence, there is currently no option to change this
     * value in VIBot. All lines greater than this length will be truncated
     * before being sent to the IRC server.
     */
    public static final short MAX_LINE_LENGTH = 512;

    public IRCConnection(VIBot bot, Socket socket, BufferedReader breader, BufferedWriter bwriter) {
        if (lockdown != null) {
            throw new IllegalStateException("Only one IRCConnection instance may be created at a time.");
        }
        this.bot = bot;
        this.socket = socket;
        this.breader = breader;
        this.bwriter = bwriter;
        this.out_Queue = new Queue();
        this.input_thread = new IRCInput(this);
        this.output_thread = new IRCOutput(this);
        this.channels = new ArrayList<Channel>();
        this.dccManager = new DccManager(this);
        this.manager = EventManager.getInstance();
        lockdown = this;
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
     * @code.derivative PircBot
     */
    public final void sendMessage(String target, String message) {
        addToQueue("PRIVMSG ".concat(target).concat(" :").concat(message));
    }

    /**
     * Sends an action to the channel or to a user.
     * 
     * @param target
     *            The name of the channel or user nick to send to.
     * @param action
     *            The action to send.
     * @code.derivative PircBot
     */
    public final void sendAction(String target, String action) {
        sendCTCPCommand(target, "ACTION ".concat(action));
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
        addToQueue("NOTICE ".concat(target).concat(" :").concat(notice));
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
        addToQueue("PRIVMSG ".concat(target).concat(" :\u0001").concat(command).concat("\u0001"));
    }

    /**
     * Sends a raw line to the IRC server as soon as possible, bypassing the
     * outgoing message queue.
     * 
     * @param line
     *            The raw line to send to the IRC server.
     */
    public final void sendRawLine(String raw_line) {
        if (raw_line == null) {
            throw new NullPointerException("raw_line cannot be null");
        }
        if (raw_line.length() > MAX_LINE_LENGTH - 2) {
            raw_line = raw_line.substring(0, MAX_LINE_LENGTH - 2);
        }

        synchronized (bwriter) {
            try {
                bwriter.write(raw_line.concat("\r\n"));
                bwriter.flush();
                if (!raw_line.startsWith("PONG :")) {
                    BotLogMan.outgoing(raw_line);
                }
            }
            catch (Exception e) {
                // Silent response - just lose the line.
            }
        }
    }

    /**
     * Returns true if this InputThread is connected to an IRC server. The
     * result of this method should only act as a rough guide, as the result may
     * not be valid by the time you act upon it.
     * 
     * @return True if still connected.
     */
    public boolean isConnected() {
        return connected;
    }

    public final synchronized void dispose() {
        disposed = true;
        connected = false;
        input_thread.dispose();
        output_thread.dispose();
        lockdown = null;
    }

    public final synchronized void addToQueue(String line) {
        out_Queue.add(line);
    }

    String getQueueNext() {
        return out_Queue.next();
    }

    public void start() {
        input_thread.start();
        output_thread.start();
    }

    public InetAddress getInetAddress() {
        if (socket != null) {
            return socket.getLocalAddress();
        }
        return null;
    }

    void disconnected() {
        if (!disposed) {
            BotLogMan.warning("Disconnected from server...");
            connected = false;
            new ReconnectionThread(bot).start();
        }
    }

    void closeSocket() throws IOException {
        socket.close();
    }

    BufferedReader getReader() {
        return breader;
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
                channel.renameUser(user, newNick);
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
                if (channel.getName().equals(chan)) {
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
    public final synchronized void connect() throws IOException, IRCException, NickAlreadyInUseException, VIBotException {
        if (isConnected()) {
            throw new IOException("The VIBot is already connected to an IRC server.  Disconnect first.");
        }

        // Clear everything we may have know about channels.
        removeAllChannels();

        // Attempt to join the server.
        if (BotConfig.getServerPassword() != null && !BotConfig.getServerPassword().isEmpty()) {
            sendRawLine("PASS ".concat(BotConfig.getServerPassword()));
        }

        String nick = BotConfig.getBotName();
        sendRawLine("NICK ".concat(nick));
        sendRawLine("USER " + BotConfig.getLogin() + " 8 * :" + bot.getRealName());

        // Read stuff back from the server to see if we connected.
        String line = null;
        int tries = 1;
        while ((line = breader.readLine()) != null) {

            handleLine(line);

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
                        sendRawLine("NICK " + nick);
                    }
                    else {
                        socket.close();
                        throw new NickAlreadyInUseException(line);
                    }
                }
                else if (code.equals("439")) {
                    // No action required.
                }
                else if (code.startsWith("5") || code.startsWith("4")) {
                    socket.close();
                    throw new IRCException("Could not log into the IRC server: " + line);
                }
            }
            bot.setNick(nick, this);

        }
        connected = true;

        //Identify
        String nickserv_pass = BotConfig.getNickServPassword();
        if (nickserv_pass != null && !nickserv_pass.isEmpty()) {
            sendRawLine("NICKSERV IDENTIFY ".concat(BotConfig.getNickServPassword()));
        }

        BotLogMan.info("Logged onto server: ".concat(BotConfig.getServer()));

        // This makes the socket timeout on read operations after 5 minutes.
        socket.setSoTimeout(300000);

        // Now start the IRCInput/IRCOutput to read all other lines from the server.
        start();

        //Join pre-defined channels
        for (String chan : BotConfig.getChannels()) {
            bot.join(chan);
        }

        EventManager.getInstance().callConnectEvent();
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
        BotLogMan.incoming("Handling Line-".concat(line));

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
        try {
            if (channelPrefixes.indexOf(target.charAt(0)) >= 0) {
                channel = getChannel(target);
                if (channel == null) {
                    channel = new Channel(target, this);
                    channels.add(channel);
                }
            }
        }
        catch (Exception e) {}

        User user = null;
        if (channel != null) {
            user = channel.getUser(sourceNick);
        }

        if (user == null) {
            user = new User("", sourceNick, sourceHostname, sourceLogin, this);
            if (channel != null) {
                channel.addUser(user);
            }
        }
        // Check for CTCP requests.
        switch (command) {
            case "PRIVMSG":
                if (line.indexOf(":\u0001") > 0 && line.endsWith("\u0001")) {
                    String request = line.substring(line.indexOf(":\u0001") + 2, line.length() - 1).trim();

                    switch (request) {
                        case "VERSION":
                            // VERSION request
                            this.sendRawLine("NOTICE " + sourceNick + " :\u0001VERSION " + bot.getVersion() + "\u0001");
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
                            this.sendRawLine("NOTICE " + sourceNick + " :\u0001FINGER " + bot.getFinger() + "\u0001");
                            break;
                        default:
                            if ((tokenizer = new StringTokenizer(request)).countTokens() >= 5 && tokenizer.nextToken().equals("DCC")) {
                                // This is a DCC request.
                                user = new User("", sourceNick, sourceHostname, sourceLogin, this);
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
                if (!sourceNick.equals(bot.getNick())) {
                    if (channel.getUser(sourceNick) == null) {
                        channel.addUser(user);
                    }
                    manager.callJoinEvent(channel, user);
                }
                else {
                    channel.sendMessage(BotConfig.getJoinMessage());
                }
                BotLogMan.join("[" + channel.getName() + "] " + sourceNick + " has joined.");
                break;
            case "PART":
                // Someone is parting from a channel.
                if (sourceNick.equals(bot.getNick())) {
                    channels.remove(channel);
                }
                else {
                    user = channel.getUser(sourceNick);
                    channel.removeUser(user);
                }
                manager.callPartEvent(channel, user);
                if (user != null) {
                    BotLogMan.part("[" + channel.getName() + "] " + user.getNick() + " has parted.");
                }
                break;
            case "NICK":
                // Somebody is changing their nick.
                String newNick = target;
                if (sourceNick.equals(bot.getNick())) {
                    // Update our nick if it was us that changed nick.
                    bot.setNick(newNick, this);
                }
                else {
                    user = getUser(sourceNick);
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
                else if (sourceNick.equals(bot.getNick())) {
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
                if (recipient.equals(bot.getNick())) {
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

            for (int i = 0; i < params[0].length(); i++) {
                char atPos = params[0].charAt(i);

                switch (atPos) {
                    case '+':
                    case '-':
                        pn = atPos;
                        break;
                    case 'b':
                        if (pn == '+') {
                            // onSetChannelBan(channel, sourceNick, sourceLogin,
                            // sourceHostname,params[p]);
                        }
                        else {
                            // onRemoveChannelBan(channel, sourceNick, sourceLogin,
                            // sourceHostname, params[p]);
                        }
                        p++;
                        break;

                    case 'i':
                        if (pn == '+') {
                            // onSetInviteOnly(channel, sourceNick, sourceLogin,
                            // sourceHostname);
                        }
                        else {
                            // onRemoveInviteOnly(channel, sourceNick, sourceLogin,
                            // sourceHostname);
                        }
                        break;

                    case 'k':
                        if (pn == '+') {
                            // onSetChannelKey(channel, sourceNick, sourceLogin,
                            // sourceHostname, params[p]);
                        }
                        else {
                            // onRemoveChannelKey(channel, sourceNick, sourceLogin,
                            // sourceHostname, params[p]);
                        }
                        p++;
                        break;

                    case 'l':
                        if (pn == '+') {
                            // onSetChannelLimit(channel, sourceNick, sourceLogin,
                            // sourceHostname, Integer.parseInt(params[p]));
                        }
                        else {
                            // onRemoveChannelLimit(channel, sourceNick,
                            // sourceLogin, sourceHostname);
                        }
                        p++;
                        break;

                    case 'm':
                        if (pn == '+') {
                            // onSetModerated(channel, sourceNick, sourceLogin,
                            // sourceHostname);
                        }
                        else {
                            // onRemoveModerated(channel, sourceNick, sourceLogin,
                            // sourceHostname);
                        }
                        break;

                    case 'n':
                        if (pn == '+') {
                            channel.addChanMode(ChannelMode.NO_EXTERNAL_MESSAGES);
                        }
                        else {
                            channel.removeChannelMode(ChannelMode.NO_EXTERNAL_MESSAGES);
                        }
                        break;

                    case 'o':
                        if (pn == '+') {
                            if (params[p].equals(bot.getNick())) {
                                channel.opBot();
                            }
                            else {
                                channel.getUser(params[p]).op();
                            }
                        }
                        else {
                            if (params[p].equals(bot.getNick())) {
                                channel.deOpBot();
                            }
                            else {
                                channel.getUser(params[p]).deOp();
                            }
                        }
                        p++;
                        break;

                    case 'p':
                        if (pn == '+') {
                            // onSetPrivate(channel, sourceNick, sourceLogin,
                            // sourceHostname);
                        }
                        else {
                            // onRemovePrivate(channel, sourceNick, sourceLogin,
                            // sourceHostname);
                        }
                        break;

                    case 's':
                        if (pn == '+') {
                            // onSetSecret(channel, sourceNick, sourceLogin,
                            // sourceHostname);
                        }
                        else {
                            // onRemoveSecret(channel, sourceNick, sourceLogin,
                            // sourceHostname);
                        }
                        break;

                    case 't':
                        if (pn == '+') {
                            // onSetTopicProtection(channel, sourceNick,
                            // sourceLogin, sourceHostname);
                        }
                        else {
                            // onRemoveTopicProtection(channel, sourceNick,
                            // sourceLogin, sourceHostname);
                        }
                        break;

                    case 'v':
                        if (pn == '+') {
                            if (params[p].equals(bot.getNick())) {
                                channel.voiceBot();
                            }
                            else {
                                channel.getUser(params[p]).voice();
                            }
                        }
                        else {
                            if (params[p].equals(bot.getNick())) {
                                channel.deVoiceBot();
                            }
                            else {
                                channel.getUser(params[p]).deVoice();
                            }
                        }
                        p++;
                        break;

                }
            }
        }
        else {
            // The mode of a user is being changed.
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
        String[] parsed = response.split(" ");
        int firstSpace = response.indexOf(' ');
        int secondSpace = response.indexOf(' ', firstSpace + 1);
        // int thirdSpace = response.indexOf(' ', secondSpace + 1);
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
                        if (nick.equals(bot.getNick())) {
                            if (prefix.contains("@")) {
                                chan.opBot();
                            }
                            else if (prefix.contains("+")) {
                                chan.voiceBot();
                            }
                        }
                        else {
                            chan.addUser(new User(prefix, nick, null, null, this));
                        }
                    }
                    channels.add(chan);

                    break;

                case RPL_ENDOFNAMES:
                    sendRawLine("MODE ".concat(channel).concat(" +b")); //Get the Ban List now
                    break;

                case RPL_WHOREPLY:
                    chan = getChannel(parsed[1]);

                    //Setup user
                    User curUser = chan.getUser(parsed[5]);
                    curUser.setLogin(parsed[2]);
                    curUser.setHost(parsed[3]);
                    break;

                case RPL_ENDOFWHO:
                    break;

                case RPL_BANLIST:
                    // This is a list of bans in a channel.
                    chan = getChannel(parsed[1]);
                    //Index 2 = nick!user@host Index 3 = user who banned Index 4 = timestamp
                    String[] userInfo = parsed[2].split("!");
                    String[] userHostLogin = userInfo[1].split("@");
                    String userNick = userInfo[0];
                    User banned = new User("", userNick, userHostLogin[1], userHostLogin[0], this);
                    String dateTime = "1-Jan-1970 00:00:00";
                    try {
                        dateTime = DateUtils.longToDateTime(Long.parseLong(parsed[4])).toString();
                    }
                    catch (NumberFormatException e1) {}
                    catch (UtilityException e1) {}
                    Ban ban = new Ban(chan, banned, new User("", parsed[3], null, null, this), dateTime);
                    chan.addBan(ban);
                    break;

                default:
                    break;
            }
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

    private User getUser(String nick) {
        User user = null;

        for (Channel chan : channels) {
            user = chan.getUser(nick);
            if (user != null) {
                break;
            }
        }
        return user;
    }
}
