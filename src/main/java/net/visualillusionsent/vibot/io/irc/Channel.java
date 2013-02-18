/* 
 * Copyright 2012 - 2013 Visual Illusions Entertainment.
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
 * You should have received a copy of the GNU Lesser General Public License along with VIBot.
 * If not, see http://www.gnu.org/licenses/lgpl.html
 */
package net.visualillusionsent.vibot.io.irc;

import java.util.ArrayList;
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
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
public final class Channel {

    /**
     * The name of the Channel
     */
    private final String name;

    /**
     * The {@link IRCConnection} instance
     */
    private final IRCConnection irc_conn;

    /**
     * The {@link Topic} for the {@code Channel}
     */
    private Topic topic;

    /**
     * The {@link ArrayList} of {@link User}s for the {@code Channel}
     */
    private final ArrayList<User> users;

    /**
     * The {@link ArrayList} of {@link User}s being ignored in the {@code Channel}
     */
    private final ArrayList<User> ignored;

    /**
     * The {@link ArrayList} of banned {@link User}s
     */
    private final ArrayList<Ban> bans;

    /**
     * The {@link ArrayList} of {@link ChannelMode}s
     */
    private final ArrayList<ChannelMode> modes;

    /**
     * The {@link HashMap} of the {@link VIBot} modes
     */
    private final HashMap<String, Boolean> botModes;

    /**
     * Whether the {@code Channel} is muted or not
     */
    private boolean muted = false;

    /**
     * CONSOLE {@code Channel} instance
     */
    public static final Channel CONSOLE;

    /**
     * The default {@link Map} of allowed {@link VIBot} modes
     */
    private static final Map<String, Boolean> allowedBotModes;

    static {
        HashMap<String, Boolean> temp = new HashMap<String, Boolean>();
        temp.put("OP", Boolean.FALSE);
        temp.put("VOICE", Boolean.FALSE);
        allowedBotModes = Collections.unmodifiableMap(temp);
        CONSOLE = new Channel("CONSOLE", null);
    }

    /**
     * Constructs a new {@code Channel} object
     * <p>
     * {@code Channel} should not be constructed outside of {@link VIBot}
     * 
     * @param name
     *            the name of the {@code Channel}
     * @param irc_conn
     *            the IRCConnection instance
     */
    public Channel(String name, IRCConnection irc_conn) {
        this.name = name;
        this.irc_conn = irc_conn;
        this.users = new ArrayList<User>();
        this.ignored = new ArrayList<User>();
        this.modes = new ArrayList<ChannelMode>();
        this.bans = new ArrayList<Ban>();
        this.botModes = new HashMap<String, Boolean>(allowedBotModes.size());
        botModes.putAll(allowedBotModes);
    }

    /**
     * Private Constructor used in cloning
     * 
     * @param name
     *            the name of the {@code Channel}
     * @param irc_conn
     *            the IRCConnection instance
     * @param topic
     *            the {@link Topic} for the {@code Channel}
     * @param users
     *            the {@link ArrayList} of {@link User}s
     * @param ignored
     *            the {@link ArrayList} of ignored {@link User}s
     * @param modes
     *            the {@link ArrayList} of {@link ChannelMode}s
     * @param bans
     *            the {@link ArrayList} of {@link User}s banned in the {@code Channel}
     * @param botModes
     *            the {@link HashMap} of {@link VIBot} modes
     */
    private Channel(String name, IRCConnection irc_conn, Topic topic, ArrayList<User> users, ArrayList<User> ignored, ArrayList<ChannelMode> modes, ArrayList<Ban> bans, HashMap<String, Boolean> botModes, boolean muted) {
        this.name = name;
        this.irc_conn = irc_conn;
        this.topic = topic;
        this.users = users;
        this.ignored = ignored;
        this.modes = modes;
        this.bans = bans;
        this.botModes = botModes;
        this.muted = muted;
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
     * Gets the topic for the {@code Channel}
     * 
     * @return the topic for the {@code Channel}
     */
    public final Topic getTopic() {
        return topic;
    }

    /**
     * Adds a {@link User} to the {@code Channel}
     * 
     * @param user
     *            the {@link User} to be added
     */
    public final void addUser(User user) {
        users.add(user);
    }

    /**
     * Removes a {@link User} from the {@code Channel}
     * 
     * @param user
     *            the {@link User} to be removed
     */
    public final void removeUser(User user) {
        users.remove(user);
    }

    /**
     * Renames a {@link User} in the {@code Channel}
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
     * Sets the {@link Topic} for the {@code Channel}
     * 
     * @param topic
     *            the {@link Topic} to be set
     */
    public final void setTopic(Topic topic) {
        this.topic = topic;
    }

    /**
     * Set the topic for the {@code Channel}. This method attempts to set the topic of a
     * channel. This may require the bot to have operator status if the topic is
     * protected.
     * 
     * @param topic
     *            The new topic for the {@code Channel}.
     */
    public final void setNewTopic(String topic) {
        irc_conn.sendRawLine("TOPIC ".concat(name).concat(" :").concat(topic));
    }

    /**
     * Gets an unmodifiable {@link List} of all {@link User}s in this channel
     * 
     * @return an unmodifiable {@link List} of {@link User}s
     */
    public final List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }

    /**
     * Sends the {@code Channel} a message
     * 
     * @param message
     *            the message to send to the {@code Channel}
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
     * Sends an Action to the {@code Channel} (like /me dosomething)
     * 
     * @param action
     *            the action to send the {@code Channel}
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
     * Checks if the {@code Channel} is muted
     * 
     * @return {@code true} if muted, {@code false} otherwise
     */
    public final boolean isMuted() {
        return muted;
    }

    /**
     * Un-Mutes the {@code Channel}
     */
    public final void unMute() {
        muted = false;
    }

    /**
     * Mutes the {@code Channel}
     */
    public final void mute() {
        muted = true;
    }

    /**
     * Checks if an {@link User} is being ignored in the {@code Channel}
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
     * @return an unmodifiable {@link List} of ignored {@link User}s
     */
    public final List<User> getIgnoreList() {
        return Collections.unmodifiableList(ignored);
    }

    /**
     * Adds a {@link ChannelMode} to the list of modes
     * 
     * @param mode
     *            the {@link ChannelMode} to add
     */
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

    public boolean isPrivate() {
        return modes.contains(ChannelMode.PRIVATE);
    }

    public void addBan(Ban ban) {
        if (!bans.contains(ban)) {
            bans.add(ban);
        }
    }

    public void removeBan(Ban ban) {
        if (bans.contains(ban)) {
            bans.remove(ban);
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
     *            the {@link User} to ban from the channel
     */
    public final void banUser(User user) {
        setMode("+b ".concat(user.getHostMask()));
    }

    /**
     * Unbans a user from a channel.<br>
     * Successful use of this method may require the bot to have operator status itself.
     * 
     * @param user
     *            the {@link User} to unban from the {@code Channel}
     */
    public final void unBanUser(User user) {
        setMode("-b ".concat(user.getHostMask()));
    }

    /**
     * Grants operator privilidges to a user on a channel. Successful use of
     * this method may require the bot to have operator status itself.
     * 
     * @param user
     *            the {@link User} to op.
     */
    public final void op(User user) {
        setMode("+o ".concat(user.getNick()));
    }

    /**
     * Removes operator privilidges from a user on a channel. Successful use of
     * this method may require the bot to have operator status itself.
     * 
     * @param user
     *            the {@link User} to de-op
     */
    public final void deOp(User user) {
        setMode("-o ".concat(user.getNick()));
    }

    /**
     * Grants voice privilidges to a user on a channel. Successful use of this
     * method may require the bot to have operator status itself.
     * 
     * @param user
     *            the {@link User} to voice
     */
    public final void voice(User user) {
        setMode("+v ".concat(user.getNick()));
    }

    /**
     * Removes voice privilidges from a user on a channel. Successful use of
     * this method may require the bot to have operator status itself.
     * 
     * @param user
     *            the {@link User} to de-voice
     */
    public final void deVoice(User user) {
        setMode("-v ".concat(user.getNick()));
    }

    /**
     * Kicks a user from a channel. This method attempts to kick a user from a
     * channel and may require the bot to have operator status in the channel.
     * 
     * @param user
     *            the {@link User} to kick
     */
    public final void kick(User user) {
        this.kick(user, "kick.genericReason");
    }

    /**
     * Kicks a user from a channel, giving a reason. This method attempts to
     * kick a user from a channel and may require the bot to have operator
     * status in the channel.
     * 
     * @param user
     *            the {@link User} to kick
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
        if (!Boolean.valueOf(muted).equals(Boolean.valueOf(other.isMuted()))) {
            return false;
        }
        if (!topic.equals(other.getTopic())) {
            return false;
        }
        return true;
    }

    public final String toString() {
        return String.format("Channel[Name=%s]", name);
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
        return new Channel(name, irc_conn, topic, users, ignored, modes, bans, botModes, muted);
    }
}
