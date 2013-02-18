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

import net.visualillusionsent.vibot.VIBot;
import net.visualillusionsent.vibot.api.commands.BaseCommand;
import net.visualillusionsent.vibot.api.commands.BotCommand;
import net.visualillusionsent.vibot.api.plugin.BotPlugin;
import net.visualillusionsent.vibot.io.irc.Channel;
import net.visualillusionsent.vibot.io.irc.User;

/**
 * Join Command<br>
 * Tells the {@link VIBot} to join a {@link Channel}<br>
 * <b>Usage:</b> !join {@literal <channel>}<br>
 * <b>Minimum Params:</b> 2<br>
 * <b>Maximum Params:</b> 2<br>
 * <b>Requires:</b> BotOwner<br>
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
@BotCommand(main = "join", usage = "!join <channel>", desc = "Joins a channel if exists and can", owner = true)
public final class JoinChannelCommand extends BaseCommand {

    /**
     * Constructs a new {@code JoinChannelCommand}
     */
    public JoinChannelCommand(BotPlugin fake) {
        super(fake);
    }

    @Override
    public final synchronized boolean execute(Channel channel, User user, String[] args) {
        if (!args[1].startsWith("#")) {
            user.sendNotice("Channels need to start with a '#'!");
        }
        else {
            user.sendNotice("Attempting to join Channel: '" + args[1] + "'");
            VIBot.joinChannel(args[1]);
        }
        return true;
    }
}
