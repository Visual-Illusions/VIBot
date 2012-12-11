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

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;
import net.visualillusionsent.vibot.plugin.BotPluginLoader;

final class ReloadPluginCommand extends BaseCommand {

    public ReloadPluginCommand() {
        super(null, new String[] { "reloadplugin" }, "!reloadplugin <plugin>", "Reloads a plugin", 2, 2, false, false, true);
    }

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        String message = BotPluginLoader.getInstance().reloadPlugin(args[1]) ? "Reloaded plugin successfully!" : "An exception occured while reloading plugin...";
        channel.sendMessage(user.getNick().concat(", ").concat(message));
        return true;
    }
}
