package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;

final class PingCommand extends BaseCommand {

    public PingCommand() {
        super(null, new String[] { "ping" }, "!ping", "Sends a Pong", 1, -1, false, false, false);
    }

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        channel.sendMessage("| . :  |");
        return true;
    }

}
