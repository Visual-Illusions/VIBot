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

import net.visualillusionsent.utils.StringUtils;
import net.visualillusionsent.utils.UtilityException;
import net.visualillusionsent.vibot.api.commands.BaseCommand;
import net.visualillusionsent.vibot.api.commands.BotCommand;
import net.visualillusionsent.vibot.api.plugin.BotPlugin;
import net.visualillusionsent.vibot.io.irc.Channel;
import net.visualillusionsent.vibot.io.irc.User;

/**
 * Echo Command<br>
 * Echoes a given message back to the channel<br>
 * <b>Usage:</b> !echo {@literal <message>}<br>
 * <b>Minimum Params:</b> 1<br>
 * <b>Maximum Params:</b> &infin;<br>
 * <b>Requires:</b> Voice Channel<br>
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
@BotCommand(main = "echo", usage = "!echo <message>", desc = "Echoes the message", minParam = 1, voice = true, chanOnly = true)
public final class EchoCommand extends BaseCommand {

    /**
     * Constructs a new {@code EchoCommand}
     */
    public EchoCommand(BotPlugin fake) {
        super(fake);
    }

    @Override
    public final boolean execute(Channel channel, User user, String[] args) {
        String message = "";
        try {
            message = StringUtils.joinString(args, " ", 0);
        }
        catch (UtilityException e) {
            message = e.getMessage(); //This shouldn't ever happen but you never know...
        }
        channel.sendMessage(message);
        return true;
    }
}
