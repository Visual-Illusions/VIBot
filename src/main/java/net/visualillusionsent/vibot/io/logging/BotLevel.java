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
package net.visualillusionsent.vibot.io.logging;

import java.util.logging.Level;

public class BotLevel extends Level {

    private static final long serialVersionUID = 180437111987L;

    public static final BotLevel INCOMING = new BotLevel("INCOMING", 1);
    public static final BotLevel OUTGOING = new BotLevel("OUTGOING", 2);
    public static final BotLevel PING = new BotLevel("PING", 3);
    public static final BotLevel SERVER_PING = new BotLevel("SERVER PING", 4);
    public static final BotLevel CHANNEL_MESSAGE = new BotLevel("CHANNEL MESSAGE", 5);
    public static final BotLevel PRIVATE_MESSAGE = new BotLevel("PRIVATE MESSAGE", 6);
    public static final BotLevel COMMAND = new BotLevel("COMMAND", 7);
    public static final BotLevel NOTICE = new BotLevel("NOTICE", 8);
    public static final BotLevel JOIN = new BotLevel("JOIN", 9);
    public static final BotLevel PART = new BotLevel("PART", 10);
    public static final BotLevel CONSOLE_MESSAGE = new BotLevel("CONSOLE MESSAGE", 11);

    public BotLevel(String name, int ordinal) {
        super(name, ordinal);
    }
}
