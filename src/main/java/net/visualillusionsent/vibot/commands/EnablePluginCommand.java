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

import net.visualillusionsent.vibot.api.commands.BaseCommand;
import net.visualillusionsent.vibot.api.commands.BotCommand;
import net.visualillusionsent.vibot.api.plugin.BotPluginLoader;
import net.visualillusionsent.vibot.io.irc.Channel;
import net.visualillusionsent.vibot.io.irc.User;

@BotCommand(main = "enableplugin", usage = "!enableplugin <plugin>", desc = "Enables a plugin", minParam = 2, maxParam = 2, owner = true)
public final class EnablePluginCommand extends BaseCommand {

    public EnablePluginCommand() {
        super(null);
    }

    @Override
    public final synchronized boolean execute(Channel channel, User user, String[] args) {
        String message = BotPluginLoader.getInstance().enablePlugin(args[1]) ? "Enabled plugin successfully!" : "An exception occured while enabling plugin...";
        channel.sendMessage(message);
        return true;
    }
}
