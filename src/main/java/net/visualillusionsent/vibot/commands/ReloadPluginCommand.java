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
 * Reload Plugin Command<br>
 * Reloads a {@link BotPlugin}<br>
 * <b>Usage:</b> !reloadplugin {@literal <plugin>}<br>
 * <b>Minimum Params:</b> 1<br>
 * <b>Maximum Params:</b> 1<br>
 * <b>Requires:</b> Owner<br>
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
@BotCommand(main = "reloadplugin", usage = "!reloadplugin <plugin>", desc = "Reloads a plugin", owner = true)
public final class ReloadPluginCommand extends BaseCommand {

    /**
     * Constructs a new {@code ReloadPluginCommand}
     */
    public ReloadPluginCommand(BotPlugin fake) {
        super(fake);
    }

    @Override
    public final synchronized boolean execute(Channel channel, User user, String[] args) {
        String message;
        if (BotPluginLoader.reloadBotPlugin(args[0])) {
            message = BotPluginLoader.getBotPlugin(args[0]).toString().concat(" reloaded successfully!");
        }
        else {
            message = "An exception occured while reloading the plugin...";
        }
        if (channel != null) {
            channel.sendMessage(message);
        }
        else {
            user.sendNotice(message);
        }
        return true;
    }
}
