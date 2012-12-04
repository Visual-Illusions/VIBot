package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;
import net.visualillusionsent.vibot.Utils;

final class EchoCommand extends BaseCommand {

    public EchoCommand() {
        super(null, new String[] { "echo" }, "!echo <message>", "Echoes the message", 2, -1, true, false, false);
    }

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        String message = Utils.combineSplit(1, args, " ");
        channel.sendMessage(message);
        return true;
    }
}
