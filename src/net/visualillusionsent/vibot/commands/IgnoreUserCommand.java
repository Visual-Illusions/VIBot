package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;

final class IgnoreUserCommand extends BaseCommand {
    public IgnoreUserCommand() {
        super(null, new String[] { "ignore" }, "!ignore <user>", "Ignores a user", 2, 2, false, true, false);
    }

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        User ignore = channel.getUser(args[1]);
        if (!channel.isUserIgnored(ignore)) {
            channel.ignoreUser(ignore);
            channel.sendMessage("Now ignoring " + args[1]);
        }
        else {
            channel.sendMessage("I was already ignoring " + args[1]);
        }
        return true;
    }
}
