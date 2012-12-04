package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;

final class TopicCommand extends BaseCommand {

    public TopicCommand() {
        super(null, new String[] { "topic" }, "!topic", "Shows the channel's topic", 1, -1, false, false, false);
    }

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        if (channel.getName().startsWith("#")) {
            channel.sendMessage(channel.getTopic() != null ? channel.getTopic() : "No topic set");
        }
        else {
            user.sendMessage("You need to be in a channel to display the topic!");
        }
        return true;
    }

}
