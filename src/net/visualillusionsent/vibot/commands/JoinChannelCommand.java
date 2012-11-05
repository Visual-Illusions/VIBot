package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.Misc;
import net.visualillusionsent.vibot.User;

@BotCommand(aliases = { "join" }, usage = "!join <channel>", desc = "Joins a channel if exists and can", adminonly = true)
final class JoinChannelCommand extends BaseCommand {

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        if (!argCheck(2, args)) {
            user.sendMessage("Usage: " + this.getClass().getAnnotation(BotCommand.class).usage());
        } else if (!args[1].startsWith("#")) {
            user.sendMessage("Channels need to start with a '#'!");
        } else {
            user.sendMessage("Attempting to join Channel: '" + args[1] + "'");
            Misc.joinChannel(args[1]);
        }
        return true;
    }
}
