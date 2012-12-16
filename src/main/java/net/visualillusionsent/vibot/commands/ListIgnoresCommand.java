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
package net.visualillusionsent.vibot.commands;

import java.util.List;

import net.visualillusionsent.vibot.api.BaseCommand;
import net.visualillusionsent.vibot.io.irc.Channel;
import net.visualillusionsent.vibot.io.irc.User;

public final class ListIgnoresCommand extends BaseCommand {

    public ListIgnoresCommand() {
        super(null, new String[] { "listignore" }, "!listignore", "List of users being ignored in the channel", 1, -1, false, true, false);
    }

    @Override
    public final synchronized boolean execute(Channel channel, User user, String[] args) {
        List<User> ignore = channel.getIgnoreList();
        if (!ignore.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (User igno : ignore) {
                sb.append(igno.getNick());
                sb.append(", ");
            }
            channel.sendMessage("I am ignoring: ".concat(sb.toString().trim()));
        }
        else {
            channel.sendMessage("I'm not ignoring anyone...");
        }
        return true;
    }
}
