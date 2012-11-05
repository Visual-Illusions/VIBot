package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.Misc;
import net.visualillusionsent.vibot.User;

@BotCommand(aliases = { "unignore" }, usage = "!unignore <user>", desc = "Stops ignoring a user", oponly = true)
final class UnignoreUserCommand extends BaseCommand {

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        if (!argCheck(2, args)) {
            user.sendMessage("Usage: " + this.getClass().getAnnotation(BotCommand.class).usage());
        } else if (Misc.isIgnored(channel.getName(), args[1])) {
            Misc.removeIgnored(channel.getName(), args[1]);
            channel.sendMessage("No longer ignoring " + args[1]);
        } else {
            channel.sendMessage("I wasn't ignoring " + args[1]);
        }
        return true;
    }
}
