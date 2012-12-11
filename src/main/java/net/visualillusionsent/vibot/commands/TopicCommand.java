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
import net.visualillusionsent.vibot.Topic;
import net.visualillusionsent.vibot.User;

final class TopicCommand extends BaseCommand {

    public TopicCommand() {
        super(null, new String[] { "topic" }, "!topic", "Shows the channel's topic", 1, -1, false, false, false);
    }

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        if (channel.getName().startsWith("#")) {
            if (channel.getTopic() != null) {
                Topic topic = channel.getTopic();
                user.sendNotice("Topic for ".concat(channel.getName()).concat(" is: ").concat(topic.getTopic()));
                user.sendNotice("Topic set by ".concat(topic.getSetBy()).concat(" on ").concat(topic.getDate()));
            }
            else {
                user.sendNotice("No topic set");
            }
        }
        else {
            user.sendMessage("You need to be in a channel to display the topic!");
        }
        return true;
    }

}
