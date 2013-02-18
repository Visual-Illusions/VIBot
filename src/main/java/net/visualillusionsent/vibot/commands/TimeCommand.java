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
package net.visualillusionsent.vibot.commands;

import java.util.TimeZone;

import net.visualillusionsent.utils.DateUtils;
import net.visualillusionsent.vibot.VIBot;
import net.visualillusionsent.vibot.api.commands.BaseCommand;
import net.visualillusionsent.vibot.api.commands.BotCommand;
import net.visualillusionsent.vibot.api.plugin.BotPlugin;
import net.visualillusionsent.vibot.io.irc.Channel;
import net.visualillusionsent.vibot.io.irc.User;

/**
 * Time Command<br>
 * Gives the current time at the {@link VIBot} location, or time in the specified TimeZone<br>
 * <b>Usage:</b> !time [TimeZone]<br>
 * <b>Minimum Params:</b> 1<br>
 * <b>Maximum Params:</b> 2<br>
 * <b>Requires:</b> n/a<br>
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
@BotCommand(main = "time", usage = "!time [TimeZone]", desc = "Shows the current time", maxParam = 2)
public final class TimeCommand extends BaseCommand {
    private final String print = "The current time in TimeZone: '%s' is %s";

    public TimeCommand(BotPlugin fake) {
        super(fake);
    }

    @Override
    public final synchronized boolean execute(Channel channel, User user, String[] args) {
        long current = System.currentTimeMillis();
        if (args.length > 1) {
            TimeZone zone = TimeZone.getTimeZone(args[1]);
            if (channel != null) {
                channel.sendMessage(String.format(print, zone.getID(), DateUtils.longToTimeDate(current, zone), ""));
            }
            else {
                user.sendMessage(String.format(print, zone.getID(), DateUtils.longToTimeDate(current, zone), ""));
            }
        }
        else {
            if (channel != null) {
                channel.sendMessage("The current time where I am is : " + DateUtils.longToTimeDate(current));
            }
            else {
                user.sendMessage("The current time where I am is : " + DateUtils.longToTimeDate(current));
            }
        }
        return true;
    }
}
