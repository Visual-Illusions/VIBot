package net.visualillusionsent.vibot;

import net.visualillusionsent.vibot.io.configuration.BotConfig;
import net.visualillusionsent.vibot.io.logging.BotLogMan;

/**
 * Channel class<br>
 * IRC User helper. Stores information about a user.
 * 
 * @since VIBot 1.0
 * @author Jason (darkdiplomat)
 */
public final class User {
    private final VIBot bot;
    private String prefix, nick, hostname, login;
    public static User BOT_CONSOLE = new User("$", "BOT", null);

    User(String prefix, String nick, VIBot bot) {
        this.prefix = prefix;
        this.nick = nick;
        this.bot = bot;
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

    //    ~ for owners – to get this, you need to be +q in the channel
    //    & for admins – to get this, you need to be +a in the channel
    //    @ for full operators – to get this, you need to be +o in the channel
    //    % for half operators – to get this, you need to be +h in the channel
    //    + for voiced users – to get this, you need to be +v in the channel

    /**
     * Gets whether the User is a Channel Operator or not
     * 
     * @return {@code true} if op, {@code false} otherwise
     */
    public boolean isOp() {
        return prefix.indexOf('@') >= 0;
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
        if (bot != null) {
            bot.sendMessage(nick, message);
        }
        else {
            BotLogMan.consoleMessage(message);
        }
    }

    public void sendNotice(String message) {
        if (bot != null) {
            bot.sendNotice(nick, message);
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

    void voice() {
        prefix = prefix.concat("+");
    }

    void op() {
        prefix = prefix.concat("@");
    }

    void deVoice() {
        prefix = prefix.replace("+", "");
    }

    void deOp() {
        prefix = prefix.replace("@", "");
    }

    void setHost(String hostname) {
        this.hostname = hostname;
    }

    void setLogin(String login) {
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
}
