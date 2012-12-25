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

/**
 * Topic object for {@link Channel}s
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
public final class Topic {
    private String topic, setBy, date;

    /**
     * Constructs a new {@code Topic} object
     * 
     * @param topic
     */
    public Topic(String topic) {
        this.topic = topic;
    }

    /**
     * Gets the topic text
     * 
     * @return the topic text
     */
    public final String getTopic() {
        return topic;
    }

    final void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * Gets the nick that set the topic
     * 
     * @return the nick that set the topic
     */
    public final String getSetBy() {
        return setBy;
    }

    final void setSetBy(String setBy) {
        this.setBy = setBy;
    }

    /**
     * Gets the Date of when the topic was set
     * 
     * @return date of when the topic was set
     */
    public final String getDate() {
        return date;
    }

    /**
     * Sets the Date of when the topic was set
     * 
     * @param date
     *            the Date of when the topic was set
     */
    final void setDate(String date) {
        this.date = date;
    }

    @Override
    public final String toString() {
        return String.format("Topic[Topic=%s SetBy=%s Date=%s]", topic, setBy, date);
    }

    @Override
    public final boolean equals(Object obj) {
        if (!(obj instanceof Topic)) {
            return false;
        }
        Topic other = (Topic) obj;
        if (!topic.equals(other.getTopic())) {
            return false;
        }
        if (!setBy.equals(other.getSetBy())) {
            return false;
        }
        if (!date.equals(other.getDate())) {
            return false;
        }
        return true;
    }

    @Override
    public final int hashCode() {
        int hash = 4;
        hash = 31 * hash + topic.hashCode();
        hash = 31 * hash + setBy.hashCode();
        hash = 31 * hash + date.hashCode();
        return hash;
    }
}
