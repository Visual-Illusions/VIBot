package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;

final class UnignoreUserCommand extends BaseCommand {

    public UnignoreUserCommand() {
        super(null, new String[] { "unignore" }, "!unignore <user>", "Stops ignoring a user", 2, 2, false, true, false);
    }

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        User ignore = channel.getUser(args[1]);
        if (channel.isUserIgnored(ignore)) {
            channel.unIgnoreUser(ignore);
            channel.sendMessage("No longer ignoring User: ".concat(args[1]));
        }
        else {
            channel.sendMessage("I wasn't ignoring " + args[1]);
        }
        return true;
    }
}
