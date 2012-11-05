package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.Misc;
import net.visualillusionsent.vibot.User;

@BotCommand(aliases = { "part" }, usage = "!part [channel] [reason]", desc = "Quites the Bot", adminonly = true)
final class PartChannelCommand extends BaseCommand {

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        if (args.length > 2) {
            if (!args[1].startsWith("#")) {
                String reason = Misc.combineSplit(1, args, " ");
                Misc.partChannel(channel.getName(), reason);
            } else {
                String reason = Misc.combineSplit(2, args, " ");
                Misc.partChannel(args[1], reason);
            }
        } else if (args.length > 1) {
            if (args[1].startsWith("#")) {
                Misc.partChannel(args[1], "disconnect.leaving");
            } else {
                Misc.partChannel(channel.getName(), "disconnect.leaving");
            }
        } else {
            Misc.partChannel(channel.getName(), "disconnect.leaving");
        }
        return true;
    }
}
