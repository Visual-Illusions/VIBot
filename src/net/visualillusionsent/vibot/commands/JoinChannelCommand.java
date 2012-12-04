package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;
import net.visualillusionsent.vibot.VIBot;

final class JoinChannelCommand extends BaseCommand {

    public JoinChannelCommand() {
        super(null, new String[] { "join" }, "!join <channel>", "Joins a channel if exists and can", 2, 2, false, false, true);
    }

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        if (!args[1].startsWith("#")) {
            user.sendMessage("Channels need to start with a '#'!");
        }
        else {
            user.sendMessage("Attempting to join Channel: '" + args[1] + "'");
            VIBot.joinChannel(args[1]);
        }
        return true;
    }
}
