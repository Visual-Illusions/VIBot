package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.utils.StringUtils;
import net.visualillusionsent.utils.UtilityException;
import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;
import net.visualillusionsent.vibot.VIBot;

final class PartChannelCommand extends BaseCommand {

    public PartChannelCommand() {
        super(null, new String[] { "part" }, "!part [channel] [reason]", "Makes the bot leave a channel", 1, -1, false, false, true);
    }

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        String reason = "disconnect.leaving";
        if (args.length > 2) {
            if (!args[1].startsWith("#")) {
                try {
                    reason = StringUtils.joinString(args, " ", 1);
                }
                catch (UtilityException e) {}
                VIBot.partChannel(channel.getName(), reason);
            }
            else {
                try {
                    reason = StringUtils.joinString(args, " ", 2);
                }
                catch (UtilityException e) {}

                VIBot.partChannel(args[1], reason);
            }
        }
        else if (args.length > 1) {
            if (args[1].startsWith("#")) {
                VIBot.partChannel(args[1], reason);
            }
            else {
                VIBot.partChannel(channel.getName(), reason);
            }
        }
        else {
            VIBot.partChannel(channel.getName(), reason);
        }
        return true;
    }
}
