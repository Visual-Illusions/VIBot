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
package net.visualillusionsent.vibot.api.plugin.events;

import net.visualillusionsent.utils.UtilityException;
import net.visualillusionsent.vibot.api.plugin.BotPlugin;
import net.visualillusionsent.vibot.io.exception.VIBotException;

public abstract class BaseEvent {
    private final BotPlugin plugin;
    private final EventType type;
    private final EventPriority priority;

    public BaseEvent(BotPlugin plugin, EventType type) {
        if (type == null) {
            throw new VIBotException("EventType cannot be null");
        }
        this.plugin = plugin;
        this.type = type;
        if (plugin == null) {
            priority = EventPriority.LOW;
        }
        else {
            try {
                priority = EventPriority.valueOf(plugin.getPluginConfiguration().getString(getClass().getSimpleName().concat(".priority")));
            }
            catch (UtilityException ue) {
                throw new VIBotException("Unable to read Priority from plugin.cfg", ue);
            }
            catch (IllegalArgumentException iae) {
                throw new VIBotException("Invaild argument for ".concat(getClass().getSimpleName()).concat(".priority"));
            }
        }
        EventManager.addEvent(this);
    }

    public BaseEvent(BotPlugin plugin, EventPriority priority, EventType type) {
        if (priority == null) {
            throw new VIBotException("Priority cannot be null");
        }
        if (type == null) {
            throw new VIBotException("EventType cannot be null");
        }
        this.plugin = plugin;
        this.type = type;
        this.priority = EventPriority.LOW;
        EventManager.addEvent(this);
    }

    public BotPlugin getPlugin() {
        return plugin;
    }

    public EventType getType() {
        return type;
    }

    public EventPriority getPriority() {
        return priority;
    }
}
