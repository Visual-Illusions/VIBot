package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;

@BotCommand(aliases = { "topic" }, usage = "!topic", desc = "Shows the channel's topic")
final class TopicCommand extends BaseCommand {

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        if (channel.getName().startsWith("#")) {
            channel.sendMessage(channel.getTopic() != null ? channel.getTopic() : "No topic set");
        } else {
            user.sendMessage("You need to be in a channel to display the topic!");
        }
        return true;
    }

}
