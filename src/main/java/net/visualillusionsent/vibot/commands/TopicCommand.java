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
import net.visualillusionsent.vibot.io.irc.Channel;
import net.visualillusionsent.vibot.io.irc.Topic;
import net.visualillusionsent.vibot.io.irc.User;

/**
 * Topic Command<br>
 * Displays the {@link Topic} for a {@link Channel}<br>
 * <b>Usage:</b> !topic [user]<br>
 * <b>Minimum Params:</b> 1<br>
 * <b>Maximum Params:</b> 2<br>
 * <b>Requires:</b> Op Channel<br>
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
@BotCommand(main = "topic", usage = "!topic [user]", desc = "Shows the channel's topic", maxParam = 2, chanOnly = true)
public final class TopicCommand extends BaseCommand {

    public TopicCommand() {
        super(null);
    }

    @Override
    public final synchronized boolean execute(Channel channel, User user, String[] args) {
        if (args.length > 1 && user.hasVoice()) {
            User theUser = channel.getUser(args[1]);
            if (theUser != null) {
                if (channel.getTopic() != null) {
                    Topic topic = channel.getTopic();
                    theUser.sendNotice("Topic for ".concat(channel.getName()).concat(" is: ").concat(topic.getTopic()));
                    theUser.sendNotice("Topic set by ".concat(topic.getSetBy()).concat(" on ").concat(topic.getDate()));
                }
                else {
                    user.sendNotice("No topic set");
                }
            }
            else {
                user.sendNotice("Could not find User: ".concat(args[1]));
            }
        }
        else {
            if (channel.getTopic() != null) {
                Topic topic = channel.getTopic();
                channel.sendMessage("Topic for ".concat(channel.getName()).concat(" is: ").concat(topic.getTopic()));
                channel.sendMessage("Topic set by ".concat(topic.getSetBy()).concat(" on ").concat(topic.getDate()));
            }
            else {
                channel.sendMessage("No topic set");
            }
        }
        return true;
    }
}
