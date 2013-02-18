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

import net.visualillusionsent.utils.UpdateException;
import net.visualillusionsent.vibot.api.commands.BaseCommand;
import net.visualillusionsent.vibot.api.commands.BotCommand;
import net.visualillusionsent.vibot.api.plugin.BotPlugin;
import net.visualillusionsent.vibot.api.plugin.BotPluginLoader;
import net.visualillusionsent.vibot.io.irc.Channel;
import net.visualillusionsent.vibot.io.irc.User;

/**
 * Update Plugin Command<br>
 * Updates a specifed {@link BotPlugin} if the plugin supports Updater<br>
 * <b>Usage:</b> !updateplugin {@literal <plugin>}<br>
 * <b>Minimum Params:</b> 2<br>
 * <b>Maximum Params:</b> 2<br>
 * <b>Requires:</b> botowner<br>
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
@BotCommand(main = "updateplugin", usage = "!updateplugiun <pluginname>", desc = "Attemps to update a Plugin, if the plugin supports VIUtils.Updater", minParam = 1, owner = true)
public final class UpdatePluginCommand extends BaseCommand {

    public UpdatePluginCommand(BotPlugin fake) {
        super(fake);
    }

    @Override
    public final synchronized boolean execute(Channel channel, User user, String[] args) {
        BotPlugin plugin = BotPluginLoader.getBotPlugin(args[1]);
        if (plugin != null) {
            try {
                if (plugin.runUpdate()) {
                    message(channel, user, "Update Successful!");
                }
                else {
                    message(channel, user, "Update failed for unknown reasons...");
                }
            }
            catch (UpdateException ue) {
                message(channel, user, ue.getMessage());
            }
        }
        return true;
    }

    private final void message(Channel channel, User user, String message) {
        if (channel != null) {
            channel.sendMessage(message);
        }
        else {
            user.sendNotice(message);
        }
    }
}
