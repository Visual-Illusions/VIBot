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

import net.visualillusionsent.utils.StringUtils;
import net.visualillusionsent.utils.UtilityException;
import net.visualillusionsent.vibot.VIBot;
import net.visualillusionsent.vibot.api.commands.BaseCommand;
import net.visualillusionsent.vibot.api.commands.BotCommand;
import net.visualillusionsent.vibot.io.irc.Channel;
import net.visualillusionsent.vibot.io.irc.User;

/**
 * Disconnect Command<br>
 * Disconnects the {@link VIBot} from the IRC Server and shuts down<br>
 * <b>Usage:</b> !disconnect [message]<br>
 * <b>Minimum Params:</b> 1<br>
 * <b>Maximum Params:</b> &infin;<br>
 * <b>Requires:</b> BotOwner<br>
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
@BotCommand(main = "disconnect", desc = "Disconnects the VIBot from the server and shuts down", usage = "!disconnect [message]", owner = true)
public final class DisconnectCommand extends BaseCommand {

    /**
     * Constructs a new {@code DisconnectCommand} object
     */
    public DisconnectCommand() {
        super(null);
    }

    @Override
    public final boolean execute(Channel channel, User user, String[] args) {
        if (args.length > 1) {
            try {
                VIBot.terminate(StringUtils.joinString(args, " ", 1), 0);
            }
            catch (UtilityException e) {
                VIBot.terminate(null, 0);
            }
        }
        else {
            VIBot.terminate(null, 0);
        }
        return true;
    }
}
