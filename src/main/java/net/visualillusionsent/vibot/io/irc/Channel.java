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
 */
package net.visualillusionsent.vibot.io.irc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.visualillusionsent.vibot.VIBot;
import net.visualillusionsent.vibot.io.logging.BotLogMan;

/**
 * Channel class<br>
 * IRC Channel helper. Stores information like topic and users in the channel.
 * 
 * @since VIBot 1.0
 * @author Jason (darkdiplomat)
 */
public final class Channel {

    /**
     * The name of the Channel
     */
    private final String name;

    /**
     * The {@link IRCConnect} instance
     */
    private final IRCConnection irc_conn;

    /**
     * The {@link Topic} for the channel
     */
    private Topic topic;

    /**
     * The {@link ArrayList} of {@link User}s for the channel
     */
    private ArrayList<User> users;

    /**
     * The {@link ArrayList} of {@link User}s being ignored in the Channel
     */
    private ArrayList<User> ignored;

    private ArrayList<Ban> banned;

    private ArrayList<ChannelMode> modes;

    private HashMap<String, Boolean> botModes;

    /**
     * Whether the Channel is muted or not
     */
    private boolean muted = false;

    /**
     * CONSOLE Channel instance
     */
    public static final Channel CONSOLE;

    private static final Map<String, Boolean> allowedBotModes;

    static {
        HashMap<String, Boolean> temp = new HashMap<String, Boolean>();
        temp.put("OP", Boolean.FALSE);
        temp.put("VOICE", Boolean.FALSE);
        allowedBotModes = (Map<String, Boolean>) Collections.unmodifiableMap(temp);
        CONSOLE = new Channel("CONSOLE", null);
    }

    /**
     * Channel Constructor
     * <p>
     * Channel should not be constructed outside of VIBot
     * 
     * @param name
     *            the name of the channel
     * @param irc_conn
     *            the IRCConnection instance
     */
    public Channel(String name, IRCConnection irc_conn) {
        this.name = name;
        this.irc_conn = irc_conn;
        this.users = new ArrayList<User>();
        this.ignored = new ArrayList<User>();
        this.modes = new ArrayList<ChannelMode>();
        this.banned = new ArrayList<Ban>();
        this.botModes = new HashMap<String, Boolean>(allowedBotModes.size());
        botModes.putAll(allowedBotModes);
    }

    /**
     * Gets the name of this channel
     * 
     * @return name the name of this channel
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the {@link User} instance for the given name
     * 
     * @param name
     *            the name of the {@link User} to get
     * @return {@link User} if found, {@code null} otherwise
     */
    public final User getUser(String name) {
        for (User user : users) {
            if (user.getNick().equals(name)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Gets the topic for this channel
     * 
     * @return the topic for this channel
     */
    public final Topic getTopic() {
        return topic;
    }

    /**
     * Adds a {@link User} to the Channel
     * 
     * @param user
     *            the {@link User} to be added
     */
    public final void addUser(User user) {
        users.add(user);
    }

    /**
     * Removes a {@link User} from the Channel
     * 
     * @param user
     *            the {@link User} to be removed
     */
    public final void removeUser(User user) {
        users.remove(user);
    }

    /**
     * Renames a {@link User} in the Channel
     * 
     * @param user
     *            the {@link User} to be renamed
     * @param newnick
     *            the new name for the {@link User}
     */
    public final void renameUser(User user, String newnick) {
        users.get(users.indexOf(user)).setNick(newnick);
    }

    /**
     * Sets the {@link Topic} for the Channel
     * 
     * @param topic
     *            the {@link Topic} to be set
     */
    public final void setTopic(Topic topic) {
        this.topic = topic;
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
    public final void setNewTopic(String topic) {
        irc_conn.sendRawLine("TOPIC ".concat(name).concat(" :").concat(topic));
    }

    /**
     * Gets an unmodifiable {@link List} of all {@link User}s in this channel
     * 
     * @return an unmodifiable {@link List} of {@link Users}
     */
    public final List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }

    /**
     * Sends this channel a message
     * 
     * @param message
     *            the message to send to the channel
     */
    public final void sendMessage(String message) {
        if (this.name.equals("CONSOLE")) {
            BotLogMan.consoleMessage(message);
        }
        else {
            irc_conn.sendMessage(name, message);
        }
    }

    /**
     * Sends an Action to the Channel (like /me dosomething)
     * 
     * @param action
     *            the action to send the channel
     */
    public final void sendAction(String action) {
        if (this.name.equals("CONSOLE")) {
            BotLogMan.consoleMessage(VIBot.getBotNick().concat(" ").concat(action));
        }
        else {
            irc_conn.sendAction(name, action);
        }
    }

    /**
     * Checks if this channel is muted
     * 
     * @return {@code true} if muted, {@code false} otherwise
     */
    public final boolean isMuted() {
        return muted;
    }

    /**
     * Toggles the mute status of this channel
     */
    public final void toggleMute() {
        muted = !muted;
    }

    /**
     * Checks if an {@link User} is being ignored in this channel
     * 
     * @param user
     *            the {@link User} to check
     * @return {@code true} if {@link User} is being ignored, {@code false} otherwise
     */
    public final boolean isUserIgnored(User user) {
        return ignored.contains(user);
    }

    /**
     * Adds a {@link User} to the Ignore List
     * 
     * @param user
     *            the {@link User} to ignore
     */
    public final void ignoreUser(User user) {
        ignored.add(user);
    }

    /**
     * Unignores an {@link User}
     * 
     * @param user
     *            the {@link User} to unignore
     */
    public final void unIgnoreUser(User user) {
        if (isUserIgnored(user)) {
            ignored.remove(user);
        }
    }

    /**
     * Gets an unmodifiable {@link List} of all ignored {@link User}s in this channel
     * 
     * @return an unmodifiable {@link List} of ignored {@link Users}
     */
    public final List<User> getIgnoreList() {
        return Collections.unmodifiableList(ignored);
    }

    private final void setUsersList(ArrayList<User> users) {
        this.users = users;
    }

    private final void setIgnoredUsersList(ArrayList<User> ignored) {
        this.ignored = ignored;
    }

    public void addChanMode(ChannelMode mode) {
        if (!modes.contains(mode)) {
            this.modes.add(mode);
        }
    }

    public void removeChannelMode(ChannelMode mode) {
        if (modes.contains(mode)) {
            modes.remove(mode);
        }
    }

    public boolean acceptsExternalMessages() {
        return !modes.contains(ChannelMode.NO_EXTERNAL_MESSAGES);
    }

    public void addBan(Ban ban) {
        if (!banned.contains(ban)) {
            banned.add(ban);
        }
    }

    public void removeBan(Ban ban) {
        if (banned.contains(ban)) {
            banned.remove(ban);
        }
    }

    /**
     * Set the mode of a channel. This method attempts to set the mode of a
     * channel. This may require the bot to have operator status on the channel.
     * For example, if the bot has operator status, we can grant operator status
     * to "Dave" on the #cs channel by calling setMode("#cs", "+o Dave"); An
     * alternative way of doing this would be to use the op method.
     * 
     * @param mode
     *            The new mode to apply to the channel. This may include zero or
     *            more arguments if necessary.
     */
    public final void setMode(String mode) {
        irc_conn.sendRawLine("MODE ".concat(getName()).concat(" ").concat(mode));
    }

    /**
     * Bans a user from a channel. This may be used in conjunction with the kick
     * method to permanently remove a user from a channel. Successful use of
     * this method may require the bot to have operator status itself.
     * 
     * @param user
     *            the User to ban from the channel
     */
    public final void banUser(User user) {
        setMode("+b ".concat(user.getHostMask()));
    }

    /**
     * Unbans a user from a channel. An example of a valid hostmask is
     * "nick!login@host.or.ip". Successful use of this method may require the bot
     * to have operator status itself.
     * 
     * @param channel
     *            The channel to unban the user from.
     * @param hostmask
     *            A hostmask representing the user we're unbanning.
     */
    public final void unBanUser(User user) {
        setMode("-b ".concat(user.getHostMask()));
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
    public final void op(User user) {
        setMode("+o ".concat(user.getNick()));
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
    public final void deOp(User user) {
        setMode("-o ".concat(user.getNick()));
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
    public final void voice(User user) {
        setMode("+v ".concat(user.getNick()));
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
    public final void deVoice(User user) {
        setMode("-v ".concat(user.getNick()));
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
    public final void kick(User user) {
        this.kick(user, "kick.genericReason");
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
    public final void kick(User user, String reason) {
        irc_conn.sendRawLine("KICK ".concat(name).concat(" ").concat(user.getNick()).concat(" :").concat(reason));
    }

    public boolean isBotOp() {
        return botModes.get("OP").booleanValue();
    }

    public void opBot() {
        botModes.put("OP", Boolean.TRUE);
    }

    public void deOpBot() {
        botModes.put("OP", Boolean.FALSE);
    }

    public boolean isBotVoice() {
        return botModes.get("VOICE").booleanValue();
    }

    public void voiceBot() {
        botModes.put("VOICE", Boolean.TRUE);
    }

    public void deVoiceBot() {
        botModes.put("VOICE", Boolean.FALSE);
    }

    /**
     * Checks if an Object is equal to this Channel
     * 
     * @param obj
     *            the reference object with which to compare.
     * @return
     *         {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     * @see Object#equals(Object)
     */
    @Override
    public final boolean equals(Object obj) {
        if (!(obj instanceof Channel)) {
            return false;
        }
        Channel other = (Channel) obj;
        if (!name.equals(other.getName())) {
            return false;
        }
        if (!topic.equals(other.getTopic())) {
            return false;
        }
        return true;
    }

    public final String toString() {
        return String.format("Channel[Name=%s Topic=%s Muted=%b Users=%s Ignored Users=%s]", name, topic.toString(), muted, Arrays.toString(users.toArray()), Arrays.toString(ignored.toArray()));
    }

    public final int hashcode() {
        int hash = 7;
        hash = 31 * hash + name.hashCode();
        hash = 31 * hash + (topic != null ? topic.hashCode() : 0);
        hash = 31 * hash + Boolean.valueOf(muted).hashCode();
        hash = 31 * hash + users.hashCode();
        hash = 31 * hash + ignored.hashCode();
        return hash;
    }

    public final Channel clone() {
        Channel cloned = new Channel(name, irc_conn);
        cloned.setTopic(topic);
        cloned.setUsersList(users);
        cloned.setIgnoredUsersList(ignored);
        if (muted) {
            cloned.toggleMute();
        }
        return cloned;
    }
}
