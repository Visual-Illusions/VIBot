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
package net.visualillusionsent.vibot;

/**
 * @author Jason (darkdiplomat)
 */
public final class Topic {
    private String topic, setBy, date;

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

    final void setDate(String date) {
        this.date = date;
    }

    public final String toString() {
        return String.format("Topic[Topic=%s SetBy=%s Date=%s]", topic, setBy, date);
    }

}
