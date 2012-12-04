package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;

final class HelpCommand extends BaseCommand {
    public HelpCommand() {
        super(null, new String[] { "help" }, "!help", "Displays a list of commands and their usage", 1, -1, false, false, false);
    }

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        CommandParser.printHelp(channel, user);
        return true;
    }
}
