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
package net.visualillusionsent.vibot.events;

import java.util.Comparator;

public final class EventPriorityComparator implements Comparator<BaseEvent> {

    public EventPriorityComparator() {}

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
