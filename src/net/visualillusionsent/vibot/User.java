package net.visualillusionsent.vibot;

import net.visualillusionsent.vibot.io.configuration.BotConfig;
import net.visualillusionsent.vibot.io.logging.BotLogMan;

public final class User {
    private final VIBot bot;
    private String prefix, nick, hostname, login;

    User(String prefix, String nick, VIBot bot) {
        this.prefix = prefix;
        this.nick = nick;
        this.bot = bot;
    }

    public User(String prefix, String nick) {
        this.prefix = prefix;
        this.nick = nick;
        this.bot = null;
    }

    public final String getNick() {
        return nick;
    }

    public final String getPrefix() {
        return prefix;
    }

    public final String getHostname() {
        return hostname;
    }

    public String getLogin() {
        return login;
    }

    //    ~ for owners – to get this, you need to be +q in the channel
    //    & for admins – to get this, you need to be +a in the channel
    //    @ for full operators – to get this, you need to be +o in the channel
    //    % for half operators – to get this, you need to be +h in the channel
    //    + for voiced users – to get this, you need to be +v in the channel

    public boolean isOp() {
        return prefix.indexOf('@') >= 0;
    }

    public boolean hasVoice() {
        return prefix.indexOf('+') >= 0;
    }

    public boolean isConsole() {
        return prefix.indexOf('$') == 0;
    }

    public boolean isBotOwner() {
        for (String owner : BotConfig.getBotOwners()) {
            if (owner.equals(nick)) {
                return true;
            }
        }
        return false;
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

    void Voice() {
        prefix = prefix.concat("+");
    }

    void Op() {
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

    public String toString() {
        return String.format("User[Nick=%s Prefix=%s]", nick, prefix);
    }

    public boolean equals(String nick) {
        return this.nick.equals(nick);
    }

    public boolean equals(Object o) {
        if (o instanceof User) {
            User other = (User) o;
            return other.hashCode() == hashCode();
        }
        return false;
    }

    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + prefix.hashCode();
        hash = 31 * hash + nick.hashCode();
        return hash;
    }
}
