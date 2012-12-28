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

import net.visualillusionsent.vibot.VIBot;
import net.visualillusionsent.vibot.api.commands.BaseCommand;
import net.visualillusionsent.vibot.api.commands.BotCommand;
import net.visualillusionsent.vibot.api.plugin.BotPlugin;
import net.visualillusionsent.vibot.io.irc.Channel;
import net.visualillusionsent.vibot.io.irc.User;

/**
 * Unignore User Command<br>
 * Tells the {@link VIBot} to stop ignoring a {@link User} in a specified {@link Channel}<br>
 * <b>Usage:</b> !unignore {@literal <user>}<br>
 * <b>Minimum Params:</b> 2<br>
 * <b>Maximum Params:</b> 2<br>
 * <b>Requires:</b> Op Channel<br>
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
@BotCommand(main = "unignore", usage = "!unignore <user>", desc = "Stops ignoring a user", minParam = 2, maxParam = 2, op = true, chanOnly = true)
public final class UnignoreUserCommand extends BaseCommand {

    /**
     * Constructs a new {@code UnignoreUserCommand}
     */
    public UnignoreUserCommand(BotPlugin fake) {
        super(fake);
    }

    @Override
    public final synchronized boolean execute(Channel channel, User user, String[] args) {
        User ignore = channel.getUser(args[1]);
        if (channel.isUserIgnored(ignore)) {
            channel.unIgnoreUser(ignore);
            channel.sendMessage("No longer ignoring User: ".concat(args[1]));
        }
        else {
            channel.sendMessage("I wasn't ignoring " + args[1]);
        }
        return true;
    }
}
