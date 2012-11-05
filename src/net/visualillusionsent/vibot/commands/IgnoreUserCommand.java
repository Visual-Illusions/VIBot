package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.Misc;
import net.visualillusionsent.vibot.User;

@BotCommand(aliases = { "ignore" }, usage = "!ignore <user>", desc = "Ignores a user", oponly = true)
final class IgnoreUserCommand extends BaseCommand {

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        if (!argCheck(2, args)) {
            user.sendMessage("Usage: " + this.getClass().getAnnotation(BotCommand.class).usage());
        } else if (!Misc.isIgnored(channel.getName(), args[1])) {
            Misc.addIgnored(channel.getName(), args[1]);
            channel.sendMessage("Now ignoring " + args[1]);
        } else {
            channel.sendMessage("I was already ignoring " + args[1]);
        }
        return true;
    }
}
