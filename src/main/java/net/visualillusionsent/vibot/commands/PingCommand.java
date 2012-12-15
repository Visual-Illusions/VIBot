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

import net.visualillusionsent.utils.DateUtils;
import net.visualillusionsent.utils.UtilityException;
import net.visualillusionsent.vibot.api.plugin.BaseCommand;
import net.visualillusionsent.vibot.io.irc.Channel;
import net.visualillusionsent.vibot.io.irc.User;

public final class PingCommand extends BaseCommand {

    public PingCommand() {
        super(null, new String[] { "ping" }, "!ping", "Sends a Pong", 1, -1, false, false, false);
    }

    @Override
    public final synchronized boolean execute(Channel channel, User user, String[] args) {
        try {
            channel.sendMessage("PONG: ".concat(DateUtils.longToDate(System.currentTimeMillis()).toString()));
        }
        catch (UtilityException ue) {
            //Shouldn't happen but just incase
            channel.sendMessage("PONG: ".concat(ue.getLocalizeMessage()));
        }
        return true;
    }

}
