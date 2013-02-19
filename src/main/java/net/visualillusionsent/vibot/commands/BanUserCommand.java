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
import net.visualillusionsent.vibot.io.irc.Channel;
import net.visualillusionsent.vibot.io.irc.User;

/**
 * Ban User Command<br>
 * Bans a user in the channel<br>
 * <b>Usage:</b> !ban {@literal <user>} [NICK|HOST]<br>
 * <b>Minimum Params:</b> 1<br>
 * <b>Maximum Params:</b> 2<br>
 * <b>Requires:</b> Op Channel<br>
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
@BotCommand(main = "ban", usage = "!ban <user> [NICK|HOST]", desc = "Bans a user from a channel, optionally choosing NICK/HOST will ban just the given argments for the User", minParam = 1, maxParam = 2, op = true, chanOnly = true)
public class BanUserCommand extends BaseCommand {

    /**
     * Constructs a new {@code BanUserCommand}
     */
    public BanUserCommand(BotPlugin fake) {
        super(fake);
    }

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        if (!channel.isBotOp()) {
            user.sendNotice("VIBot needs to be Op in the channel for this command");
            return false;
        }

        User theUser = channel.getUser(args[0]);
        if (theUser != null) {
            if (!theUser.isBotOwner()) {
                switch (args.length) {
                    case 2:
                        if (args[1].toUpperCase().equals("HOST")) {
                            channel.banUserHost(theUser);
                        }
                        else if (args[1].toUpperCase().equals("NICK")) {
                            channel.banUserNick(theUser);
                        }
                        else {
                            channel.banUser(theUser);
                        }
                        break;
                    default:
                        channel.banUser(theUser);
                }
            }
            else {
                user.sendNotice("I cannot ban a owner of me...");
            }
        }
        else {
            user.sendNotice("Could not find User: " + args[0]);
        }
        return true;
    }

}
