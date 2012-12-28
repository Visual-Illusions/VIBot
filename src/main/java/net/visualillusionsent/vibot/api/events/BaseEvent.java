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

import net.visualillusionsent.utils.UtilityException;
import net.visualillusionsent.vibot.api.plugin.BotPlugin;
import net.visualillusionsent.vibot.io.exception.VIBotException;

/**
 * Base Event
 * <p>
 * The Base form for an Event class<br>
 * Events are set up to auto register them selfs,<br>
 * all that needs to be done is the class to be initialized in the {@link BotPlugin}<br>
 * Example:<br>
 * <code><pre>
 * public void initialize(){
 *     new BaseEventImpl(this);
 * }
 * </code></pre>
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
public abstract class BaseEvent {

    /**
     * The {@link BotPlugin} associated with the {@code BaseEvent}
     */
    private final BotPlugin plugin;

    /**
     * The {@link EventType} of the {@code BaseEvent}
     */
    private final EventType type;

    /**
     * The {@link EventPriority} of the {@code BaseEvent}
     */
    private final EventPriority priority;

    /**
     * Constructs a new {@code BaseEvent}
     * <p>
     * This constructor requires the {@link BotPlugin} to have a {@code plugin.cfg} with the {@link EventPriority} set for it's event extensions,<br>
     * in the form of %EventClassName%.priority=PRIORITY
     * 
     * @param plugin
     *            the {@link BotPlugin} associated with the {@code BaseEvent}
     * @param type
     *            the {@link EventType} of the extending Event class
     */
    public BaseEvent(BotPlugin plugin, EventType type) {
        if (plugin == null) {
            throw new VIBotException("BotPlugin cannot be null");
        }
        else if (type == null) {
            throw new VIBotException("EventType cannot be null");
        }
        this.plugin = plugin;
        this.type = type;
        try {
            priority = EventPriority.valueOf(plugin.getPluginConfiguration().getString(getClass().getSimpleName().concat(".priority")));
        }
        catch (UtilityException ue) {
            throw new VIBotException("Unable to read Priority from plugin.cfg", ue);
        }
        catch (IllegalArgumentException iae) {
            throw new VIBotException("Invaild argument for ".concat(getClass().getSimpleName()).concat(".priority"));
        }
        EventManager.addEvent(this);
    }

    /**
     * Constructs a new {@code BaseEvent}
     * 
     * @param plugin
     *            the {@link BotPlugin} associated with the BaseEvent
     * @param priority
     *            the {@link EventPriority} of the event
     * @param type
     *            the {@link EventType} of the extending Event class
     */
    public BaseEvent(BotPlugin plugin, EventPriority priority, EventType type) {
        if (plugin == null) {
            throw new VIBotException("BotPlugin cannot be null");
        }
        else if (priority == null) {
            throw new VIBotException("Priority cannot be null");
        }
        else if (type == null) {
            throw new VIBotException("EventType cannot be null");
        }
        this.plugin = plugin;
        this.type = type;
        this.priority = EventPriority.LOW;
        EventManager.addEvent(this);
    }

    /**
     * Gets the {@link BotPlugin} associated with the {@code BaseEvent}
     * 
     * @return the {@link BotPlugin} associated with the {@code BaseEvent}
     */
    public BotPlugin getPlugin() {
        return plugin;
    }

    /**
     * Gets the {@link EventType} of the {@code BaseEvent}
     * 
     * @return the {@link EventType} of the {@code BaseEvent}
     */
    public EventType getType() {
        return type;
    }

    /**
     * Gets the {@link EventPriority} of the {@code BaseEvent}
     * 
     * @return the {@link EventPriority} of the {@code BaseEvent}
     */
    public EventPriority getPriority() {
        return priority;
    }

    /**
     * String representation as BaseEvent[ClassName=%s Type=%s] format
     * 
     * @return formated string
     * @see Object#toString()
     */
    @Override
    public final String toString() {
        return String.format("BaseEvent[ClassName=%s Type=%s Priority=%s]", this.getClass().getSimpleName(), type.name(), priority.name());
    }

    /**
     * Checks is an {@link Object} is equal to the {@code BaseEvent}
     * 
     * @return {@code true} if equal; {@code false} otherwise
     * @see Object#equals(Object)
     */
    @Override
    public final boolean equals(Object other) {
        if (!(other instanceof BaseEvent)) {
            return false;
        }
        BaseEvent that = (BaseEvent) other;
        if (this.type != that.type) {
            return false;
        }
        if (this.priority != that.priority) {
            return false;
        }
        if (this.plugin != null && !this.plugin.equals(that.getPlugin())) {
            return false;
        }
        return true;
    }

    /**
     * Returns a hash code value for the {@code BaseEvent}.
     * 
     * @see Object#hashCode()
     */
    @Override
    public final int hashCode() {
        int hash = 5;
        hash = 53 * hash + type.hashCode();
        hash = 53 * hash + plugin.hashCode();
        hash = 53 * hash + priority.hashCode();
        return hash;
    }
}
