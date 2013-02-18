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
package net.visualillusionsent.vibot.api.events;

import java.util.Comparator;

import net.visualillusionsent.vibot.api.plugin.BotPlugin;

/**
 * Used to compare {@link BotPlugin}'s {@link BaseEvent} priorities
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
public final class EventPriorityComparator implements Comparator<BaseEvent> {

    /**
     * Constructs a new {@code EventPriorityComparator}
     */
    public EventPriorityComparator() {}

    /**
     * Compares its two {@link BaseEvent}s for order.
     * 
     * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
     */
    @Override
    public final int compare(BaseEvent a, BaseEvent b) {
        int ax = a.getPriority().ordinal();
        int bx = b.getPriority().ordinal();

        if (ax > bx) {
            return 1;
        }
        else if (ax == bx) {
            return 0;
        }
        else {
            return -1;
        }
    }
}
