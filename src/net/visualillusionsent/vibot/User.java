package net.visualillusionsent.vibot;

public class User {
    private String prefix, nick, hostname, login;

    User(String prefix, String nick) {
        this.prefix = prefix;
        this.nick = nick;
    }

    public String getNick() {
        return nick;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getHostname() {
        return hostname;
    }

    public String getLogin() {
        return login;
    }

    public boolean isOp() {
        return prefix.indexOf('@') >= 0;
    }

    public boolean hasVoice() {
        return prefix.indexOf('+') >= 0;
    }

    public boolean isConsole() {
        return getPrefix().indexOf('$') == 0;
    }

    public boolean isAdmin() {
        return Misc.isAdmin(nick);
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
        if (!nick.equals("BOTCONSOLE")) {
            VIBotMain.bot.sendMessage(nick, message);
        } else {
            System.out.println(message);
        }
    }

    public void sendNotice(String message) {
        if (!nick.equals("BOTCONSOLE")) {
            VIBotMain.bot.sendNotice(nick, message);
        } else {
            System.out.println(message);
        }
    }

    public String toString() {
        return this.getPrefix() + this.getNick();
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
        hash = 31 * hash + nick.hashCode();
        return hash;
    }

    /**
     * Returns the result of calling the compareTo method on lowercased nicks.
     * This is useful for sorting lists of User objects.
     * 
     * @return the result of calling compareTo on lowercased nicks.
     */
    public int compareTo(Object o) {
        if (o instanceof User) {
            User other = (User) o;
            return other.nick.compareTo(nick);
        }
        return -1;
    }
}
