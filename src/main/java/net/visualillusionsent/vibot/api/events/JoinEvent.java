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
package net.visualillusionsent.vibot.api.events;

import net.visualillusionsent.vibot.api.plugin.BotPlugin;
import net.visualillusionsent.vibot.io.irc.Channel;
import net.visualillusionsent.vibot.io.irc.User;

/**
 * Join Event
 * <p>
 * Extending this class will allow a {@link BotPlugin}<br>
 * to listen to when a {@link User} joins a {@link Channel}
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
public abstract class JoinEvent extends BaseEvent {

    /**
     * Constructs a new {@code JoinEvent}<br>
     * <b>NOTE:</b>With this constructor, {@link EventPriority} will be read from the plugin.cfg file
     * 
     * @param plugin
     *            the {@link BotPlugin} associated with this event
     */
    public JoinEvent(BotPlugin plugin) {
        super(plugin, EventType.JOIN);
    }

    /**
     * Constructs a new {@code JoinEvent}
     * 
     * @param plugin
     *            the {@link BotPlugin} associated with this event
     * @param priority
     *            the {@link EventPriority} for the event
     */
    public JoinEvent(BotPlugin plugin, EventPriority priority) {
        super(plugin, priority, EventType.JOIN);
    }

    /**
     * Event executor<br>
     * This is where the code should go for extending classes to handle the {@code JoinEvent}
     * 
     * @param channel
     *            the {@link Channel} being joined
     * @param user
     *            the {@link user} joining
     */
    public abstract void execute(Channel channel, User user);
}
