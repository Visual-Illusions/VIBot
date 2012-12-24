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
    private final BotPlugin plugin;
    private final EventType type;
    private final EventPriority priority;

    /**
     * Constructs a new {@code BaseEvent}
     * <p>
     * This constructor requires the {@link BasePlugin} to have a {@code plugin.cfg} with the {@link EventPriority} set for it's event extensions,<br>
     * in the form of {EventClassName}.priority=PRIORITY
     * 
     * @param plugin
     *            the {@link BotPlugin} associated with the BaseEvent
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
}
