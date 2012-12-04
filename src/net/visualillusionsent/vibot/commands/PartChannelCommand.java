package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;
import net.visualillusionsent.vibot.Utils;
import net.visualillusionsent.vibot.VIBot;

final class PartChannelCommand extends BaseCommand {

    public PartChannelCommand() {
        super(null, new String[] { "part" }, "!part [channel] [reason]", "Makes the bot leave a channel", 1, -1, false, false, true);
    }

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        if (args.length > 2) {
            if (!args[1].startsWith("#")) {
                String reason = Utils.combineSplit(1, args, " ");
                VIBot.partChannel(channel.getName(), reason);
            }
            else {
                String reason = Utils.combineSplit(2, args, " ");
                VIBot.partChannel(args[1], reason);
            }
        }
        else if (args.length > 1) {
            if (args[1].startsWith("#")) {
                VIBot.partChannel(args[1], "disconnect.leaving");
            }
            else {
                VIBot.partChannel(channel.getName(), "disconnect.leaving");
            }
        }
        else {
            VIBot.partChannel(channel.getName(), "disconnect.leaving");
        }
        return true;
    }
}
