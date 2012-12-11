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

import net.visualillusionsent.utils.StringUtils;
import net.visualillusionsent.utils.UtilityException;
import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;
import net.visualillusionsent.vibot.VIBot;

final class PartChannelCommand extends BaseCommand {

    public PartChannelCommand() {
        super(null, new String[] { "part" }, "!part [channel] [reason]", "Makes the bot leave a channel", 1, -1, false, false, true);
    }

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        String reason = "disconnect.leaving";
        if (args.length > 2) {
            if (!args[1].startsWith("#")) {
                try {
                    reason = StringUtils.joinString(args, " ", 1);
                }
                catch (UtilityException e) {}
                VIBot.partChannel(channel.getName(), reason);
            }
            else {
                try {
                    reason = StringUtils.joinString(args, " ", 2);
                }
                catch (UtilityException e) {}

                VIBot.partChannel(args[1], reason);
            }
        }
        else if (args.length > 1) {
            if (args[1].startsWith("#")) {
                VIBot.partChannel(args[1], reason);
            }
            else {
                VIBot.partChannel(channel.getName(), reason);
            }
        }
        else {
            VIBot.partChannel(channel.getName(), reason);
        }
        return true;
    }
}
