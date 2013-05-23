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
import net.visualillusionsent.vibot.api.plugin.BotPluginLoader;
import net.visualillusionsent.vibot.io.irc.Channel;
import net.visualillusionsent.vibot.io.irc.User;

/**
 * Version Command<br>
 * Tells the current version of the {@link VIBot} or specified {@link BotPlugin}, if the plugin supports VersionChecker it will also display if there is an update avalible<br>
 * <b>Usage:</b> !version [plugin] <br>
 * <b>Minimum Params:</b> 0<br>
 * <b>Maximum Params:</b> 1<br>
 * <b>Requires:</b> op<br>
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
@BotCommand(main = "version", usage = "!version [pluginname]", desc = "Checks the version of the VIBot or specified plugin", maxParam = 1, op = true)
public final class VersionCheckCommand extends BaseCommand {

    public VersionCheckCommand(BotPlugin fake) {
        super(fake);
    }

    @Override
    public final synchronized boolean execute(Channel channel, User user, String[] args) {
        if (args.length > 0) {
            BotPlugin plugin = BotPluginLoader.getBotPlugin(args[0]);
            String message = "Plugin not found!";
            if (plugin != null) {
                if (!plugin.supportsversionChecker()) {
                    message = "Plugin does not currently support Version Checking...";
                }
                else {
                    Boolean isLatest = plugin.isLatestVersion();
                    message = plugin.toString() + " | " + (isLatest == null ? "An Error occured while checking version..." : plugin.getUpdateMessage());
                }
            }

            if (channel != null) {
                channel.sendMessage(message);
            }
            else {
                user.sendMessage(message);
            }
        }
        else {
            if (channel != null) {
                channel.sendMessage("VIBot v." + VIBot.getBotVersionBuild());
            }
            else {
                user.sendMessage("VIBot v." + VIBot.getBotVersionBuild());
            }
        }
        return true;
    }
}
