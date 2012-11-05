package net.visualillusionsent.vibot;

import java.util.ArrayList;

/**
 * Channel container class
 * 
 * @author darkdiplomat
 */
public final class Channel {
    private ArrayList<User> users = new ArrayList<User>();
    private String name, topic;
    private boolean mute = false;

    Channel(String name) {
        this.name = name;
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

    public final String getTopic() {
        return this.topic;
    }

    public final String getName() {
        return name;
    }

    public final User getUser(String name) {
        for (User user : users) {
            if (user.getNick().equals(name)) {
                return user;
            }
        }
        return null;
    }

    public final ArrayList<User> getUsers() {
        return users;
    }

    public final void sendMessage(String message) {
        if (this.name.equals("CONSOLE")) {
            System.out.println(message);
        } else {
            VIBotMain.bot.sendMessage(name, message);
        }
    }

    public final void sendAction(String action) {
        if (this.name.equals("CONSOLE")) {
            System.out.println(VIBotMain.bot.getNick() + " " + action);
        } else {
            VIBotMain.bot.sendAction(this.getName(), action);
        }
    }

    public final boolean isMuted() {
        return mute;
    }

    public final void toggleMute() {
        mute = !mute;
    }

    public final boolean equals(String name) {
        if (name.equals(this.name)) {
            return true;
        }
        return false;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Channel) {
            return ((Channel) obj).hashCode() == hashCode();
        }
        return false;
    }

    public int hashcode() {
        return name.hashCode();
    }
}
