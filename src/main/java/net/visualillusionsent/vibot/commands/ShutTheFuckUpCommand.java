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
 * Shut The Fuck Up Command<br>
 * Quiets the {@link VIBot} in a {@link Channel}<br>
 * <b>Usage:</b> !stfu<br>
 * <b>Minimum Params:</b> 0<br>
 * <b>Maximum Params:</b> 0<br>
 * <b>Requires:</b> Op Channel<br>
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
@BotCommand(main = "stfu", usage = "!stfu", desc = "Quiets the Bot in a channel", maxParam = 0, op = true, chanOnly = true)
public final class ShutTheFuckUpCommand extends BaseCommand {

    /**
     * Constructs a new {@code ShutTheFuckUpCommand}
     */
    public ShutTheFuckUpCommand(BotPlugin fake) {
        super(fake);
    }

    @Override
    public final synchronized boolean execute(Channel channel, User user, String[] args) {
        if (!channel.isMuted()) {
            channel.mute();
        }
        return true;
    }
}
