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
 * Disable Plugin Command<br>
 * Disables a {@link BotPlugin}<br>
 * <b>Usage:</b> !disableplugin {@literal <plugin>}<br>
 * <b>Minimum Params:</b> 1<br>
 * <b>Maximum Params:</b> 1<br>
 * <b>Requires:</b> BotOwner<br>
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
@BotCommand(main = "disableplugin", desc = "Disables a plugin", usage = "!disableplugin <plugin>", minParam = 1, maxParam = 1, owner = true)
public final class DisablePluginCommand extends BaseCommand {

    /**
     * Constructs a new {@code DisablePluginCommand} object
     */
    public DisablePluginCommand(BotPlugin fake) {
        super(fake);
    }

    @Override
    public final boolean execute(Channel channel, User user, String[] args) {
        if (BotPluginLoader.disableBotPlugin(args[0])) {
            String pluginName = BotPluginLoader.getBotPlugin(args[0]).toString();
            if (channel != null) {
                channel.sendMessage(pluginName.concat(" disabled!"));
            }
            else {
                user.sendNotice(pluginName.concat(" disabled!"));
            }
        }

        return true;
    }
}
