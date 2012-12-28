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
import net.visualillusionsent.vibot.VIBot;
import net.visualillusionsent.vibot.api.commands.BaseCommand;
import net.visualillusionsent.vibot.api.commands.BotCommand;
import net.visualillusionsent.vibot.api.plugin.BotPlugin;
import net.visualillusionsent.vibot.io.irc.Channel;
import net.visualillusionsent.vibot.io.irc.User;

/**
 * Part Channel Command<br>
 * Tells the {@link VIBot} to leave a {@link Channel}<br>
 * <b>Usage:</b> !part [channel] [reason]<br>
 * <b>Minimum Params:</b> 1<br>
 * <b>Maximum Params:</b> &infin;<br>
 * <b>Requires:</b> Owner<br>
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
@BotCommand(main = "part", usage = "!part [channel] [reason]", desc = "Makes the bot leave a channel", owner = true)
public final class PartChannelCommand extends BaseCommand {

    /**
     * Constructs a new {@code PartChannelCommand}
     */
    public PartChannelCommand(BotPlugin fake) {
        super(fake);
    }

    @Override
    public final boolean execute(Channel channel, User user, String[] args) {
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
