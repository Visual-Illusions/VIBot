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

/**
 * Ban container for {@link User}s banned in a {@link Channel}
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
public final class Ban {

    /**
     * The {@link Channel} of the {@code Ban}
     */
    private final Channel channel;

    /**
     * The {@link User} who is banned
     */
    private final User banned;

    /**
     * The {@link User} who did the banning
     */
    private final User bannedBy;

    /**
     * The date and time of the ban
     */
    private final String datetime;

    /**
     * Constructs a new {@code Ban} object
     * 
     * @param channel
     *            the {@link Channel} of the {@code Ban}
     * @param banned
     *            the {@link User} who is banned
     * @param bannedBy
     *            the {@link User} who did the banning
     * @param datetime
     *            the date and time of the {@code Ban}
     */
    public Ban(Channel channel, User banned, User bannedBy, String datetime) {
        this.channel = channel;
        this.banned = banned;
        this.bannedBy = bannedBy;
        this.datetime = datetime;
    }

    /**
     * Gets the {@link Channel} of the {@code Ban}
     * 
     * @return the {@link Channel} of the {@code Ban}
     */
    public final Channel getChannel() {
        return channel;
    }

    /**
     * Gets the {@link User} who is banned
     * 
     * @return the {@link User} who is banned
     */
    public final User getBanned() {
        return banned;
    }

    /**
     * Gets the {@link User} who did the banning
     * 
     * @return the {@link User} who did the banning
     */
    public final User getBannedBy() {
        return bannedBy;
    }

    /**
     * Gets the date and time of the {@code Ban}
     * 
     * @return date and time
     */
    public final String getDateTime() {
        return datetime;
    }

    /**
     * Checks is an {@link Object} is equal to the {@code Ban}
     * 
     * @return {@code true} if equal; {@code false} otherwise
     * @see Object#equals(Object)
     */
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

    /**
     * Returns a hash code value for the {@code Ban}.
     * 
     * @see Object#hashCode()
     */
    public final int hashCode() {
        int hash = 7;
        hash = 31 * hash + banned.hashCode();
        hash = 31 * hash + bannedBy.hashCode();
        hash = 31 * hash + datetime.hashCode();
        return hash;
    }

    /**
     * String representation as Ban[User=%s BannedBy=%s Channel=%s DateTime=%s] format
     * 
     * @return formated string
     * @see Object#toString()
     */
    public final String toString() {
        return String.format("Ban[User=%s BannedBy=%s Channel=%s DateTime=%s]", banned.getNick(), bannedBy.getNick(), channel.getName(), datetime);
    }

}
