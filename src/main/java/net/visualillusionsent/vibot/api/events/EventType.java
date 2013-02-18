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

/**
 * Event types
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
public enum EventType {
    CHANNEL_MESSAGE, //
    CONNECT, //
    FILE_TRANSFER_FINISHED, //
    INCOMING_CHAT_REQUEST, //
    INCOMING_FILE_TRANSFER, //
    INVITE, //
    JOIN, //
    KICK, //
    PART, //
    PRIVATE_MESSAGE, //
}
