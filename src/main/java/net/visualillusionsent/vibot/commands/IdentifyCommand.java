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
 * Identify Command<br>
 * Tells the {@link VIBot} to identify with NickServ<br>
 * <b>Usage:</b> !identify<br>
 * <b>Minimum Params:</b> 0<br>
 * <b>Maximum Params:</b> 0<br>
 * <b>Requires:</b> Owner<br>
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
@BotCommand(main = "identify", usage = "!identify", desc = "Tells the bot to identify with NickServ", maxParam = 0, owner = true)
public final class IdentifyCommand extends BaseCommand {

    /**
     * Constructs a new {@code IdentifyCommand}
     */
    public IdentifyCommand(BotPlugin fake) {
        super(fake);
    }

    @Override
    public final boolean execute(Channel channel, User user, String[] args) {
        VIBot.identify();
        return true;
    }
}
