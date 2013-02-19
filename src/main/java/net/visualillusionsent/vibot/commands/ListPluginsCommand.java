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

import net.visualillusionsent.vibot.api.commands.BaseCommand;
import net.visualillusionsent.vibot.api.commands.BotCommand;
import net.visualillusionsent.vibot.api.plugin.BotPlugin;
import net.visualillusionsent.vibot.api.plugin.BotPluginLoader;
import net.visualillusionsent.vibot.io.irc.Channel;
import net.visualillusionsent.vibot.io.irc.User;

/**
 * List Plugins Command<br>
 * Gets the list of {@link BotPlugin}s<br>
 * Enabled plugins show as <font color=green>green</font>; Disabled plugins show as <font color=red>red</font><br>
 * <b>Usage:</b> !listplugins<br>
 * <b>Minimum Params:</b> 0<br>
 * <b>Maximum Params:</b> 0<br>
 * <b>Requires:</b> Op<br>
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
@BotCommand(main = "listplugins", usage = "!listplugins", desc = "Gives a list of plugins", maxParam = 0, op = true)
public final class ListPluginsCommand extends BaseCommand {

    /**
     * Constructs a new {@code ListPluginsCommand}
     */
    public ListPluginsCommand(BotPlugin fake) {
        super(fake);
    }

    @Override
    public final synchronized boolean execute(Channel channel, User user, String[] args) {
        if (channel != null) {
            channel.sendMessage(BotPluginLoader.getBotPluginList());
        }
        else {
            user.sendNotice(BotPluginLoader.getBotPluginList());
        }
        return true;
    }

}
