package net.visualillusionsent.vibot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.visualillusionsent.vibot.io.logging.BotLogMan;

/**
 * Channel class<br>
 * IRC Channel helper. Stores information like topic and users in the channel.
 * 
 * @since VIBot 1.0
 * @author Jason (darkdiplomat)
 */
public final class Channel {

    private final String name;
    private final VIBot bot;
    private String topic;
    private ArrayList<User> users = new ArrayList<User>();
    private ArrayList<User> ignored = new ArrayList<User>();
    private boolean muted = false;
    

    Channel(String name, VIBot bot) {
        this.name = name;
        this.bot = bot;
    }

    /**
     * Channel constructor, though not meant to be constructed outside of internal code
     * @param name
     * the name of the channel to construct
     */
    public Channel(String name) {
        this.name = name;
        this.bot = null;
    }

    final void addUser(User user) {
        users.add(user);
    }

    final void removeUser(User user) {
        users.remove(user);
    }

    final void renameUser(User user, String newnick) {
        users.get(users.indexOf(user)).setNick(newnick);
    }

    final void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * Gets the topic for this channel
     * 
     * @return topic the topic for this channel
     */
    public final String getTopic() {
        return this.topic;
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
            bot.sendMessage(name, message);
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
            BotLogMan.consoleMessage(bot.getNick().concat(" ").concat(action));
        }
        else {
            bot.sendAction(name, action);
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
     * @param user
     * the {@link User} to check
     * @return {@code true} if {@link User} is being ignored, {@code false} otherwise
     */
    public boolean isUserIgnored(User user){
        return ignored.contains(user);
    }
    
    /**
     * Adds a {@link User} to the Ignore List
     * @param user
     * the {@link User} to ignore
     */
    public void ignoreUser(User user){
        ignored.add(user);
    }
    
    /**
     * Unignores an {@link User}
     * @param user
     * the {@link User} to unignore
     */
    public void unIgnoreUser(User user){
        if(isUserIgnored(user)){
            ignored.remove(user);
        }
    }
    
    /**
     * Gets an unmodifiable {@link List} of all ignored {@link User}s in this channel
     * 
     * @return an unmodifiable {@link List} of ignored {@link Users}
     */
    public List<User> getIgnoreList(){
        return Collections.unmodifiableList(ignored);
    }

    public final boolean equals(String name) {
        if (name.equals(this.name)) {
            return true;
        }
        return false;
    }

    public final boolean equals(Object obj) {
        if (!(obj instanceof Channel)) {
            return false;
        }
        Channel other = (Channel) obj;
        if (!name.equals(other.getName())) {
            return false;
        }
        return true;
    }

    public String toString() {
        return String.format("Channel[Name=%s Topic=%s Muted=%b Users=%s Ignored Users=%s]", name, topic, muted, Arrays.toString(users.toArray()), Arrays.toString(ignored.toArray()));
    }

    public int hashcode() {
        return 0;
    }
}
