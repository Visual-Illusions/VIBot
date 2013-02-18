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

import net.visualillusionsent.vibot.io.configuration.BotConfig;
import net.visualillusionsent.vibot.io.logging.BotLogMan;

/**
 * Channel class<br>
 * IRC User helper. Stores information about a user.
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
public final class User {
    private final IRCConnection irc_conn;
    private String prefix, nick, hostname, login;
    public static User BOT_CONSOLE = new User("$", "BOT", null, null, null);

    public User(String prefix, String nick, String hostname, String login, IRCConnection irc_conn) {
        this.prefix = prefix;
        this.nick = nick;
        this.irc_conn = irc_conn;
    }

    /**
     * Gets the nick name of this User
     * 
     * @return the nick of the User
     */
    public final String getNick() {
        return nick;
    }

    /**
     * Gets the prefix for this User
     * 
     * @return the prefix for this User
     */
    public final String getPrefix() {
        return prefix;
    }

    /**
     * Gets the HostName for this user
     * 
     * @return the host name
     */
    public final String getHostname() {
        return hostname;
    }

    /**
     * Gets the login name for this User
     * 
     * @return the Login name
     */
    public String getLogin() {
        return login;
    }

    /**
     * Gets whether the User is an IRC Server Owner
     * 
     * @return {@code true} if owner, {@code false} otherwise
     */
    public final boolean isIRCServerOwner() {
        return prefix.indexOf('~') >= 0;
    }

    /**
     * Gets whetehr the User is an IRC Server Admin
     * 
     * @return {@code true} if admin, {@code false} otherwise
     */
    public final boolean isIRCServerAdmin() {
        return prefix.indexOf('&') >= 0;
    }

    /**
     * Gets whether the User is a Channel Operator or not
     * 
     * @return {@code true} if op, {@code false} otherwise
     */
    public boolean isOp() {
        return prefix.indexOf('@') >= 0;
    }

    /**
     * Gets whether the User is a Channel Half-Operator or not
     * 
     * @return {@code true} if half-op, {@code false} otherwise
     */
    public final boolean isHalfOp() {
        return prefix.indexOf('%') >= 0;
    }

    /**
     * Gets whether the User has Voice in the Channel
     * 
     * @return {@code true} if Voiced, {@code false} otherwise
     */
    public boolean hasVoice() {
        return prefix.indexOf('+') >= 0;
    }

    /**
     * Gets whether the User is the Console or not
     * 
     * @return {@code true} if Console, {@code false} otherwise
     */
    public boolean isConsole() {
        return prefix.indexOf('$') == 0;
    }

    /**
     * Gets whether the User is an Owner of the Bot or not
     * 
     * @return {@code true} if Owner, {@code false} otherwise
     */
    public boolean isBotOwner() {
        for (String owner : BotConfig.getBotOwners()) {
            if (owner.equals(nick)) {
                return true;
            }
        }
        return false;
    }

    public void sendMessage(String message) {
        if (irc_conn != null) {
            irc_conn.sendMessage(nick, message);
        }
        else {
            BotLogMan.consoleMessage(message);
        }
    }

    public void sendNotice(String message) {
        if (irc_conn != null) {
            irc_conn.sendNotice(nick, message);
        }
        else {
            BotLogMan.consoleMessage(message);
        }
    }

    boolean hasNoHost() {
        return hostname == null;
    }

    boolean hasNoLogin() {
        return login == null;
    }

    void setNick(String nick) {
        this.nick = nick;
    }

    public void voice() {
        prefix = prefix.concat("+");
    }

    public void op() {
        prefix = prefix.concat("@");
    }

    public void deVoice() {
        prefix = prefix.replace("+", "");
    }

    public void deOp() {
        prefix = prefix.replace("@", "");
    }

    public void setHost(String hostname) {
        this.hostname = hostname;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String toString() {
        return String.format("User[Nick=%s Prefix=%s Host=%s Login=%s]", nick, prefix, hostname, login);
    }

    public boolean equals(String nick) {
        return this.nick.equals(nick);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof User)) {
            return false;
        }
        User other = (User) obj;
        if (!other.getNick().equals(this.nick)) {
            return false;
        }
        if (!other.getPrefix().equals(this.prefix)) {
            return false;
        }
        if (!hasNoHost() && !other.hasNoHost()) {
            if (!other.getHostname().equals(this.hostname)) {
                return false;
            }
        }
        if (!hasNoLogin() && !other.hasNoLogin()) {
            if (!other.getLogin().equals(this.login)) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + prefix.hashCode();
        hash = 31 * hash + nick.hashCode();
        hash = 31 * hash + hostname != null ? hostname.hashCode() : 0;
        hash = 31 * hash + login != null ? login.hashCode() : 0;
        return hash;
    }

    public String getHostMask() {
        return String.format("%s!%s@%s", nick, login != null ? login : "*", hostname != null ? hostname : "*");
    }
}
