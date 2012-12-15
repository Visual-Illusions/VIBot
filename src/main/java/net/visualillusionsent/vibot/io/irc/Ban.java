package net.visualillusionsent.vibot.io.irc;

public final class Ban {

    private final Channel channel;
    private final User banned, bannedBy;
    private final String datetime;

    public Ban(Channel channel, User banned, User bannedBy, String datetime) {
        this.channel = channel;
        this.banned = banned;
        this.bannedBy = bannedBy;
        this.datetime = datetime;
    }

    public final Channel getChannel() {
        return channel;
    }

    public final User getBanned() {
        return banned;
    }

    public final User getBannedBy() {
        return bannedBy;
    }

    public final String getDateTime() {
        return datetime;
    }

    public final boolean equals(Object obj) {
        if (!(obj instanceof Ban)) {
            return false;
        }
        Ban theBan = (Ban) obj;
        if (!theBan.getBanned().equals(banned)) {
            return false;
        }
        if (!theBan.getBannedBy().equals(bannedBy)) {
            return false;
        }
        if (!theBan.getDateTime().equals(datetime)) {
            return false;
        }
        return true;
    }

    public final int hashCode() {
        int hash = 7;
        hash = 31 * hash + banned.hashCode();
        hash = 31 * hash + bannedBy.hashCode();
        hash = 31 * hash + datetime.hashCode();
        return hash;
    }

    public final String toString() {
        return String.format("Ban[User=%s BannedBy=%s Channel=%s DateTime=%s", banned.getNick(), bannedBy.getNick(), channel.getName(), datetime);
    }

}
